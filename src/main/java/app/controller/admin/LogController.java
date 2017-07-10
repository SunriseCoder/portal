package app.controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import app.controller.BaseController;
import app.dto.LogLineDTO;
import app.enums.AuditEventTypes;
import app.enums.LogLevels;
import app.enums.OperationTypes;
import app.service.admin.LogService;
import app.util.StringUtils;

@Controller
@RequestMapping("/admin/logs")
public class LogController extends BaseController {
    private static final Logger logger = LogManager.getLogger(LogController.class.getName());

    private static final String ADMIN_LOGS_LIST = "admin/logs/list";
    private static final String ADMIN_LOGS_FILE = "admin/logs/file";

    @Autowired
    private LogService logService;

    @GetMapping
    public String logList(HttpServletRequest request, Model model) {
        injectUser(model);

        List<String> fileList;
        try {
            fileList = logService.findAllFiles();
            model.addAttribute("fileList", fileList);
        } catch (IOException e) {
            logger.error("Error due to get list of log files", e);
            String error = StringUtils.format("{0}[{1}]", e.getClass().getSimpleName(), e.getMessage());
            auditService.log(OperationTypes.ACCESS_ADMIN_LOGS, AuditEventTypes.IO_ERROR, null, null, error);
            model.addAttribute("error", error);
        }

        auditService.log(OperationTypes.ACCESS_ADMIN_LOGS, AuditEventTypes.SUCCESSFUL, "LogFileList");

        return ADMIN_LOGS_LIST;
    }

    @GetMapping("/file")
    public String logFile(HttpServletRequest request, Model model, @RequestParam("name") String name) {
        String auditObject = StringUtils.format("LogFile[name={0}]", name);

        if (!isSafe(name)) {
            logger.warn("Attempt to get log file with suspicious filename: " + auditObject);
            auditService.log(OperationTypes.ACCESS_ADMIN_LOGS, AuditEventTypes.SUSPICIOUS_ACTIVITY, auditObject);
            return "redirect:/admin";
        }

        injectUser(model);

        try {
            Map<String, String> parameters = convertParameterMap(request.getParameterMap());
            List<LogLineDTO> lines = logService.readFile(parameters);
            model.addAttribute("lines", lines);
        } catch (Exception e) {
            logger.error("Error due to read log file", e);
            String error = StringUtils.format("{0}[{1}]", e.getClass().getSimpleName(), e.getMessage());
            auditService.log(OperationTypes.ACCESS_ADMIN_LOGS, AuditEventTypes.IO_ERROR, null, null, error);
            model.addAttribute("error", error);
        }

        model.addAttribute("fileName", name);
        model.addAttribute("logLevels", LogLevels.values());

        auditService.log(OperationTypes.ACCESS_ADMIN_LOGS, AuditEventTypes.SUCCESSFUL, auditObject);

        return ADMIN_LOGS_FILE;
    }

    private boolean isSafe(String name) {
        return name != null && !name.isEmpty() && !name.contains("/");
    }
}
