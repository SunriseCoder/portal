package app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import app.dto.LoginDTO;
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

    @RequestMapping("/login")
    public ModelAndView login(HttpServletRequest request, ModelAndView mav) {
        mav.addObject("login", new LoginDTO());
        Exception e = (Exception) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        if (e != null) {
            mav.addObject("error", e.getMessage());
        }
        mav.setViewName("login");
        return mav;
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
