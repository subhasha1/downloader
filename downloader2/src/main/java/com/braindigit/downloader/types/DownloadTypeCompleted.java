package com.braindigit.downloader.types;

import android.os.Handler;

import com.braindigit.downloader.Dispatcher;
import com.braindigit.downloader.DownloadAction;
import com.braindigit.downloader.DownloadStatus;
import com.braindigit.downloader.Downloader;
import com.braindigit.downloader.FileInfo;

import java.io.IOException;
import java.text.ParseException;

/**
 * Braindigit
 * Created on 11/11/16.
 */

public class DownloadTypeCompleted extends DownloadType {

    public DownloadTypeCompleted(Downloader downloader, Handler mainThreadHandler,
                                 FileInfo.Destination destination, DownloadAction downloadAction,
                                 Dispatcher dispatcher) {
        super(downloader, mainThreadHandler, destination, downloadAction, dispatcher);
    }

    @Override
    public void prepareDownload() throws IOException, ParseException {

    }

    @Override
    public void startDownload() throws IOException {
        if(downloadAction.isCancelled())
            return;
        downloadAction.onComplete();
    }
}
