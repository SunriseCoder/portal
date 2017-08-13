package app.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.controller.BaseController;
import app.dto.JobInfoDTO;
import app.service.admin.ExternalJobService;

@Controller
@RequestMapping("/admin/ext-jobs")
public class ExternalJobController extends BaseController {
    private static final String EXT_JOB_LIST_PAGE = "admin/ext-jobs/list";
    private static final String REDIRECT_LIST_PAGE = "redirect:/admin/ext-jobs";

    @Autowired
    private ExternalJobService externalJobService;

    @GetMapping
    public String list(Model model) {
        injectUser(model);

        JobInfoDTO jobInfo = externalJobService.getСurrentJobInfo();
        model.addAttribute("jobInfo", jobInfo);

        return EXT_JOB_LIST_PAGE;
    }

    @PostMapping("/kill")
    public String kill(Model model, RedirectAttributes redirectAttributes) {
        boolean killed = externalJobService.killCurrentJob();

        if (killed) {
            redirectAttributes.addFlashAttribute("message", "Job has been killed successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Could not kill external job");
        }

        return REDIRECT_LIST_PAGE;
    }
}
