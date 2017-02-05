package app.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.dao.FileRepository;
import app.entity.FolderEntity;

@Service
public class FileService {
    @Autowired
    FileRepository repository;

    public FolderEntity getList() throws Exception {
        FolderEntity result = repository.findAll();
        return result;
    }

    public void downloadFile(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        repository.downloadFile(request, response, url);
    }

    public void uploadFile(String name, MultipartFile file) {
        repository.uploadFile(name, file);
    }
}
