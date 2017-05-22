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
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[A-Za-z0-9]*$");
    private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9\\-\\ \\_\\(\\)]*$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> cl) {
        return UserEntity.class.equals(cl);
    }

    @Override
    public void validate(Object o, Errors errors) {
        UserEntity user = (UserEntity) o;

        validateLogin(user.getLogin(), errors);
        validatePassword(user.getPass(), errors);

        if (!user.getPass().equals(user.getConfirm())) {
            errors.rejectValue("confirm", "user.confirm.different");
        }

        validateDisplayName(user.getDisplayName(), errors);

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "required");
        if (userService.findByEmail(user.getEmail()) != null) {
            errors.rejectValue("email", "user.email.duplication");
        }
        if (user.getEmail().length() > 64) {
            errors.rejectValue("email", "user.email.size");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            errors.rejectValue("email", "user.email.wrong_format");
        }
    }

    public void validateLogin(String login, Errors errors) {
        if (login == null || login.trim().isEmpty()) {
            errors.rejectValue("login", "required");
        }
        if (login.length() < 4 || login.length() > 32) {
            errors.rejectValue("login", "user.login.size");
        }
        if (!LOGIN_PATTERN.matcher(login).matches()) {
            errors.rejectValue("login", "user.login.not_allowed_chars");
        }
        if (userService.findByLogin(login) != null) {
            errors.rejectValue("login", "user.login.duplication");
        }
    }

    public void validatePassword(String password, Errors errors) {
        if (password == null || password.trim().isEmpty()) {
            errors.rejectValue("pass", "required");
        }
        if (password.length() < 8 || password.length() > 32) {
            errors.rejectValue("pass", "user.pass.size");
        }
    }

    public void validateDisplayName(String displayName, Errors errors) {
        if (displayName == null || displayName.trim().isEmpty()) {
            errors.rejectValue("displayName", "required");
        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "displayName", "required");
        if (userService.findByDisplayName(displayName) != null) {
            errors.rejectValue("displayName", "user.displayName.duplication");
        }
        if (!DISPLAY_NAME_PATTERN.matcher(displayName).matches()) {
            errors.rejectValue("displayName", "user.displayName.not_allowed_chars");
        }
        if (displayName.length() > 64) {
            errors.rejectValue("displayName", "user.displayName.size");
        }
    }
}
