package app.service.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import app.dto.LogLineDTO;

@Service
public interface LogService {
    List<String> findAllFiles() throws IOException;
    List<LogLineDTO> readFile(Map<String, String> parameters) throws IOException;
}
