package app.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

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
import app.util.SafeUtils;
import app.util.StringUtils;

@Component
public class FileStorageServiceImpl implements FileStorageService {

    private static final String CHECK_SUM_FILE_SUFFIX = "_sum";

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
    public List<StorageFileEntity> findAllNonCompletedUploadedByCurrentUser() {
        UserEntity user = userService.getLoggedInUser();
        List<StorageFileEntity> nonCompleted = repository.findByCompletedIsFalseAndUploadedBy(user);
        return nonCompleted;
    }

    @Override
    public List<StorageFileEntity> findAllCompletedUploadedByCurrentUser() {
        UserEntity user = userService.getLoggedInUser();
        List<StorageFileEntity> completed = repository.findByCompletedIsTrueAndUploadedBy(user);
        return completed;
    }

    @Override
    @Transactional
    public StorageFileEntity createFilePlaceHolder(HttpServletRequest request) throws Exception {
        UserEntity user = userService.getLoggedInUser();

        StorageFileEntity entity;
        String fileIdStr = request.getParameter("fileId");
        if (NumberUtils.isValidLong(fileIdStr)) {
            Long fileId = Long.valueOf(fileIdStr);
            entity = repository.findOne(fileId);
            if (entity != null && SafeUtils.safeEquals(entity.getUploadedBy().getId(), user.getId())) {
                return entity;
            } else {
                entity = new StorageFileEntity();
            }
        } else {
            entity = new StorageFileEntity();
        }

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
        int chunkSize = Integer.valueOf(chunkSizeStr);
        entity.setChunkSize(chunkSize);

        // Check sum validation
        String checkSum = request.getParameter("checkSum");
        byte[] checkSumByteArray = StringUtils.hexToBytes(checkSum);
        if (!CheckSumUtils.isChunksDigestsValid(checkSumByteArray)) {
            throw new IllegalArgumentException("Invalid check sum");
        }

        FileNode placeHolder = fileTree.createPlaceHolder();
        String relativePath = placeHolder.getRelativePath();
        entity.setPath(relativePath);
        saveCheckSumToFile(checkSumByteArray, relativePath);

        entity.setUploadedBy(user);
        entity.setLastUpdated(new Date());

        entity = repository.saveAndFlush(entity);

        return entity;
    }

    @Override
    @Transactional
    public int uploadFileChunk(MultipartFile chunk, String filePlaceHolderIdString) throws Exception {
        // Checking that filePlaceHolderId is a number
        boolean valid = NumberUtils.isValidLong(filePlaceHolderIdString);
        if (!valid) {
            throw new IllegalArgumentException("Invalid file placeholder Id: " + filePlaceHolderIdString);
        }
        Long filePlaceHolderId = Long.valueOf(filePlaceHolderIdString);

        // Checking that FilePlaceHolder with give Id is actually exists
        StorageFileEntity filePlaceHolder = repository.findOne(filePlaceHolderId);
        if (filePlaceHolder == null) {
            throw new IllegalArgumentException("File placeholder Id '" + filePlaceHolderIdString + "' not found");
        }

        // Checking that FilePlaceHolder is not yet completely download
        if (filePlaceHolder.isCompleted()) {
            throw new IllegalStateException("File with Id '" + filePlaceHolderId + "' is already completely uploaded");
        }

        // Verifying checksum of the given chunk
        FileNode fileNode = fileTree.getFileNode(filePlaceHolder.getPath());
        File file = new File(fileNode.getAbsolutePath());
        int uploadedChunks = filePlaceHolder.calclateUploadedChunksNumber();
        byte[] allCheckSums = extractCheckSumFromFile(filePlaceHolder.getPath());
        byte[] currentChunkCheckSum = new byte[16];
        System.arraycopy(allCheckSums, uploadedChunks * 16, currentChunkCheckSum, 0, 16);
        byte[] chunkBytes = chunk.getBytes();
        boolean chunkValid = CheckSumUtils.isCheckSumValid(chunkBytes, currentChunkCheckSum);
        if (!chunkValid) {
            throw new IllegalArgumentException("Chunk checksum is not valid");
        }

        // Writing chunk data to the file on the file system
        try (FileOutputStream fos = new FileOutputStream(file, true);) {
            fos.write(chunkBytes);
            fos.flush();
        }
        filePlaceHolder.setUploadedBytes(file.length());

        // Checking if it was last chunk - then changing status to completed
        if (file.length() == filePlaceHolder.getSize()) {
            checkWholeFile(filePlaceHolder, file);
            return -1;
        }

        filePlaceHolder.setLastUpdated(new Date());
        repository.saveAndFlush(filePlaceHolder);

        return uploadedChunks + 1;
    }

    private void checkWholeFile(StorageFileEntity filePlaceHolder, File file) throws Exception {
        int chunkSize = filePlaceHolder.getChunkSize();

        byte[] chunkCheckSums = extractCheckSumFromFile(filePlaceHolder.getPath());
        byte[] chunkBuffer = new byte[chunkSize];

        try (FileInputStream inputStream = new FileInputStream(file);) {
            MessageDigest digestCalculator = CheckSumUtils.getCalculator();

            int numberOfChunks = chunkCheckSums.length / 16 - 1;
            for (int i = 0; i < numberOfChunks; i++) {
                int read = inputStream.read(chunkBuffer);
                if (read == 0 || read == -1) {
                    break;
                }

                byte[] actualReadBuffer = getSubArray(chunkBuffer, 0, read);
                byte[] expectedCheckSum = getSubArray(chunkCheckSums, i * 16, 16);
                boolean chunkValid = CheckSumUtils.isCheckSumValid(actualReadBuffer, expectedCheckSum);
                if (!chunkValid) {
                    throw new IllegalStateException("Checksum of chunk " + i + " is not valid");
                }

                digestCalculator.update(actualReadBuffer);
            }

            byte[] wholeFileCheckSum = digestCalculator.digest();
            String wholeFileCheckSumString = StringUtils.bytesToHex(wholeFileCheckSum);
            filePlaceHolder.setCheckSum(wholeFileCheckSumString);
            deleteCheckSumFile(filePlaceHolder.getPath());
            filePlaceHolder.setCompleted(true);
        }
    }

    private byte[] extractCheckSumFromFile(String relativePath) throws Exception {
        String checkSumPath = getCheckSumPath(relativePath);
        try (FileInputStream inputStream = new FileInputStream(checkSumPath);) {
            int available = inputStream.available();
            byte[] checkSum = new byte[available];
            inputStream.read(checkSum);
            return checkSum;
        }
    }

    private void saveCheckSumToFile(byte[] checkSum, String relativePath) throws Exception {
        String checkSumPath = getCheckSumPath(relativePath);
        try (FileOutputStream outputStream = new FileOutputStream(checkSumPath);) {
            outputStream.write(checkSum);
            outputStream.flush();
        }
    }

    private void deleteCheckSumFile(String relativePath) {
        String checkSumPath = getCheckSumPath(relativePath);
        File file = new File(checkSumPath);
        file.delete();
    }

    private String getCheckSumPath(String relativePath) {
        FileNode fileNode = fileTree.getFileNode(relativePath);
        String filePath = fileNode.getAbsolutePath();
        String checkSumPath = filePath + CHECK_SUM_FILE_SUFFIX;
        return checkSumPath;
    }

    private byte[] getSubArray(byte[] array, int start, int length) {
        byte[] subArray = new byte[length];
        System.arraycopy(array, start, subArray, 0, length);
        return subArray;
    }

    @Override
    public void uploadFile(StorageFileEntity placeHolder, MultipartFile file) {
        // TODO Auto-generated method stub

    }
}
