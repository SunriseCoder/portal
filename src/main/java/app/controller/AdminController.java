package app.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.dto.ChangeLoginDTO;
import app.entity.UserEntity;
import app.service.UserService;
import app.util.LogUtils;
import app.validator.UserEntityValidator;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {
    private static final Logger logger = LogManager.getLogger(AdminController.class.getName());

    private static final String ADMIN_DASHBOARD = "admin/dashboard";
    private static final String ADMIN_USERS_LIST = "admin/users/list";
    private static final String ADMIN_USERS_DETAILS = "admin/users/details";

    private static final String REDIRECT_USERS = "redirect:../../users";

    @Autowired
    private UserService userService;

    @Autowired
    private UserEntityValidator userValidator;

    @GetMapping("/")
    public String index(Model model) {
        injectUser(model);
        return ADMIN_DASHBOARD;
    }

    @GetMapping("/users")
    public String userList(Model model) {
        injectUser(model);
        List<UserEntity> userList = userService.findAll();
        model.addAttribute("userList", userList);
        return ADMIN_USERS_LIST;
    }

    @GetMapping("/users/details/{id}")
    public String userDetails(Model model, @PathVariable Long id) {
        injectUser(model);
        UserEntity user = injectUserEntity(model, id);

        if (user == null) {
            return REDIRECT_USERS;
        }

        injectChangeLoginDTO(model, user);
        return ADMIN_USERS_DETAILS;
    }

    @PostMapping("/users/login")
    public String changeLogin(@ModelAttribute("changeLogin") ChangeLoginDTO changeLogin, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);

        userValidator.validateLogin(changeLogin.getLogin(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity user = userService.findById(changeLogin.getId());
            user.setLogin(changeLogin.getLogin());
            userService.save(user);
        }

        injectUser(model);
        injectUserEntity(model, changeLogin.getId());
        return ADMIN_USERS_DETAILS;
    }

    private UserEntity injectUserEntity(Model model, Long id) {
        UserEntity user = userService.findById(id);
        model.addAttribute("userEntity", user);
        return user;
    }

    private void injectChangeLoginDTO(Model model, UserEntity user) {
        ChangeLoginDTO changeLogin = new ChangeLoginDTO(user.getId(), user.getLogin());
        model.addAttribute("changeLogin", changeLogin);
    }
}
