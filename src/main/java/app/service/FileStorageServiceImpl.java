package app.service;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import app.dao.StorageFileRepository;
import app.entity.StorageFileEntity;
import app.properties.FileStorageProperties;
import app.structures.FileTree;
import app.structures.FileTree.FileNode;

@Component
public class FileStorageServiceImpl implements FileStorageService {
    @Autowired
    private StorageFileRepository repository;
    @Autowired
    private FileStorageProperties properties;

    private FileTree fileTree;

    @PostConstruct
    public void initializeStorage() throws IOException {
        String path = properties.getStoragePath();
        int maxFilesInFolder = properties.getMaxFilesInFolder();
        fileTree = FileTree.scan(path, maxFilesInFolder);
    }

    @Override
    @Transactional
    public StorageFileEntity createFilePlaceHolder(String name, long size) throws IOException {
        StorageFileEntity entity = new StorageFileEntity();

        entity.setName(name);
        entity.setSize(size);

        FileNode placeHolder = fileTree.createPlaceHolder();
        String relativePath = placeHolder.getRelativePath();
        entity.setPath(relativePath);

        entity = repository.saveAndFlush(entity);

        return entity;
    }

    @Override
    public void uploadFile(StorageFileEntity placeHolder, MultipartFile file) {
        // TODO Auto-generated method stub

    }
}
