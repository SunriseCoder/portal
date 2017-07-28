package app.security;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import app.entity.UserEntity;
import app.service.UserService;
import app.util.CookieHelper;

@Component
public class AutologinHandler implements Filter, LogoutHandler {
    private static final Logger logger = LogManager.getLogger(AutologinHandler.class.getName());

    private static final String COOKIE_KEY = "remember-me";
    private static final String COOKIE_DELIMITER = ":";

    @Autowired
    private UserService userService;
    @Autowired
    private DatabaseAuthenticationProvider databaseAuthProvider;

    private int seriesLength = 16;
    private int tokenLength = 16;
    private int tokenValiditySeconds = 31536000;

    private PersistentTokenRepository tokenRepository;
    private SecureRandom random;
    private CookieHelper cookieHelper;

    public AutologinHandler() {
        random = new SecureRandom();
        cookieHelper = new CookieHelper()
                        .setCookieDelimiter(COOKIE_DELIMITER)
                        .setCookieKey(COOKIE_KEY);
    }

    @Autowired
    public void setTokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        this.tokenRepository = repository;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
                    throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication authentication = authenticateFromCookies(request, response);
            if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    public Authentication authenticateFromCookies(HttpServletRequest request, HttpServletResponse response) {
        cookieHelper.setRequest(request).setResponse(response).setMaxAge(tokenValiditySeconds);

        String[] cookieTokens = cookieHelper.extractCookie();

        if (cookieTokens == null || cookieTokens.length != 2) {
            cookieHelper.deleteCookie();
            return null;
        }

        UserEntity user = processCookieTokens(cookieTokens);

        if (user == null) {
            return null;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user.getLogin(), null, new ArrayList<>());

        return authentication;
    }

    private UserEntity processCookieTokens(String[] cookieTokens) {
        if (cookieTokens.length != 2) {
            return null;
        }

        final String presentedSeries = cookieTokens[0];
        final String presentedToken = cookieTokens[1];

        PersistentRememberMeToken token = tokenRepository.getTokenForSeries(presentedSeries);

        if (token == null) {
            return null;
        }

        String login = token.getUsername();
        if (!presentedToken.equals(token.getTokenValue())) {
            tokenRepository.removeUserTokens(login);
            return null;
        }

        if (token.getDate().getTime() + tokenValiditySeconds * 1000L < System.currentTimeMillis()) {
            return null;
        }

        PersistentRememberMeToken newToken = new PersistentRememberMeToken(login, token.getSeries(), generateTokenData(), new Date());

        try {
            tokenRepository.updateToken(newToken.getSeries(), newToken.getTokenValue(), newToken.getDate());
            cookieHelper.addCookie(newToken);
        } catch (Exception e) {
            logger.error("Failed to update token: ", e);
            return null;
        }

        UserEntity user = userService.findByLogin(login);
        databaseAuthProvider.checkUserExists(user, login);
        databaseAuthProvider.checkUserNotLocked(user);
        return user;
    }

    public void storeAutologin(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        cookieHelper.setRequest(request).setResponse(response).setMaxAge(tokenValiditySeconds);

        PersistentRememberMeToken persistentToken = new PersistentRememberMeToken(authentication.getName(),
                        generateSeriesData(), generateTokenData(), new Date());
        try {
            tokenRepository.createNewToken(persistentToken);
            cookieHelper.addCookie(persistentToken);
        } catch (Exception e) {
            logger.error("Failed to save persistent token ", e);
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        cookieHelper.setRequest(request).setResponse(response).setMaxAge(tokenValiditySeconds);
        cookieHelper.deleteCookie();

        if (authentication != null) {
            tokenRepository.removeUserTokens(authentication.getName());
        }
    }

    private String generateSeriesData() {
        byte[] newSeries = new byte[seriesLength];
        random.nextBytes(newSeries);
        return new String(Base64.encode(newSeries));
    }

    private String generateTokenData() {
        byte[] newToken = new byte[tokenLength];
        random.nextBytes(newToken);
        return new String(Base64.encode(newToken));
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }
}
