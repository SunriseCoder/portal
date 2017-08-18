package app.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.entity.StorageFileEntity;

@Service
public interface FileStorageService {
    StorageFileEntity createFilePlaceHolder(HttpServletRequest request) throws IOException;
    void uploadFile(StorageFileEntity placeHolder, MultipartFile file);
}
