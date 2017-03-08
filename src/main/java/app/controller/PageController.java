package app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import app.entity.UserEntity;

@Controller
public class PageController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/files")
    public String files() {
        return "files";
    }

    @GetMapping("/register")
    public ModelAndView register(ModelAndView mav) {
        mav.addObject("user", new UserEntity());
        mav.setViewName("register");
        return mav;
    }

    @RequestMapping("/upload")
    public String upload() {
        return "upload";
    }
}
