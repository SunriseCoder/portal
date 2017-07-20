package app.validator;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import app.entity.FestivalEntity;
import app.entity.PlaceEntity;

@Component
public class FestivalEntityValidator implements Validator {
    @Autowired
    private PlaceEntityValidator placeValidator;

    @Override
    public boolean supports(Class<?> cl) {
        return FestivalEntity.class.equals(cl);
    }

    @Override
    public void validate(Object o, Errors errors) {
        FestivalEntity entity = (FestivalEntity) o;

        validateDetails(entity.getDetails(), errors);
        validatePlace(entity.getPlace(), errors);
        validateDate("start", entity.getStart(), errors);
        validateDate("end", entity.getEnd(), errors);
    }

    public void validateDetails(String details, Errors errors) {
        if (details.length() > 255) {
            errors.rejectValue("details", "festival.details.size");
        }
    }

    public void validatePlace(PlaceEntity place, Errors errors) {
        if (place == null || place.getId() == null) {
            return;
        }

        if (place.getId().longValue() == 0) {
            errors.rejectValue("place", "festival.place.notSelected");
            return;
        }

        if (place.getId().longValue() == 1) {
            String placeName = place.getName();
            placeValidator.validateName("place", placeName, errors);
        }

        placeValidator.validateId(place.getId(), errors);
    }

    private void validateDate(String field, Date date, Errors errors) {
        if (date == null) {
            errors.rejectValue(field, "required");
        }
    }
}
