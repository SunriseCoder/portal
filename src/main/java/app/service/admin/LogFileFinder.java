package app.service.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface LogFileFinder {
    List<String> getLogFileList() throws IOException;
    BufferedReader getLogFileReader(String name) throws IOException;
}
