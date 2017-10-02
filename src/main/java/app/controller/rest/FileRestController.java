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

import app.dto.FileInfoDTO;
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
        SimpleResult result = SimpleResult.UnknownError;
        try {
            StorageFileEntity placeHolder = fileStorageService.getOrCreateFilePlaceHolder(request);
            Long id = placeHolder.getId();
            Integer nextChunk = placeHolder.calclateUploadedChunksNumber();
            Map<String, String> info = toMap("placeHolderId", String.valueOf(id), "nextChunk", String.valueOf(nextChunk));
            result = SimpleResult.createOk(info);
            auditService.log(OperationTypes.CHANGE_FILE_CREATE_PLACEHOLDER, AuditEventTypes.SUCCESSFUL, null, "id = " + id);
        } catch (Exception e) {
            String message = "Error due to create file placeholder";
            logger.error(message, e);
            auditService.log(OperationTypes.CHANGE_FILE_CREATE_PLACEHOLDER, AuditEventTypes.SAVING_ERROR, null, message, e.getMessage());
        }
        return result;
    }

    @PostMapping("upload-chunk")
    public SimpleResult uploadFileChunk(@RequestParam("chunk") MultipartFile chunk, MultipartHttpServletRequest request) {
        SimpleResult result = SimpleResult.UnknownError;
        String filePlaceHolderId = request.getParameter("filePlaceHolderId");

        String auditObject = "filePlaceHolderId=" + filePlaceHolderId;
        try {
            int nextChunkId = fileStorageService.uploadFileChunk(chunk, filePlaceHolderId);
            if (nextChunkId == -1) {
                auditService.log(OperationTypes.CHANGE_FILE_UPLOAD_CHUNK, AuditEventTypes.SUCCESSFUL, null, auditObject);
            }
            return SimpleResult.createOk(nextChunkId);
        } catch (Exception e) {
            String message = "Chunk upload error";
            logger.error(message, e);
            auditService.log(OperationTypes.CHANGE_FILE_UPLOAD_CHUNK, AuditEventTypes.SAVING_ERROR, message, auditObject, e.getMessage());
        }
        return result;
    }

    @PostMapping("delete")
    public SimpleResult deleteFile(HttpServletRequest request, HttpServletResponse response) {
        SimpleResult result = SimpleResult.UnknownError;
        String idsStr = request.getParameter("ids");
        if (idsStr != null && !idsStr.isEmpty()) {
            try {
                String[] idsArr = idsStr.split(",");
                for (String idStr : idsArr) {
                    Long id = Long.valueOf(idStr);
                    fileStorageService.deletePlaceHolder(id);
                }
                auditService.log(OperationTypes.CHANGE_FILE_DELETE_PLACEHOLDER, AuditEventTypes.SUCCESSFUL, "ids=" + idsStr);
                result = SimpleResult.Ok;
            } catch (Exception e) {
                String message = "Error due to delete filePlaceHolder with ids: " + idsStr;
                logger.error(message, e);
                auditService.log(OperationTypes.CHANGE_FILE_DELETE_PLACEHOLDER, AuditEventTypes.DELETE_ERROR, "ids=" + idsStr, null, e.getMessage());
            }
        } else {
            result = processMissingParameter(logger, "ids", OperationTypes.CHANGE_FILE_DELETE_PLACEHOLDER);
        }
        return result;
    }

    @PostMapping("save-info")
    public SimpleResult saveFileInfo(HttpServletRequest request, HttpServletResponse response) {
        SimpleResult result = SimpleResult.UnknownError;
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            try {
                FileInfoDTO fileInfo = FileInfoDTO.fromRequest(request);
                fileStorageService.updateFileInfo(fileInfo);
                auditService.log(OperationTypes.CHANGE_FILE_UPDATE_INFO, AuditEventTypes.SUCCESSFUL, fileInfo.toString());
                result = SimpleResult.Ok;
            } catch (Exception e) {
                String message = "Error due to update filePlaceHolder info with id: " + idStr;
                logger.error(message, e);
                auditService.log(OperationTypes.CHANGE_FILE_UPDATE_INFO, AuditEventTypes.SAVING_ERROR, "id=" + idStr, null, e.getMessage());
            }
        } else {
            result = processMissingParameter(logger, "id", OperationTypes.CHANGE_FILE_UPDATE_INFO);
        }
        return result;
    }

    @PostMapping("publish")
    public SimpleResult publishFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SimpleResult result = SimpleResult.UnknownError;
        String idsStr = request.getParameter("ids");
        if (idsStr != null && !idsStr.isEmpty()) {
            try {
                String[] idsArr = idsStr.split(",");
                for (String idStr : idsArr) {
                    Long id = Long.valueOf(idStr);
                    fileStorageService.updateFileVisibility(id, true);
                }
                auditService.log(OperationTypes.CHANGE_FILE_PUBLISH, AuditEventTypes.SUCCESSFUL, idsStr);
                result = SimpleResult.Ok;
            } catch (SecurityException e) {
                String message = "Error due to publish filePlaceHolder with ids: " + idsStr;
                logger.error(message, e);
                auditService.log(OperationTypes.CHANGE_FILE_PUBLISH, AuditEventTypes.SUSPICIOUS_ACTIVITY, "ids=" + idsStr, null, e.getMessage());
                result = SimpleResult.createError("You don't have permissions to publish this file");
            } catch (Exception e) {
                String message = "Error due to publish filePlaceHolder with ids: " + idsStr;
                logger.error(message, e);
                auditService.log(OperationTypes.CHANGE_FILE_PUBLISH, AuditEventTypes.SAVING_ERROR, "ids=" + idsStr, null, e.getMessage());
            }
        } else {
            String message = "Error due to publish filePlaceHolder with empty 'ids'" + idsStr;
            logger.error(message);
            auditService.log(OperationTypes.CHANGE_FILE_PUBLISH, AuditEventTypes.SUSPICIOUS_ACTIVITY, "ids=" + idsStr);
            result = processMissingParameter(logger, "ids", OperationTypes.CHANGE_FILE_PUBLISH);
        }
        return result;
    }

    @PostMapping("unpublish")
    public SimpleResult unpublishFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SimpleResult result = SimpleResult.UnknownError;
        String idsStr = request.getParameter("ids");
        if (idsStr != null && !idsStr.isEmpty()) {
            try {
                String[] idsArr = idsStr.split(",");
                for (String idStr : idsArr) {
                    Long id = Long.valueOf(idStr);
                    fileStorageService.updateFileVisibility(id, false);
                }
                auditService.log(OperationTypes.CHANGE_FILE_UNPUBLISH, AuditEventTypes.SUCCESSFUL, idsStr);
                result = SimpleResult.Ok;
            } catch (SecurityException e) {
                String message = "Error due to unpublish filePlaceHolder with ids: " + idsStr;
                logger.error(message, e);
                auditService.log(OperationTypes.CHANGE_FILE_UNPUBLISH, AuditEventTypes.SUSPICIOUS_ACTIVITY, "ids=" + idsStr, null, e.getMessage());
                result = SimpleResult.createError("You don't have permissions to unpublish this file");
            } catch (Exception e) {
                String message = "Error due to unpublish filePlaceHolder with ids: " + idsStr;
                logger.error(message, e);
                auditService.log(OperationTypes.CHANGE_FILE_UNPUBLISH, AuditEventTypes.SAVING_ERROR, "ids=" + idsStr, null, e.getMessage());
            }
        } else {
            String message = "Error due to unpublish filePlaceHolder with empty 'ids'" + idsStr;
            logger.error(message);
            auditService.log(OperationTypes.CHANGE_FILE_UNPUBLISH, AuditEventTypes.SUSPICIOUS_ACTIVITY, "ids=" + idsStr);
            result = processMissingParameter(logger, "ids", OperationTypes.CHANGE_FILE_UNPUBLISH);
        }
        return result;
    }
}
