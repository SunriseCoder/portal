package app.dao;

import java.io.File;
import java.io.IOException;

import javax.annotation.ManagedBean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

import app.entity.FolderEntity;
import app.util.FileUtils;
import app.util.HttpUtils;

@ManagedBean
public class FileRepositoryImpl implements FileRepository {
    @Value("${files.storage}")
    private String storageUrl;
    @Value("${files.filesystem}")
    private String unsortedFilesUrl;

    @Override
    public FolderEntity findAll() throws Exception {
        File folderOnDisk = new File(unsortedFilesUrl); 
        FileUtils.checkDirectoryExists(folderOnDisk);
        FolderEntity virtualTree = FileUtils.getVirtualTreeRecursively(folderOnDisk);
        return virtualTree;
    }

    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String path = unsortedFilesUrl + "/" + url;
        File file = new File(path);
        FileUtils.checkExists(file);

        if (file.isDirectory()) {
            HttpUtils.sendFolderAsZip(request, response, file);
        } else {
            HttpUtils.sendFile(request, response, file);
        }
    }
}
