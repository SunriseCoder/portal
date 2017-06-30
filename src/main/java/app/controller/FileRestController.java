package app.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.entity.FolderEntity;
import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.enums.Permissions;
import app.service.AuditService;
import app.service.FileService;
import app.service.UserService;
import app.util.HttpUtils;
import app.util.LogUtils;
import app.util.StringUtils;

@RestController
@RequestMapping("/rest/files/")
public class FileRestController {
    private static final Logger logger = LogManager.getLogger(FileRestController.class.getName());

    @Autowired
    private AuditService auditService;
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;

    @RequestMapping("/list")
    public FolderEntity list(HttpServletRequest request) throws Exception {
        LogUtils.logRequest(logger, request);
        FolderEntity data = fileService.getList();
        return data;
    }

    @RequestMapping("/get/{url}")
    public void getFile(HttpServletRequest request, HttpServletResponse response, @PathVariable("url") String url) {
        try {
            String safeUrl = StringUtils.decodeDownloadPath(url);
            LogUtils.logDecodedRequest(logger, request, safeUrl);

            fileService.downloadFile(request, response, safeUrl);
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

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file,
            HttpServletRequest request, HttpServletResponse response) {

        UserEntity user = userService.getLoggedInUser();
        String name = user == null ? "" : user.getLogin();
        LogUtils.logUploadRequest(logger, request, name, file);

        if (user == null) {
            logger.error("Unauthorized user tries to upload file");
            auditService.log(OperationTypes.CHANGE_FILE_UPLOAD, AuditEventTypes.ACCESS_DENIED);
            HttpUtils.sendResponseError(response, HttpServletResponse.SC_FORBIDDEN, "You are not authorized.");
            return;
        }

        Permissions permssion = Permissions.UPLOAD_FILES;
        if (!userService.hasPermission(permssion)) {
            logger.error("User {} tries to upload file, but don't have permission {}", user.getLogin(), permssion);
            auditService.log(OperationTypes.CHANGE_FILE_UPLOAD, AuditEventTypes.ACCESS_DENIED, user.getLogin());
            HttpUtils.sendResponseError(response, HttpServletResponse.SC_FORBIDDEN,
                            "You don't have sufficient permissions to upload files.");
            return;
        }

        String auditObject = MessageFormat.format("File[name={},size={}]", file.getName(), file.getSize());
        try {
            fileService.uploadFile(name, file);
            auditService.log(OperationTypes.CHANGE_FILE_UPLOAD, AuditEventTypes.SUCCESSFUL, null, auditObject);
        } catch (IOException e) {
            logger.error(e);
            auditService.log(OperationTypes.CHANGE_FILE_UPLOAD, AuditEventTypes.SAVING_ERROR, null, auditObject, e.getMessage());
            HttpUtils.sendResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
