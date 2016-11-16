package com.braindigit.downloader;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;

import static android.text.TextUtils.concat;
import static com.braindigit.downloader.PriorityRunnable.PRIORITY.HIGH;
import static java.io.File.separator;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * Braindigit
 * Created on 11/10/16.
 */

 class DownloadHunter implements PriorityRunnable {
    private static final String TAG = "DownloadHunter";
    private static final String TMP_SUFFIX = ".tmp";  //temp file
    private static final String LMF_SUFFIX = ".lmf";
    private static final String CACHE = ".cache";

    @NonNull
    private final Downloader downloader;
    @NonNull
    private final Handler handlerMainThread;
    @NonNull
    private final Dispatcher dispatcher;
    @NonNull
    private final DownloadRequest downloadRequest;
    @NonNull
    private final NetworkHelper networkHelper;

    private final FileInfo.Destination fileDestination;
    private final int RECORD_FILE_TOTAL_SIZE = ExecutorService.DEFAULT_THREAD_COUNT *
            DockerResumable.EACH_RECORD_SIZE;

    private DownloadHunter(@NonNull Downloader downloader, @NonNull Handler handlerMainThread,
                           @NonNull Dispatcher dispatcher, @NonNull DownloadRequest downloadRequest,
                           @NonNull NetworkHelper networkHelper) {
        this.downloader = downloader;
        this.handlerMainThread = handlerMainThread;
        this.dispatcher = dispatcher;
        this.downloadRequest = downloadRequest;
        this.networkHelper = networkHelper;

        String filePath = downloadRequest.fileInfo.getSavePath().getPath();
        String cachePath = concat(filePath, separator, CACHE).toString();
        new File(filePath).mkdir();
        new File(cachePath).mkdir();
        this.fileDestination = new FileInfo.Destination(
                concat(filePath, separator, downloadRequest.fileInfo.getFileName()).toString(),
                concat(cachePath, separator, downloadRequest.fileInfo.getFileName(), TMP_SUFFIX).toString(),
                concat(cachePath, separator, downloadRequest.fileInfo.getFileName(), LMF_SUFFIX).toString());
    }

    public static DownloadHunter from(@NonNull Downloader downloader, @NonNull Handler handlerMainThread,
                                      Dispatcher dispatcher, DownloadRequest downloadRequest,
                                      NetworkHelper networkHelper) {
        return new DownloadHunter(downloader, handlerMainThread, dispatcher, downloadRequest, networkHelper);
    }

    @Override
    public void run() {
        if (downloadRequest.isCancelled())
            return;
        try {
            Docker docker = prepareDocker();
            downloadRequest.canPause(docker.canPause());
            docker.prepareDownload();
            docker.startDownload();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            downloadRequest.onError(e);
        }
    }

    private Docker prepareDocker() throws IOException {
        return fileExists() ? getWhenFileExists() : getWhenFileNotExists();
    }


    private Docker getWhenFileNotExists() throws IOException {
        NetworkHelper.Header header = networkHelper.getHeader(downloadRequest.fileInfo.getUrl());
        downloadRequest.fileInfo.setFileLength(header.contentLength);
        downloadRequest.fileInfo.setLastModify(header.lastModified);

        return Utils.supportsRange(header) ?
                new DockerMultiThread(downloadRequest, downloader, handlerMainThread,
                        fileDestination, dispatcher, networkHelper) :
                new DockerNormal(downloadRequest, downloader, handlerMainThread,
                        fileDestination, dispatcher, networkHelper);
    }


    private Docker getWhenFileExists() throws IOException {
        NetworkHelper.Header header = networkHelper.getHeader(downloadRequest.fileInfo.getUrl(),
                getLastModified(fileDestination.lastModifiedPath));
        if (header.responseCode == 206) {
            return getWhen206(header);
        } else {
            return getWhen200(header);
        }
    }

    private Docker getWhen200(NetworkHelper.Header resp) {
        downloadRequest.fileInfo.setLastModify(resp.lastModified);
        downloadRequest.fileInfo.setFileLength(resp.contentLength);
        return Utils.supportsRange(resp) ?
                new DockerMultiThread(downloadRequest, downloader, handlerMainThread,
                        fileDestination, dispatcher, networkHelper) :
                new DockerNormal(downloadRequest, downloader, handlerMainThread,
                        fileDestination, dispatcher, networkHelper);
    }

    private Docker getWhen206(NetworkHelper.Header respHeader) {
        if (Utils.supportsRange(respHeader)) {
            return getWhenSupportRange(respHeader);
        } else {
            return getWhenNotSupportRange(respHeader);
        }
    }


    private Docker getWhenSupportRange(NetworkHelper.Header resp) {
        downloadRequest.fileInfo.setFileLength(resp.contentLength);
        downloadRequest.fileInfo.setLastModify(resp.lastModified);
        try {
            if (tempFileNotExists() || tempFileDamaged(fileDestination.tempPath,
                    downloadRequest.fileInfo.getFileLength())) {
                return new DockerMultiThread(downloadRequest, downloader, handlerMainThread,
                        fileDestination, dispatcher, networkHelper);
            }
            if (downloadNotComplete()) {
                return new DockerResumable(downloadRequest, downloader, handlerMainThread,
                        fileDestination, dispatcher, networkHelper);
            }
        } catch (IOException e) {
            Log.w(TAG, "download record file may be damaged,so we will re download");
            return new DockerMultiThread(downloadRequest, downloader, handlerMainThread,
                    fileDestination, dispatcher, networkHelper);
        }
        return new DockerCompleted(downloadRequest, downloader, handlerMainThread,
                fileDestination, dispatcher, networkHelper);
    }

    private Docker getWhenNotSupportRange(NetworkHelper.Header resp) {
        final long contentLength = resp.contentLength;
        downloadRequest.fileInfo.setFileLength(contentLength);
        downloadRequest.fileInfo.setLastModify(resp.lastModified);
        return new File(fileDestination.filePath).length() == contentLength ?
                new DockerCompleted(downloadRequest, downloader, handlerMainThread,
                        fileDestination, dispatcher, networkHelper) :
                new DockerNormal(downloadRequest, downloader, handlerMainThread,
                        fileDestination, dispatcher, networkHelper);
    }

    private boolean fileExists() {
        return new File(fileDestination.filePath).exists();
    }

    private boolean tempFileNotExists() {
        return !(new File(fileDestination.tempPath).exists());
    }

    boolean downloadNotComplete() throws IOException {
        RandomAccessFile record = null;
        FileChannel channel = null;
        try {
            record = new RandomAccessFile(new File(fileDestination.tempPath), "rws");
            channel = record.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);
            long startByte;
            long endByte;
            for (int i = 0; i < ExecutorService.DEFAULT_THREAD_COUNT; i++) {
                startByte = buffer.getLong();
                endByte = buffer.getLong();
                if (startByte <= endByte) {
                    return true;
                }
            }
            return false;
        } finally {
            Utils.close(channel);
            Utils.close(record);
        }
    }


    private boolean tempFileDamaged(String url, long fileLength) throws IOException {
        RandomAccessFile record = null;
        FileChannel channel = null;
        try {
            record = new RandomAccessFile(new File(url), "rws");
            channel = record.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);
            long recordTotalSize = buffer.getLong(RECORD_FILE_TOTAL_SIZE - 8) + 1;
            return recordTotalSize != fileLength;
        } finally {
            Utils.close(channel);
            Utils.close(record);
        }
    }

    private String getLastModified(String url) throws IOException {
        RandomAccessFile record = null;
        try {
            record = new RandomAccessFile(new File(url), "rws");
            record.seek(0);
            return Utils.longToGMT(record.readLong());
        } finally {
            Utils.close(record);
        }
    }

    @Override
    public int getPriority() {
        return HIGH.ordinal();
    }
}
