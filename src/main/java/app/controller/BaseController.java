package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import app.dto.LoginDTO;
import app.entity.UserEntity;
import app.service.UserService;

public class BaseController {
    @Autowired
    protected UserService userService;

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
}
