package app.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.ManagedBean;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;

import app.entity.AudioFile;
import app.entity.Folder;

@ManagedBean
public class AudioRepositoryImpl implements AudioRepository {
	@Value("${audio.folder}")
	private String audioFolder;
	private Map<String, String> contentTypes;

	public AudioRepositoryImpl() {
		fillContentTypes();
	}

	@Override
	public Folder findAll() throws Exception {
		File file = new File(audioFolder);

		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		if (!file.isDirectory()) {
			throw new NotDirectoryException(file.getAbsolutePath());
		}

		Folder folder = scanRecursively(file);
		return folder;
	}

	private Folder scanRecursively(File dir) {
		Folder folder = new Folder();
		folder.setName(dir.getName());

		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				Folder childFolder = scanRecursively(file);
				folder.addFolder(childFolder);
			} else {
				AudioFile audio = new AudioFile();
				audio.setName(file.getName());
				audio.setSize(file.length());
				folder.addFile(audio);
			}
		}

		return folder;
	}

	@Override
	public void downloadFile(String url, HttpServletResponse response) throws Exception {
		String path = audioFolder + "/" + url;
		File file = new File(path);
		if (!file.exists()) {
			response.sendError(404);
			return;
		}

		if (file.isDirectory()) {
			sendFolder(response, file);
		} else {
			sendFile(response, file);
		}
	}

	private void sendFolder(HttpServletResponse response, File file) throws Exception {
		String fileName = file.getName() + ".zip";

		fileName = URLEncoder.encode(fileName, "UTF-8");
		// URL Encoder replaces whitespaces with pluses,
		// therefore filename by saving contains pluses instead of whitespaces
		fileName = fileName.replaceAll("\\+", "%20");

		String contentType = getContentType(fileName);
		response.setContentType(contentType);
		// Use "attachment; filename=..." to download instead of playing file
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=utf8''" + fileName);

		OutputStream outputStream = response.getOutputStream();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);) {
			zipOutputStream.setLevel(Deflater.NO_COMPRESSION);

			File root = file.getParentFile();
			addFolderRecursively(root, file, zipOutputStream);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error due to ZIP-file creation.");
		} finally {
			response.flushBuffer();
		}
	}

	private void addFolderRecursively(File root, File folder, ZipOutputStream zipOutputStream) throws Exception {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				addFolderRecursively(root, file, zipOutputStream);
			} else {
				// Calculating relative path from root folder of the archive
				String rootFolderPath = root.getAbsolutePath();
				String filePath = file.getAbsolutePath();
				String relativeFilePath = filePath.substring(rootFolderPath.length() + 1);

				// Adding Entry with path to the archive
				ZipEntry entry = new ZipEntry(relativeFilePath);
				zipOutputStream.putNextEntry(entry);

				// Copying file data
				try (InputStream inputStream = new FileInputStream(file);) {
					IOUtils.copy(inputStream, zipOutputStream);
				}
			}
		}
	}

	private void sendFile(HttpServletResponse response, File file) throws Exception {
		String fileName = file.getName();
		fileName = URLEncoder.encode(fileName, "UTF-8");
		// URL Encoder replaces whitespaces with pluses,
		// therefore filename by saving contains pluses instead of whitespaces
		fileName = fileName.replaceAll("\\+", "%20");

		String contentType = getContentType(fileName);
		response.setContentType(contentType);
		// Use "attachment; filename=..." to download instead of playing file
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "filename*=utf8''" + fileName);
		response.setContentLengthLong(file.length());

		InputStream inputStream = new FileInputStream(file);
		OutputStream outputStream = response.getOutputStream();
		IOUtils.copy(inputStream, outputStream);
		response.flushBuffer();
	}

	private String getContentType(String fileName) {
		// Extracting File Extension
		int lastDotPosition = fileName.lastIndexOf(".");
		String fileExttension;
		if (lastDotPosition != -1) {
			fileExttension = fileName.substring(lastDotPosition + 1);
		} else {
			fileExttension = fileName;
		}
		fileExttension = fileExttension.toLowerCase();

		// Retrieving Content Type by File Extension
		String contentType = contentTypes.get(fileExttension);
		if (contentType == null) {
			contentType = contentTypes.get(null);
		}
		return contentType;
	}

	private void fillContentTypes() {
		contentTypes = new HashMap<>();

		// Default content type - just to be able to download file
		contentTypes.put(null, "application/octet-stream");

		// Text files
		contentTypes.put("htm", "text/html");
		contentTypes.put("html", "text/html");
		contentTypes.put("pdf", "application/pdf");
		contentTypes.put("txt", "text/plain");

		// Images
		contentTypes.put("gif", "image/gif");
		contentTypes.put("jpeg", "image/jpeg");
		contentTypes.put("jpg", "image/jpeg");
		contentTypes.put("png", "image/png");

		// Audio
		contentTypes.put("m4a", "audio/mp4");
		contentTypes.put("mp3", "audio/mpeg");
		contentTypes.put("ogg", "audio/ogg");
		contentTypes.put("wav", "audio/wave");
		contentTypes.put("wave", "audio/wave");

		// Video
		contentTypes.put("mp4", "video/mp4");
		contentTypes.put("m4v", "video/mp4");

		// Archives
		contentTypes.put("", "application/zip");
	}
}
