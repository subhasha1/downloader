package com.braindigit.downloader.types;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.braindigit.downloader.DownloadAction;
import com.braindigit.downloader.DownloadStatus;
import com.braindigit.downloader.Downloader;
import com.braindigit.downloader.FileInfo;
import com.braindigit.downloader.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;

import static com.braindigit.downloader.Utils.writeLastModify;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class DownloadTypeNormal extends DownloadType {

    private static final String TAG = "DOWNLOAD_TYPE_NORMAL";

    public DownloadTypeNormal(Downloader downloader, Handler mainThreadHandler,
                              FileInfo.Destination destination, DownloadAction downloadAction) {
        super(downloader, mainThreadHandler, destination, downloadAction);
    }

    @Override
    public void prepareDownload() throws IOException, ParseException {
        writeLastModify(new File(destination.lastModifiedPath), downloadAction.getFileInfo().getLastModify());
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(new File(destination.filePath), "rws");
            file.setLength(downloadAction.getFileInfo().getFileLength());
        } finally {
            Utils.close(file);
        }
    }

    @Override
    public DownloadStatus startDownload() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(downloadAction.getFileInfo().getUrl()).openConnection();
        connection.setConnectTimeout(Utils.DEFAULT_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(Utils.DEFAULT_READ_TIMEOUT_MILLIS);

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            try {
                int readLen;
                int downloadSize = 0;
                byte[] buffer = new byte[8192];

                DownloadStatus status = new DownloadStatus();
                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(new File(destination.filePath));

                long contentLength = connection.getContentLength();
                boolean isChunked = !TextUtils.isEmpty(Utils.transferEncoding(connection));
                if (isChunked || contentLength == -1) {
                    status.isChunked = true;
                }
                status.setTotalSize(contentLength);

                while ((readLen = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readLen);
                    downloadSize += readLen;
                    status.setDownloadSize(downloadSize);
                    mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(Downloader.DOWNLOAD_PROGRESS, status));
                }
                outputStream.flush(); // This is important!!!
                mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(Downloader.DOWNLOAD_COMPLETE,status));
                Log.i(TAG, "Normal download completed!");
            } finally {
                Utils.close(inputStream);
                Utils.close(outputStream);
            }
        } catch (IOException e) {
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(Downloader.DOWNLOAD_FAILED,
                    new Throwable("Normal download stopped! Failed to save normal file!", e)));
        }
        return null;
    }
}
