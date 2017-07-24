package app.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.enums.Permissions;
import app.security.AccessCheckResult.Action;
import app.service.AuditService;
import app.service.UserService;
import app.service.admin.IPBanService;

@Component
public class SecurityChecker {
    private static final Logger logger = LogManager.getLogger(SecurityChecker.class.getName());

    @Autowired
    private AuditService auditService;
    @Autowired
    private IPBanService ipBanService;
    @Autowired
    private UserService userService;

    private Map<String, AccessRule> urlRules;
    private Map<Pattern, AccessRule> patternRules;

    public AccessCheckResult check(HttpServletRequest request) {
        UserEntity user = userService.getLoggedInUser();
        String ip = request.getRemoteAddr();
        boolean isBanned = ipBanService.isBanned(ip);
        if (isBanned) {
            return new AccessCheckResult(Action.DENY, null, "Your IP is banned");
        }

        // Check URL-based access rules and return "REDIRECT" if user has no permissions
        String url = request.getRequestURI();
        AccessRule rule = urlRules.get(url);
        if (rule != null) {
            return checkRule(user, rule, request);
        }

        // Checking Pattern-based access rules
        AccessCheckResult result = null;
        for (Map.Entry<Pattern, AccessRule> entry : patternRules.entrySet()) {
            Pattern pattern = entry.getKey();
            if (pattern.matcher(url).matches()) {
                rule = entry.getValue();
                result = checkRule(user, rule, request);
                if (Action.REDIRECT.equals(result.getAction())) {
                    return result;
                }
            }
        }
        if (result != null) {
            return result;
        }

        // If no rules has been found, return "NOT_FOUND"
        auditService.log(OperationTypes.ACCESS_PAGE_UNKNOWN, AuditEventTypes.PAGE_NOT_FOUND, url);
        return new AccessCheckResult(Action.NOT_FOUND, null, null);
    }

    private AccessCheckResult checkRule(UserEntity user, AccessRule rule, HttpServletRequest request) {
        Permissions permission = rule.getPermission();

        if (permission == null) {
            return createAllow();
        }

        if (Permissions.USER_LOGGED_IN.equals(permission)) {
            return userService.isAuthenticated() ? createAllow() : createRedirect(rule);
        }

        if (Permissions.USER_LOGGED_OUT.equals(permission)) {
            return userService.isAuthenticated() ? createRedirect(rule) : createAllow();
        }

        if (user.hasPermission(permission)) {
            return createAllow();
        } else {
            logger.warn("Attempt to enter '{}' by '{}' without permissions", request.getRequestURL(), user.getLogin());
            auditService.log(rule.getOperationType(), AuditEventTypes.ACCESS_DENIED, request.getQueryString());
            return createRedirect(rule);
        }
    }

    private AccessCheckResult createAllow() {
        return new AccessCheckResult(Action.ALLOW, null, null);
    }

