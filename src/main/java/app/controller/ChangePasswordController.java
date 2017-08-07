package app.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.dto.ChangePasswordDTO;
import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.validator.UserEntityValidator;

@Controller
@RequestMapping("/change-pass")
public class ChangePasswordController extends BaseController {
    private static final Logger logger = LogManager.getLogger(ChangePasswordController.class.getName());

    private static final String CHANGE_PASS_PAGE = "change-pass";

    @Autowired
    private UserEntityValidator userValidator;

    @GetMapping
    public String changePassPage(HttpServletRequest request, Model model) {
        String auditObject = request.getQueryString();

        injectUser(model);

        UserEntity user = userService.getLoggedInUser();
        if (!user.isShouldChangePassword()) {
            String message = "Access to change-pass page without need";
            logger.warn(message);
            auditService.log(OperationTypes.ACCESS_USER_PASSWORD, AuditEventTypes.SUSPICIOUS_ACTIVITY, null, null, message);
            return "redirect:/";
        }

        model.addAttribute("changePassDTO", new ChangePasswordDTO());

        auditService.log(OperationTypes.ACCESS_USER_PASSWORD, AuditEventTypes.ACCESS_ALLOWED, auditObject);

        return CHANGE_PASS_PAGE;
    }

    @PostMapping
    public String changePassSave(@ModelAttribute("changePassDTO") ChangePasswordDTO changePass, Model model,
                    HttpServletRequest request, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        UserEntity user = userService.getLoggedInUser();

        userValidator.validatePassword(changePass.getPass(), bindingResult);
        userValidator.validatePassConfirm(changePass.getPass(), changePass.getConfirm(), bindingResult);
        if (userService.isPasswordMatches(changePass.getPass(), user.getPass())) {
            userValidator.clearValue(bindingResult, "pass");
            bindingResult.rejectValue("pass", "changePass.pass.matchOldPass");
        }

        if (bindingResult.hasErrors()) {
            String error = bindingErrorsToString(bindingResult);
            auditService.log(OperationTypes.CHANGE_USER_PASSWORD, AuditEventTypes.VALIDATION_ERROR, null, null, error);
            changePass.clearPasses();
            injectUser(model);
            return CHANGE_PASS_PAGE;
        }

        String auditObjectBefore = user.toString();

        user.setPass(changePass.getPass());
        userService.encryptPass(user);
        user.setShouldChangePassword(false);

        String auditObjectBeforeSave = user.toString();

        try {
            user = userService.save(user);
            String auditObjectAfter = user.toString();
            auditService.log(OperationTypes.CHANGE_USER_PASSWORD, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
        } catch (Exception e) {
            logger.error("Error due to save user by changing user's own password", e);
            auditService.log(OperationTypes.CHANGE_USER_PASSWORD, AuditEventTypes.SAVING_ERROR,
                            auditObjectBefore, auditObjectBeforeSave, e.getMessage());
        }

        redirectAttributes.addFlashAttribute("message", "Your password has been changed successfully");
        return "redirect:/";
    }
}
