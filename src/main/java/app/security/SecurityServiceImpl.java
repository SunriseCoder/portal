package app.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class SecurityServiceImpl implements SecurityService {
    private static final Logger logger = LogManager.getLogger(SecurityServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public String getLoggedInUsername() {
        String username = null;
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) details;
            username = userDetails.getUsername();
        }
        return username;
    }

    @Override
    public void autologin(String login, String pass) {
        UserDetails details = userDetailsService.loadUserByUsername(login);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                details, pass, details.getAuthorities());

        authenticationManager.authenticate(token);

        if (token.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(token);
            logger.info(String.format("Autologin as '%s' successful.", login));
        }
    }
}
