package app.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import app.dto.LoginDTO;
import app.entity.UserEntity;
import app.service.AuditService;
import app.service.UserService;

public class BaseController {
    @Autowired
    protected AuditService auditService;
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

    protected String bindingErrorsToString(BindingResult bindingResult) {
        Locale locale = LocaleContextHolder.getLocale();
        String errors = bindingResult.getAllErrors().stream()
                .map(error -> messageSource.getMessage(error, locale))
                .collect(Collectors.joining(",", "Errors[", "]"));
        return errors;
    }

    protected Map<String, String> convertParameterMap(Map<String, String[]> parameterMap) {
        Map<String, String> resultMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String value = convertParameter(parameterMap, entry.getKey());
            resultMap.put(entry.getKey(), value);
        }
        return resultMap;
    }

    private String convertParameter(Map<String, String[]> params, String name) {
        String[] value = params.get(name);
        if (value.length == 0) {
            return null;
        }
        return value[0];
    }
}
