package app.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
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
import javassist.NotFoundException;

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
        List<StorageFileEntity> completed = repository.findByCompletedIsTrueAndDeletedIsFalseAndUploadedBy(user);
        return completed;
    }

    @Override
    public StorageFileEntity getOrCreateFilePlaceHolder(HttpServletRequest request) throws Exception {
        UserEntity user = userService.getLoggedInUser();

        // Checking, is this attempt to resume upload of incomplete file
        StorageFileEntity filePlaceHolderEntity;
        String fileIdStr = request.getParameter("fileId");
        if (NumberUtils.isValidLong(fileIdStr)) {
            Long fileId = Long.valueOf(fileIdStr);
            filePlaceHolderEntity = repository.findOne(fileId);
            if (filePlaceHolderEntity != null && SafeUtils.safeEquals(filePlaceHolderEntity.getUploadedBy().getId(), user.getId())) {
                boolean matches = checkSumMatchWithExisting(filePlaceHolderEntity, request);
                if (matches) {
                    return filePlaceHolderEntity;
                }
                throw new IllegalArgumentException("Checksum of the file being submitted doesn't match with existing one");
            }
        }

        // Otherwise creating new file placeholder
        filePlaceHolderEntity = createFilePlaceHolder(request, user);
        return filePlaceHolderEntity;
    }

    @Transactional
    private StorageFileEntity createFilePlaceHolder(HttpServletRequest request, UserEntity user) throws Exception {
        checkFreeSpace();

        StorageFileEntity filePlaceHolderEntity = new StorageFileEntity();

        // File name validation
        String name = request.getParameter("name");
        if (!FileUtils.isValidFilename(name, 255)) {
            throw new IllegalArgumentException("Invalid file name");
        }
        filePlaceHolderEntity.setFilename(name);

        // File size validation
        String sizeStr = request.getParameter("size");
        long maxFileSize = properties.getMaxFileSize();
        if (!NumberUtils.isValidLong(sizeStr, 0, maxFileSize)) {
            throw new IllegalArgumentException("Invalid file size");
        }
        long size = Long.valueOf(sizeStr);
        filePlaceHolderEntity.setSize(size);

        // Chunk size validation
        String chunkSizeStr = request.getParameter("chunkSize");
        long minChunkSize = properties.getMinChunkSize();
        long maxChunkSize = properties.getMaxChunkSize();
        if (!NumberUtils.isValidLong(chunkSizeStr, minChunkSize , maxChunkSize )) {
            throw new IllegalArgumentException("Invalid file size");
        }
        int chunkSize = Integer.valueOf(chunkSizeStr);
        filePlaceHolderEntity.setChunkSize(chunkSize);

        // Check sum validation
        String checkSum = request.getParameter("checkSum");
        byte[] checkSumByteArray = StringUtils.hexToBytes(checkSum);
        if (!CheckSumUtils.isChunksDigestsValid(checkSumByteArray)) {
            throw new IllegalArgumentException("Invalid check sum");
        }

        FileNode placeHolder = fileTree.createPlaceHolder();
        String relativePath = placeHolder.getRelativePath();
        filePlaceHolderEntity.setPath(relativePath);
        saveCheckSumToFile(checkSumByteArray, relativePath);

        filePlaceHolderEntity.setUploadedBy(user);
        filePlaceHolderEntity.setLastUpdated(new Date());

        filePlaceHolderEntity = repository.saveAndFlush(filePlaceHolderEntity);

        return filePlaceHolderEntity;
    }

    private boolean checkSumMatchWithExisting(StorageFileEntity filePlaceHolderEntity, HttpServletRequest request) throws Exception {
        String filePath = filePlaceHolderEntity.getPath();
        byte[] existingChecksum = extractCheckSumFromFile(filePath);
        String checkSum = request.getParameter("checkSum");
        byte[] submittedCheckSum = StringUtils.hexToBytes(checkSum);
        boolean equals = Arrays.equals(existingChecksum, submittedCheckSum);
        return equals;
    }

    @Override
    @Transactional
    public int uploadFileChunk(MultipartFile chunk, String filePlaceHolderIdString) throws Exception {
        checkFreeSpace();
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

    private void checkFreeSpace() throws IOException {
        long actualFreeSpace = getFreeSpace();
        long minFreeSpace = properties.getMinFreeSpaceOnDisk();
        if (actualFreeSpace < minFreeSpace) {
            throw new IOException("Storage disk space quota exceeded");
        }
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

    private void deleteFile(String relativePath) {
        String filePath = getFilePath(relativePath);
        if (filePath == null) {
            return;
        }
        File file = new File(filePath);
        file.delete();
    }

    private String getFilePath(String relativePath) {
        FileNode fileNode = fileTree.getFileNode(relativePath);
        if (fileNode == null) {
            return null;
        }
        String filePath = fileNode.getAbsolutePath();
        return filePath;
    }

    private void deleteCheckSumFile(String relativePath) {
        String checkSumPath = getCheckSumPath(relativePath);
        if (checkSumPath == null) {
            return;
        }
        File file = new File(checkSumPath);
        file.delete();
    }

    private String getCheckSumPath(String relativePath) {
        FileNode fileNode = fileTree.getFileNode(relativePath);
        if (fileNode == null) {
            return null;
        }
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

    @Override
    @Transactional
    public void deletePlaceHolder(Long id) throws Exception {
        UserEntity user = userService.getLoggedInUser();
        StorageFileEntity filePlaceHolder = repository.findOne(id);
        if (filePlaceHolder == null) {
            throw new NotFoundException("File placeholder with id: '" + id + "' was not found");
        }
        if (!SafeUtils.safeEquals(filePlaceHolder.getUploadedBy().getId(), user.getId())) {
            throw new IllegalAccessException("User tries to delete not his own file placeholder");
        }

        if (filePlaceHolder.isCompleted()) {
            filePlaceHolder.setDeleted(true);
            repository.saveAndFlush(filePlaceHolder);
        } else {
            repository.delete(filePlaceHolder);
            deleteFile(filePlaceHolder.getPath());
            deleteCheckSumFile(filePlaceHolder.getPath());
            repository.flush();
        }
    }

    @Override
    public long getFreeSpace() throws IOException {
        String storagePath = properties.getStoragePath();
        File storageFolder = new File(storagePath);
        long freeSpace = storageFolder.getFreeSpace();
        return freeSpace;
    }
}
