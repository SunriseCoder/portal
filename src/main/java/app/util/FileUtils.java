package app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NotDirectoryException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import app.entity.FileEntity;
import app.entity.FolderEntity;

public class FileUtils {

	public static void checkExists(File file) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
	}

	public static void checkDirectoryExists(File folder) throws FileNotFoundException, NotDirectoryException {
		if (!folder.exists()) {
			throw new FileNotFoundException(folder.getAbsolutePath());
		}
		if (!folder.isDirectory()) {
			throw new NotDirectoryException(folder.getAbsolutePath());
		}
	}

	public static FolderEntity getVirtualTreeRecursively(File dir) {
		FolderEntity folder = new FolderEntity();
		folder.setName(dir.getName());

		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				FolderEntity childFolder = getVirtualTreeRecursively(file);
				folder.addFolder(childFolder);
			} else {
				FileEntity fileEntity = new FileEntity();
				fileEntity.setName(file.getName());
				fileEntity.setSize(file.length());
				folder.addFile(fileEntity);
			}
		}

		return folder;
	}

	static void writeFile(HttpServletResponse response, File file, long start, long length) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			OutputStream outputStream = response.getOutputStream();
			IOUtils.copyLarge(inputStream, outputStream, start, length);
		}
	}
}
