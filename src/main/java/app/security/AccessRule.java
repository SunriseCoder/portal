package app.security;

import app.enums.OperationTypes;
import app.enums.Permissions;

public class AccessRule {
    private String url;
    private OperationTypes operationType;
    private String redirect;
    private Permissions[] permissions;

    public AccessRule(String url, OperationTypes operationType, String redirect, Permissions... permissions) {
        super();
        this.url = url;
        this.operationType = operationType;
        this.redirect = redirect;
        this.permissions = permissions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OperationTypes getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationTypes operationType) {
        this.operationType = operationType;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public Permissions[] getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions... permissions) {
        this.permissions = permissions;
    }
}
