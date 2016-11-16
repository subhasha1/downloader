package com.braindigit.downloader;

import java.io.File;

/**
 * Braindigit
 * Created on 11/16/16.
 */
public class DownloadRequestCreator {
    private final Downloader downloader;
    private final String url;
    private File savePath;
    private String fileName;
    private DownloadListener downloadListener;

    DownloadRequestCreator(Downloader downloader, String url, File defaultPath) {
        this.downloader = downloader;
        this.url = url;
        this.savePath = defaultPath;
    }

    public DownloadRequestCreator visible(boolean visible) {
        return this;
    }

    public DownloadRequestCreator path(File filePath) {
        this.savePath = filePath;
        return this;
    }

    public DownloadRequestCreator fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public DownloadRequestCreator listener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    public DownloadRequest download() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUrl(url);
        fileInfo.setSavePath(savePath);
        fileInfo.setFileName(fileName);
        DownloadRequest downloadRequest = new DownloadRequest(Downloader.HANDLER, fileInfo,
                downloader);
        if (downloadListener != null)
            downloadRequest.addListener(downloadListener);
        downloadRequest.downloader.enqueue(downloadRequest);
        return downloadRequest;
    }

}
