package app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;

public class StringUtils {
    private static final Logger logger = LogManager.getLogger(StringUtils.class.getName());

    public static String decodeDownloadPath(String url) {
        // De-escaping slashes due to URL security
        url = url.replaceAll("_", "/");

        // Decode URL from Base64 to String
        byte[] decodedArray = Base64.decodeBase64(url);
        String decodedUrl = new String(decodedArray);
        try {
            decodedUrl = URLDecoder.decode(decodedUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Actually should never happens, as long as "UTF-8" supports, but
            logger.error(e);
        }

        String safeUrl = cleanFilePath(decodedUrl);
        return safeUrl;
    }

    public static String cleanFilePath(String filePath) {
        // Deleting double dots, followed by slashes
        String safeUrl = filePath.replaceAll("\\.\\.(/|\\\\)", "");
        return safeUrl;
    }

    /**
     * Format message by passing arguments in placeholders
     *
     * @param pattern Like "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}"
     * @param arguments objects to be injected
     * @return formatted string
     */
    public static String format(String pattern, Object... arguments) {
        return MessageFormat.format(pattern, arguments);
    }

    public static boolean isStartsWidthAny(String candidate, String[] strings) {
        if (strings == null || strings.length == 0 || candidate == null) {
            return false;
        }

        for (String string : strings) {
            if (string == null) {
                continue;
            }
            if (candidate.startsWith(string)) {
                return true;
            }
        }

        return false;
    }
}
