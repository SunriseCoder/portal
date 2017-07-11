package app.controller.admin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.controller.BaseController;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(AdminController.class.getName());

    private static final String ADMIN_DASHBOARD = "admin/dashboard";

    @GetMapping({"", "/"})
    public String index(Model model) {
        injectUser(model);
        auditService.log(OperationTypes.ACCESS_ADMIN_DASHBOARD, AuditEventTypes.ACCESS_ALLOWED);
        return ADMIN_DASHBOARD;
    }
}
