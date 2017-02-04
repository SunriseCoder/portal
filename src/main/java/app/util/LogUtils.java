package app.util;

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
		logger.info("From IP {} {} {} ({})", ipAddress, method, path, safeUrl);
	}
}
