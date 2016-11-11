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
 * Created on 11/9/16.
 */

public abstract class DownloadType {
    final DownloadAction downloadAction;
    final Downloader downloader;
    final Handler mainThreadHandler;
    final FileInfo.Destination destination;
    final Dispatcher dispatcher;

    public DownloadType(Downloader downloader, Handler mainThreadHandler,
                        FileInfo.Destination destination, DownloadAction downloadAction,
                        Dispatcher dispatcher) {
        this.downloader = downloader;
        this.mainThreadHandler = mainThreadHandler;
        this.destination = destination;
        this.downloadAction = downloadAction;
        this.dispatcher = dispatcher;
    }

    public abstract void prepareDownload() throws IOException, ParseException;

    public abstract void startDownload() throws IOException;
}
