package app.controller.admin;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.controller.BaseController;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.FileStorageService;
import app.util.NumberUtils;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {
    private static final Logger logger = LogManager.getLogger(AdminController.class.getName());

    private static final String ADMIN_DASHBOARD = "admin/dashboard";

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping({"", "/"})
    public String index(Model model) {
        injectUser(model);
        injectDashboardData(model);
        auditService.log(OperationTypes.ACCESS_ADMIN_DASHBOARD, AuditEventTypes.ACCESS_ALLOWED);
        return ADMIN_DASHBOARD;
    }

    private void injectDashboardData(Model model) {
        try {
            long freeSpace = fileStorageService.getFreeSpace();
            String freeSpaceHumanReadable = NumberUtils.humanReadableSize(freeSpace);
            model.addAttribute("freeSpace", freeSpaceHumanReadable);
        } catch (IOException e) {
            logger.error("Error due to calculate file storage free space", e);
        }
    }
}
