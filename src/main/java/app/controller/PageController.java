package app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.enums.Permissions;

@Controller
public class PageController extends BaseController {

    @GetMapping("/")
    public String index(Model model) {
        UserEntity user = userService.getLoggedInUser();
        if (user != null && user.isLocked()) {
            auditService.log(OperationTypes.ACCESS_PAGE_MAIN, AuditEventTypes.ACCESS_DENIED, null, null, "User is locked");
            return REDIRECT_LOGOUT;
        }

        injectUser(model);
        return "index";
    }

    @GetMapping("/files")
    public String files(Model model) {
        if (!userService.hasPermission(Permissions.PAGES_VIEW)) {
            auditService.log(OperationTypes.ACCESS_PAGE_FILES, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_MAIN;
        }

        injectUser(model);
        return "files";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        if (userService.isAuthenticated()) {
            return REDIRECT_MAIN;
        }

        injectLoginDTO(model);

        Exception e = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        if (e != null) {
            model.addAttribute("error", e.getMessage());
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (userService.isAuthenticated()) {
            return REDIRECT_MAIN;
        }

        injectLoginDTO(model);
        model.addAttribute("userForm", new UserEntity());
        return "register";
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        if (!userService.hasPermission(Permissions.UPLOAD_FILES)) {
            auditService.log(OperationTypes.ACCESS_PAGE_UPLOAD, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_MAIN;
        }

        injectUser(model);
        return "upload";
    }

    @GetMapping("/logout")
    public String logout(Model model) {
        if (!userService.isAuthenticated()) {
            return REDIRECT_MAIN;
        }

        return "logout";
    }
}
