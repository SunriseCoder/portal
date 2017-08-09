package app.service;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.dao.FileRepository;
import app.entity.FolderEntity;

@Service
public class FileService {
    private static final Logger logger = LogManager.getLogger(FileService.class);

    @Autowired
    FileRepository repository;

    private FolderEntity fileList;

    @Value("${files.rescan}")
    private int rescanInterval;
    private Date lastRescan;
    private volatile boolean rescanInProgress;

    public FolderEntity getList() throws Exception {
        rescanIfNeeded();
        return fileList;
    }

    public void forceRescan() {
        if (!rescanInProgress) {
            lastRescan = null;
            rescanIfNeeded();
        }
    }

    private synchronized void rescanIfNeeded() {
        if ((fileList == null || isTimeToRescan())) {
            try {
                rescanInProgress = true;
                updateFileList();
            } finally {
                rescanInProgress = false;
            }
        }
    }

    private boolean isTimeToRescan() {
        if (lastRescan == null) {
            return true;
        }

        long sinceLastRescan = lastRescan.getTime() - System.currentTimeMillis();
        boolean needToRescan = sinceLastRescan > rescanInterval * 1000;
        return needToRescan;
    }

    private void updateFileList() {
        try {
            FolderEntity result = repository.findAll();
            fileList = result;
            lastRescan = new Date();
        } catch (Exception e) {
            logger.error("Error due to rescan filesystem", e);
        }
    }

    public void downloadFile(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        repository.downloadFile(request, response, url);
    }

    public void uploadFile(String name, MultipartFile file) throws IOException {
        repository.uploadFile(name, file);
    }

    public long getFileListSize() {
        long size = fileList == null ? 0 : fileList.getSize();
        return size;
    }

    public Date getLastUpdate() {
        return rescanInProgress ? new Date() : lastRescan;
    }
}
