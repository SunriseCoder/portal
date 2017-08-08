package app.controller.admin;

import java.util.HashSet;
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
import app.dto.ChangeDisplayNameDTO;
import app.dto.ChangeEmailDTO;
import app.dto.ChangeLoginDTO;
import app.dto.ChangePasswordDTO;
import app.dto.ChangeRolesDTO;
import app.entity.PermissionEntity;
import app.entity.RoleEntity;
import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.PermissionService;
import app.service.RoleService;
import app.validator.UserEntityValidator;

@Controller
@RequestMapping("/admin/users")
public class UserController extends BaseController {
    private static final Logger logger = LogManager.getLogger(UserController.class.getName());

    private static final String USERS_LIST = "admin/users/list";
    private static final String USERS_EDIT = "admin/users/edit";
    private static final String REDIRECT_USERS = "redirect:/admin/users";
    private static final String DEFAULT_PASSWORD_STUB = "<hidden>";

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private UserEntityValidator userValidator;

    @GetMapping
    public String userList(Model model) {
        injectUser(model);
        List<UserEntity> userList = userService.findAllNonSystem();
        model.addAttribute("userList", userList);

        auditService.log(OperationTypes.ACCESS_USER_LIST, AuditEventTypes.ACCESS_ALLOWED);

        return USERS_LIST;
    }

    @GetMapping("/edit")
    public String editUser(HttpServletRequest request, Model model, @RequestParam("id") Long id) {
        OperationTypes operation = OperationTypes.ACCESS_USER_EDIT;
        String auditObject = request.getQueryString();

        boolean editable = isEditableUser(id, operation);
        if (!editable) {
            return REDIRECT_USERS;
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, id);
        injectChangeLoginDTO(model, userEntity);
        injectChangePasswordDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);

        auditService.log(operation, AuditEventTypes.ACCESS_ALLOWED, auditObject);

