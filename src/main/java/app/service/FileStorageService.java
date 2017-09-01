package app.service;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.entity.StorageFileEntity;

@Service
public interface FileStorageService {
    List<StorageFileEntity> findAllNonCompletedUploadedByCurrentUser();
    List<StorageFileEntity> findAllCompletedUploadedByCurrentUser();
    StorageFileEntity getOrCreateFilePlaceHolder(HttpServletRequest request) throws Exception;
    int uploadFileChunk(MultipartFile chunk, String filePlaceHolderId) throws Exception;
    void uploadFile(StorageFileEntity placeHolder, MultipartFile file);
    void deletePlaceHolder(Long id) throws Exception;
    long getFreeSpace() throws IOException;
}
