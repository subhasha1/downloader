package com.braindigit.downloader;

import java.io.File;

/**
 * Braindigit
 * Created on 11/9/16.
 */
public class FileInfo {
    private String url;
    private File savePath;
    private long fileLength;
    private String lastModify;
    private String fileName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getSavePath() {
        return savePath;
    }

    public void setSavePath(File savePath) {
        this.savePath = savePath;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public String getLastModify() {
        return lastModify;
    }

    public void setLastModify(String lastModify) {
        this.lastModify = lastModify;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public static class Destination {
       public final String filePath;
       public final String tempPath;
       public final String lastModifiedPath;

        public Destination(String filePath, String tempPath, String lastModifiedPath) {
            this.filePath = filePath;
            this.tempPath = tempPath;
            this.lastModifiedPath = lastModifiedPath;
        }
    }
}
