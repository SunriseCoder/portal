package app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZipUtils {
    private static final Logger logger = LogManager.getLogger(ZipUtils.class.getName());

    public static void createAndWriteZipArchive(File folder, OutputStream outputStream) throws IOException {
        FileUtils.checkDirectoryExists(folder);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);) {
            zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
    
            File root = folder.getParentFile();
            addFolderToZipRecursively(root, folder, zipOutputStream);
        }
    }

    private static void addFolderToZipRecursively(File root, File folder, ZipOutputStream zipOutputStream) throws IOException {
        // Adding folder entry to archive
        ZipEntry folderEntry = createZipEntry(root, folder, true);
        zipOutputStream.putNextEntry(folderEntry);

        // Scanning folder recursively
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderToZipRecursively(root, file, zipOutputStream);
            } else {
                try {
                    // Adding entry to archive
                    ZipEntry entry = createZipEntry(root, file, false);
                    zipOutputStream.putNextEntry(entry);

                    // Copying file data
                    try (InputStream inputStream = new FileInputStream(file);) {
                        IOUtils.copy(inputStream, zipOutputStream);
                    }
                } catch (IOException e) {
                    logger.error("Can not add file {} to ZIP-archive", file.getAbsolutePath(), e);
                    throw e;
                }
            }
        }
    }

    private static ZipEntry createZipEntry(File root, File file, boolean isFolder) {
        // Calculating relative path from root folder of the archive
        String rootFolderPath = root.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        String relativeFilePath = filePath.substring(rootFolderPath.length() + 1);

        // Adding slash at the end of folder entry
        if (isFolder) {
            relativeFilePath += "/";
        }

        // Creating Entry with relative path
        ZipEntry entry = new ZipEntry(relativeFilePath);
        return entry;
    }
}
