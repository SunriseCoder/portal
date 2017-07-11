package app.controller.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.controller.BaseController;
import app.entity.IPBanEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.AuditService;
import app.service.admin.IPBanService;
import app.validator.IPBanEntityValidator;

@Controller
@RequestMapping("/admin/ip-bans")
public class IPBanController extends BaseController {
    private static final Logger logger = LogManager.getLogger(IPBanController.class.getName());

    private static final String ADMIN_IPBAN_LIST = "admin/ip-bans/list";
    private static final String REDIRECT_ADMIN_IPBAN = "redirect:/admin/ip-bans";

    @Autowired
    private AuditService auditService;
    @Autowired
    private IPBanService ipBanService;

    @Autowired
    private IPBanEntityValidator ipBanValidator;

    @GetMapping
    public String ipBanList(HttpServletRequest request, Model model) {
        injectUser(model);

        List<IPBanEntity> entityList = ipBanService.findAll();
        model.addAttribute("entityList", entityList);
        model.addAttribute("ipBan", new IPBanEntity());
        model.addAttribute("myIP", request.getRemoteAddr());

        auditService.log(OperationTypes.ACCESS_ADMIN_IPBAN, AuditEventTypes.SUCCESSFUL);

        return ADMIN_IPBAN_LIST;
    }

    @PostMapping("/add")
    public String addIp(@ModelAttribute("ipBan") IPBanEntity entity, BindingResult bindingResult, RedirectAttributes ra) {
        ipBanValidator.validate(entity, bindingResult);
        String auditObject = entity.toString();
        if (!bindingResult.hasErrors()) {
            try {
                entity = ipBanService.add(entity);
                auditObject = entity.toString();
                logger.info("IP-address was added to ban list: " + auditObject);
                auditService.log(OperationTypes.CHANGE_IPBAN_ADD, AuditEventTypes.SUCCESSFUL, null, auditObject);
                ra.addFlashAttribute("message", "IP-address has been added successfully");
            } catch (Exception e) {
                String message = "Error due to add IP-address to ban list";
                logger.error(message + ": " + auditObject, e);
                auditService.log(OperationTypes.CHANGE_IPBAN_ADD, AuditEventTypes.SAVING_ERROR, auditObject, null, e.getMessage());
                ra.addFlashAttribute("error", message);
            }
        } else {
            String message = bindingErrorsToString(bindingResult);
            ra.addFlashAttribute("error", message);
        }

        return REDIRECT_ADMIN_IPBAN;
    }

    @PostMapping("/remove")
    public String removeIp(@RequestParam("id") Long id, RedirectAttributes ra) {
        try {
            IPBanEntity entity = ipBanService.remove(id);
            String auditObject = entity.toString();
            logger.info("IP-address has been removed from the ban list: " + auditObject);
            auditService.log(OperationTypes.CHANGE_IPBAN_REMOVE, AuditEventTypes.SUCCESSFUL, auditObject);
            ra.addFlashAttribute("message", "IP-address " + entity.getIp() + " has been removed from ban list successfully");
        } catch (Exception e) {
            String message = "Error due to remove IP-address from ban list: id=" + id;
            logger.error(message, e);
            auditService.log(OperationTypes.CHANGE_IPBAN_REMOVE, AuditEventTypes.DELETE_ERROR, "id=" + id, null, e.getMessage());
            ra.addFlashAttribute("error", message);
        }

        return REDIRECT_ADMIN_IPBAN;
    }
}
