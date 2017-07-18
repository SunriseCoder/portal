package app.controller.admin;

import java.util.Arrays;
import java.util.List;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.controller.BaseController;
import app.entity.PermissionEntity;
import app.entity.RoleEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.enums.Permissions;
import app.service.PermissionService;
import app.service.RoleService;
import app.validator.RoleEntityValidator;

@Controller
@RequestMapping("/admin/roles")
public class RoleController extends BaseController {
    private static final Logger logger = LogManager.getLogger(RoleController.class.getName());

    private static final String ROLES_LIST = "admin/roles/list";
    private static final String ROLES_EDIT = "admin/roles/edit";
    private static final String REDIRECT_ROLES = "redirect:/admin/roles";

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleEntityValidator roleValidator;

    @GetMapping
    public String rolesList(Model model) {
        injectUser(model);

        List<RoleEntity> roleList = roleService.findAll();
        roleList.sort((r1, r2) -> r1.getName().compareTo(r2.getName()));
        model.addAttribute("roleList", roleList);

        injectAllPermissions(model);

        auditService.log(OperationTypes.ACCESS_ROLE_LIST, AuditEventTypes.ACCESS_ALLOWED);

        return ROLES_LIST;
    }

    @GetMapping("/create")
    public String createRole(Model model) {
        injectUser(model);
        injectRoleEntity(model, new RoleEntity());
        injectAllPermissions(model);

        auditService.log(OperationTypes.ACCESS_ROLE_CREATE, AuditEventTypes.ACCESS_ALLOWED);

        return ROLES_EDIT;
    }

    @GetMapping("/edit")
    public String editRole(Model model, @RequestParam("id") Long id) {
        RoleEntity roleEntity = roleService.findById(id);
        if (roleEntity == null) {
            return REDIRECT_ROLES;
        }

        injectUser(model);
        injectRoleEntity(model, roleEntity);
        injectAllPermissions(model);

        auditService.log(OperationTypes.ACCESS_ROLE_EDIT, AuditEventTypes.ACCESS_ALLOWED);

        return ROLES_EDIT;
    }

    @PostMapping("/save")
    public String saveRole(@ModelAttribute("roleEntity") RoleEntity roleEntity, Model model,
                    HttpServletRequest request, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        validateRolePermissions(roleEntity, bindingResult);
        roleValidator.validate(roleEntity, bindingResult);
        if (bindingResult.hasErrors()) {
            injectUser(model);
            injectRoleEntity(model, roleEntity);
            injectAllPermissions(model);

            String error = bindingErrorsToString(bindingResult);
            auditService.log(OperationTypes.CHANGE_ROLE_SAVE, AuditEventTypes.VALIDATION_ERROR, null, roleEntity.toString(), error);
            return ROLES_EDIT;
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

    @PostMapping("/delete")
    public String deleteRole(@RequestParam("id") Long id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String auditObject = request.getQueryString();
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

    private void injectRoleEntity(Model model, RoleEntity roleEntity) {
        model.addAttribute("roleEntity", roleEntity);
    }

    private void injectAllPermissions(Model model) {
        Set<String> systemPermissions = Arrays.stream(Permissions.SYSTEM)
                        .map(p -> p.name())
                        .collect(Collectors.toSet());

        List<PermissionEntity> permissions = permissionService.findAll();

        permissions = permissions.stream()
                        .filter(p -> !systemPermissions.contains(p.getName()))
                        .collect(Collectors.toList());

        permissions.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        model.addAttribute("allPermissions", permissions);
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
