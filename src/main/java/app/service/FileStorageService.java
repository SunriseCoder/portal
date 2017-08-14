package app.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.entity.StorageFileEntity;

@Service
public interface FileStorageService {
    StorageFileEntity createFilePlaceHolder(String name, long size) throws IOException;
    void uploadFile(StorageFileEntity placeHolder, MultipartFile file);
}
