package app.controller;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.dto.ChangeDisplayNameDTO;
import app.dto.ChangeEmailDTO;
import app.dto.ChangeLoginDTO;
import app.dto.ChangePasswordDTO;
import app.dto.ChangeRolesDTO;
import app.entity.AuditEventEntity;
import app.entity.AuditEventTypeEntity;
import app.entity.OperationTypeEntity;
import app.entity.PermissionEntity;
import app.entity.RoleEntity;
import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.enums.Permissions;
import app.service.PermissionService;
import app.service.RoleService;
import app.util.LogUtils;
import app.validator.RoleEntityValidator;
import app.validator.UserEntityValidator;

@Controller
@RequestMapping(BaseController.PATH_ADMIN)
public class AdminController extends BaseController {
    private static final Logger logger = LogManager.getLogger(AdminController.class.getName());

    private static final String ADMIN_DASHBOARD = "admin/dashboard";
    private static final String ADMIN_USERS_LIST = "admin/users/list";
    private static final String ADMIN_USERS_EDIT = "admin/users/edit";
    private static final String ADMIN_ROLES_LIST = "admin/roles/list";
    private static final String ADMIN_ROLES_EDIT = "admin/roles/edit";
    private static final String ADMIN_AUDIT_LIST = "admin/audit/list";

    private static final String REDIRECT_USERS = "redirect:/admin/users";
    private static final String REDIRECT_ROLES = "redirect:/admin/roles";

    private static final String DEFAULT_PASSWORD_STUB = "<hidden>";

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private UserEntityValidator userValidator;
    @Autowired
    private RoleEntityValidator roleValidator;