    private AccessCheckResult createRedirect(AccessRule rule) {
        return new AccessCheckResult(Action.REDIRECT, rule.getRedirect(), "You do not have permission for this operation");
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

        // XXX URL-based access rules

        //             URL                          Redirect        Permission                          OperationType
        addRule(rules, "/",                         null,           null,                               OperationTypes.ACCESS_PAGE_MAIN);
        addRule(rules, "/files",                    "/",            Permissions.PAGES_VIEW,             OperationTypes.ACCESS_PAGE_FILES);
        addRule(rules, "/festivals",                "/",            Permissions.PAGES_VIEW,             OperationTypes.ACCESS_PAGE_FESTIVALS);
        addRule(rules, "/festivals/create",         "/festivals",   Permissions.ADMIN_FESTIVALS_ADD,    OperationTypes.CHANGE_FESTIVAL_ADD);
        addRule(rules, "/festivals/edit",           "/festivals",   Permissions.ADMIN_FESTIVALS_EDIT,   OperationTypes.CHANGE_FESTIVAL_EDIT);
        addRule(rules, "/festivals/delete",         "/festivals",   Permissions.ADMIN_FESTIVALS_DELETE, OperationTypes.CHANGE_FESTIVAL_DELETE);
        addRule(rules, "/places",                   "/",            Permissions.ADMIN_PLACES_VIEW,      OperationTypes.ACCESS_PLACE_LIST);
        addRule(rules, "/places/add",               "/places",      Permissions.ADMIN_PLACES_EDIT,      OperationTypes.ACCESS_PLACE_ADD);
        addRule(rules, "/places/edit",              "/places",      Permissions.ADMIN_PLACES_EDIT,      OperationTypes.ACCESS_PLACE_EDIT);
        addRule(rules, "/places/save",              "/places",      Permissions.ADMIN_PLACES_EDIT,      OperationTypes.CHANGE_PLACE_EDIT);
        addRule(rules, "/places/delete",            "/places",      Permissions.ADMIN_PLACES_EDIT,      OperationTypes.CHANGE_PLACE_DELETE);
        addRule(rules, "/upload",                   "/",            Permissions.UPLOAD_FILES,           OperationTypes.ACCESS_PAGE_UPLOAD);
        addRule(rules, "/register",                 "/",            Permissions.USER_LOGGED_OUT,        OperationTypes.CHANGE_USER_REGISTER);
        addRule(rules, "/login",                    "/",            Permissions.USER_LOGGED_OUT,        OperationTypes.ACCESS_USER_LOGIN);
        addRule(rules, "/logout",                   "/",            Permissions.USER_LOGGED_IN,         OperationTypes.ACCESS_USER_LOGOUT);
        addRule(rules, "/profile",                  "/",            Permissions.USER_LOGGED_IN,         OperationTypes.CHANGE_USER_PROFILE);

        addRule(rules, "/rest/files/list",          "/",            Permissions.PAGES_VIEW,             OperationTypes.ACCESS_PAGE_FILES);
        addRule(rules, "/rest/files/get",           "/",            Permissions.PAGES_VIEW,             OperationTypes.ACCESS_PAGE_FILES);
        addRule(rules, "/rest/files/upload",        "/",            Permissions.UPLOAD_FILES,           OperationTypes.CHANGE_FILE_UPLOAD);

        addRule(rules, "/admin",                    "/",            Permissions.ADMIN_PAGE,             OperationTypes.ACCESS_ADMIN_DASHBOARD);
        addRule(rules, "/admin/",                   "/",            Permissions.ADMIN_PAGE,             OperationTypes.ACCESS_ADMIN_DASHBOARD);

        addRule(rules, "/admin/users",              "/admin",       Permissions.ADMIN_USERS_VIEW,       OperationTypes.ACCESS_USER_LIST);
        addRule(rules, "/admin/users/edit",         "/admin/users", Permissions.ADMIN_USERS_VIEW,       OperationTypes.ACCESS_USER_EDIT);
        addRule(rules, "/admin/users/login",        "/admin/users", Permissions.ADMIN_USERS_EDIT,       OperationTypes.CHANGE_USER_LOGIN);
        addRule(rules, "/admin/users/password",     "/admin/users", Permissions.ADMIN_USERS_EDIT,       OperationTypes.CHANGE_USER_PASSWORD);
        addRule(rules, "/admin/users/display-name", "/admin/users", Permissions.ADMIN_USERS_EDIT,       OperationTypes.CHANGE_USER_DISPLAY_NAME);
        addRule(rules, "/admin/users/email",        "/admin/users", Permissions.ADMIN_USERS_EDIT,       OperationTypes.CHANGE_USER_EMAIL);
        addRule(rules, "/admin/users/roles",        "/admin/users", Permissions.ADMIN_USERS_ROLES,      OperationTypes.CHANGE_USER_ROLES);
        addRule(rules, "/admin/users/confirm",      "/admin/users", Permissions.ADMIN_USERS_CONFIRM,    OperationTypes.CHANGE_USER_CONFIRM);
        addRule(rules, "/admin/users/unconfirm",    "/admin/users", Permissions.ADMIN_USERS_UNCONFIRM,  OperationTypes.CHANGE_USER_UNCONFIRM);
        addRule(rules, "/admin/users/lock",         "/admin/users", Permissions.ADMIN_USERS_LOCK,       OperationTypes.CHANGE_USER_LOCK);
        addRule(rules, "/admin/users/unlock",       "/admin/users", Permissions.ADMIN_USERS_UNLOCK,     OperationTypes.CHANGE_USER_UNLOCK);

        addRule(rules, "/admin/roles",              "/admin",       Permissions.ADMIN_ROLES_VIEW,       OperationTypes.ACCESS_ROLE_LIST);
        addRule(rules, "/admin/roles/create",       "/admin/roles", Permissions.ADMIN_ROLES_EDIT,       OperationTypes.ACCESS_ROLE_CREATE);
        addRule(rules, "/admin/roles/edit",         "/admin/roles", Permissions.ADMIN_ROLES_VIEW,       OperationTypes.ACCESS_ROLE_EDIT);
        addRule(rules, "/admin/roles/save",         "/admin/roles", Permissions.ADMIN_ROLES_EDIT,       OperationTypes.CHANGE_ROLE_SAVE);
        addRule(rules, "/admin/roles/delete",       "/admin/roles", Permissions.ADMIN_ROLES_EDIT,       OperationTypes.CHANGE_ROLE_DELETE);

        addRule(rules, "/admin/ip-bans",            "/admin",       Permissions.ADMIN_IPBAN_VIEW,       OperationTypes.ACCESS_ADMIN_IPBAN);
        addRule(rules, "/admin/ip-bans/add",        "/admin",       Permissions.ADMIN_IPBAN_EDIT,       OperationTypes.CHANGE_IPBAN_ADD);
        addRule(rules, "/admin/ip-bans/remove",     "/admin",       Permissions.ADMIN_IPBAN_EDIT,       OperationTypes.CHANGE_IPBAN_REMOVE);

        addRule(rules, "/admin/audit",              "/admin",       Permissions.ADMIN_AUDIT_VIEW,       OperationTypes.ACCESS_ADMIN_AUDIT);

        addRule(rules, "/admin/logs",               "/admin",       Permissions.ADMIN_LOGS_VIEW,        OperationTypes.ACCESS_ADMIN_LOGS);
        addRule(rules, "/admin/logs/file",          "/admin",       Permissions.ADMIN_LOGS_VIEW,        OperationTypes.ACCESS_ADMIN_LOGS);

        addRule(rules, "/admin/cache",              "/admin",       Permissions.ADMIN_CACHE_VIEW,       OperationTypes.ACCESS_ADMIN_CACHE);
        addRule(rules, "/admin/cache/ip-ban",       "/admin",       Permissions.ADMIN_CACHE_REFRESH,    OperationTypes.REFRESH_ADMIN_CACHE);

        addRule(rules, "/error",                    "/",            null,                               OperationTypes.ACCESS_PAGE_ERROR);

        return rules;
    }

    private List<AccessRule> createPatternRules() {
        List<AccessRule> rules = new ArrayList<>();

        // XXX Pattern-based access rules

        //             URL            Redirect  Permission  OperationType
        addRule(rules, "/i18n/.*",    "/",      null,       OperationTypes.ACCESS_PAGE_STATIC);
        addRule(rules, "/icons/.*",   "/",      null,       OperationTypes.ACCESS_PAGE_STATIC);
        addRule(rules, "/scripts/.*", "/",      null,       OperationTypes.ACCESS_PAGE_STATIC);
        addRule(rules, "/styles/.*",  "/",      null,       OperationTypes.ACCESS_PAGE_STATIC);
        addRule(rules, "/webjars/.*", "/",      null,       OperationTypes.ACCESS_PAGE_STATIC);

        return rules;
    }

    private void addRule(List<AccessRule> rules, String url, String redirect, Permissions permission, OperationTypes operationType) {
        AccessRule rule = new AccessRule(url, redirect, permission, operationType);
        rules.add(rule);
    }

    public Map<String, AccessRule> getUrlRules() {
        return urlRules;
    }

    public Map<Pattern, AccessRule> getPatternRules() {
        return patternRules;
    }
}
