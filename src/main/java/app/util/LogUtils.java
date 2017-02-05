package app.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;

public class LogUtils {

    public static void logRequest(Logger logger, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String method = request.getMethod();
        String path = request.getServletPath();
        logger.info("From IP {} {} {}", ipAddress, method, path);
    }

    public static void logDecodedRequest(Logger logger, HttpServletRequest request, String safeUrl) {
        String ipAddress = request.getRemoteAddr();
        String method = request.getMethod();
        String path = request.getServletPath();

        String rangeHeaderValue = request.getHeader("range");
        if (rangeHeaderValue == null) {
            logger.info("From IP {} {} {} ({})", ipAddress, method, path, safeUrl);
        } else {
            // Parsing string like "bytes=100-" or "bytes=100-200"
            Matcher matcher = Pattern.compile("^bytes=(\\d+)-(\\d*)$").matcher(rangeHeaderValue);
            if (matcher.matches()) {
                logger.info("From IP {} {} {} ({}) from '{}' to '{}'",
                        ipAddress, method, path, safeUrl, matcher.group(1), matcher.group(2));
            }
        }
    }
}
