package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public interface DownloadListener {
    void onProgress(DownloadStatus status);

    void onComplete();

    void onError(Exception e);
}
