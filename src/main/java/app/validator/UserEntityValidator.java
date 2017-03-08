package app.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import app.entity.UserEntity;
import app.service.UserService;

@Component
public class UserEntityValidator implements Validator {
    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> cl) {
        return UserEntity.class.equals(cl);
    }

    @Override
    public void validate(Object o, Errors errors) {
        UserEntity user = (UserEntity) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "login", "required");
        if (user.getLogin().length() < 4 || user.getLogin().length() > 32) {
            errors.rejectValue("login", "user.login.size");
        }
        if (userService.findByLogin(user.getLogin()) != null) {
            errors.rejectValue("login", "user.login.duplication");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pass", "required");
        if (user.getPass().length() < 8 || user.getPass().length() > 32) {
            errors.rejectValue("pass", "user.pass.size");
        }

        if (!user.getPass().equals(user.getConfirm())) {
            errors.rejectValue("confirm", "user.confirm.different");
        }
    }
}
