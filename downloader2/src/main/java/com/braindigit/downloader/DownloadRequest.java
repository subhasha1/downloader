package com.braindigit.downloader;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Braindigit
 * Created on 11/10/16.
 */
public final class DownloadRequest {
    private static final int PAUSABLE_UNKNOWN = -1;
    private static final int PAUSABLE = 1;
    private static final int NOT_PAUSABLE = 0;

    final Handler mainThreadHandler;
    final FileInfo fileInfo;
    final Downloader downloader;
    private final List<DownloadListener> downloadListeners;
    private boolean isCancelled;
    private int pauseState = PAUSABLE_UNKNOWN;

    DownloadRequest(Handler mainThreadHandler,
                    FileInfo fileInfo,
                    Downloader downloader) {
        this.mainThreadHandler = mainThreadHandler;
        this.fileInfo = fileInfo;
        this.downloader = downloader;
        this.downloadListeners = new ArrayList<>(2);
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void cancel() {
        isCancelled = true;
        if (!downloadListeners.isEmpty()) {
            for (DownloadListener listener : downloadListeners) {
                listener.onCancelled();
            }
            downloadListeners.clear();
        }
    }

    public void addListener(DownloadListener downloadListener) {
        this.downloadListeners.add(downloadListener);
        if (pauseState != PAUSABLE_UNKNOWN) {
            downloadListener.onPauseStateIdentified(pauseState == PAUSABLE);
        }
    }

    void canPause(boolean pausable) {
        pauseState = pausable ? PAUSABLE : NOT_PAUSABLE;
        for (DownloadListener listener : downloadListeners) {
            listener.onPauseStateIdentified(pauseState == PAUSABLE);
        }
    }

    public void removeListener(DownloadListener listener) {
        downloadListeners.remove(listener);
    }

    void onProgress(DownloadStatus status) {
        if (!isCancelled()) {
            mainThreadHandler.sendMessage(mainThreadHandler.
                    obtainMessage(Downloader.DOWNLOAD_PROGRESS, new ActionStatus(this, status, null)));
        }
    }

    void onComplete() {
        if (!isCancelled) {
            mainThreadHandler.sendMessage(mainThreadHandler.
                    obtainMessage(Downloader.DOWNLOAD_COMPLETE, new ActionStatus(this, null, null)));
        }
    }

    void onError(Exception e) {
        if (!isCancelled()) {
            mainThreadHandler.sendMessage(mainThreadHandler.
                    obtainMessage(Downloader.DOWNLOAD_FAILED, new ActionStatus(this, null, e)));
        }
    }

    void performProgress(DownloadStatus downloadStatus) {
        if (!isCancelled()) {
            for (DownloadListener listener : downloadListeners) {
                listener.onProgress(downloadStatus);
            }
        }
    }

    void performComplete() {
        if (!isCancelled()) {
            for (DownloadListener listener : downloadListeners) {
                listener.onComplete();
            }
        }
    }

    void performError(Exception e) {
        if (!isCancelled()) {
            for (DownloadListener listener : downloadListeners) {
                listener.onError(e);
            }
        }
    }

    boolean isCancelled() {
        return isCancelled;
    }
}
