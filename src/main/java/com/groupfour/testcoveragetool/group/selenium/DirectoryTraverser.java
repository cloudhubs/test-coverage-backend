package com.groupfour.testcoveragetool.group.selenium;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DirectoryTraverser {
    public interface FileHandler {
        void handle(int level, String path, File file) throws IOException;
    }
    public interface Filter {
        boolean interested(int level, String path, File file);
    }
    private FileHandler fileHandler;
    private Filter filter;
    public DirectoryTraverser(Filter filter, FileHandler fileHandler) {
        this.filter = filter;
        this.fileHandler = fileHandler;
    }
    public void explore(File root) throws IOException {
        explore(0, "", root);
    }
    private void explore(int level, String path, File file) throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                explore(level + 1, path + "/" + child.getName(), child);
            }
        } else {
            if (filter.interested(level, path, file)) {
                fileHandler.handle(level, path, file);
            }
        }
    }
}