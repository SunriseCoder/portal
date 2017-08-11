package app.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testCleanFilePathEmptyString() {
        String filePath = "";
        String clean = StringUtils.cleanFilePath(filePath);
        assertEquals("", clean);
    }

    @Test
    public void testCleanFilePathValidFileName() {
        String filePath = "file.zip";
        String clean = StringUtils.cleanFilePath(filePath);
        assertEquals("file.zip", clean);
    }

    @Test
    public void testCleanFilePathValidFilePathSlash() {
        String filePath = "folder/file.zip";
        String clean = StringUtils.cleanFilePath(filePath);
        assertEquals("folder/file.zip", clean);
    }

    @Test
    public void testCleanFilePathValidFilePathBackSlash() {
        String filePath = "folder\\file.zip";
        String clean = StringUtils.cleanFilePath(filePath);
        assertEquals("folder\\file.zip", clean);
    }

    @Test
    public void testCleanFilePathInvalidFilePathSlash() {
        String filePath = "folder/../file.zip";
        String clean = StringUtils.cleanFilePath(filePath);
        assertEquals("folder/file.zip", clean);
    }

    @Test
    public void testCleanFilePathInvalidFilePathBackSlash() {
        String filePath = "folder\\..\\file.zip";
        String clean = StringUtils.cleanFilePath(filePath);
        assertEquals("folder\\file.zip", clean);
    }

    @Test
    public void testCleanFilePathStartsWithDotsSlash() {
        String filePath = "../file.zip";
        String clean = StringUtils.cleanFilePath(filePath);
        assertEquals("file.zip", clean);
    }

    @Test
    public void testCleanFilePathStartsWithDotsBackSlash() {
        String filePath = "..\\file.zip";
        String clean = StringUtils.cleanFilePath(filePath);
        assertEquals("file.zip", clean);
    }

    @Test
    public void testFormatMessageNoArguments() {
        String result = StringUtils.format("text");
        assertEquals("text", result);
    }

    @Test
    public void testFormatMessageStringArguments() {
        String result = StringUtils.format("text {0} {1}", "test", "param");
        assertEquals("text test param", result);
    }


    @Test
    public void testFormatMessageDecimalArguments() {
        String result = StringUtils.format("text {0} {1}", "5", "2.5");
        assertEquals("text 5 2.5", result);
    }

    @Test
    public void testFormatMessageBraces() {
        String result = StringUtils.format("text [name={0}, {1}]", "test", "param");
        assertEquals("text [name=test, param]", result);
    }}
