package app.entity;

import java.util.ArrayList;
import java.util.List;

public class FolderEntity {
    private String name;
    private List<FolderEntity> folders;
    private List<FileEntity> files;

    private int size;

    public FolderEntity() {
        folders = new ArrayList<>();
        files = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FolderEntity> getFolders() {
        return folders;
    }

    public List<FileEntity> getFiles() {
        return files;
    }

    public void addFolder(FolderEntity folder) {
        folders.add(folder);
    }

    public void addFile(FileEntity file) {
        files.add(file);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int countChilds() {
        int size = folders.stream().mapToInt(folder -> folder.countChilds()).sum();
        size += files.size() + 1;
        return this.size = size;
    }
}
