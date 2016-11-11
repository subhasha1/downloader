package com.braindigit.downloader;

import android.os.Handler;

/**
 * Braindigit
 * Created on 11/10/16.
 */
public final class DownloadAction implements DownloadListener {
    final Handler mainThreadHandler;
    final FileInfo fileInfo;
    DownloadListener downloadListener;
    private boolean isCancelled;

    public DownloadAction(Handler mainThreadHandler, FileInfo fileInfo, DownloadListener listener) {
        this.mainThreadHandler = mainThreadHandler;
        this.fileInfo = fileInfo;
        this.downloadListener = listener;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public DownloadListener getDownloadListener() {
        return downloadListener;
    }

    public void cancel() {
        isCancelled = true;
        if (downloadListener != null)
            downloadListener = null;
    }

    @Override
    public void onProgress(DownloadStatus status) {
        if (!isCancelled()) {
            mainThreadHandler.sendMessage(mainThreadHandler.
                    obtainMessage(Downloader.DOWNLOAD_PROGRESS, new ActionStatus(this, status, null)));
        }
    }

    @Override
    public void onComplete() {
        if (!isCancelled) {
            mainThreadHandler.sendMessage(mainThreadHandler.
                    obtainMessage(Downloader.DOWNLOAD_COMPLETE, new ActionStatus(this, null, null)));
        }
    }

    @Override
    public void onError(Exception e) {
        if (!isCancelled()) {
            mainThreadHandler.sendMessage(mainThreadHandler.
                    obtainMessage(Downloader.DOWNLOAD_FAILED, new ActionStatus(this, null, e)));
        }
    }

    void performProgress(DownloadStatus downloadStatus) {
        if (!isCancelled())
            downloadListener.onProgress(downloadStatus);
    }

    void performComplete() {
        if (!isCancelled)
            downloadListener.onComplete();
    }

    void performError(Exception e) {
        if (!isCancelled)
            downloadListener.onError(e);
    }

    public boolean isCancelled() {
        return isCancelled;
    }

}
