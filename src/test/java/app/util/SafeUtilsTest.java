package app.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SafeUtilsTest {

    // Longs
    @Test
    public void testSafeEqualsLongsOk() {
        Long l1 = 123L;
        Long l2 = 123L;
        assertTrue(SafeUtils.safeEquals(l1, l2));
    }

    @Test
    public void testSafeEqualsLongsDifferent() {
        Long l1 = 12L;
        Long l2 = 123L;
        assertFalse(SafeUtils.safeEquals(l1, l2));
    }

    @Test
    public void testSafeEqualsLongsDiffS1IsNull() {
        Long l1 = null;
        Long l2 = 123L;
        assertFalse(SafeUtils.safeEquals(l1, l2));
    }

    @Test
    public void testSafeEqualsLongsOkBothNull() {
        Long l1 = null;
        Long l2 = null;
        assertTrue(SafeUtils.safeEquals(l1, l2));
    }

    @Test
    public void testSafeEqualsLongsDiffS2IsNull() {
        Long l1 = 123L;
        Long l2 = null;
        assertFalse(SafeUtils.safeEquals(l1, l2));
    }

    // Strings
    
    @Test
    public void testSafeEqualsStringsOk() {
        String s1 = "123";
        String s2 = "123";
        assertTrue(SafeUtils.safeEquals(s1, s2));
    }

    @Test
    public void testSafeEqualsStringsDifferent() {
        String s1 = "12";
        String s2 = "123";
        assertFalse(SafeUtils.safeEquals(s1, s2));
    }

    @Test
    public void testSafeEqualsStringsDiffS1IsNull() {
        String s1 = null;
        String s2 = "123";
        assertFalse(SafeUtils.safeEquals(s1, s2));
    }

    @Test
    public void testSafeEqualsStringsOkBothNull() {
        String s1 = null;
        String s2 = null;
        assertTrue(SafeUtils.safeEquals(s1, s2));
    }

    @Test
    public void testSafeEqualsStringsDiffS2IsNull() {
        String s1 = "123";
        String s2 = null;
        assertFalse(SafeUtils.safeEquals(s1, s2));
    }
}
