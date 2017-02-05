package app.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import app.util.HttpUtils.Range;

public class HttpUtilsTest {

    @Test
    public void testGetRequestedRangeStringIsNull() {
        String rangeHeader = null;
        long fileLength = 1000;
        Range range = HttpUtils.getRequestRange(rangeHeader, fileLength);
        assertEquals(0, range.start);
        assertEquals(1000, range.end);
        assertEquals(1000, range.length);
        assertEquals(fileLength, range.total);
    }

    @Test
    public void testGetRequestedRangeWrongString() {
        String rangeHeader = "some-junk-in-this-line";
        long fileLength = 1000;
        Range range = HttpUtils.getRequestRange(rangeHeader, fileLength);
        assertEquals(0, range.start);
        assertEquals(1000, range.end);
        assertEquals(1000, range.length);
        assertEquals(fileLength, range.total);
    }

    @Test
    public void testGetRequestedRangeZeroToEmpty() {
        String rangeHeader = "bytes=0-";
        long fileLength = 1000;
        Range range = HttpUtils.getRequestRange(rangeHeader, fileLength);
        assertEquals(0, range.start);
        assertEquals(1000, range.end);
        assertEquals(1000, range.length);
        assertEquals(fileLength, range.total);
    }

    @Test
    public void testGetRequestedRangeNonZeroToEmpty() {
        String rangeHeader = "bytes=100-";
        long fileLength = 1000;
        Range range = HttpUtils.getRequestRange(rangeHeader, fileLength);
        assertEquals(100, range.start);
        assertEquals(1000, range.end);
        assertEquals(900, range.length);
        assertEquals(fileLength, range.total);
    }

    @Test
    public void testGetRequestedRangeNonZeroToEnd() {
        String rangeHeader = "bytes=100-1000";
        long fileLength = 1000;
        Range range = HttpUtils.getRequestRange(rangeHeader, fileLength);
        assertEquals(100, range.start);
        assertEquals(1000, range.end);
        assertEquals(900, range.length);
        assertEquals(fileLength, range.total);
    }

    @Test
    public void testGetRequestedRangeNonZeroToNonEnd() {
        String rangeHeader = "bytes=100-900";
        long fileLength = 1000;
        Range range = HttpUtils.getRequestRange(rangeHeader, fileLength);
        assertEquals(100, range.start);
        assertEquals(900, range.end);
        assertEquals(800, range.length);
        assertEquals(fileLength, range.total);
    }

    @Test
    public void testGetRequestedRangeNonZeroToBeyondEnd() {
        String rangeHeader = "bytes=100-2000";
        long fileLength = 1000;
        Range range = HttpUtils.getRequestRange(rangeHeader, fileLength);
        assertEquals(100, range.start);
        assertEquals(1000, range.end);
        assertEquals(900, range.length);
        assertEquals(fileLength, range.total);
    }

    @Test
    public void testGetRequestedRangeStartLessThanEnd() {
        String rangeHeader = "bytes=800-200";
        long fileLength = 1000;
        Range range = HttpUtils.getRequestRange(rangeHeader, fileLength);
        assertEquals(800, range.start);
        assertEquals(800, range.end);
        assertEquals(0, range.length);
        assertEquals(fileLength, range.total);
    }
}
