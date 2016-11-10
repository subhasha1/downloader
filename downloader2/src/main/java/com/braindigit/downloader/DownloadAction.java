package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/10/16.
 */

public class DownloadAction {
    final FileInfo fileInfo;
    final DownloadListener downloadListener;

    public DownloadAction(FileInfo fileInfo, DownloadListener listener) {
        this.fileInfo = fileInfo;
        this.downloadListener = listener;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public DownloadListener getDownloadListener() {
        return downloadListener;
    }
}
