package com.braindigit.downloader;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.braindigit.downloader.network.Header;
import com.braindigit.downloader.network.RangeSupport;
import com.braindigit.downloader.types.DownloadType;
import com.braindigit.downloader.types.DownloadTypeCompleted;
import com.braindigit.downloader.types.DownloadTypeMultiThread;
import com.braindigit.downloader.types.DownloadTypeNormal;
import com.braindigit.downloader.types.DownloadTypeResumable;

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

public class DownloadRunnable implements PriorityRunnable {
    private static final String TAG = "DownloadRunnable";
    private static final String TMP_SUFFIX = ".tmp";  //temp file
    private static final String LMF_SUFFIX = ".lmf";
    private static final String CACHE = ".cache";

    @NonNull
    private final Downloader downloader;
    @NonNull
    private final Handler handlerMainThread;
    private final Dispatcher dispatcher;
    private final DownloadAction downloadAction;

    private final FileInfo.Destination fileDestination;
    private final int RECORD_FILE_TOTAL_SIZE = ExecutorService.DEFAULT_THREAD_COUNT *
            DownloadTypeResumable.EACH_RECORD_SIZE;

    private DownloadRunnable(@NonNull Downloader downloader, @NonNull Handler handlerMainThread,
                             @NonNull Dispatcher dispatcher, @NonNull DownloadAction downloadAction) {
        this.downloader = downloader;
        this.handlerMainThread = handlerMainThread;
        this.dispatcher = dispatcher;
        this.downloadAction = downloadAction;

        String filePath = downloadAction.fileInfo.getSavePath().getPath();
        String cachePath = concat(filePath, separator, CACHE).toString();
        new File(filePath).mkdir();
        new File(cachePath).mkdir();
        this.fileDestination = new FileInfo.Destination(
                concat(filePath, separator, downloadAction.fileInfo.getFileName()).toString(),
                concat(cachePath, separator, downloadAction.fileInfo.getFileName(), TMP_SUFFIX).toString(),
                concat(cachePath, separator, downloadAction.fileInfo.getFileName(), LMF_SUFFIX).toString());
    }

    public static DownloadRunnable from(@NonNull Downloader downloader, @NonNull Handler handlerMainThread,
                                        Dispatcher dispatcher, DownloadAction downloadAction) {
        return new DownloadRunnable(downloader, handlerMainThread, dispatcher, downloadAction);
    }

    @Override
    public void run() {
        if (downloadAction.isCancelled())
            return;
        DownloadType downloadType = getDownloadType();
        try {
            downloadType.prepareDownload();
            downloadType.startDownload();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private DownloadType getDownloadType() {
        if (fileExists()) {
            return getWhenFileExists();
        } else {
            return getWhenFileNotExists();
        }
    }

    private DownloadType getWhenFileNotExists() {
        Header header = new RangeSupport()
                .supportsRange(downloadAction.fileInfo.getUrl());
        downloadAction.fileInfo.setFileLength(header.getContentLength());
        downloadAction.fileInfo.setLastModify(header.getLastModified());
        return Utils.supportsRange(header) ?
                new DownloadTypeMultiThread(downloader, handlerMainThread, fileDestination,
                        downloadAction, dispatcher) :
                new DownloadTypeNormal(downloader, handlerMainThread, fileDestination,
                        downloadAction, dispatcher);
    }


    private DownloadType getWhenFileExists() {
        try {
            Header header = new RangeSupport()
                    .supportHeaderWithIfRange(downloadAction.fileInfo.getUrl(),
                            getLastModified(fileDestination.lastModifiedPath));
            if (header.getResponseCode() == 206) {
                return getWhen206(header);
            } else {
                return getWhen200(header);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DownloadType getWhen200(Header resp) {
        downloadAction.fileInfo.setLastModify(resp.getLastModified());
        downloadAction.fileInfo.setFileLength(resp.getContentLength());
        return Utils.supportsRange(resp) ?
                new DownloadTypeMultiThread(downloader, handlerMainThread, fileDestination,
                        downloadAction, dispatcher) :
                new DownloadTypeNormal(downloader, handlerMainThread,
                        fileDestination, downloadAction, dispatcher);
    }

    private DownloadType getWhen206(Header respHeader) {
        if (Utils.supportsRange(respHeader)) {
            return getWhenSupportRange(respHeader);
        } else {
            return getWhenNotSupportRange(respHeader);
        }
    }


    private DownloadType getWhenSupportRange(Header resp) {
        final long contentLength = resp.getContentLength();
        downloadAction.fileInfo.setFileLength(contentLength);
        downloadAction.fileInfo.setLastModify(resp.getLastModified());
        try {
            if (tempFileNotExists() || tempFileDamaged(fileDestination.tempPath,
                    downloadAction.fileInfo.getFileLength())) {
                return new DownloadTypeMultiThread(downloader, handlerMainThread, fileDestination,
                        downloadAction, dispatcher);
            }
            if (downloadNotComplete()) {
                return new DownloadTypeResumable(downloader, handlerMainThread,
                        fileDestination, downloadAction, dispatcher);
            }
        } catch (IOException e) {
            Log.w(TAG, "download record file may be damaged,so we will re download");
            return new DownloadTypeMultiThread(downloader, handlerMainThread, fileDestination,
                    downloadAction, dispatcher);
        }
        return new DownloadTypeCompleted(downloader, handlerMainThread, fileDestination,
                downloadAction, dispatcher);
    }

    private DownloadType getWhenNotSupportRange(Header resp) {
        final long contentLength = resp.getContentLength();
        downloadAction.fileInfo.setFileLength(contentLength);
        downloadAction.fileInfo.setLastModify(resp.getLastModified());
        return new File(fileDestination.filePath).length() == contentLength ?
                new DownloadTypeCompleted(downloader, handlerMainThread,
                        fileDestination, downloadAction, dispatcher) :
                new DownloadTypeNormal(downloader, handlerMainThread,
                        fileDestination, downloadAction, dispatcher);
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
