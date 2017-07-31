package app.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityServiceImpl implements SecurityService {
    private static final Logger logger = LogManager.getLogger(SecurityServiceImpl.class);

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public List<String> getIps() {
        List<String> ips = new ArrayList<>();
        addIp(ips, request.getHeader("X-FORWARDED-FOR"));
        addIp(ips, request.getRemoteAddr());
        return ips;
    }

    private void addIp(List<String> ips, String ip) {
        if (ip != null && !"".equals(ip) && !ips.contains(ip)) {
            ips.add(ip);
        }
    }

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
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                login, pass, Collections.emptyList());

        authenticationManager.authenticate(token);

        if (token.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(token);
            logger.info(String.format("Autologin as '%s' successful.", login));
        }
    }
}
