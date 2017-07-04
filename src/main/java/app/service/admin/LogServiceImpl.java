package app.service.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import app.dto.LogLineDTO;

@Component
public class LogServiceImpl implements LogService {
    private static final Pattern LINE_PATTERN = Pattern.compile("^\\[([A-Z]*)\\]\\s([0-9\\-]*)\\s([0-9\\:\\.]*)\\s(.*)$");

    @Value("${logs.path}")
    private String logsPath;

    @Override
    public List<String> findAllFiles() throws IOException {
        File folder = new File(logsPath);
        checkLogFolder(folder);

        List<String> fileList = Arrays.stream(folder.list()).collect(Collectors.toList());
        return fileList;
    }

    @Override
    public List<LogLineDTO> readFile(Map<String, String> parameters) throws IOException {
        File folder = new File(logsPath);
        checkLogFolder(folder);

        String name = parameters.get("name");
        File file = new File(folder, name);
        checkLogFile(file);

        List<LogLineDTO> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            long counter = 0;
            while ((line = reader.readLine()) != null) {
                LogLineDTO logLine = parseLine(line, ++counter);
                lines.add(logLine);
            }
        }
        return lines;
    }

    private void checkLogFolder(File folder) throws IOException {
        if (!folder.exists()) {
            throw new FileNotFoundException(folder.getAbsolutePath());
        }
        if (!folder.isDirectory()) {
            throw new NotDirectoryException(folder.getAbsolutePath());
        }
    }

    private void checkLogFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            throw new IOException("File '" + file.getAbsolutePath() + "' is a Directory");
        }
    }

    private LogLineDTO parseLine(String line, long lineNumber) {
        LogLineDTO logLine = new LogLineDTO();
        logLine.setLineNumber(lineNumber);

        Matcher matcher = LINE_PATTERN.matcher(line);
        if (matcher.matches() && matcher.groupCount() > 3) {
            logLine.setLevel(matcher.group(1));
            logLine.setDate(matcher.group(2));
            logLine.setTime(matcher.group(3));
            logLine.setContent(matcher.group(matcher.groupCount()));
        } else {
            logLine.setContent(line);
        }

        return logLine;
    }
}
