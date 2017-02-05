package app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
}
