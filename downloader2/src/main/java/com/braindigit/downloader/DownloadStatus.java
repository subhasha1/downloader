package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class DownloadStatus {
    public boolean isChunked;
    private long totalSize;
    private long downloadSize;

    public long getDownloadSize() {
        return downloadSize;
    }

    void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}
