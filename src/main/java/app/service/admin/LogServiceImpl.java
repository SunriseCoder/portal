package app.service.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dto.LogLineDTO;
import app.enums.AuditEventTypes;
import app.enums.LogLevels;
import app.enums.OperationTypes;
import app.service.AuditService;
import app.util.DateUtils;

@Component
public class LogServiceImpl implements LogService {
    private static final Logger logger = LogManager.getLogger(LogServiceImpl.class.getName());
    private static final Pattern LINE_PATTERN = Pattern.compile("^\\[([A-Z]*)\\]\\s([0-9\\-]*)\\s([0-9\\:\\.]*)\\s\\[([^]]*)\\]\\s(.*)$");
    private static final SimpleDateFormat DATE_FORMAT_FILTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    private static final SimpleDateFormat DATE_FORMAT_LOGLINE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private AuditService auditService;
    private LogFileFinder logFileFinder;

    @Autowired
    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    @Autowired
    public void setLogFileFinder(LogFileFinder logFileFinder) {
        this.logFileFinder = logFileFinder;
    }

    @Override
    public List<String> findAllFiles() throws IOException {
        List<String> fileList = logFileFinder.getLogFileList();
        return fileList;
    }

    @Override
    public List<LogLineDTO> readFile(Map<String, String> parameters) throws IOException {
        String name = parameters.get("name");
        List<LogLineDTO> lines = new ArrayList<>();
        try (BufferedReader reader = logFileFinder.getLogFileReader(name);) {
            String line;
            long counter = 0;
            LogLineDTO logLine = null;
            while ((line = reader.readLine()) != null) {
                logLine = parseLine(line, ++counter, logLine);
                if (isLineMatches(logLine, parameters)) {
                    lines.add(logLine);
                }
            }
        }
        return lines;
    }

    private LogLineDTO parseLine(String line, long lineNumber, LogLineDTO lastLogLine) {
        LogLineDTO logLine = new LogLineDTO();
        logLine.setLineNumber(lineNumber);

        Matcher matcher = LINE_PATTERN.matcher(line);
        if (matcher.matches() && matcher.groupCount() > 4) {
            logLine.setLevel(matcher.group(1));
            logLine.setDate(matcher.group(2));
            logLine.setTime(matcher.group(3));
            logLine.setThread(matcher.group(4));
            logLine.setContent(matcher.group(matcher.groupCount()));
        } else {
            copyFromLastLogLine(logLine, lastLogLine);
            logLine.setContent(line);
        }

        return logLine;
    }

    private void copyFromLastLogLine(LogLineDTO logLine, LogLineDTO lastLogLine) {
        if (lastLogLine != null) {
            logLine.setLevel(lastLogLine.getLevel());
            logLine.setDate(lastLogLine.getDate());
            logLine.setTime(lastLogLine.getTime());
        }
    }

    private boolean isLineMatches(LogLineDTO logLine, Map<String, String> parameters) {
        boolean matches = isLineMatchesLogLevel(logLine, parameters);
        matches &= isLineMatchesFilterFrom(logLine, parameters);
        matches &= isLineMatchesFilterTo(logLine, parameters);
        return matches;
    }

    private boolean isLineMatchesLogLevel(LogLineDTO logLine, Map<String, String> parameters) {
        String level = parameters.get("level");
        if (level == null) {
            return true;
        }

        LogLevels logLevel = null;
        try {
            logLevel = LogLevels.valueOf(level);
            if (logLevel == null) {
                logNonExistentLevel(level, "filter");
                return true;
            }
        } catch (Exception e) {
            logNonExistentLevel(level, "filter");
            return true;
        }

        if (logLine.getLevel() == null || logLine.getLevel().isEmpty()) {
            return true;
        }

        LogLevels lineLogLevel = null;
        try {
            lineLogLevel = LogLevels.valueOf(logLine.getLevel());
            if (lineLogLevel == null) {
                logNonExistentLevel(level, "log file");
                return true;
            }
        } catch (Exception e) {
            logNonExistentLevel(level, "log file");
            return true;
        }

        boolean matches = lineLogLevel.ordinal() <= logLevel.ordinal();
        return matches;
    }

    private boolean isLineMatchesFilterFrom(LogLineDTO logLine, Map<String, String> parameters) {
        String fromStr = parameters.get("from");
        if (fromStr == null || fromStr.isEmpty() || logLine.getDate() == null || logLine.getDate().isEmpty()
                        || logLine.getTime() == null || logLine.getTime().isEmpty()) {
            return true;
        }

        Date from = DateUtils.parseDateSilent(DATE_FORMAT_FILTER, fromStr);
        if (from == null) {
            logWrongDateTimeFilter("from", fromStr);
        }

        Date logDateTime = DateUtils.parseDateSilent(DATE_FORMAT_LOGLINE, logLine.getDate() + "T" + logLine.getTime());
        boolean matches = from.compareTo(logDateTime) <= 0;
        return matches;
    }

    private boolean isLineMatchesFilterTo(LogLineDTO logLine, Map<String, String> parameters) {
        String toStr = parameters.get("to");
        if (toStr == null || toStr.isEmpty() || logLine.getDate() == null || logLine.getDate().isEmpty()
                        || logLine.getTime() == null || logLine.getTime().isEmpty()) {
            return true;
        }

        Date rawTo = DateUtils.parseDateSilent(DATE_FORMAT_FILTER, toStr);
        if (rawTo == null) {
            logWrongDateTimeFilter("to", toStr);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(rawTo);
        // Increasing filter parameter "to" by 1 minute to get strict interval condition
        calendar.add(Calendar.MINUTE, 1);
        Date to = calendar.getTime();

        Date logDateTime = DateUtils.parseDateSilent(DATE_FORMAT_LOGLINE, logLine.getDate() + "T" + logLine.getTime());
        boolean matches = to.compareTo(logDateTime) > 0;
        return matches;
    }

    private void logNonExistentLevel(String level, String place) {
        String message = "Non-existing log level '" + level + "' from " + place;
        logger.warn(message);
        auditService.log(OperationTypes.ACCESS_ADMIN_LOGS, AuditEventTypes.SUSPICIOUS_ACTIVITY, level, null, message);
        throw new IllegalArgumentException(message);
    }

    private void logWrongDateTimeFilter(String filter, String value) {
        String message = "Invalid date '" + value + "' in filter '" + filter + "'";
        logger.warn(message);
        auditService.log(OperationTypes.ACCESS_ADMIN_LOGS, AuditEventTypes.SUSPICIOUS_ACTIVITY, "LogFilter[" + filter + "=" + value + "]", null, message);
        throw new IllegalArgumentException(message);
    }
}
