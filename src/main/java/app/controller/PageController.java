package app.controller;

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
}
