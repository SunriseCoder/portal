package app.structures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;

import app.util.FileUtils;

public class FileTree {
    private FolderNode root;
    private int maxFilesInFolder;

    private FolderNode folderForFiles;
    private FolderNode folderForFolders;

    private FileTree() {
        // Create by static method(s) only
    }

    public synchronized FileNode createPlaceHolder() throws IOException {
        FileNode fileNode = createFile();
        return fileNode;
    }

    private FileNode createFile() throws IOException {
        FileNode fileNode = new FileNode(folderForFiles, getUniqueName(folderForFiles.fileSystemPath));
        folderForFiles.files.put(fileNode.name, fileNode);
        fileNode.createFile();
        findCandidates();
        return fileNode;
    }

    private String getUniqueName(String folderPath) throws NotDirectoryException, FileNotFoundException {
        File folder = new File(folderPath);
        FileUtils.checkDirectoryExists(folder);

        Set<String> files = new HashSet<>(Arrays.asList(folder.list()));
        String filename;
        do {
            filename = RandomStringUtils.randomAlphanumeric(4);
        } while (files.contains(filename));
        return filename;
    }

    private void findCandidates() throws NotDirectoryException, FileNotFoundException {
        if (folderForFolders == null || folderForFolders.folders.size() >= maxFilesInFolder) {
            findFolderForFolders();
        }

        if (folderForFiles == null || folderForFiles.files.size() >= maxFilesInFolder) {
            findFolderForFiles();
        }
    }

    private void findFolderForFolders() {
        List<FolderNode> folders = new ArrayList<>();
        findFoldersForFolders(folders, root);
        folders.sort((a, b) -> a.level != b.level ? a.level - b.level : a.folders.size() - b.folders.size());
        folderForFolders = folders.get(0);
    }

    private void findFoldersForFolders(List<FolderNode> folders, FolderNode parent) {
        if (parent.folders.size() < maxFilesInFolder) {
            folders.add(parent);
        }

        for (FolderNode folder : parent.folders.values()) {
            findFoldersForFolders(folders, folder);
        }
    }

    private void findFolderForFiles() throws NotDirectoryException, FileNotFoundException {
        List<FolderNode> folders = new ArrayList<>();
        findFoldersForFiles(folders, root);

        if (folders.isEmpty()) {
            createFolder();
            return;
        }

        folderForFiles = folders.get(0);
    }

    private void createFolder() throws NotDirectoryException, FileNotFoundException {
        File parent = new File(folderForFolders.fileSystemPath);
        File file = new File(parent, getUniqueName(parent.getAbsolutePath()));
        FolderNode folder = new FolderNode(folderForFolders, file);
        folderForFolders.folders.put(folder.name, folder);
        folder.createFolder();
        findCandidates();
    }

    private void findFoldersForFiles(List<FolderNode> folders, FolderNode parent) {
        if (parent.files.size() < maxFilesInFolder) {
            folders.add(parent);
        }

        for (FolderNode folder : parent.folders.values()) {
            findFoldersForFiles(folders, folder);
        }
    }

    public FileNode getFileNode(String path) {
        String[] pathElements = path.split("/");
        FolderNode folder = root;
        for (int i = 1; i < pathElements.length - 1; i++) {
            String folderName = pathElements[i];
            folder = folder.getFolder(folderName);
        }
        FileNode fileNode = folder.getFile(pathElements[pathElements.length - 1]);
        return fileNode;
    }

    public static FileTree scan(String path, int maxFilesInFolder) throws FileNotFoundException, NotDirectoryException {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        FolderNode folder = scanRecurisevely(file, null);

        FileTree tree = new FileTree();
        tree.root = folder;
        tree.maxFilesInFolder = maxFilesInFolder;
        tree.findCandidates();
        return tree;
    }

    private static FolderNode scanRecurisevely(File file, FolderNode parent) {
        FolderNode folderNode = new FolderNode(parent, file);

        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                FolderNode childFolder = scanRecurisevely(child, folderNode);
                folderNode.folders.put(childFolder.name, childFolder);
            } else {
                FileNode childFile = new FileNode(folderNode, child.getName());
                folderNode.files.put(childFile.name, childFile);
            }
        }

        return folderNode;
    }

    public static class FolderNode {
        public FolderNode parent;
        public String name;
        public int level;
        public String fileSystemPath;
        public Map<String, FolderNode> folders = new HashMap<>();
        public Map<String, FileNode> files = new HashMap<>();

        public FolderNode(FolderNode parent, File file) {
            this.parent = parent;
            this.level = parent == null ? 0 : parent.level + 1;
            this.name = file.getName();
            this.fileSystemPath = file.getAbsolutePath();
        }

        public FolderNode getFolder(String folderName) {
            FolderNode folder = folders.get(folderName);
            return folder;
        }

        public FileNode getFile(String fileName) {
            FileNode file = files.get(fileName);
            return file;
        }

        public void createFolder() {
            File folder = new File(fileSystemPath);
            folder.mkdirs();
        }
    }

    public static class FileNode {
        public FolderNode parent;
        public String name;

        public FileNode(FolderNode parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        public void createFile() throws IOException {
            File parentFile = new File(parent.fileSystemPath);
            File file = new File(parentFile, name);
            file.createNewFile();
        }

        public String getRelativePath() {
            String path = getParentPath(parent) + name;
            return path;
        }

        private String getParentPath(FolderNode parent) {
            if (parent == null) {
                return "";
            }

            String path = getParentPath(parent.parent) + parent.name + "/";
            return path;
        }

        public String getAbsolutePath() {
            File parentFile = new File(parent.fileSystemPath);
            File file = new File(parentFile, name);
            String path = file.getAbsolutePath();
            return path;
        }
    }
}
