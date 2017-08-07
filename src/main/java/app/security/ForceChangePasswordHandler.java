package app.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.entity.UserEntity;
import app.service.UserService;
import app.util.StringUtils;

@Component
public class ForceChangePasswordHandler implements Filter {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(ForceChangePasswordHandler.class.getName());

    private static final String PAGE_CHANGE_PASS = "/change-pass";
    private static final String[] PAGES_TO_IGNORE = { PAGE_CHANGE_PASS,
                    "/WEB-INF/", "/css/", "/js/", "/webjars/", "/images/", "/i18n/" };

    @Autowired
    private UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        if (!StringUtils.isStartsWidthAny(uri, PAGES_TO_IGNORE)) {
            UserEntity user = userService.getLoggedInUser();
            if (user != null) {
                boolean shouldChangePassword = user.isShouldChangePassword();
                if (shouldChangePassword) {
                    response.sendRedirect(PAGE_CHANGE_PASS);
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to do
    }
}
