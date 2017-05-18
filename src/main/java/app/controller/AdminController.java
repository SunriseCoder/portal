package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.entity.UserEntity;
import app.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(Model model) {
        injectUser(model);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userList(Model model) {
        injectUser(model);
        List<UserEntity> userList = userService.findAll();
        model.addAttribute("userList", userList);
        return "admin/users";
    }
}
