package app.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.entity.StorageFileEntity;

@Service
public interface FileStorageService {
    List<StorageFileEntity> findAllNonCompletedUploadedByCurrentUser();
    List<StorageFileEntity> findAllCompletedUploadedByCurrentUser();
    StorageFileEntity createFilePlaceHolder(HttpServletRequest request) throws Exception;
    int uploadFileChunk(MultipartFile chunk, String filePlaceHolderId) throws Exception;
    void uploadFile(StorageFileEntity placeHolder, MultipartFile file);
}
