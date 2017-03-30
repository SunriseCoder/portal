package app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.dto.LoginDTO;
import app.entity.UserEntity;
import app.service.UserService;

@Controller
public class PageController {
    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String index(Model model) {
        if (userService.isAuthenticated()) {
            addUser(model);
        } else {
            addLoginDto(model);
        }
        return "index";
    }

    @RequestMapping("/files")
    public String files(Model model) {
        if (userService.isAuthenticated()) {
            addUser(model);
        } else {
            addLoginDto(model);
        }
        return "files";
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        if (userService.isAuthenticated()) {
            return "redirect:/";
        }

        addLoginDto(model);

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

        addLoginDto(model);
        model.addAttribute("userForm", new UserEntity());
        return "register";
    }

    @RequestMapping("/upload")
    public String upload(Model model) {
        if (userService.isAuthenticated()) {
            addUser(model);
        } else {
            addLoginDto(model);
        }
        return "upload";
    }

    private void addUser(Model model) {
        UserEntity user = userService.getLoggedInUser();
        model.addAttribute("user", user);
    }

    private void addLoginDto(Model model) {
        model.addAttribute("login", new LoginDTO());
    }
}
