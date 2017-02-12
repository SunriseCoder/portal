package app.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.entity.FolderEntity;
import app.service.FileService;
import app.util.HttpUtils;
import app.util.LogUtils;
import app.util.StringUtils;

@RestController
@RequestMapping("/rest/files/")
public class FileRestController {
    private static final Logger logger = LogManager.getLogger(FileRestController.class.getName());

    @Autowired
    FileService service;

    @RequestMapping("/list")
    public FolderEntity list(HttpServletRequest request) throws Exception {
        LogUtils.logRequest(logger, request);
        FolderEntity data = service.getList();
        return data;
    }

    @RequestMapping("/get/{url}")
    public void getFile(HttpServletRequest request, HttpServletResponse response, @PathVariable("url") String url) {
        try {
            String safeUrl = StringUtils.decodeDownloadPath(url);
            LogUtils.logDecodedRequest(logger, request, safeUrl);

            service.downloadFile(request, response, safeUrl);
        } catch (ClientAbortException e) {
            // Do not need to log this junk
        } catch (FileNotFoundException | NotDirectoryException e) {
            logger.error(e);
            HttpUtils.sendResponseError(response, HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {
            logger.error(e);
            HttpUtils.sendResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping("/upload")
    public void uploadFile(@RequestParam("name") String name, @RequestParam("file") MultipartFile file,
            HttpServletRequest request, HttpServletResponse response) {
        LogUtils.logUploadRequest(logger, request, name, file);

        if (name.isEmpty()) {
            logger.error("Name is empty");
            HttpUtils.sendResponseError(response, HttpServletResponse.SC_BAD_REQUEST, "Name is empty");
            return;
        }

        try {
            service.uploadFile(name, file);
        } catch (IOException e) {
            logger.error(e);
            HttpUtils.sendResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
