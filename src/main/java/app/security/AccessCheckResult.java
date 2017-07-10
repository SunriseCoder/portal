package app.security;

public class AccessCheckResult {
    private Action action;
    private String redirectUrl;
    private String message;

    public AccessCheckResult(Action action, String redirectUrl, String message) {
        this.action = action;
        this.redirectUrl = redirectUrl;
        this.message = message;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum Action {
        ALLOW, DENY, NOT_FOUND, REDIRECT
    }
}
