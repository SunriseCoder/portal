package app.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import app.entity.UserEntity;
import app.service.SecurityService;
import app.service.UserService;
import app.util.LogUtils;
import app.validator.UserEntityValidator;

@Controller
public class RegisterController {
    private static final Logger logger = LogManager.getLogger(RegisterController.class.getName());

    @Autowired
    private UserService userService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserEntityValidator userValidator;

    @PostMapping("/register")
    public String registration(@ModelAttribute("user") UserEntity user, BindingResult bindingResult, Model model,
            HttpServletRequest request) {
        LogUtils.logRequest(logger, request);

        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Getting password before it becomes encrypted
        String pass = user.getPass();

        userService.save(user);

        securityService.autologin(user.getLogin(), pass);

        return "redirect:/";
    }
}
