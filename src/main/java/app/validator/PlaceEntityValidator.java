package app.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import app.dto.PlaceDTO;
import app.entity.PlaceEntity;
import app.service.PlaceService;

@Component
public class PlaceEntityValidator implements Validator {
    @Autowired
    private PlaceService placeService;

    @Override
    public boolean supports(Class<?> cl) {
        return PlaceDTO.class.equals(cl);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PlaceDTO entity = (PlaceDTO) o;

        validateId(entity.getId(), errors);
        validateParent(entity, entity.getParent(), errors);
        validateName("name", entity.getName(), errors);
    }

    public void validateId(Long id, Errors errors) {
        if (id != null && placeService.findById(id) == null) {
            errors.rejectValue("id", "place.notExists");
        }
    }

    public void validateParent(PlaceDTO dto, PlaceDTO parent, Errors errors) {
        if (parent == null || parent.getId().longValue() == 0) {
            return;
        }

        PlaceEntity parentEntity = placeService.findById(parent.getId());
        if(isInBranch(parentEntity, dto.getId())) {
            errors.rejectValue("parent", "place.parent.loop");
        }
    }

    public void validateName(String field, String name, Errors errors) {
        if (name == null || name.trim().isEmpty()) {
            errors.rejectValue(field, "required");
        }
        if (name.length() > 64) {
            errors.rejectValue(field, "place.name.size");
        }
    }

    public boolean isInBranch(PlaceEntity entity, Long branchIdToExclude) {
        boolean exclude = entity.getId().equals(branchIdToExclude);
        if (exclude || entity.getParent() == null) {
            return exclude;
        }
        return isInBranch(entity.getParent(), branchIdToExclude);
    }
}
