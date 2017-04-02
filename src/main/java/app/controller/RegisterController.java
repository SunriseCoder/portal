package app.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import app.dto.LoginDTO;
import app.entity.RoleEntity;
import app.entity.UserEntity;
import app.service.RolesService;
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
    @Autowired
    private RolesService rolesService;

    @PostMapping("/register")
    public String registration(@ModelAttribute("userForm") UserEntity user, BindingResult bindingResult, Model model,
            HttpServletRequest request) {
        LogUtils.logRequest(logger, request);

        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("login", new LoginDTO());
            return "register";
        }

        // Getting password before it becomes encrypted
        String pass = user.getPass();

        // Assigning default role for new user
        List<RoleEntity> roles = rolesService.getRoleByName("User");
        user.setRoles(roles);

        userService.save(user);

        securityService.autologin(user.getLogin(), pass);

        return "redirect:/";
    }
}
