package app.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.entity.UserEntity;
import app.enums.OperationTypes;
import app.enums.Permissions;
import app.security.AccessCheckResult.Action;
import app.service.UserService;

@Component
public class SecurityChecker {
    @Autowired
    private UserService userService;

    private Map<String, AccessRule> urlRules;
    private Map<Pattern, AccessRule> patternRules;

    public AccessCheckResult check(HttpServletRequest request) {
        UserEntity user = userService.getLoggedInUser();
        // TODO Check if IP is banned. If yes - return DENY result with message

        // Check URL-based access rules and return "REDIRECT" if user has no permissions
        String url = request.getRequestURI();
        AccessRule rule = urlRules.get(url);
        if (rule != null) {
            return checkRule(user, rule);
        }

        // Checking Pattern-based access rules
        for (Map.Entry<Pattern, AccessRule> entry : patternRules.entrySet()) {
            Pattern pattern = entry.getKey();
            if (pattern.matcher(url).matches()) {
                return checkRule(user, entry.getValue());
            }
        }

        // If no rules has been found, return "NOT_FOUND"
        return new AccessCheckResult(Action.NOT_FOUND, null);
    }

    private AccessCheckResult checkRule(UserEntity user, AccessRule rule) {
        if (hasAllPermissions(user, rule)) {
            return new AccessCheckResult(Action.ALLOW, null);
        } else {
            return new AccessCheckResult(Action.REDIRECT, rule.getRedirect());
        }
    }

    private boolean hasAllPermissions(UserEntity user, AccessRule rule) {
        Permissions[] permissions = rule.getPermissions();
        for (Permissions permission : permissions) {
            if (!user.hasPermission(permission.name())) {
                return false;
            }
        }
        return true;
    }

    @PostConstruct
    private void initializeAccessRules() {
        urlRules = new HashMap<>();
        List<AccessRule> ruleList = createUrlRules();
        for (AccessRule rule : ruleList) {
            urlRules.put(rule.getUrl(), rule);
        }

        patternRules = new LinkedHashMap<>();
        ruleList = createPatternRules();
        for (AccessRule rule : ruleList) {
            patternRules.put(Pattern.compile(rule.getUrl()), rule);
        }
    }

