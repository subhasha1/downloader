package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/11/16.
 */

public class ActionStatus {
    public final DownloadAction downloadAction;
    public final DownloadStatus downloadStatus;
    public final Exception e;

    public ActionStatus(DownloadAction downloadAction, DownloadStatus downloadStatus, Exception e) {
        this.downloadAction = downloadAction;
        this.downloadStatus = downloadStatus;
        this.e = e;
    }
}
