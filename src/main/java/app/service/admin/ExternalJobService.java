package app.service.admin;

import org.springframework.stereotype.Service;

import app.dto.JobInfoDTO;

@Service
public interface ExternalJobService {
    void start(String command) throws Exception;
    JobInfoDTO getСurrentJobInfo();
    boolean killCurrentJob();
}
