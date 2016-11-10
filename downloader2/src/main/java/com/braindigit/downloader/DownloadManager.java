package com.braindigit.downloader;

import android.util.Log;

import com.braindigit.downloader.network.Header;
import com.braindigit.downloader.network.RangeSupport;
import com.braindigit.downloader.types.DownloadType;
import com.braindigit.downloader.types.DownloadTypeAlreadyDownloaded;
import com.braindigit.downloader.types.DownloadTypeContinue;
import com.braindigit.downloader.types.DownloadTypeMulti;
import com.braindigit.downloader.types.DownloadTypeNormal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.HashMap;

import static android.content.ContentValues.TAG;
import static android.text.TextUtils.concat;
import static java.io.File.separator;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class DownloadManager {
    private static final int EACH_RECORD_SIZE = 16; //long + long = 8 + 8
    private int RECORD_FILE_TOTAL_SIZE;
    //|*********************|
    //|*****Record  File****|
    //|*********************|
    //|  0L      |     7L   | 0
    //|  8L      |     15L  | 1
    //|  16L     |     31L  | 2
    //|  ...     |     ...  | MAX_THREADS-1
    //|*********************|
    private int MAX_THREADS = 3;

    private final HashMap<String, DownloadAction> urlToActions;

    public DownloadManager() {
        urlToActions = new HashMap<>();
        RECORD_FILE_TOTAL_SIZE = MAX_THREADS * EACH_RECORD_SIZE;
    }

    public void enque(FileInfo info, DownloadListener listener) {
        if (urlToActions.containsKey(info.getUrl())) {
            return;
        }

        addToCurrentDownloads(info);
        DownloadType downloadType = getDownloadType(info, listener);
        try {
            downloadType.prepareDownload();
            downloadType.startDownload();
            currentDownloadTasks.remove(info.getUrl());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private DownloadType getDownloadType(FileInfo info, DownloadListener listener) {
        if (getFileBy(info.getUrl()).exists()) {
            return getWhenFileExists(info, listener);
        } else {
            return getWhenFileNotExists(info, listener);
        }
    }

    private void addToCurrentDownloads(FileInfo fileInfo) {
        String cachePath = concat(fileInfo.getSavePath().getPath(), separator, CACHE).toString();
        File file = new File(fileInfo.getSavePath().getPath());
        File cache = new File(cachePath);
        file.mkdir();
        cache.mkdir();

        String filePath = concat(fileInfo.getSavePath().getPath(), separator, fileInfo.getFileName()).toString();
        String tempPath = concat(cachePath, separator, fileInfo.getFileName(), TMP_SUFFIX).toString();
        String lmfPath = concat(cachePath, separator, fileInfo.getFileName(), LMF_SUFFIX).toString();

        currentDownloadTasks.put(fileInfo.getUrl(), new String[]{filePath, tempPath, lmfPath});
    }

    public File getFileBy(String url) {
        return new File(currentDownloadTasks.get(url)[0]);
    }


    public File getTempFileBy(String url) {
        return new File(currentDownloadTasks.get(url)[1]);
    }

    public File getLastModifyFileBy(String url) {
        return new File(currentDownloadTasks.get(url)[2]);
    }

    boolean tempFileNotExists(String url) {
        return !getTempFileBy(url).exists();
    }

    boolean tempFileDamaged(String url, long fileLength) throws IOException {
        RandomAccessFile record = null;
        FileChannel channel = null;
        try {
            record = new RandomAccessFile(getTempFileBy(url), "rws");
            channel = record.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);
            long recordTotalSize = buffer.getLong(RECORD_FILE_TOTAL_SIZE - 8) + 1;
            return recordTotalSize != fileLength;
        } finally {
            Utils.close(channel);
            Utils.close(record);
        }
    }

    boolean downloadNotComplete(String url) throws IOException {
        RandomAccessFile record = null;
        FileChannel channel = null;
        try {
            record = new RandomAccessFile(getTempFileBy(url), "rws");
            channel = record.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);
            long startByte;
            long endByte;
            for (int i = 0; i < MAX_THREADS; i++) {
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


    private DownloadType getWhenFileNotExists(FileInfo fileInfo, DownloadListener listener) {
        Header header = new RangeSupport().supportsRange(fileInfo.getUrl());
        if (Utils.supportsRange(header)) {
            return new DownloadTypeMulti(fileInfo, this, listener);
        } else {
            return new DownloadTypeNormal(fileInfo, this, listener);
        }
    }

    String getLastModified(String url) throws IOException {
        RandomAccessFile record = null;
        try {
            record = new RandomAccessFile(getLastModifyFileBy(url), "rws");
            record.seek(0);
            return Utils.longToGMT(record.readLong());
        } finally {
            Utils.close(record);
        }
    }


    private DownloadType getWhenFileExists(FileInfo info, DownloadListener listener) {
        try {
            Header header = new RangeSupport().supportHeaderWithIfRange(info.getUrl(),
                    getLastModified(info.getUrl()));
            if (header.getResponseCode() == 206) {
                return getWhen206(header, info, listener);
            } else {
                return getWhen200(header, info, listener);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DownloadType getWhen200(Header resp, FileInfo info, DownloadListener listener) {
        if (Utils.supportsRange(resp)) {
            info.setLastModify(resp.getLastModified());
            info.setFileLength(resp.getContentLength());
            return new DownloadTypeMulti(info, this, listener);
        } else {
            info.setLastModify(resp.getLastModified());
            info.setFileLength(resp.getContentLength());
            return new DownloadTypeNormal(info, this, listener);
        }
    }

    private DownloadType getWhen206(Header respHeader, FileInfo fileInfo, DownloadListener listener) {
        if (Utils.supportsRange(respHeader)) {
            return getWhenSupportRange(respHeader, fileInfo, listener);
        } else {
            return getWhenNotSupportRange(respHeader, fileInfo, listener);
        }
    }

    private DownloadType getWhenSupportRange(Header resp, FileInfo fileInfo, DownloadListener listener) {
        fileInfo.setFileLength(resp.getContentLength());
        try {
            if (tempFileNotExists(fileInfo.getUrl()) || tempFileDamaged(fileInfo.getUrl(), fileInfo.getFileLength())) {
                return new DownloadTypeMulti(fileInfo, this, listener);
            }
            if (downloadNotComplete(fileInfo.getUrl())) {
                return new DownloadTypeContinue(fileInfo, this, listener);
            }
        } catch (IOException e) {
            Log.w(TAG, "download record file may be damaged,so we will re download");
            return new DownloadTypeMulti(fileInfo, this, listener);
        }
        return new DownloadTypeAlreadyDownloaded(fileInfo, this, listener);
    }

    private DownloadType getWhenNotSupportRange(Header resp, FileInfo fileInfo, DownloadListener listener) {
        if (getFileBy(fileInfo.getUrl()).length() == resp.getContentLength()) {
            return new DownloadTypeAlreadyDownloaded(fileInfo, this, listener);
        } else {
            fileInfo.setLastModify(resp.getLastModified());
            fileInfo.setFileLength(resp.getContentLength());
            return new DownloadTypeNormal(fileInfo, this, listener);
        }
    }

}