        return USERS_EDIT;
    }

    @PostMapping("/login")
    public String changeLogin(@ModelAttribute("changeLogin") ChangeLoginDTO changeLogin, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        OperationTypes operation = OperationTypes.CHANGE_USER_LOGIN;

        boolean editable = isEditableUser(changeLogin.getId(), operation);
        if (!editable) {
            return REDIRECT_USERS;
        }

        userValidator.validateLogin(changeLogin.getLogin(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity userEntity = userService.findById(changeLogin.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setLogin(changeLogin.getLogin());
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(operation, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user login", e);
                auditService.log(operation, AuditEventTypes.SAVING_ERROR,
                                auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        } else {
            String error = bindingErrorsToString(bindingResult);
            logger.warn("Validation error due to save user by changing user login: {}", error);
            auditService.log(operation, AuditEventTypes.VALIDATION_ERROR, changeLogin.toString(), null, error);
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changeLogin.getId());
        injectChangePasswordDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return USERS_EDIT;
    }

    @PostMapping("/password")
    public String changePassword(@ModelAttribute("changePassword") ChangePasswordDTO changePassword, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        OperationTypes operation = OperationTypes.CHANGE_USER_PASSWORD;

        boolean editable = isEditableUser(changePassword.getId(), operation);
        if (!editable) {
            return REDIRECT_USERS;
        }

        userValidator.validatePassword(changePassword.getPass(), bindingResult);
        if (!bindingResult.hasErrors() && !DEFAULT_PASSWORD_STUB.equals(changePassword.getPass())) {
            UserEntity userEntity = userService.findById(changePassword.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setPass(changePassword.getPass());
            userEntity.setShouldChangePassword(true);
            userService.encryptPass(userEntity);
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(operation, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user password", e);
                auditService.log(operation, AuditEventTypes.SAVING_ERROR,
                                auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        } else {
            String error = bindingErrorsToString(bindingResult);
            logger.warn("Validation error due to save user by changing user password: {}", error);
            auditService.log(operation, AuditEventTypes.VALIDATION_ERROR, changePassword.toString(), null, error);
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changePassword.getId());
        injectChangeLoginDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return USERS_EDIT;
    }

    @PostMapping("/display-name")
    public String changeDisplayName(@ModelAttribute("changeDisplayName") ChangeDisplayNameDTO changeDisplayName, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        OperationTypes operation = OperationTypes.CHANGE_USER_DISPLAY_NAME;

        boolean editable = isEditableUser(changeDisplayName.getId(), operation);
        if (!editable) {
            return REDIRECT_USERS;
        }

        userValidator.validateDisplayName(changeDisplayName.getDisplayName(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity userEntity = userService.findById(changeDisplayName.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setDisplayName(changeDisplayName.getDisplayName());
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(operation, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user display name", e);
                auditService.log(operation, AuditEventTypes.SAVING_ERROR, auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        } else {
            String error = bindingErrorsToString(bindingResult);
            logger.warn("Validation error due to save user by changing user display name: {}", error);
            auditService.log(operation, AuditEventTypes.VALIDATION_ERROR, changeDisplayName.toString(), null, error);
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changeDisplayName.getId());
        injectChangeLoginDTO(model, userEntity);
        injectChangePasswordDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return USERS_EDIT;
    }

    @PostMapping("/email")
    public String changeEmail(@ModelAttribute("changeEmail") ChangeEmailDTO changeEmail, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        OperationTypes operation = OperationTypes.CHANGE_USER_EMAIL;

        boolean editable = isEditableUser(changeEmail.getId(), operation);
        if (!editable) {
            return REDIRECT_USERS;
        }

        userValidator.validateEmail(changeEmail.getEmail(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity userEntity = userService.findById(changeEmail.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setEmail(changeEmail.getEmail());
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(operation, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user email", e);
                auditService.log(operation, AuditEventTypes.SAVING_ERROR, auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        } else {
            String error = bindingErrorsToString(bindingResult);
            logger.warn("Validation error due to save user by changing user email: {}", error);
            auditService.log(operation, AuditEventTypes.VALIDATION_ERROR, changeEmail.toString(), null, error);
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changeEmail.getId());
        injectChangeLoginDTO(model, userEntity);
        injectChangePasswordDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return USERS_EDIT;
    }

    @PostMapping("/roles")
    public String changeRoles(@ModelAttribute("changeRoles") ChangeRolesDTO changeRoles, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        OperationTypes operation = OperationTypes.CHANGE_USER_ROLES;

        boolean editable = isEditableUser(changeRoles.getId(), operation);
        if (!editable) {
            return REDIRECT_USERS;
        }

        validateSelectedRoles(changeRoles, bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity userEntity = userService.findById(changeRoles.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setRoles(changeRoles.getRoles());
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(operation, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user roles", e);
                auditService.log(operation, AuditEventTypes.SAVING_ERROR,
                            auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        } else {
            String error = bindingErrorsToString(bindingResult);
            logger.warn("Validation error due to save user by changing user roles: {}", error);
            auditService.log(operation, AuditEventTypes.VALIDATION_ERROR, changeRoles.toString(), null, error);
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changeRoles.getId());
        injectChangeLoginDTO(model, userEntity);
        injectChangePasswordDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return USERS_EDIT;
    }

    private boolean isEditableUser(Long id, OperationTypes operation) {
        UserEntity userEntity = userService.findById(id);

        if (userEntity == null) {
            String message = "Attempt to changing user login for non-existing user with id '" + id + "'";
            logger.error(message);
            auditService.log(operation, AuditEventTypes.SUSPICIOUS_ACTIVITY, null, String.valueOf(id), message);
            return false;
        }

        if (userEntity.isSystem()) {
            String message = "Attempt to change system user '" + userEntity.getLogin() + "'";
            logger.error(message);
            auditService.log(operation, AuditEventTypes.SUSPICIOUS_ACTIVITY, userEntity.toString(), String.valueOf(id), message);
            return false;
        }

        return true;
    }

    @PostMapping("/confirm")
    public String confirmUser(@RequestParam("id") Long id, @RequestParam("comment") String comment, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        if (id != null) {
            userService.confirmUser(id, comment);
            redirectAttributes.addFlashAttribute("message", "User confirmed successfully");
        }

        return REDIRECT_USERS;
    }

    @PostMapping("/unconfirm")
    public String unconfirmUser(@RequestParam("id") Long id, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        if (id != null) {
            userService.unconfirmUser(id);
            redirectAttributes.addFlashAttribute("message", "User identity confirmation successfully rejected");
        }

        return REDIRECT_USERS;
    }

    @PostMapping("/lock")
    public String lockUser(@RequestParam("id") Long id, @RequestParam("reason") String reason, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        if (id != null && reason != null && !reason.trim().isEmpty()) {
            userService.lockUser(id, reason);
            redirectAttributes.addFlashAttribute("message", "User locked successfully");
        }

        return REDIRECT_USERS;
    }

    @PostMapping("/unlock")
    public String unlockUser(@RequestParam("id") Long id, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        if (id != null) {
            userService.unlockUser(id);
            redirectAttributes.addFlashAttribute("message", "User unlocked successfully");
        }

        return REDIRECT_USERS;
    }

    private UserEntity injectUserEntity(Model model, Long id) {
        UserEntity user = userService.findById(id);
        model.addAttribute("userEntity", user);
        return user;
    }

    private void injectChangeLoginDTO(Model model, UserEntity user) {
        ChangeLoginDTO changeLogin = new ChangeLoginDTO(user.getId(), user.getLogin());
        model.addAttribute("changeLogin", changeLogin);
    }

    private void injectChangePasswordDTO(Model model, UserEntity user) {
        ChangePasswordDTO changePassword = new ChangePasswordDTO(user.getId(), DEFAULT_PASSWORD_STUB);
        model.addAttribute("changePassword", changePassword);
    }

    private void injectChangeDisplayNameDTO(Model model, UserEntity user) {
        ChangeDisplayNameDTO changeDisplayName = new ChangeDisplayNameDTO(user.getId(), user.getDisplayName());
        model.addAttribute("changeDisplayName", changeDisplayName);
    }

    private void injectChangeEmailDTO(Model model, UserEntity user) {
        ChangeEmailDTO changeEmail = new ChangeEmailDTO(user.getId(), user.getEmail());
        model.addAttribute("changeEmail", changeEmail);
    }

    private void injectChangeRolesDTO(Model model, UserEntity user) {
        ChangeRolesDTO changeRoles = new ChangeRolesDTO(user.getId(), user.getRoles());
        model.addAttribute("changeRoles", changeRoles);
    }

    private void injectChangeRolesData(Model model, UserEntity user) {
        List<RoleEntity> allRoles = roleService.findAll();
        allRoles.sort((r1, r2) -> r1.getName().compareTo(r2.getName()));
        model.addAttribute("allRoles", allRoles);

        List<PermissionEntity> allPermissions = permissionService.findAll();
        allPermissions.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        model.addAttribute("allPermissions", allPermissions);
    }

    private void validateSelectedRoles(ChangeRolesDTO changeRoles, BindingResult bindingResult) {
        HashSet<RoleEntity> allRoles = new HashSet<>(roleService.findAll());
        if(!allRoles.containsAll(changeRoles.getRoles())) {
            bindingResult.rejectValue("roles", "user.roles.notExists");
        }
    }
}
