package app;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import app.security.AccessCheckResult;
import app.security.SecurityChecker;
import app.service.StatisticService;
import app.service.admin.IPBanService;
import app.util.RequestUtils;
import app.util.StringUtils;

public class RequestDispatcher extends DispatcherServlet {
    private static final long serialVersionUID = 1002903203998146268L;
    private static final Logger logger = LogManager.getLogger(RequestDispatcher.class.getName());

    @Autowired
    private SecurityChecker securityChecker;

    private List<String> nonLoggablePaths;
    private Set<String> nonLoggableResources;

    public RequestDispatcher() {
        nonLoggableResources = new HashSet<>();
    }

    @Value("${statistic.non-loggable-paths}")
    public void setNonLoggablePaths(String paths) {
        nonLoggablePaths = Arrays.stream(paths.split(",")).collect(Collectors.toList());
    }

    @Autowired
    private IPBanService ipBanService;
    @Autowired
    private StatisticService statisticService;

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String ip = request.getRemoteAddr();
        boolean isBanned = ipBanService.isBanned(ip);
        if (isBanned) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your IP is banned");
            return;
        }

        if (needToLog(request)) {
            HandlerExecutionChain handler = getHandler(request);
            log(request, response, handler);
        }

        AccessCheckResult result = securityChecker.check(request);
        String message = result.getMessage();
        if (AccessCheckResult.Action.DENY.equals(result.getAction())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
            return;
        } else if (AccessCheckResult.Action.REDIRECT.equals(result.getAction())) {
            String redirectUrl = result.getRedirectUrl();
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                redirectUrl = "/";
            }

            response.sendRedirect(redirectUrl);

            return;
        } else if (AccessCheckResult.Action.NOT_FOUND.equals(result.getAction())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }

        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        try {
            super.doDispatch(request, response);
        } finally {
            updateResponse(response);
        }
    }

    private boolean needToLog(HttpServletRequest request) {
        String url = RequestUtils.getUrlWithParameters(request);
        if (nonLoggableResources.contains(url)) {
            return false;
        }

        for (String path : nonLoggablePaths) {
            if (url.toLowerCase().startsWith(path)) {
                nonLoggableResources.add(url);
                return false;
            }
        }

        return true;
    }

    private void log(HttpServletRequest request, HttpServletResponse response, HandlerExecutionChain handler) {
        String url = RequestUtils.getUrlWithParameters(request);
        try {
            statisticService.log(url);
        } catch (Exception e) {
            String message = StringUtils.format("Failed to log statistic hit for url: {0}", url);
            logger.error(message, e);
        }
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper =
            WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        responseWrapper.copyBodyToResponse();
    }
}
