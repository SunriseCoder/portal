package app.controller;

import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import app.dto.LoginDTO;
import app.entity.UserEntity;
import app.enums.Permissions;
import app.service.UserService;

public class BaseController {
    protected static final String REDIRECT_MAIN = "redirect:/";
    protected static final String REDIRECT_LOGOUT = "redirect:/logout";

    @Autowired
    protected UserService userService;

    @Autowired
    protected MessageSource messageSource;

    protected void injectUser(Model model) {
        if (userService.isAuthenticated()) {
            UserEntity user = userService.getLoggedInUser();
            model.addAttribute("user", user);
        } else {
            injectLoginDTO(model);
        }
    }

    protected void injectLoginDTO(Model model) {
        model.addAttribute("login", new LoginDTO());
    }

    protected void validatePermission(Permissions permission, BindingResult bindingResult) {
        validatePermission(permission, bindingResult, null);
    }

    protected void validatePermission(Permissions permission, BindingResult bindingResult, String field) {
        if (!userService.hasPermission(permission)) {
            if (field == null) {
                bindingResult.reject("noRights");
            } else {
                bindingResult.rejectValue(field, "noRights");
            }
        }
    }

    protected String bindingErrorsToString(BindingResult bindingResult) {
        Locale locale = LocaleContextHolder.getLocale();
        String errors = bindingResult.getAllErrors().stream()
                .map(error -> messageSource.getMessage(error, locale))
                .collect(Collectors.joining(",", "Errors[", "]"));
        return errors;
    }
}