    @GetMapping({"", "/"})
    public String index(Model model) {
        if (!userService.hasPermission(Permissions.ADMIN_PAGE)) {
            logger.warn("Attempt to enter admin section without permissions");
            auditService.log(OperationTypes.ACCESS_ADMIN_DASHBOARD, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_MAIN;
        }

        injectUser(model);
        auditService.log(OperationTypes.ACCESS_ADMIN_DASHBOARD, AuditEventTypes.ACCESS_ALLOWED);
        return ADMIN_DASHBOARD;
    }

    @GetMapping("/users")
    public String userList(Model model) {
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            logger.warn("Attempt to enter user management without permissions");
            auditService.log(OperationTypes.ACCESS_USER_LIST, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_ADMIN;
        }

        injectUser(model);
        List<UserEntity> userList = userService.findAll();
        model.addAttribute("userList", userList);

        auditService.log(OperationTypes.ACCESS_USER_LIST, AuditEventTypes.ACCESS_ALLOWED);

        return ADMIN_USERS_LIST;
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(Model model, @PathVariable Long id) {
        String auditObject = "userId=" + id;

        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            logger.warn("Attempt to edit user without permissions");
            auditService.log(OperationTypes.ACCESS_USER_EDIT, AuditEventTypes.ACCESS_DENIED, auditObject);
            return REDIRECT_ADMIN;
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, id);

        if (userEntity == null) {
            auditService.log(OperationTypes.ACCESS_USER_EDIT, AuditEventTypes.ENTITY_NOT_EXISTS, auditObject);
            return REDIRECT_USERS;
        }

        injectChangeLoginDTO(model, userEntity);
        injectChangePasswordDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        auditService.log(OperationTypes.ACCESS_USER_EDIT, AuditEventTypes.ACCESS_ALLOWED, auditObject);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/login")
    public String changeLogin(@ModelAttribute("changeLogin") ChangeLoginDTO changeLogin, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);

        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            logger.warn("Attempt to change user's login without permissions");
            auditService.log(OperationTypes.CHANGE_USER_LOGIN, AuditEventTypes.ACCESS_DENIED, changeLogin.toString());
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_EDIT, bindingResult, "login");
        userValidator.validateLogin(changeLogin.getLogin(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity userEntity = userService.findById(changeLogin.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setLogin(changeLogin.getLogin());
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(OperationTypes.CHANGE_USER_LOGIN, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user login", e);
                auditService.log(OperationTypes.CHANGE_USER_LOGIN, AuditEventTypes.SAVING_ERROR,
                                auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        } else {
            String error = bindingErrorsToString(bindingResult);
            logger.warn("Validation error due to save user by changing user login: {}", error);
            auditService.log(OperationTypes.CHANGE_USER_LOGIN, AuditEventTypes.VALIDATION_ERROR, changeLogin.toString(), null, error);
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changeLogin.getId());
        injectChangePasswordDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/password")
    public String changePassword(@ModelAttribute("changePassword") ChangePasswordDTO changePassword, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);

        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            logger.warn("Attempt to change user's password without permissions");
            auditService.log(OperationTypes.CHANGE_USER_PASSWORD, AuditEventTypes.ACCESS_DENIED, changePassword.toString());
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_EDIT, bindingResult, "pass");
        userValidator.validatePassword(changePassword.getPass(), bindingResult);
        if (!bindingResult.hasErrors() && !DEFAULT_PASSWORD_STUB.equals(changePassword.getPass())) {
            UserEntity userEntity = userService.findById(changePassword.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setPass(changePassword.getPass());
            userService.encryptPass(userEntity);
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(OperationTypes.CHANGE_USER_PASSWORD, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user password", e);
                auditService.log(OperationTypes.CHANGE_USER_PASSWORD, AuditEventTypes.SAVING_ERROR,
                                auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changePassword.getId());
        injectChangeLoginDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/display-name")
    public String changeDisplayName(@ModelAttribute("changeDisplayName") ChangeDisplayNameDTO changeDisplayName, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            logger.warn("Attempt to change user's display name without permissions");
            auditService.log(OperationTypes.CHANGE_USER_DISPLAY_NAME, AuditEventTypes.ACCESS_DENIED, changeDisplayName.toString());
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_EDIT, bindingResult, "displayName");
        userValidator.validateDisplayName(changeDisplayName.getDisplayName(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity userEntity = userService.findById(changeDisplayName.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setDisplayName(changeDisplayName.getDisplayName());
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(OperationTypes.CHANGE_USER_DISPLAY_NAME, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user display name", e);
                auditService.log(OperationTypes.CHANGE_USER_DISPLAY_NAME, AuditEventTypes.SAVING_ERROR,
                                auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changeDisplayName.getId());
        injectChangeLoginDTO(model, userEntity);
        injectChangePasswordDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/email")
    public String changeEmail(@ModelAttribute("changeEmail") ChangeEmailDTO changeEmail, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            logger.warn("Attempt to change user's email without permissions");
            auditService.log(OperationTypes.CHANGE_USER_EMAIL, AuditEventTypes.ACCESS_DENIED, changeEmail.toString());
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_EDIT, bindingResult, "email");
        userValidator.validateEmail(changeEmail.getEmail(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity userEntity = userService.findById(changeEmail.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setEmail(changeEmail.getEmail());
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(OperationTypes.CHANGE_USER_EMAIL, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user email", e);
                auditService.log(OperationTypes.CHANGE_USER_EMAIL, AuditEventTypes.SAVING_ERROR,
                            auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changeEmail.getId());
        injectChangeLoginDTO(model, userEntity);
        injectChangePasswordDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeRolesDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/roles")
    public String changeRoles(@ModelAttribute("changeRoles") ChangeRolesDTO changeRoles, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            logger.warn("Attempt to change user's roles without permissions");
            auditService.log(OperationTypes.CHANGE_USER_ROLES, AuditEventTypes.ACCESS_DENIED, changeRoles.toString());
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_ROLES, bindingResult, "roles");
        validateSelectedRoles(changeRoles, bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity userEntity = userService.findById(changeRoles.getId());
            String auditObjectBefore = userEntity.toString();
            userEntity.setRoles(changeRoles.getRoles());
            String auditObjectBeforeSave = userEntity.toString();
            try {
                userEntity = userService.save(userEntity);
                String auditObjectAfter = userEntity.toString();
                auditService.log(OperationTypes.CHANGE_USER_ROLES, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
            } catch (Exception e) {
                logger.error("Error due to save user by changing user roles", e);
                auditService.log(OperationTypes.CHANGE_USER_ROLES, AuditEventTypes.SAVING_ERROR,
                            auditObjectBefore, auditObjectBeforeSave, e.getMessage());
            }
        }

        injectUser(model);
        UserEntity userEntity = injectUserEntity(model, changeRoles.getId());
        injectChangeLoginDTO(model, userEntity);
        injectChangePasswordDTO(model, userEntity);
        injectChangeDisplayNameDTO(model, userEntity);
        injectChangeEmailDTO(model, userEntity);
        injectChangeRolesData(model, userEntity);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/confirm")
    public String confirmUser(@RequestParam("id") Long id, @RequestParam("comment") String comment, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_CONFIRM)) {
            logger.warn("Attempt to confirm user's identity without permissions");
            String object = MessageFormat.format("ConfirmUser[id={},comment={}]", id, comment);
            auditService.log(OperationTypes.CHANGE_USER_CONFIRM, AuditEventTypes.ACCESS_DENIED, object);
            return REDIRECT_ADMIN;
        }

        if (id != null) {
            userService.confirmUser(id, comment);
            redirectAttributes.addFlashAttribute("message", "User confirmed successfully");
        }

        return REDIRECT_USERS;
    }

    @PostMapping("/users/unconfirm")
    public String unconfirmUser(@RequestParam("id") Long id, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_UNCONFIRM)) {
            logger.warn("Attempt to reject user's identity without permissions");
            String object = MessageFormat.format("UnconfirmUser[id={}]", id);
            auditService.log(OperationTypes.CHANGE_USER_UNCONFIRM, AuditEventTypes.ACCESS_DENIED, object);
            return REDIRECT_ADMIN;
        }

        if (id != null) {
            userService.unconfirmUser(id);
            redirectAttributes.addFlashAttribute("message", "User identity confirmation successfully rejected");
        }

        return REDIRECT_USERS;
    }

    @PostMapping("/users/lock")
    public String lockUser(@RequestParam("id") Long id, @RequestParam("reason") String reason, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_LOCK)) {
            logger.warn("Attempt to lock user without permissions");
            String object = MessageFormat.format("LockUser[id={},reason={}]", id, reason);
            auditService.log(OperationTypes.CHANGE_USER_LOCK, AuditEventTypes.ACCESS_DENIED, object);
            return REDIRECT_ADMIN;
        }

        if (id != null && reason != null && !reason.trim().isEmpty()) {
            userService.lockUser(id, reason);
            redirectAttributes.addFlashAttribute("message", "User locked successfully");
        }

        return REDIRECT_USERS;
    }

    @PostMapping("/users/unlock")
    public String unlockUser(@RequestParam("id") Long id, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_UNLOCK)) {
            logger.warn("Attempt to unlock user without permissions");
            String object = MessageFormat.format("UnlockUser[id={}]", id);
            auditService.log(OperationTypes.CHANGE_USER_UNLOCK, AuditEventTypes.ACCESS_DENIED, object);
            return REDIRECT_ADMIN;
        }

        if (id != null) {
            userService.unlockUser(id);
            redirectAttributes.addFlashAttribute("message", "User unlocked successfully");
        }

        return REDIRECT_USERS;
    }

    @GetMapping("/roles")
    public String rolesList(Model model) {
        if (!userService.hasPermission(Permissions.ADMIN_ROLES_VIEW)) {
            logger.warn("Attempt to enter role management without permissions");
            auditService.log(OperationTypes.ACCESS_ROLE_LIST, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_ADMIN;
        }

        injectUser(model);

        List<RoleEntity> roleList = roleService.findAll();
        roleList.sort(
                        (r1, r2) -> r1.getName().compareTo(r2.getName()));
        model.addAttribute("roleList", roleList);

        List<PermissionEntity> permissionList = permissionService.findAll();
        permissionList.sort(
                        (r1, r2) -> r1.getName().compareTo(r2.getName()));
        model.addAttribute("permissionList", permissionList);

        auditService.log(OperationTypes.ACCESS_ROLE_LIST, AuditEventTypes.ACCESS_ALLOWED);

        return ADMIN_ROLES_LIST;
    }

    @GetMapping("/roles/create")
    public String createRole(Model model) {
        if (!userService.hasPermission(Permissions.ADMIN_ROLES_EDIT)) {
            logger.warn("Attempt to create new role without permissions");
            auditService.log(OperationTypes.ACCESS_ROLE_CREATE, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_ADMIN;
        }

        injectUser(model);
        injectRoleEntity(model, new RoleEntity());
        injectAllPermissions(model);

        auditService.log(OperationTypes.ACCESS_ROLE_CREATE, AuditEventTypes.ACCESS_ALLOWED);

        return ADMIN_ROLES_EDIT;
    }

    @GetMapping("/roles/edit/{id}")
    public String editRole(@PathVariable Long id, Model model) {
        if (!userService.hasPermission(Permissions.ADMIN_ROLES_EDIT)) {
            logger.warn("Attempt to edit role without permissions");
            auditService.log(OperationTypes.ACCESS_ROLE_EDIT, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_ADMIN;
        }

        RoleEntity roleEntity = roleService.findById(id);
        if (roleEntity == null) {
            return REDIRECT_ROLES;
        }

        injectUser(model);
        injectRoleEntity(model, roleEntity);
        injectAllPermissions(model);

        auditService.log(OperationTypes.ACCESS_ROLE_EDIT, AuditEventTypes.ACCESS_ALLOWED);

        return ADMIN_ROLES_EDIT;
    }

    @PostMapping("/roles/save")
    public String saveRole(@ModelAttribute("roleEntity") RoleEntity roleEntity, Model model,
                    HttpServletRequest request, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_ROLES_EDIT)) {
            logger.warn("Attempt to save role without permissions");
            auditService.log(OperationTypes.CHANGE_ROLE_SAVE, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_ROLES;
        }

        validateRolePermissions(roleEntity, bindingResult);
        roleValidator.validate(roleEntity, bindingResult);
        if (bindingResult.hasErrors()) {
            injectUser(model);
            injectRoleEntity(model, roleEntity);
            injectAllPermissions(model);

            String error = bindingErrorsToString(bindingResult);
            auditService.log(OperationTypes.CHANGE_ROLE_SAVE, AuditEventTypes.VALIDATION_ERROR, null, roleEntity.toString(), error);
            return ADMIN_ROLES_EDIT;
        }

        String auditObjectBefore = null;
        if (roleEntity.getId() != null) {
            RoleEntity storedRoleEntity = roleService.findById(roleEntity.getId());
            if (storedRoleEntity != null) {
                auditObjectBefore = storedRoleEntity.toString();
            }
        }
        String auditObjectBeforeSave = roleEntity != null ? roleEntity.toString() : null;

        try {
            roleEntity = roleService.save(roleEntity);
            String auditObjectAfter = roleEntity.toString();
            auditService.log(OperationTypes.CHANGE_ROLE_SAVE, AuditEventTypes.SUCCESSFUL, auditObjectBefore, auditObjectAfter);
        } catch (Exception e) {
            logger.error("Error due to save role", e);
            auditService.log(OperationTypes.CHANGE_ROLE_SAVE, AuditEventTypes.SAVING_ERROR,
                    auditObjectBefore, auditObjectBeforeSave, e.getMessage());
        }

        redirectAttributes.addFlashAttribute("message", "Role has been saved successfully");
        return REDIRECT_ROLES;
    }

    @PostMapping("/roles/delete")
    public String deleteRole(@RequestParam("id") Long id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        LogUtils.logRequest(logger, request);
        String auditObject = "roleId=" + id;
        if (!userService.hasPermission(Permissions.ADMIN_ROLES_EDIT)) {
            logger.warn("Attempt to delete role without permissions");
            auditService.log(OperationTypes.CHANGE_ROLE_DELETE, AuditEventTypes.ACCESS_DENIED, auditObject);
            return REDIRECT_ADMIN;
        }

        RoleEntity roleEntity = roleService.findById(id);
        if (roleEntity == null) {
            auditService.log(OperationTypes.CHANGE_ROLE_DELETE, AuditEventTypes.ENTITY_NOT_EXISTS, auditObject);
            return REDIRECT_ROLES;
        }

        String auditObjectBefore = roleEntity.toString();

        try {
            roleService.delete(roleEntity);
            auditService.log(OperationTypes.CHANGE_ROLE_DELETE, AuditEventTypes.SUCCESSFUL, auditObjectBefore, null);
            redirectAttributes.addFlashAttribute("message", "Role has been deleted successfully");
        } catch (Exception e) {
            String message = "Error due to delete role";
            logger.error(message, e);
            auditService.log(OperationTypes.CHANGE_ROLE_DELETE, AuditEventTypes.DELETE_ERROR, auditObjectBefore, null, e.getMessage());
            redirectAttributes.addFlashAttribute("error", message);
        }

        return REDIRECT_ROLES;
    }

    @GetMapping("/audit")
    public String auditList(Model model, HttpServletRequest request) {
        if (!userService.hasPermission(Permissions.ADMIN_AUDIT_VIEW)) {
            logger.warn("Attempt to enter audit page without permissions");
            auditService.log(OperationTypes.ACCESS_ADMIN_AUDIT, AuditEventTypes.ACCESS_DENIED);
            return REDIRECT_ADMIN;
        }

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

    private void injectRoleEntity(Model model, RoleEntity roleEntity) {
        model.addAttribute("roleEntity", roleEntity);
    }

    private void injectAllPermissions(Model model) {
        List<PermissionEntity> permissions = permissionService.findAll();
        permissions.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        model.addAttribute("allPermissions", permissions);
    }

    private void validateSelectedRoles(ChangeRolesDTO changeRoles, BindingResult bindingResult) {
        HashSet<RoleEntity> allRoles = new HashSet<>(roleService.findAll());
        if(!allRoles.containsAll(changeRoles.getRoles())) {
            bindingResult.rejectValue("roles", "user.roles.notExists");
        }
    }

    private void validateRolePermissions(RoleEntity roleEntity, BindingResult bindingResult) {
        Set<PermissionEntity> permissions = roleEntity.getPermissions();
        if (permissions == null) {
            return;
        }
        Set<Long> allIds = permissionService.findAll().stream().map(PermissionEntity::getId).collect(Collectors.toSet());
        Set<Long> selectedIds = permissions.stream().map(PermissionEntity::getId).collect(Collectors.toSet());
        if (!allIds.containsAll(selectedIds)) {
            bindingResult.rejectValue("permissions", "Non-existing permission");
        }
    }
}
