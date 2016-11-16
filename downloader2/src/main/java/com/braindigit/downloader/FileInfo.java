package com.braindigit.downloader;

import java.io.File;

/**
 * Braindigit
 * Created on 11/9/16.
 */
 class FileInfo {
    private String url;
    private File savePath;
    private long fileLength;
    private String lastModify;
    private String fileName;

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    File getSavePath() {
        return savePath;
    }

    void setSavePath(File savePath) {
        this.savePath = savePath;
    }

    long getFileLength() {
        return fileLength;
    }

    void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    String getLastModify() {
        return lastModify;
    }

    void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }


    static class Destination {
       final String filePath;
       final String tempPath;
       final String lastModifiedPath;

        Destination(String filePath, String tempPath, String lastModifiedPath) {
            this.filePath = filePath;
            this.tempPath = tempPath;
            this.lastModifiedPath = lastModifiedPath;
        }
    }
}
