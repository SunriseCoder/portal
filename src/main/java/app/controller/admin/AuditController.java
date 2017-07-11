package app.controller.admin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.controller.BaseController;
import app.entity.AuditEventEntity;
import app.entity.AuditEventTypeEntity;
import app.entity.OperationTypeEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;

@Controller
@RequestMapping("/admin/audit")
public class AuditController extends BaseController {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(AuditController.class.getName());

    private static final String ADMIN_AUDIT_LIST = "admin/audit/list";

    @GetMapping("/audit")
    public String auditList(Model model, HttpServletRequest request) {
        injectUser(model);
        Map<String, String> parameters = convertParameterMap(request.getParameterMap());
        List<AuditEventEntity> auditEventList = auditService.findEvents(parameters);
        model.addAttribute("auditEventList", auditEventList);
        List<OperationTypeEntity> operationList = auditService.findAllOperationTypes();
        model.addAttribute("operationList", operationList);
        List<AuditEventTypeEntity> typeList = auditService.findAllEventTypes();
        model.addAttribute("typeList", typeList);

        auditService.log(OperationTypes.ACCESS_ADMIN_AUDIT, AuditEventTypes.ACCESS_ALLOWED);

        return ADMIN_AUDIT_LIST;
    }
}
