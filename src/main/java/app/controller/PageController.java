package app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;

@Controller
public class PageController extends BaseController {

    @GetMapping("/")
    public String index(Model model) {
        UserEntity user = userService.getLoggedInUser();
        if (user != null && user.isLocked()) {
            auditService.log(OperationTypes.ACCESS_PAGE_MAIN, AuditEventTypes.ACCESS_DENIED, null, null, "User is locked");
            return "redirect:/logout";
        }

        injectUser(model);
        return "index";
    }

    @GetMapping("/files")
    public String files(Model model) {
        injectUser(model);
        return "files";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        if (userService.isAuthenticated()) {
            return "redirect:/";
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
            return "redirect:/";
        }

        injectLoginDTO(model);
        model.addAttribute("userForm", new UserEntity());
        return "register";
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        injectUser(model);
        return "upload";
    }

    @GetMapping("/logout")
    public String logout(Model model) {
        if (!userService.isAuthenticated()) {
            return "redirect:/";
        }

        return "logout";
    }
}
