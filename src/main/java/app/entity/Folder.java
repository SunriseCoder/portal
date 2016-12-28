package app.entity;

import java.util.ArrayList;
import java.util.List;

public class Folder {
	private String name;
	private List<Folder> folders;
	private List<AudioFile> files;

	public Folder() {
		folders = new ArrayList<>();
		files = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Folder> getFolders() {
		return folders;
	}

	public List<AudioFile> getFiles() {
		return files;
	}

	public void addFolder(Folder folder) {
		folders.add(folder);
	}

	public void addFile(AudioFile audio) {
		files.add(audio);
	}
}
