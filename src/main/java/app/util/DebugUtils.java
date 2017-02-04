package app.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

public class DebugUtils {

	public static void dumpRequestHeaders(HttpServletRequest request) {
		System.out.println("--- request start ---");
		Enumeration<String> keys = request.getHeaderNames();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = request.getHeader(key);
			System.out.println(key + ":=" + value);
		}
		System.out.println("--- request end ---");
	}
}
