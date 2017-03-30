package app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.dto.LoginDTO;
import app.entity.UserEntity;

@Controller
public class PageController {

    @RequestMapping("/")
    public String index(Model model) {
        addLoginDto(model);
        return "index";
    }

    @RequestMapping("/files")
    public String files(Model model) {
        addLoginDto(model);
        return "files";
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        addLoginDto(model);

        Exception e = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        if (e != null) {
            model.addAttribute("error", e.getMessage());
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        addLoginDto(model);
        model.addAttribute("user", new UserEntity());
        return "register";
    }

    @RequestMapping("/upload")
    public String upload(Model model) {
        addLoginDto(model);
        return "upload";
    }

    private void addLoginDto(Model model) {
        model.addAttribute("login", new LoginDTO());
    }
}
