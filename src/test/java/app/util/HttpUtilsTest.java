package app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static app.util.HttpUtils.HEADER_PROPERTY_PREFIX;
import static app.util.HttpUtils.HEADER_RESPONSE_STATUS;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

import app.util.HttpUtils.Properties;

public class HttpUtilsTest {
    @Test
    public void calculateSendFileContentTypeTest() {
        Map<String, String> requestHeaders = toMap();
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertEquals("audio/mpeg", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void calculateSendFileContentDispositionTest() {
        Map<String, String> requestHeaders = toMap();
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertEquals("filename*=UTF-8''One%20file.mp3",
                sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    public void calculateSendFileContentDispositionUnicodeFileNameTest() {
        Map<String, String> requestHeaders = toMap();
        Map<String, String> fileProperties = defaultFileProperties();
        fileProperties.put(Properties.FILE_NAME.getPropertyName(), "One файл.mp3");

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertEquals("filename*=UTF-8''One%20%D1%84%D0%B0%D0%B9%D0%BB.mp3",
                sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    public void calculateSendFileRangeIsNullTest() {
        Map<String, String> requestHeaders = toMap();
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertEquals("bytes", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.ACCEPT_RANGES));
        assertEquals("1000", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_LENGTH));

        assertEquals("0", sendFileParameters.get(Properties.STREAM_START.getPropertyName()));
        assertEquals("1000", sendFileParameters.get(Properties.STREAM_LENGTH.getPropertyName()));

        assertEquals("200", sendFileParameters.get(HEADER_RESPONSE_STATUS));
    }

    @Test
    public void calculateSendFileRangeIsFromZeroToEndTest() {
        Map<String, String> requestHeaders = toMap(HttpHeaders.RANGE, "bytes=0-");
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertNull(sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.ACCEPT_RANGES));
        assertEquals("1000", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_LENGTH));
        assertEquals("bytes 0-999/1000", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_RANGE));

        assertEquals("0", sendFileParameters.get(Properties.STREAM_START.getPropertyName()));
        assertEquals("1000", sendFileParameters.get(Properties.STREAM_LENGTH.getPropertyName()));

        assertEquals("206", sendFileParameters.get(HEADER_RESPONSE_STATUS));
    }

    @Test
    public void calculateSendFileRangeIsFromNonZeroToEndTest() {
        Map<String, String> requestHeaders = toMap(HttpHeaders.RANGE, "bytes=100-");
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertNull(sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.ACCEPT_RANGES));
        assertEquals("900", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_LENGTH));
        assertEquals("bytes 100-999/1000", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_RANGE));

        assertEquals("100", sendFileParameters.get(Properties.STREAM_START.getPropertyName()));
        assertEquals("900", sendFileParameters.get(Properties.STREAM_LENGTH.getPropertyName()));

        assertEquals("206", sendFileParameters.get(HEADER_RESPONSE_STATUS));
    }

    @Test
    public void calculateSendFileRangeIsFromNonZeroToNonEmptyEndTest() {
        Map<String, String> requestHeaders = toMap(HttpHeaders.RANGE, "bytes=100-1000");
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertNull(sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.ACCEPT_RANGES));
        assertEquals("900", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_LENGTH));
        assertEquals("bytes 100-999/1000", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_RANGE));

        assertEquals("100", sendFileParameters.get(Properties.STREAM_START.getPropertyName()));
        assertEquals("900", sendFileParameters.get(Properties.STREAM_LENGTH.getPropertyName()));

        assertEquals("206", sendFileParameters.get(HEADER_RESPONSE_STATUS));
    }

    @Test
    public void calculateSendFileRangeIsFromNonZeroToNonEndTest() {
        Map<String, String> requestHeaders = toMap(HttpHeaders.RANGE, "bytes=100-899");
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertNull(sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.ACCEPT_RANGES));
        assertEquals("800", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_LENGTH));
        assertEquals("bytes 100-899/1000", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_RANGE));

        assertEquals("100", sendFileParameters.get(Properties.STREAM_START.getPropertyName()));
        assertEquals("800", sendFileParameters.get(Properties.STREAM_LENGTH.getPropertyName()));

        assertEquals("206", sendFileParameters.get(HEADER_RESPONSE_STATUS));
    }

    @Test
    public void calculateSendFileRangeIsFromNonZeroToMoreThanEndTest() {
        Map<String, String> requestHeaders = toMap(HttpHeaders.RANGE, "bytes=100-2000");
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertNull(sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.ACCEPT_RANGES));
        assertEquals("900", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_LENGTH));
        assertEquals("bytes 100-999/1000", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_RANGE));

        assertEquals("100", sendFileParameters.get(Properties.STREAM_START.getPropertyName()));
        assertEquals("900", sendFileParameters.get(Properties.STREAM_LENGTH.getPropertyName()));

        assertEquals("206", sendFileParameters.get(HEADER_RESPONSE_STATUS));
    }

    @Test
    public void calculateSendFileRangeIsFromMoreThanEndTest() {
        Map<String, String> requestHeaders = toMap(HttpHeaders.RANGE, "bytes=800-200");
        Map<String, String> fileProperties = defaultFileProperties();

        Map<String, String> sendFileParameters = HttpUtils.calculateSendFile(requestHeaders, fileProperties);

        assertNull(sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.ACCEPT_RANGES));
        assertEquals("0", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_LENGTH));
        assertEquals("bytes 800-200/1000", sendFileParameters.get(HEADER_PROPERTY_PREFIX + HttpHeaders.CONTENT_RANGE));

        assertEquals("800", sendFileParameters.get(Properties.STREAM_START.getPropertyName()));
        assertEquals("0", sendFileParameters.get(Properties.STREAM_LENGTH.getPropertyName()));

        assertEquals("206", sendFileParameters.get(HEADER_RESPONSE_STATUS));
    }

    private Map<String, String> defaultFileProperties() {
        return toMap(
                Properties.FILE_NAME.getPropertyName(), "One file.mp3",
                Properties.FILE_LENGTH.getPropertyName(), String.valueOf(1000));
    }

    private Map<String, String> toMap(String... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments should be even (dividable by 2)");
        }

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i].toLowerCase(), args[i + 1]);
        }

        return map;
    }
}
