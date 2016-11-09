package com.braindigit.downloader.types;

import com.braindigit.downloader.DownloadListener;
import com.braindigit.downloader.DownloadManager;
import com.braindigit.downloader.DownloadStatus;
import com.braindigit.downloader.FileInfo;
import com.braindigit.downloader.Utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;

import static com.braindigit.downloader.Utils.writeLastModify;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class DownloadTypeNormal extends DownloadType {

    public DownloadTypeNormal(FileInfo fileInfo, DownloadManager downloadManager, DownloadListener downloadListener) {
        super(fileInfo, downloadManager, downloadListener);
    }

    @Override
    public void prepareDownload() throws IOException, ParseException {
        writeLastModify(downloadManager.getLastModifyFileBy(fileInfo.getUrl()), fileInfo.getLastModify());
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(downloadManager.getFileBy(fileInfo.getUrl()), "rws");
            file.setLength(fileInfo.getFileLength());
        } finally {
            Utils.close(file);
        }
    }

    @Override
    public DownloadStatus startDownload() throws IOException {
        return null;
    }
}