    private List<AccessRule> createUrlRules() {
        List<AccessRule> rules = new ArrayList<>();

        // TODO XXX URL-based access rules (Remove TO_DO, but leave X.X.X after done)
/*
[INFO] 2017-07-07 16:24:09.117 [main] RequestMappingHandlerMapping - Mapped "{[/admin/logs/file],methods=[GET]}" onto public java.lang.String app.controller.admin.LogController.logFile(javax.servlet.http.HttpServletRequest,org.springframework.ui.Model,java.lang.String)
[INFO] 2017-07-07 16:24:09.121 [main] RequestMappingHandlerMapping - Mapped "{[/admin/logs],methods=[GET]}" onto public java.lang.String app.controller.admin.LogController.logList(javax.servlet.http.HttpServletRequest,org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.135 [main] RequestMappingHandlerMapping - Mapped "{[/admin || /admin/],methods=[GET]}" onto public java.lang.String app.controller.AdminController.index(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.136 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users],methods=[GET]}" onto public java.lang.String app.controller.AdminController.userList(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.136 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/display-name],methods=[POST]}" onto public java.lang.String app.controller.AdminController.changeDisplayName(app.dto.ChangeDisplayNameDTO,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.validation.BindingResult)
[INFO] 2017-07-07 16:24:09.136 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/edit],methods=[GET]}" onto public java.lang.String app.controller.AdminController.editUser(org.springframework.ui.Model,java.lang.Long)
[INFO] 2017-07-07 16:24:09.136 [main] RequestMappingHandlerMapping - Mapped "{[/admin/roles/delete],methods=[POST]}" onto public java.lang.String app.controller.AdminController.deleteRole(java.lang.Long,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.web.servlet.mvc.support.RedirectAttributes)
[INFO] 2017-07-07 16:24:09.137 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/unconfirm],methods=[POST]}" onto public java.lang.String app.controller.AdminController.unconfirmUser(java.lang.Long,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.web.servlet.mvc.support.RedirectAttributes)
[INFO] 2017-07-07 16:24:09.137 [main] RequestMappingHandlerMapping - Mapped "{[/admin/roles],methods=[GET]}" onto public java.lang.String app.controller.AdminController.rolesList(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.137 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/login],methods=[POST]}" onto public java.lang.String app.controller.AdminController.changeLogin(app.dto.ChangeLoginDTO,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.validation.BindingResult)
[INFO] 2017-07-07 16:24:09.137 [main] RequestMappingHandlerMapping - Mapped "{[/admin/roles/create],methods=[GET]}" onto public java.lang.String app.controller.AdminController.createRole(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.138 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/confirm],methods=[POST]}" onto public java.lang.String app.controller.AdminController.confirmUser(java.lang.Long,java.lang.String,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.web.servlet.mvc.support.RedirectAttributes)
[INFO] 2017-07-07 16:24:09.138 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/password],methods=[POST]}" onto public java.lang.String app.controller.AdminController.changePassword(app.dto.ChangePasswordDTO,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.validation.BindingResult)
[INFO] 2017-07-07 16:24:09.139 [main] RequestMappingHandlerMapping - Mapped "{[/admin/roles/edit],methods=[GET]}" onto public java.lang.String app.controller.AdminController.editRole(java.lang.Long,org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.141 [main] RequestMappingHandlerMapping - Mapped "{[/admin/audit],methods=[GET]}" onto public java.lang.String app.controller.AdminController.auditList(org.springframework.ui.Model,javax.servlet.http.HttpServletRequest)
[INFO] 2017-07-07 16:24:09.141 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/email],methods=[POST]}" onto public java.lang.String app.controller.AdminController.changeEmail(app.dto.ChangeEmailDTO,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.validation.BindingResult)
[INFO] 2017-07-07 16:24:09.141 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/lock],methods=[POST]}" onto public java.lang.String app.controller.AdminController.lockUser(java.lang.Long,java.lang.String,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.web.servlet.mvc.support.RedirectAttributes)
[INFO] 2017-07-07 16:24:09.141 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/unlock],methods=[POST]}" onto public java.lang.String app.controller.AdminController.unlockUser(java.lang.Long,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.web.servlet.mvc.support.RedirectAttributes)
[INFO] 2017-07-07 16:24:09.143 [main] RequestMappingHandlerMapping - Mapped "{[/admin/users/roles],methods=[POST]}" onto public java.lang.String app.controller.AdminController.changeRoles(app.dto.ChangeRolesDTO,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.validation.BindingResult)
[INFO] 2017-07-07 16:24:09.143 [main] RequestMappingHandlerMapping - Mapped "{[/admin/roles/save],methods=[POST]}" onto public java.lang.String app.controller.AdminController.saveRole(app.entity.RoleEntity,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest,org.springframework.validation.BindingResult,org.springframework.web.servlet.mvc.support.RedirectAttributes)
[INFO] 2017-07-07 16:24:09.145 [main] RequestMappingHandlerMapping - Mapped "{[/rest/files/list]}" onto public app.entity.FolderEntity app.controller.FileRestController.list(javax.servlet.http.HttpServletRequest) throws java.lang.Exception
[INFO] 2017-07-07 16:24:09.146 [main] RequestMappingHandlerMapping - Mapped "{[/rest/files/get]}" onto public void app.controller.FileRestController.getFile(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse,java.lang.String)
[INFO] 2017-07-07 16:24:09.146 [main] RequestMappingHandlerMapping - Mapped "{[/rest/files/upload],methods=[POST]}" onto public void app.controller.FileRestController.uploadFile(org.springframework.web.multipart.MultipartFile,javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
[INFO] 2017-07-07 16:24:09.151 [main] RequestMappingHandlerMapping - Mapped "{[/],methods=[GET]}" onto public java.lang.String app.controller.PageController.index(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.152 [main] RequestMappingHandlerMapping - Mapped "{[/register],methods=[GET]}" onto public java.lang.String app.controller.PageController.register(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.152 [main] RequestMappingHandlerMapping - Mapped "{[/files],methods=[GET]}" onto public java.lang.String app.controller.PageController.files(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.155 [main] RequestMappingHandlerMapping - Mapped "{[/login],methods=[GET]}" onto public java.lang.String app.controller.PageController.login(javax.servlet.http.HttpServletRequest,org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.155 [main] RequestMappingHandlerMapping - Mapped "{[/logout],methods=[GET]}" onto public java.lang.String app.controller.PageController.logout(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.155 [main] RequestMappingHandlerMapping - Mapped "{[/upload],methods=[GET]}" onto public java.lang.String app.controller.PageController.upload(org.springframework.ui.Model)
[INFO] 2017-07-07 16:24:09.156 [main] RequestMappingHandlerMapping - Mapped "{[/register],methods=[POST]}" onto public java.lang.String app.controller.RegisterController.registration(app.entity.UserEntity,org.springframework.validation.BindingResult,org.springframework.ui.Model,javax.servlet.http.HttpServletRequest)
[INFO] 2017-07-07 16:24:09.158 [main] RequestMappingHandlerMapping - Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
[INFO] 2017-07-07 16:24:09.158 [main] RequestMappingHandlerMapping - Mapped "{[/error],produces=[text/html]}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
*/
        return rules;
    }

    private List<AccessRule> createPatternRules() {
        List<AccessRule> rules = new ArrayList<>();

        // TODO XXX Pattern-based access rules (Remove TO_DO, but leave X.X.X after done)
        // TODO cut off this rule after migration of security and audit is done
        rules.add(new AccessRule(".*", OperationTypes.ACCESS_PAGE_MAIN, "/", Permissions.PAGES_VIEW));

/*
[INFO] 2017-07-07 16:24:09.207 [main] SimpleUrlHandlerMapping - Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
[INFO] 2017-07-07 16:24:09.208 [main] SimpleUrlHandlerMapping - Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
[INFO] 2017-07-07 16:24:09.273 [main] SimpleUrlHandlerMapping - Mapped URL path [/** /favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
         */

        return rules;
    }
}
