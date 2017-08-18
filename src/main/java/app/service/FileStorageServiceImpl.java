package app.service;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import app.dao.StorageFileRepository;
import app.entity.StorageFileEntity;
import app.entity.UserEntity;
import app.properties.FileStorageProperties;
import app.structures.FileTree;
import app.structures.FileTree.FileNode;
import app.util.CheckSumUtils;
import app.util.FileUtils;
import app.util.NumberUtils;
import app.util.StringUtils;

@Component
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private FileStorageProperties properties;

    @Autowired
    private UserService userService;

    @Autowired
    private StorageFileRepository repository;

    private FileTree fileTree;

    @PostConstruct
    public void initializeStorage() throws IOException {
        String path = properties.getStoragePath();
        int maxFilesInFolder = properties.getMaxFilesInFolder();
        fileTree = FileTree.scan(path, maxFilesInFolder);
        //TODO perform data consistency check between database and file system
    }

    @Override
    @Transactional
    public StorageFileEntity createFilePlaceHolder(HttpServletRequest request) throws IOException {
        StorageFileEntity entity = new StorageFileEntity();

        // File name validation
        String name = request.getParameter("name");
        if (!FileUtils.isValidFilename(name, 255)) {
            throw new IllegalArgumentException("Invalid file name");
        }
        entity.setName(name);

        // File size validation
        String sizeStr = request.getParameter("size");
        long maxFileSize = properties.getMaxFileSize();
        if (!NumberUtils.isValidLong(sizeStr, 0, maxFileSize)) {
            throw new IllegalArgumentException("Invalid file size");
        }
        long size = Long.valueOf(sizeStr);
        entity.setSize(size);

        // Chunk size validation
        String chunkSizeStr = request.getParameter("chunkSize");
        long minChunkSize = properties.getMinChunkSize();
        long maxChunkSize = properties.getMaxChunkSize();
        if (!NumberUtils.isValidLong(chunkSizeStr, minChunkSize , maxChunkSize )) {
            throw new IllegalArgumentException("Invalid file size");
        }
        long chunkSize = Long.valueOf(chunkSizeStr);
        entity.setChunkSize(chunkSize);

        // Check sum validation
        String checkSum = request.getParameter("checkSum");
        byte[] checkSumByteArray = StringUtils.hexToBytes(checkSum);
        if (!CheckSumUtils.isChunkCheckValid(checkSumByteArray)) {
            throw new IllegalArgumentException("Invalid check sum");
        }
        entity.setCheckSum(checkSumByteArray);

        FileNode placeHolder = fileTree.createPlaceHolder();
        String relativePath = placeHolder.getRelativePath();
        entity.setPath(relativePath);

        UserEntity user = userService.getLoggedInUser();
        entity.setOwner(user);

        entity = repository.saveAndFlush(entity);

        return entity;
    }

    @Override
    public void uploadFile(StorageFileEntity placeHolder, MultipartFile file) {
        // TODO Auto-generated method stub

    }
}
