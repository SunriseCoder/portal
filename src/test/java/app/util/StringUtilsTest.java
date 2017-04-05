package app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void testSafeUtilsOk() {
        String s1 = "123";
        String s2 = "123";
        assertTrue(StringUtils.safeEquals(s1, s2));
    }

    @Test
    public void testSafeUtilsDifferent() {
        String s1 = "12";
        String s2 = "123";
        assertFalse(StringUtils.safeEquals(s1, s2));
    }

    @Test
    public void testSafeUtilsDiffS1IsNull() {
        String s1 = null;
        String s2 = "123";
        assertFalse(StringUtils.safeEquals(s1, s2));
    }

    @Test
    public void testSafeUtilsOkBothNull() {
        String s1 = null;
        String s2 = null;
        assertTrue(StringUtils.safeEquals(s1, s2));
    }

    @Test
    public void testSafeUtilsDiffS2IsNull() {
        String s1 = "123";
        String s2 = null;
        assertFalse(StringUtils.safeEquals(s1, s2));
    }
}
