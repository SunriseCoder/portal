package app.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;

public class HttpUtils {
    public static final String HEADER_PROPERTY_PREFIX = "header.";
    public static final String HEADER_RESPONSE_STATUS = "response.status";
    private static final Logger logger = LogManager.getLogger(HttpUtils.class.getName());

    private static Map<String, ContentType> contentTypes;

    public static void sendFile(HttpServletRequest request, HttpServletResponse response, File file) throws IOException {
        Map<String, String> requestHeaders = getRequestHeaders(request);
        Map<String, String> fileProperties = getFileProperties(file);

        Map<String, String> sendFileParameters = calculateSendFile(requestHeaders, fileProperties);

        setResponseHeaders(response, sendFileParameters);
        response.setStatus(Integer.parseInt(sendFileParameters.get(HEADER_RESPONSE_STATUS)));

        long start = Long.parseLong(sendFileParameters.get(Properties.STREAM_START.getPropertyName()));
        long length = Long.parseLong(sendFileParameters.get(Properties.STREAM_LENGTH.getPropertyName()));
        FileUtils.writeFile(response, file, start, length);

        response.flushBuffer();
    }

    public static void sendFolderAsZip(HttpServletRequest request, HttpServletResponse response, File file) throws IOException {
        try {
            String fileName = file.getName() + ".zip";

            Map<String, String> requestHeaders = getRequestHeaders(request);
            Map<String, String> fileProperties = new HashMap<>();
            fileProperties.put(Properties.FILE_NAME.getPropertyName(), fileName);

            Map<String, String> sendFileParameters = calculateSendFolderAsZip(requestHeaders, fileProperties);
            setResponseHeaders(response, sendFileParameters);

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

    private static Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(header -> header.toString().toLowerCase(), header -> request.getHeader(header)));
        return headersMap;
    }

    private static Map<String, String> getFileProperties(File file) {
        Map<String, String> properties = new HashMap<>();
        properties.put(Properties.FILE_NAME.getPropertyName(), file.getName());
        properties.put(Properties.FILE_LENGTH.getPropertyName(), String.valueOf(file.length()));
        return properties;
    }

    static Map<String, String> calculateSendFile(Map<String, String> requestHeaders, Map<String, String> fileProperties) {
        Map<String, String> sendFileParameters = new HashMap<>();

        Long fileLength = Long.parseLong(fileProperties.get(Properties.FILE_LENGTH.getPropertyName()));
        Long contentLength = 0L;
        Long streamStart = 0L;
        Long streamLength = fileLength;
        String rangeHeader = requestHeaders.get(HttpHeaders.RANGE.toLowerCase());
        if (rangeHeader != null) {
            sendFileParameters.put(HEADER_RESPONSE_STATUS, String.valueOf(HttpServletResponse.SC_PARTIAL_CONTENT));

            String[] range = rangeHeader.split("=")[1].split("-");
            streamStart = Long.valueOf(range[0]);
            Long end = Long.valueOf(range.length > 1 ? range[1] : fileProperties.get(Properties.FILE_LENGTH.getPropertyName()));
            end = Math.min(end, fileLength - 1);

            contentLength = Math.max(end - streamStart + 1, 0);
            streamLength = contentLength;

            String contentRange = "bytes " + streamStart + "-" + end + "/" + fileLength;
            sendFileParameters.put(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_RANGE, contentRange);
        } else {
            sendFileParameters.put(HEADER_RESPONSE_STATUS, String.valueOf(HttpServletResponse.SC_OK));

            sendFileParameters.put(HEADER_PROPERTY_PREFIX + HttpHeaders.ACCEPT_RANGES, "bytes");
            contentLength = fileLength;
        }
        sendFileParameters.put(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));

        sendFileParameters.put(Properties.STREAM_START.getPropertyName(), String.valueOf(streamStart));
        sendFileParameters.put(Properties.STREAM_LENGTH.getPropertyName(), String.valueOf(streamLength));

        setContentHeaders(requestHeaders, fileProperties, sendFileParameters);

        return sendFileParameters;
    }

    static Map<String, String> calculateSendFolderAsZip(Map<String, String> requestHeaders, Map<String, String> fileProperties) {
        Map<String, String> sendFileParameters = new HashMap<>();
        setContentHeaders(requestHeaders, fileProperties, sendFileParameters);
        return sendFileParameters;
    }

    private static void setContentHeaders(Map<String, String> requestHeaders, Map<String, String> fileProperties,
            Map<String, String> sendFileParameters) {

        String fileName = fileProperties.get(Properties.FILE_NAME.getPropertyName());

        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }

        // URL Encoder replaces whitespaces with pluses,
        // therefore filename by saving contains pluses instead of whitespaces
        fileName = fileName.replaceAll("\\+", "%20");
        fileName = "filename*=UTF-8''" + fileName;

        ContentType contentType = getContentType(fileName);
        sendFileParameters.put(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_TYPE, contentType.getContentType());

        // Use "attachment; filename=..." to download instead of playing file
        String contentDisposition = contentType.isAttachment() ? "attachment; " + fileName : fileName;
        sendFileParameters.put(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    }

    private static void setResponseHeaders(HttpServletResponse response, Map<String, String> sendFileParameters) {
        sendFileParameters.keySet().stream()
                .filter(key -> key.startsWith(HEADER_PROPERTY_PREFIX))
                .forEach(key -> response.setHeader(
                        key.substring(HEADER_PROPERTY_PREFIX.length()), sendFileParameters.get(key)));
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

    public static enum Properties {
        FILE_NAME("file.name"), FILE_LENGTH("file.length"),
        STREAM_START("stream.start"), STREAM_LENGTH("stream.length");

        private String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }
}
