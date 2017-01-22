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
		if (!file.exists() || file.isDirectory()) {
			response.sendError(404);
			return;
		}

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

		InputStream is = new FileInputStream(file);
		OutputStream os = response.getOutputStream();
		IOUtils.copy(is, os);
		response.flushBuffer();
	}

	private String getContentType(String fileName) {
		// Extracting File Extension
		int lastDotPosition = fileName.lastIndexOf(".");
		String fileExt;
		if (lastDotPosition != -1) {
			fileExt = fileName.substring(lastDotPosition + 1);
		} else {
			fileExt = fileName;
		}
		fileExt = fileExt.toLowerCase();

		// Retrieving Content Type by File Extention
		String contentType = contentTypes.get(fileExt);
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
