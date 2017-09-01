package app.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileStorageProperties {
    @Value("${files.storage.path}")
    private String storagePath;
    @Value("${files.storage.max-files-in-folder}")
    private int maxFilesInFolder;
    @Value("${files.storage.max-file-size}")
    private long maxFileSize;
    @Value("${files.storage.min-chunk-size}")
    private long minChunkSize;
    @Value("${files.storage.max-chunk-size}")
    private long maxChunkSize;
    @Value("${files.storage.min-free-space-on-disk}")
    private long minFreeSpaceOnDisk;

    public String getStoragePath() {
        return storagePath;
    }

    public int getMaxFilesInFolder() {
        return maxFilesInFolder;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public long getMinChunkSize() {
        return minChunkSize;
    }

    public long getMaxChunkSize() {
        return maxChunkSize;
    }

    public long getMinFreeSpaceOnDisk() {
        return minFreeSpaceOnDisk;
    }
}
