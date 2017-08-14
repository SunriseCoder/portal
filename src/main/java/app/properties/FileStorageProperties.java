package app.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileStorageProperties {
    @Value("${files.storage.path}")
    private String storagePath;
    @Value("${files.storage.max-files-in-folder}")
    private int maxFilesInFolder;

    public String getStoragePath() {
        return storagePath;
    }

    public int getMaxFilesInFolder() {
        return maxFilesInFolder;
    }
}
