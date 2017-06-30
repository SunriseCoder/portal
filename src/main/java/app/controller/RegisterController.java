package app.controller;

import java.util.ArrayList;
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
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.security.SecurityService;
import app.service.RoleService;
import app.service.UserService;
import app.util.LogUtils;
import app.validator.UserEntityValidator;

@Controller
public class RegisterController extends BaseController {
    private static final Logger logger = LogManager.getLogger(RegisterController.class.getName());

    @Autowired
    private UserService userService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserEntityValidator userValidator;
    @Autowired
    private RoleService rolesService;

    @PostMapping("/register")
    public String registration(@ModelAttribute("userForm") UserEntity user, BindingResult bindingResult, Model model,
            HttpServletRequest request) {
        LogUtils.logRequest(logger, request);

        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("login", new LoginDTO());
            String error = bindingErrorsToString(bindingResult);
            auditService.log(OperationTypes.CHANGE_USER_REGISTER, AuditEventTypes.VALIDATION_ERROR, null, user.toString(), error);
            return "register";
        }

        // Getting password before it becomes encrypted
        String pass = user.getPass();

        // Assigning default role for new user
        List<RoleEntity> roles = new ArrayList<>();
        RoleEntity roleUser = rolesService.findByName("User");
        roles.add(roleUser);
        user.setRoles(roles);

        try {
            user = userService.save(user);
            auditService.log(OperationTypes.CHANGE_USER_REGISTER, AuditEventTypes.SUCCESSFUL, null, user.toString());
            securityService.autologin(user.getLogin(), pass);
        } catch (Exception e) {
            logger.error("Failed to register new user", e);
            auditService.log(OperationTypes.CHANGE_USER_REGISTER, AuditEventTypes.SAVING_ERROR, null, user.toString(), e.getMessage());
        }

        return REDIRECT_MAIN;
    }
}
