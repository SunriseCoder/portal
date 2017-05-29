package app.controller;

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
import app.entity.PermissionEntity;
import app.entity.RoleEntity;
import app.entity.UserEntity;
import app.enums.Permissions;
import app.service.PermissionService;
import app.service.RolesService;
import app.util.LogUtils;
import app.validator.UserEntityValidator;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {
    private static final Logger logger = LogManager.getLogger(AdminController.class.getName());

    private static final String ADMIN_DASHBOARD = "admin/dashboard";
    private static final String ADMIN_USERS_LIST = "admin/users/list";
    private static final String ADMIN_USERS_EDIT = "admin/users/edit";
    private static final String ADMIN_ROLES_LIST = "admin/roles/list";

    private static final String REDIRECT_ADMIN = "redirect:/admin";
    private static final String REDIRECT_USERS = "redirect:/admin/users";

    private static final String DEFAULT_PASSWORD_STUB = "<hidden>";

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RolesService rolesService;

    @Autowired
    private UserEntityValidator userValidator;

    @GetMapping({"", "/"})
    public String index(Model model) {
        if (!userService.hasPermission(Permissions.ADMIN_PAGE)) {
            return REDIRECT_MAIN;
        }

        injectUser(model);
        return ADMIN_DASHBOARD;
    }

    @GetMapping("/users")
    public String userList(Model model) {
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            return REDIRECT_ADMIN;
        }

        injectUser(model);
        List<UserEntity> userList = userService.findAll();
        model.addAttribute("userList", userList);
        return ADMIN_USERS_LIST;
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(Model model, @PathVariable Long id) {
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            return REDIRECT_ADMIN;
        }

        injectUser(model);
        UserEntity user = injectUserEntity(model, id);

        if (user == null) {
            return REDIRECT_USERS;
        }

        injectChangeLoginDTO(model, user);
        injectChangePasswordDTO(model, user);
        injectChangeDisplayNameDTO(model, user);
        injectChangeEmailDTO(model, user);
        injectChangeRolesDTO(model, user);
        injectChangeRolesData(model, user);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/login")
    public String changeLogin(@ModelAttribute("changeLogin") ChangeLoginDTO changeLogin, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_EDIT, bindingResult, "login");
        userValidator.validateLogin(changeLogin.getLogin(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity user = userService.findById(changeLogin.getId());
            user.setLogin(changeLogin.getLogin());
            userService.save(user);
        }

        injectUser(model);
        UserEntity user = injectUserEntity(model, changeLogin.getId());
        injectChangePasswordDTO(model, user);
        injectChangeDisplayNameDTO(model, user);
        injectChangeEmailDTO(model, user);
        injectChangeRolesDTO(model, user);
        injectChangeRolesData(model, user);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/password")
    public String changePassword(@ModelAttribute("changePassword") ChangePasswordDTO changePassword, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_EDIT, bindingResult, "pass");
        userValidator.validatePassword(changePassword.getPass(), bindingResult);
        if (!bindingResult.hasErrors() && !DEFAULT_PASSWORD_STUB.equals(changePassword.getPass())) {
            UserEntity user = userService.findById(changePassword.getId());
            user.setPass(changePassword.getPass());
            userService.encryptPass(user);
            userService.save(user);
        }

        injectUser(model);
        UserEntity user = injectUserEntity(model, changePassword.getId());
        injectChangeLoginDTO(model, user);
        injectChangeDisplayNameDTO(model, user);
        injectChangeEmailDTO(model, user);
        injectChangeRolesDTO(model, user);
        injectChangeRolesData(model, user);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/display-name")
    public String changeDisplayName(@ModelAttribute("changeDisplayName") ChangeDisplayNameDTO changeDisplayName, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_EDIT, bindingResult, "displayName");
        userValidator.validateDisplayName(changeDisplayName.getDisplayName(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity user = userService.findById(changeDisplayName.getId());
            user.setDisplayName(changeDisplayName.getDisplayName());
            userService.save(user);
        }

        injectUser(model);
        UserEntity user = injectUserEntity(model, changeDisplayName.getId());
        injectChangeLoginDTO(model, user);
        injectChangePasswordDTO(model, user);
        injectChangeEmailDTO(model, user);
        injectChangeRolesDTO(model, user);
        injectChangeRolesData(model, user);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/email")
    public String changeEmail(@ModelAttribute("changeEmail") ChangeEmailDTO changeEmail, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_EDIT, bindingResult, "email");
        userValidator.validateEmail(changeEmail.getEmail(), bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity user = userService.findById(changeEmail.getId());
            user.setEmail(changeEmail.getEmail());
            userService.save(user);
        }

        injectUser(model);
        UserEntity user = injectUserEntity(model, changeEmail.getId());
        injectChangeLoginDTO(model, user);
        injectChangePasswordDTO(model, user);
        injectChangeDisplayNameDTO(model, user);
        injectChangeRolesDTO(model, user);
        injectChangeRolesData(model, user);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/roles")
    public String changeRoles(@ModelAttribute("changeRoles") ChangeRolesDTO changeRoles, Model model,
                    HttpServletRequest request, BindingResult bindingResult) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_VIEW)) {
            return REDIRECT_ADMIN;
        }

        validatePermission(Permissions.ADMIN_USERS_ROLES, bindingResult, "roles");
        validateSelectedRoles(changeRoles, bindingResult);
        if (!bindingResult.hasErrors()) {
            UserEntity user = userService.findById(changeRoles.getId());
            user.setRoles(changeRoles.getRoles());
            userService.save(user);
        }

        injectUser(model);
        UserEntity user = injectUserEntity(model, changeRoles.getId());
        injectChangeLoginDTO(model, user);
        injectChangePasswordDTO(model, user);
        injectChangeDisplayNameDTO(model, user);
        injectChangeEmailDTO(model, user);
        injectChangeRolesData(model, user);
        return ADMIN_USERS_EDIT;
    }

    @PostMapping("/users/confirm")
    public String confirmUser(@RequestParam("id") Long id, @RequestParam("comment") String comment, Model model,
                    HttpServletRequest request, RedirectAttributes redirectAttributes) {

        LogUtils.logRequest(logger, request);
        if (!userService.hasPermission(Permissions.ADMIN_USERS_CONFIRM)) {
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
            return REDIRECT_ADMIN;
        }

        injectUser(model);
        List<RoleEntity> roleList = rolesService.findAll();
        roleList.sort(
                        (r1, r2) -> r1.getName().compareTo(r2.getName()));
        roleList.forEach(
                        r -> r.getPermissions().sort(
                                        (r1, r2) -> r1.getName().compareTo(r2.getName())));
        model.addAttribute("roleList", roleList);
        return ADMIN_ROLES_LIST;
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
        List<RoleEntity> allRoles = rolesService.findAll();
        allRoles.sort((r1, r2) -> r1.getName().compareTo(r2.getName()));
        model.addAttribute("allRoles", allRoles);

        List<PermissionEntity> allPermissions = permissionService.findAll();
        allPermissions.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        model.addAttribute("allPermissions", allPermissions);
    }

    private void validateSelectedRoles(ChangeRolesDTO changeRoles, BindingResult bindingResult) {
        HashSet<RoleEntity> allRoles = new HashSet<>(rolesService.findAll());
        if(!allRoles.containsAll(changeRoles.getRoles())) {
            bindingResult.rejectValue("roles", "user.roles.notExists");
        }
    }
}
