package app.validator;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import app.entity.UserEntity;
import app.service.UserService;

@Component
public class UserEntityValidator implements Validator {
    private static final String EMAIL_PATTERN_STRING = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_PATTERN_STRING);

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

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "required");
        if (userService.findByEmail(user.getEmail()) != null) {
            errors.rejectValue("email", "user.email.duplication");
        }
        if (user.getEmail().length() > 254) {
            errors.rejectValue("email", "user.email.size");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            errors.rejectValue("email", "user.email.wrong_format");
        }
    }
}
