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

public class DownloadTypeContinue extends DownloadType {
    public DownloadTypeContinue(FileInfo fileInfo, DownloadManager downloadManager, DownloadListener downloadListener) {
        super(fileInfo, downloadManager, downloadListener);
    }

    @Override
    public void prepareDownload() throws IOException, ParseException {

    }

    @Override
    public DownloadStatus startDownload() throws IOException {
        return null;
    }
}
