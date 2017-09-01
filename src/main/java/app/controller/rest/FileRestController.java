package app.controller.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import app.controller.rest.BaseRestController.SimpleResult.Status;
import app.entity.FolderEntity;
import app.entity.StorageFileEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.AuditService;
import app.service.FileService;
import app.service.FileStorageService;
import app.util.HttpUtils;
import app.util.LogUtils;
import app.util.StringUtils;

@RestController
@RequestMapping("/rest/files/")
public class FileRestController extends BaseRestController {
    private static final Logger logger = LogManager.getLogger(FileRestController.class.getName());

    @Autowired
    private AuditService auditService;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileStorageService fileStorageService;

    @RequestMapping("/list")
    public FolderEntity list(HttpServletRequest request) throws Exception {
        LogUtils.logRequest(logger, request);
        FolderEntity data = fileService.getList();
        return data;
    }

    @RequestMapping("get")
    public void getFile(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id) {
        try {
            String safeUrl = StringUtils.decodeDownloadPath(id);
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

    @PostMapping("create")
    public SimpleResult createFiles(HttpServletRequest request, HttpServletResponse response) {
        try {
            StorageFileEntity placeHolder = fileStorageService.getOrCreateFilePlaceHolder(request);
            Long id = placeHolder.getId();
            Integer nextChunk = placeHolder.calclateUploadedChunksNumber();
            Map<String, String> info = toMap("placeHolderId", String.valueOf(id), "nextChunk", String.valueOf(nextChunk));
            SimpleResult result = new SimpleResult(Status.Ok, info);
            auditService.log(OperationTypes.CHANGE_FILE_CREATE_PLACEHOLDER, AuditEventTypes.SUCCESSFUL, null, "id = " + id);
            return result;
        } catch (Exception e) {
            String message = "Error due to create file placeholder";
            logger.error(message, e);
            auditService.log(OperationTypes.CHANGE_FILE_CREATE_PLACEHOLDER, AuditEventTypes.SAVING_ERROR, null, message, e.getMessage());
            return new SimpleResult(Status.Error, message);
        }
    }

    @PostMapping("upload-chunk")
    public SimpleResult uploadFileChunk(@RequestParam("chunk") MultipartFile chunk, MultipartHttpServletRequest request) {
        String filePlaceHolderId = request.getParameter("filePlaceHolderId");

        String auditObject = "filePlaceHolderId=" + filePlaceHolderId;
        try {
            int nextChunkId = fileStorageService.uploadFileChunk(chunk, filePlaceHolderId);
            if (nextChunkId == -1) {
                auditService.log(OperationTypes.CHANGE_FILE_UPLOAD_CHUNK, AuditEventTypes.SUCCESSFUL, null, auditObject);
            }
            return new SimpleResult(Status.Ok, nextChunkId);
        } catch (Exception e) {
            String message = "Chunk upload error";
            logger.error(message, e);
            auditService.log(OperationTypes.CHANGE_FILE_UPLOAD_CHUNK, AuditEventTypes.SAVING_ERROR, message, auditObject, e.getMessage());
            return new SimpleResult(Status.Error, message);
        }
    }

    @PostMapping("delete")
    public void deleteFile(HttpServletRequest request, HttpServletResponse response) {
        String idsStr = request.getParameter("ids");
        try {
            if (idsStr != null && !idsStr.isEmpty()) {
                String[] idsArr = idsStr.split(",");
                for (String idStr : idsArr) {
                    Long id = Long.valueOf(idStr);
                    fileStorageService.deletePlaceHolder(id);
                }
                auditService.log(OperationTypes.CHANGE_FILE_DELETE_PLACEHOLDER, AuditEventTypes.SUCCESSFUL, "ids=" + idsStr);
            }
        } catch (Exception e) {
            String message = "Error due to delete filePlaceHolder with ids: " + idsStr;
            logger.error(message, e);
            auditService.log(OperationTypes.CHANGE_FILE_DELETE_PLACEHOLDER, AuditEventTypes.DELETE_ERROR, "ids=" + idsStr, null, e.getMessage());
        }
    }
}
