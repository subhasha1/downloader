package com.braindigit.downloader.types;

import com.braindigit.downloader.DownloadListener;
import com.braindigit.downloader.DownloadManager;
import com.braindigit.downloader.DownloadStatus;
import com.braindigit.downloader.FileInfo;

import java.io.IOException;
import java.text.ParseException;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public abstract class DownloadType {
    final FileInfo fileInfo;
    final DownloadManager downloadManager;
    final DownloadListener downloadListener;

    public DownloadType(FileInfo fileInfo, DownloadManager downloadManager, DownloadListener downloadListener) {
        this.fileInfo = fileInfo;
        this.downloadManager = downloadManager;
        this.downloadListener = downloadListener;
    }

    public abstract void prepareDownload() throws IOException, ParseException;

    public abstract DownloadStatus startDownload() throws IOException;
}
