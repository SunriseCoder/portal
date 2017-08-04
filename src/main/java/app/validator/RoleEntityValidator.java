package app.validator;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import app.entity.RoleEntity;
import app.service.RoleService;

@Component
public class RoleEntityValidator extends AbstractValidator {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9]*$");

    @Autowired
    private RoleService roleService;

    @Override
    public boolean supports(Class<?> cl) {
        return RoleEntity.class.equals(cl);
    }

    @Override
    public void validate(Object o, Errors errors) {
        RoleEntity role = (RoleEntity) o;

        validateName(role.getName(), errors);
        if (role.getId() == null) {
            checkNameDuplication(role.getName(), errors);
        }
        validateComment(role.getComment(), errors);
    }

    public void validateName(String name, Errors errors) {
        if (name == null || name.trim().isEmpty()) {
            errors.rejectValue("name", "required");
        }
        if (name.length() < 4 || name.length() > 64) {
            errors.rejectValue("name", "role.name.size");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            errors.rejectValue("name", "role.name.not_allowed_chars");
        }
    }

    private void checkNameDuplication(String name, Errors errors) {
        if (roleService.findByName(name) != null) {
            errors.rejectValue("name", "role.name.duplication");
        }
    }

    public void validateComment(String comment, Errors errors) {
        if (comment == null || comment.trim().isEmpty()) {
            errors.rejectValue("comment", "required");
        }
        if (comment.length() > 255) {
            errors.rejectValue("comment", "role.comment.size");
        }
    }
}
