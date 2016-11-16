package com.braindigit.downloader;

import android.os.Handler;

import java.io.IOException;
import java.text.ParseException;

/**
 * Braindigit
 * Created on 11/16/16.
 */

 class DockerCompleted extends Docker {

    DockerCompleted(DownloadRequest downloadRequest, Downloader downloader,
                    Handler mainThreadHandler, FileInfo.Destination destination,
                    Dispatcher dispatcher, NetworkHelper networkHelper) {
        super(downloadRequest, downloader, mainThreadHandler,
                destination, dispatcher, networkHelper);
    }

    @Override
    void prepareDownload() throws IOException, ParseException {

    }

    @Override
    void startDownload() throws IOException {
        if (downloadRequest.isCancelled())
            return;
        downloadRequest.onComplete();
    }

    @Override
    boolean canPause() {
        return false;
    }
}
