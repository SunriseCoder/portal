package app.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;

import javax.annotation.ManagedBean;

import org.springframework.beans.factory.annotation.Value;

import app.entity.AudioFile;
import app.entity.Folder;

@ManagedBean
public class AudioRepositoryImpl implements AudioRepository {
	@Value("${audios.folder}")
	private String audiosFolder;

	public Folder findAll() throws Exception {
		File file = new File(audiosFolder);

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
				folder.addFile(audio);
			}
		}

		return folder;
	}
}
