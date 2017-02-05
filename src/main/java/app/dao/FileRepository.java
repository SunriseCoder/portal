package app.dao;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import app.entity.FolderEntity;

@Repository
public interface FileRepository {
    FolderEntity findAll() throws Exception;
    void downloadFile(HttpServletRequest request, HttpServletResponse response, String url) throws IOException;
    void uploadFile(String name, MultipartFile file) throws IOException;
}
