package com.braindigit.downloader;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.ParseException;

import static com.braindigit.downloader.Utils.writeLastModify;

/**
 * Braindigit
 * Created on 11/16/16.
 */
class DockerNormal extends Docker {

    DockerNormal(DownloadRequest downloadRequest, Downloader downloader,
                 Handler mainThreadHandler, FileInfo.Destination destination,
                 Dispatcher dispatcher, NetworkHelper networkHelper) {
        super(downloadRequest, downloader, mainThreadHandler, destination, dispatcher, networkHelper);
    }

    @Override
    void prepareDownload() throws IOException, ParseException {
        writeLastModify(new File(destination.lastModifiedPath), downloadRequest.getFileInfo().getLastModify());
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(new File(destination.filePath), "rws");
            file.setLength(downloadRequest.getFileInfo().getFileLength());
        } finally {
            Utils.close(file);
        }
    }

    @Override
    void startDownload() throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            try {
                int readLen;
                int downloadSize = 0;
                byte[] buffer = new byte[8192];

                NetworkHelper.Response response =
                        networkHelper.download(null, downloadRequest.fileInfo.getUrl());

                DownloadStatus status = new DownloadStatus();
                inputStream = response.inputStream;
                outputStream = new FileOutputStream(new File(destination.filePath));

                long contentLength = response.contentLength;
                boolean isChunked = !TextUtils.isEmpty(response.transferEncoding);
                if (isChunked || contentLength == -1) {
                    status.isChunked = true;
                }
                status.setTotalSize(contentLength);

                while ((readLen = inputStream.read(buffer)) != -1) {
                    if (downloadRequest.isCancelled()) {
                        return;
                    }
                    outputStream.write(buffer, 0, readLen);
                    downloadSize += readLen;
                    status.setDownloadSize(downloadSize);
                    downloadRequest.onProgress(status);
                }
                outputStream.flush(); // This is important!!!
                downloadRequest.onComplete();
                Log.i("DockerNormal", "Normal download completed!");
            } finally {
                Utils.close(inputStream);
                Utils.close(outputStream);
            }
        } catch (IOException e) {
            downloadRequest.onError(e);
        }
    }

    @Override
    boolean canPause() {
        return false;
    }
}
