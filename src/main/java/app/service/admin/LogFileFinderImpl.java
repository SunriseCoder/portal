package app.service.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LogFileFinderImpl implements LogFileFinder {
    private String logsPath;

    @Value("${logs.path}")
    public void setLogsPath(String logsPath) {
        this.logsPath = logsPath;
    }

    @Override
    public List<String> getLogFileList() throws IOException {
        File folder = new File(logsPath);
        checkLogFolder(folder);
        List<String> fileList = Arrays.stream(folder.list()).collect(Collectors.toList());
        return fileList;
    }

    @Override
    public BufferedReader getLogFileReader(String name) throws IOException {
        File folder = new File(logsPath);
        checkLogFolder(folder);

        File file = new File(folder, name);
        checkLogFile(file);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        return reader;
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
}
