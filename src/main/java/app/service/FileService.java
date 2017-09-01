package app.service;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import app.entity.FolderEntity;

@Service
public interface FileService {
    FolderEntity getList() throws Exception;
    void forceRescan();
    void downloadFile(HttpServletRequest request, HttpServletResponse response, String url) throws IOException;
    long getFileListSize();
    Date getLastUpdate();
}
