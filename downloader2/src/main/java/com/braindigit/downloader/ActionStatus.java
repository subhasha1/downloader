package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/11/16.
 */

class ActionStatus {
    final DownloadRequest downloadRequest;
    final DownloadStatus downloadStatus;
    final Exception e;

    ActionStatus(DownloadRequest downloadRequest, DownloadStatus downloadStatus, Exception e) {
        this.downloadRequest = downloadRequest;
        this.downloadStatus = downloadStatus;
        this.e = e;
    }
}
