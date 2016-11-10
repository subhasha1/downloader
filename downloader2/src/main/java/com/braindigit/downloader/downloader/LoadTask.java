package com.braindigit.downloader.downloader;

import com.braindigit.downloader.DownloadStatus;

import java.io.IOException;
import java.text.ParseException;

/**
 * Braindigit
 * Created on 11/10/16.
 */

public abstract class LoadTask {

    public LoadTask(String range, String url) {

    }

    public abstract void prepareDownload() throws IOException, ParseException;

    public abstract DownloadStatus startDownload() throws IOException;
}
