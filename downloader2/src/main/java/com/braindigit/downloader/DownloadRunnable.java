package com.braindigit.downloader;

import com.braindigit.downloader.network.Header;
import com.braindigit.downloader.network.RangeSupport;
import com.braindigit.downloader.types.DownloadType;
import com.braindigit.downloader.types.DownloadTypeMulti;
import com.braindigit.downloader.types.DownloadTypeNormal;

import java.io.File;
import java.io.IOException;

import static android.text.TextUtils.concat;
import static java.io.File.separator;

/**
 * Braindigit
 * Created on 11/10/16.
 */

public class DownloadRunnable implements Runnable {
    public static final String TEST_RANGE_SUPPORT = "bytes=0-";

    private static final String TMP_SUFFIX = ".tmp";  //temp file
    private static final String LMF_SUFFIX = ".lmf";
    private static final String CACHE = ".cache";

    private final DownloadAction downloadAction;

    private final String filePath;
    private final String tempPath;
    private final String lmfPath;


    private DownloadRunnable(DownloadAction downloadAction) {
        this.downloadAction = downloadAction;
        String filePath = downloadAction.fileInfo.getSavePath().getPath();
        String cachePath = concat(filePath, separator, CACHE).toString();
        new File(filePath).mkdir();
        new File(cachePath).mkdir();

        this.filePath = concat(filePath, separator, downloadAction.fileInfo.getFileName()).toString();
        this.tempPath = concat(cachePath, separator, downloadAction.fileInfo.getFileName(), TMP_SUFFIX).toString();
        this.lmfPath = concat(cachePath, separator, downloadAction.fileInfo.getFileName(), LMF_SUFFIX).toString();
    }

    public static DownloadRunnable from(DownloadAction downloadAction) {
        return new DownloadRunnable(downloadAction);
    }

    @Override
    public void run() {

    }

    private DownloadType getDownloadType(FileInfo info, DownloadListener listener) {
        if (fileExists()) {
            return getWhenFileExists(info, listener);
        } else {
            return getWhenFileNotExists(info, listener);
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

    private boolean fileExists() {
        return new File(filePath).exists();
    }



}
