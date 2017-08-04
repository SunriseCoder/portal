package app.security;

import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.AuditService;
import app.service.UserService;
import app.service.admin.IPBanService;

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider, UserDetailsService {
    private static final Logger logger = LogManager.getLogger(DatabaseAuthenticationProvider.class.getName());

    @Autowired
    private AuditService auditService;
    @Autowired
    private UserService userService;
    @Autowired
    private AutologinHandler autologinFilter;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @Autowired
    private BruteForceDetector bruteForceDetector;
    @Autowired
    private IPBanService ipBanService;

    private BCryptPasswordEncoder passwordEncoder;

    public DatabaseAuthenticationProvider() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        boolean isBanned = ipBanService.isIPBanned();
        if (isBanned) {
            return null;
        }

        String login = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            bruteForceDetector.checkShouldWait();

            UsernamePasswordAuthenticationToken token = checkUser(login, password);

            autologinFilter.storeAutologin(request, response, token);

            return token;
        } catch (ShouldWaitException e) {
            logger.warn("BruteForce attempt");
            bruteForceDetector.logBruteAttempt();
            throw e;
        } catch (Exception e) {
            logger.error(e);
            bruteForceDetector.logFail();
            throw e;
        }
    }

    private UsernamePasswordAuthenticationToken checkUser(String login, String password) throws AuthenticationException {
        UserEntity user = userService.findByLogin(login);

        checkUserExists(user, login);
        checkUserNotLocked(user);
        checkUserPassword(user, password);

        return new UsernamePasswordAuthenticationToken(login, password, new ArrayList<>());
    }

    public void checkUserExists(UserEntity user, String login) throws UsernameNotFoundException {
        if (user == null) {
            String message = "User not found: " + login;
            auditService.log(OperationTypes.ACCESS_USER_LOGIN, AuditEventTypes.ACCESS_DENIED, login, null, message);
            throw new UsernameNotFoundException(message);
        }
    }

    public void checkUserNotLocked(UserEntity user) throws LockedException {
        if (user.isLocked()) {
            String message = "User is locked: " + user.getLogin();
            auditService.log(OperationTypes.ACCESS_USER_LOGIN, AuditEventTypes.ACCESS_DENIED, user.getLogin(), null, message);
            throw new LockedException(message);
        }
    }

    public void checkUserPassword(UserEntity user, String password) throws BadCredentialsException {
        if (!passwordEncoder.matches(password, user.getPass())) {
            String message = "Wrong password for user: " + user.getLogin();
            auditService.log(OperationTypes.ACCESS_USER_LOGIN, AuditEventTypes.ACCESS_DENIED, user.getLogin(), null, message);
            throw new BadCredentialsException(message);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userService.findByLogin(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        UserDetails details = new User(user.getLogin(), user.getPass(), Collections.emptyList());
        return details;
    }
}
