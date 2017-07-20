package app.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import app.entity.PlaceEntity;
import app.service.PlaceService;

@Component
public class PlaceEntityValidator implements Validator {
    @Autowired
    private PlaceService placeService;

    @Override
    public boolean supports(Class<?> cl) {
        return PlaceEntity.class.equals(cl);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PlaceEntity entity = (PlaceEntity) o;

        validateId(entity.getId(), errors);
        validateParent(entity, entity.getParent(), errors);
        validateName("name", entity.getName(), errors);
    }

    public void validateId(Long id, Errors errors) {
        if (placeService.findById(id) == null) {
            errors.rejectValue("id", "place.notExists");
        }
    }

    public void validateParent(PlaceEntity entity, PlaceEntity parent, Errors errors) {
        if (parent == null) {
            return;
        }

        validateParentRecursively(parent, entity, errors);
    }

    private void validateParentRecursively(PlaceEntity parent, PlaceEntity entity, Errors errors) {
        if (parent.getId().equals(entity.getId())) {
            errors.rejectValue("parent", "place.parent.loop");
        }

        PlaceEntity nextParent = parent.getParent();
        if (nextParent == null) {
            return;
        }

        validateParentRecursively(nextParent, entity, errors);
    }

    public void validateName(String field, String name, Errors errors) {
        if (name == null || name.trim().isEmpty()) {
            errors.rejectValue(field, "required");
        }
        if (name.length() > 64) {
            errors.rejectValue(field, "place.name.size");
        }
    }
}
