package app.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.util.StringUtils;

public class CookieHelper {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String cookieKey;
    private String cookieDelimiter;
    private int maxAge;

    public CookieHelper setRequest(HttpServletRequest request) {
        this.request = request;
        return this;
    }

    public CookieHelper setResponse(HttpServletResponse response) {
        this.response = response;
        return this;
    }

    public CookieHelper setCookieKey(String cookieKey) {
        this.cookieKey = cookieKey;
        return this;
    }

    public CookieHelper setCookieDelimiter(String cookieDelimiter) {
        this.cookieDelimiter = cookieDelimiter;
        return this;
    }

    public CookieHelper setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public void addCookie(PersistentRememberMeToken token) {
        setCookie(new String[] { token.getSeries(), token.getTokenValue() });
    }

    public String[] extractCookie() {
        String cookieValue = getCookie();

        if (cookieValue == null || cookieValue.length() == 0) {
            return null;
        }

        String[] tokens = decodeCookie(cookieValue);
        return tokens;
    }

    public void deleteCookie() {
        Cookie cookie = new Cookie(cookieKey, null);
        cookie.setMaxAge(0);
        cookie.setPath(getCookiePath());
        cookie.setSecure(request.isSecure());
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private  String getCookiePath() {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }

    private String getCookie() {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            return null;
        }

        Map<String, String> cookieMap = cookiesToMap(cookies);
        String cookieValue = cookieMap.get(cookieKey);
        return cookieValue;
    }

    private void setCookie(String[] tokens) {
        String cookieValue = encodeCookie(tokens);
        Cookie cookie = new Cookie(cookieKey, cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setPath(getCookiePath());
        cookie.setSecure(request.isSecure());
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private  String encodeCookie(String[] cookieTokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cookieTokens.length; i++) {
            sb.append(cookieTokens[i]);
            if (i < cookieTokens.length - 1) {
                sb.append(cookieDelimiter);
            }
        }

        String value = sb.toString();

        sb = new StringBuilder(new String(Base64.encode(value.getBytes())));

        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    private String[] decodeCookie(String cookieValue) {
        for (int j = 0; j < cookieValue.length() % 4; j++) {
            cookieValue = cookieValue + "=";
        }

        if (!Base64.isBase64(cookieValue.getBytes())) {
            return null;
        }

        String cookieAsPlainText = new String(Base64.decode(cookieValue.getBytes()));
        String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, cookieDelimiter);

        if ((tokens[0].equalsIgnoreCase("http") || tokens[0].equalsIgnoreCase("https")) && tokens[1].startsWith("//")) {
            String[] newTokens = new String[tokens.length - 1];
            newTokens[0] = tokens[0] + ":" + tokens[1];
            System.arraycopy(tokens, 2, newTokens, 1, newTokens.length - 1);
            tokens = newTokens;
        }

        return tokens;
    }

    private Map<String, String> cookiesToMap(Cookie[] cookies) {
        Map<String, String> map = new HashMap<>();
        Arrays.stream(cookies).forEach(cookie -> {
            if (cookie != null) {
                map.put(cookie.getName(), cookie.getValue());
            }
        });
        return map;
    }
}
