package app.security;

import app.enums.OperationTypes;
import app.enums.Permissions;

public class AccessRule {
    private String url;
    private String redirect;
    private OperationTypes operationType;
    private Permissions permission;

    public AccessRule(String url, String redirect, Permissions permission, OperationTypes operationType) {
        this.url = url;
        this.redirect = redirect;
        this.operationType = operationType;
        this.permission = permission;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public OperationTypes getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationTypes operationType) {
        this.operationType = operationType;
    }

    public Permissions getPermission() {
        return permission;
    }

    public void setPermission(Permissions permission) {
        this.permission = permission;
    }
}
