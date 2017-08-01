package app.security;

import org.springframework.security.core.AuthenticationException;

public class RequestLimitException extends AuthenticationException {
    private static final long serialVersionUID = -4624633721519141669L;

    public RequestLimitException(String msg) {
        super(msg);
    }
}
