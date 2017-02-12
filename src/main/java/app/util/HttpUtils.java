package app.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;

public class HttpUtils {
    private static final Logger logger = LogManager.getLogger(HttpUtils.class.getName());

    private static Map<String, ContentType> contentTypes;

    public static void sendFile(HttpServletRequest request, HttpServletResponse response, File file) throws IOException {
        String fileName = file.getName();
        fillResponseHeaders(request, response, fileName);

        String rangeHeaderValue = request.getHeader("range");
        Range range = prepareResponseRange(rangeHeaderValue, file.length());
        fillContentRange(response, range);

        FileUtils.writeFile(response, file, range.start, range.length);

        response.flushBuffer();
    }

    public static void sendFolderAsZip(HttpServletRequest request, HttpServletResponse response, File file) throws IOException {
        try {
            String fileName = file.getName() + ".zip";
            fillResponseHeaders(request, response, fileName);

            OutputStream outputStream = response.getOutputStream();
            ZipUtils.createAndWriteZipArchive(file, outputStream);
        } catch (IOException e) {
            logger.error("Error due to create and send ZIP-file",  e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error due to create and send ZIP-file");
            throw e;
        } finally {
            response.flushBuffer();
        }
    }

    private static void fillResponseHeaders(HttpServletRequest request, HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        // URL Encoder replaces whitespaces with pluses,
        // therefore filename by saving contains pluses instead of whitespaces
        fileName = fileName.replaceAll("\\+", "%20");
        fileName = "filename*=UTF-8''" + fileName;

        ContentType contentType = getContentType(fileName);
        response.setContentType(contentType.getContentType());

        // Use "attachment; filename=..." to download instead of playing file
        String contentDisposition = contentType.isAttachment() ? "attachment; " + fileName : fileName;
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    }

    private static void fillContentRange(HttpServletResponse response, Range range) {
        response.setContentLengthLong(range.length);
        String contentRange = range.start + "-" + range.end + "/" + range.total;
        response.setHeader(HttpHeaders.CONTENT_RANGE, contentRange);
    }

    private static ContentType getContentType(String fileName) {
        // Extracting File Extension
        int lastDotPosition = fileName.lastIndexOf(".");
        String fileExtension;
        if (lastDotPosition != -1) {
            fileExtension = fileName.substring(lastDotPosition + 1);
        } else {
            fileExtension = fileName;
        }
        fileExtension = fileExtension.toLowerCase();

        // Retrieving Content Type by File Extension
        if (contentTypes == null) {
            fillContentTypes();
        }
        ContentType contentType = contentTypes.get(fileExtension);
        if (contentType == null) {
            logger.error("Content-Type for file extension '{}' was not found", fileExtension);
            contentType = contentTypes.get("data");
        }
        return contentType;
    }

    static Range prepareResponseRange(String rangeHeader, long fileLength) {
        Range range = new Range();
        if (rangeHeader == null) {
            range.start = 0;
            range.end = fileLength;
        } else {
            long parsedEnd = Long.MAX_VALUE;
            // Parsing string like "bytes=100-" or "bytes=100-200"
            Matcher matcher = Pattern.compile("^bytes=(\\d+)-(\\d*)$").matcher(rangeHeader);
            if (matcher.matches()) {
                range.start = Long.parseLong(matcher.group(1));
                if (!matcher.group(2).isEmpty()) {
                    parsedEnd = Long.parseLong(matcher.group(2));
                }
            } else {
                range.start = 0;
            }
            range.end = Math.max(range.start, parsedEnd);
            range.end = Math.min(range.end, fileLength);
        }
        range.calculateLength();
        range.total = fileLength;
        return range;
    }

    public static void sendResponseError(HttpServletResponse response, int status) {
        try {
            if (!response.isCommitted()) {
                response.sendError(status);
            }
        } catch (IOException e) {
            // Swallowing exception due to close response
        }
    }

    public static void sendResponseError(HttpServletResponse response, int status, String message) {
        try {
            if (!response.isCommitted()) {
                response.sendError(status, message);
            }
        } catch (IOException e) {
            // Swallowing exception due to close response
        }
    }

    private static void fillContentTypes() {
        contentTypes = new HashMap<>();

        // Default content type - just to be able to download file
        contentTypes.put("data", new ContentType("application/octet-stream", true));

        // Text files
        contentTypes.put("htm", new ContentType("text/html", false));
        contentTypes.put("html", new ContentType("text/html", false));
        contentTypes.put("txt", new ContentType("text/plain", false));

        // Documents
        contentTypes.put("pdf", new ContentType("application/pdf", false));

        // Images
        contentTypes.put("gif", new ContentType("image/gif", false));
        contentTypes.put("jpeg", new ContentType("image/jpeg", false));
        contentTypes.put("jpg", new ContentType("image/jpeg", false));
        contentTypes.put("png", new ContentType("image/png", false));

        // Audio
        contentTypes.put("m4a", new ContentType("audio/mp4", false));
        contentTypes.put("mp3", new ContentType("audio/mpeg", false));
        contentTypes.put("ogg", new ContentType("audio/ogg", false));
        contentTypes.put("wav", new ContentType("audio/wave", false));
        contentTypes.put("wave", new ContentType("audio/wave", false));
        contentTypes.put("wma", new ContentType("audio/x-ms-wma", false));

        // Video
        contentTypes.put("3gp", new ContentType("video/3gp", false));
        contentTypes.put("mp4", new ContentType("video/mp4", false));
        contentTypes.put("m4v", new ContentType("video/mp4", false));
        contentTypes.put("wmv", new ContentType("video/x-ms-wmv", false));

        // Archives
        contentTypes.put("zip", new ContentType("application/zip", true));
    }

    private static class ContentType {
        private String contentType;
        private boolean attachment;

        private ContentType(String contentType, boolean attachment) {
            this.contentType = contentType;
            this.attachment = attachment;
        }

        public String getContentType() {
            return contentType;
        }

        public boolean isAttachment() {
            return attachment;
        }
    }

    static class Range {
        long start;
        long end;
        long length;
        long total;

        public void calculateLength() {
            length = end > start ? end - start : 0;
        }

        @Override
        public String toString() {
            return "Range: {start: " + start + ", end: " + end + ", length: " + length + ", total: " + total + "}";
        }
    }
}
