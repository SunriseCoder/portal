package app.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;

public class HttpUtils {
    private static final Logger logger = LogManager.getLogger(HttpUtils.class.getName());

    public static void sendFile(HttpServletRequest request, HttpServletResponse response, File file) throws IOException {
        String fileName = file.getName();
        fillResponseHeaders(request, response, fileName);

        String rangeHeader = request.getHeader("range");
        Range range = getRequestRange(rangeHeader, file.length());
        fillContentRange(response, range);

        System.out.println(range);

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
        fileName = "filename*=utf8''" + fileName;

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
        ContentType contentType = ContentType.valueOf(fileExtension);
        if (contentType == null) {
            logger.error("Content-Type for file extension '{}' was not found", fileExtension);
            contentType = ContentType.data;
        }
        return contentType;
    }

    static Range getRequestRange(String rangeHeader, long fileLength) {
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

    public static void setResponseError(HttpServletResponse response, int status) {
        try {
            if (!response.isCommitted()) {
                response.sendError(status);
            }
        } catch (IOException e) {
            // Swallowing exception due to close response
        }
    }

    private static enum ContentType {
        // Default content type - just to be able to download file
        data("application/octet-stream", true),

        // Text files
        htm("text/html", false),
        html("text/html", false),
        txt("text/plain", false),

        // Documents
        pdf("application/pdf", false),

        // Images
        gif("image/gif", false),
        jpeg("image/jpeg", false),
        jpg("image/jpeg", false),
        png("image/png", false),

        // Audio
        m4a("audio/mp4", false),
        mp3("audio/mpeg", false),
        ogg("audio/ogg", false),
        wav("audio/wave", false),
        wave("audio/wave", false),
        wma("audio/x-ms-wma", false),

        // Video
        mp4("video/mp4", false),
        m4v("video/mp4", false),
        wmv("video/x-ms-wmv", false),

        // Archives
        zip("application/zip", true);

        private String contentType;
        private boolean attachment;

        ContentType(String contentType, boolean attachment) {
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
