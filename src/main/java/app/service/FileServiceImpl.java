package app.service;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import app.dao.FileRepository;
import app.entity.FolderEntity;

@Component
public class FileServiceImpl implements FileService {
    private static final Logger logger = LogManager.getLogger(FileService.class);

    // TODO Cut off after migration process will be done
    @Autowired
    private FileRepository repository;

    private FolderEntity fileList;

    @Value("${files.rescan}")
    private int rescanInterval;
    private Date lastRescan;
    private volatile boolean rescanInProgress;

    @Override
    public FolderEntity getList() throws Exception {
        rescanIfNeeded();
        return fileList;
    }

    @Override
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

    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        repository.downloadFile(request, response, url);
    }

    @Override
    public long getFileListSize() {
        long size = fileList == null ? 0 : fileList.getSize();
        return size;
    }

    @Override
    public Date getLastUpdate() {
        return rescanInProgress ? new Date() : lastRescan;
    }
}
