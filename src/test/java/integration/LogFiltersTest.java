package integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import app.dto.LogLineDTO;
import app.enums.AuditEventTypes;
import app.enums.LogLevels;
import app.enums.OperationTypes;
import app.service.AuditService;
import app.service.admin.LogFileFinderImpl;
import app.service.admin.LogServiceImpl;

public class LogFiltersTest extends BaseTest {
    @Autowired
    private LogServiceImpl logService;

    @Autowired
    private LogFileFinderImpl logFileFinder;

    @Mock
    private AuditService auditServiceMock;

    @Before
    public void beforeTest() throws IOException {
        logService.setAuditService(auditServiceMock);
        logFileFinder.setLogsPath(this.getClass().getResource(".").getPath());
        logService.setLogFileFinder(logFileFinder);
    }

    @Test
    public void testWithoutLogFilters() throws Exception {
        List<LogLineDTO> lines = logService.readFile(createParameters("name", "filter-test-log-file.txt"));
        assertEquals(68, lines.size()); // Should be all lines from the file
    }

    @Test
    public void testFilterLogLevel() throws Exception {
        List<LogLineDTO> lines = logService.readFile(createParameters("name", "filter-test-log-file.txt", "level", "WARN"));
        for (LogLineDTO line : lines) {
            assertTrue("Log level '" + "' is lower than WARN, so it should be filtered out",
                            line.getLevel() == null || LogLevels.valueOf(line.getLevel()).ordinal() <= LogLevels.WARN.ordinal());
        }
    }

    @Test
    public void testFilterFrom() throws Exception {
        List<LogLineDTO> lines = logService.readFile(createParameters("name", "filter-test-log-file.txt", "from", "2017-07-04T18:05"));
        assertEquals("Should start with this line number", 6, lines.get(0).getLineNumber());
        assertEquals("Should ends with this line number", 68, lines.get(lines.size() - 1).getLineNumber());
    }

    @Test
    public void testFilterTo() throws Exception {
        List<LogLineDTO> lines = logService.readFile(createParameters("name", "filter-test-log-file.txt", "to", "2017-07-04T18:04"));
        assertEquals("Should start with this line number", 1, lines.get(0).getLineNumber());
        assertEquals("Should ends with this line number", 5, lines.get(lines.size() - 1).getLineNumber());
    }

    @Test(expected = Throwable.class)
    public void testWrongLogLevelFilter() throws Throwable {
        try {
            logService.readFile(createParameters("name", "filter-test-log-file.txt", "level", "BINGO"));
        } catch (Throwable t) {
            throw t;
        } finally {
            verify(auditServiceMock, times(1)).log(eq(OperationTypes.ACCESS_ADMIN_LOGS), eq(AuditEventTypes.SUSPICIOUS_ACTIVITY),
                            anyString(), anyString(), anyString());
        }
    }
}
