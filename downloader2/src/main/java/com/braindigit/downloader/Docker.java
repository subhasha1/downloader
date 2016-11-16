package com.braindigit.downloader;


import android.os.Handler;

import java.io.IOException;
import java.text.ParseException;

/**
 * Braindigit
 * Created on 11/15/16.
 */

abstract class Docker {
    final DownloadRequest downloadRequest;
    final Downloader downloader;
    final Handler mainThreadHandler;
    final FileInfo.Destination destination;
    final Dispatcher dispatcher;
    final NetworkHelper networkHelper;

    Docker(DownloadRequest downloadRequest, Downloader downloader,
           Handler mainThreadHandler, FileInfo.Destination destination,
           Dispatcher dispatcher, NetworkHelper networkHelper) {
        this.downloadRequest = downloadRequest;
        this.downloader = downloader;
        this.mainThreadHandler = mainThreadHandler;
        this.destination = destination;
        this.dispatcher = dispatcher;
        this.networkHelper = networkHelper;
    }


    abstract void prepareDownload() throws IOException, ParseException;

    abstract void startDownload() throws IOException;

    abstract boolean canPause();
}
