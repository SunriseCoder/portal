package app.security;

import java.time.LocalDateTime;

import org.springframework.security.core.AuthenticationException;

public class ShouldWaitException extends AuthenticationException {
    private static final long serialVersionUID = -3930957614123848916L;

    private LocalDateTime waitUntil;

    public ShouldWaitException(String msg, LocalDateTime waitUntil) {
        super(msg);
        this.waitUntil = waitUntil;
    }

    public LocalDateTime getWaitUntil() {
        return waitUntil;
    }
}
