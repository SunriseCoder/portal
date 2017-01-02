package app.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.NotDirectoryException;

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

		response.setContentType("audio/mpeg");
		// Use "attachment; filename=..." to download instead of playing file
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "filename*=utf8''" + fileName);
		response.setContentLengthLong(file.length());

		InputStream is = new FileInputStream(file);
		OutputStream os = response.getOutputStream();
		IOUtils.copy(is, os);
		response.flushBuffer();
	}
}
