package app.controller;

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

import app.dto.UserProfileDTO;
import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.UserService;
import app.util.StringUtils;
import app.validator.UserEntityValidator;

@Controller
@RequestMapping("/profile")
public class UserProfileController extends BaseController {
    private static final Logger logger = LogManager.getLogger(UserProfileController.class.getName());

    private static final String PROFILE_PAGE = "profile";

    @Autowired
    private UserService userService;
    @Autowired
    private UserEntityValidator userValidator;

    @GetMapping
    public String editProfile(Model model) {
        injectUser(model);
        injectUserProfileDTO(model);
        return PROFILE_PAGE;
    }

    @PostMapping
    public String saveProfile(@ModelAttribute("userProfile") UserProfileDTO profile, Model model, BindingResult bindingResult) {
        validateProfile(profile, bindingResult);
        UserEntity userBeforeSave = userService.getLoggedInUser();
        if (!bindingResult.hasErrors()) {
            try {
                UserEntity userAfterSave = userService.updateLoggedUser(profile);
                auditService.log(OperationTypes.CHANGE_USER_PROFILE, AuditEventTypes.SUCCESSFUL, userBeforeSave.toString(), userAfterSave.toString());
                model.addAttribute("message", "Profile has been updated successfully");
            } catch (Exception e) {
                logger.error("Error due to update user profile", e);
                auditService.log(OperationTypes.CHANGE_USER_PROFILE, AuditEventTypes.SAVING_ERROR, userBeforeSave.toString(), profile.toString(), e.getMessage());
                model.addAttribute("error", "Error due to update user profile");
            }
        } else {
            String errors = bindingErrorsToString(bindingResult);
            auditService.log(OperationTypes.CHANGE_USER_PROFILE, AuditEventTypes.VALIDATION_ERROR, userBeforeSave.toString(), profile.toString(), errors);
        }

        injectUser(model);
        profile.clearPasswords();
        return PROFILE_PAGE;
    }

    private void validateProfile(UserProfileDTO profile, BindingResult bindingResult) {
        UserEntity user = userService.getLoggedInUser();

        // Check ID
        if (!user.getId().equals(profile.getId())) {
            auditService.log(OperationTypes.CHANGE_USER_PROFILE, AuditEventTypes.SUSPICIOUS_ACTIVITY,
                    user.toString(), profile.toString(), "IDs are different");
        }

        // DisplayName
        if (!StringUtils.safeEquals(user.getDisplayName(), profile.getDisplayName())) {
            userValidator.validateDisplayName(profile.getDisplayName(), bindingResult);
        }

        // Email
        if (!StringUtils.safeEquals(user.getEmail(), profile.getEmail())) {
            userValidator.validateEmail(profile.getEmail(), bindingResult);
        }

        // Password
        String pass = profile.getPass();
        if (pass != null && !pass.isEmpty()) {
            userValidator.validatePassword(pass, bindingResult);
            String confirm = profile.getConfirm();
            userValidator.validatePassConfirm(pass, confirm, bindingResult);
        }
    }

    private void injectUserProfileDTO(Model model) {
        UserEntity user = userService.getLoggedInUser();

        UserProfileDTO userDTO = new UserProfileDTO();
        userDTO.setId(user.getId());
        userDTO.setDisplayName(user.getDisplayName());
        userDTO.setEmail(user.getEmail());

        model.addAttribute("userProfile", userDTO);
    }
}
