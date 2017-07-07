package app.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static String getUrlWithParameters(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isEmpty()) {
            queryString = "";
        } else {
            queryString = "?" + queryString;
        }
        String url = request.getRequestURI() + queryString;
        return url;
    }
}
