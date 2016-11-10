package com.braindigit.downloader;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.braindigit.downloader.types.DownloadType;
import com.braindigit.downloader.types.DownloadTypeNormal;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static android.text.TextUtils.concat;
import static java.io.File.separator;

/**
 * Braindigit
 * Created on 11/10/16.
 */

public class DownloadRunnable implements Runnable {
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
        return new DownloadTypeNormal(downloader, handlerMainThread, fileDestination, downloadAction);
//        if (fileExists()) {
//            return getWhenFileExists();
//        } else {
//            return getWhenFileNotExists();
//        }
    }
//
//    private DownloadType getWhenFileExists() {
//        try {
//            Header header = new RangeSupport().supportHeaderWithIfRange(downloadAction.fileInfo.getUrl(),
//                    getLastModified(lastModifiedPath));
//            if (header.getResponseCode() == 206) {
//                return getWhen206(header);
//            } else {
//                return getWhen200(header);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private DownloadType getWhen200(Header resp) {
//        downloadAction.fileInfo.setLastModify(resp.getLastModified());
//        downloadAction.fileInfo.setFileLength(resp.getContentLength());
//        return Utils.supportsRange(resp) ?
//                new DownloadTypeMulti(info, this, listener) :
//                new DownloadTypeNormal();
//    }
//
//    private DownloadType getWhen206(Header respHeader, FileInfo fileInfo, DownloadListener listener) {
//        if (Utils.supportsRange(respHeader)) {
//            return getWhenSupportRange(respHeader, fileInfo, listener);
//        } else {
//            return getWhenNotSupportRange(respHeader, fileInfo, listener);
//        }
//    }
//
//    private boolean fileExists() {
//        return new File(filePath).exists();
//    }
//
//    private String getLastModified(String url) throws IOException {
//        RandomAccessFile record = null;
//        try {
//            record = new RandomAccessFile(getLastModifyFileBy(url), "rws");
//            record.seek(0);
//            return Utils.longToGMT(record.readLong());
//        } finally {
//            Utils.close(record);
//        }
//    }
}
