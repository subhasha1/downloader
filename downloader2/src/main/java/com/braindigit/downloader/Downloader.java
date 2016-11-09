package com.braindigit.downloader;

import java.io.File;
import java.util.HashMap;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class Downloader {

    private static volatile Downloader downloader;

    private final DownloadManager downloadManager;


    private Downloader() {
        downloadManager = new DownloadManager();
    }

    public static Downloader.From from(String url) {
        if (downloader == null) {
            downloader = new Downloader();
        }
        return new From(downloader, url);
    }

    static class From {
        private final Downloader downloader;
        private final String url;

        private From(Downloader downloader, String url) {
            this.downloader = downloader;
            this.url = url;
        }

        public Downloader.Into into(File file) {
            return new Into(downloader, url, file);
        }
    }

    static class Into {
        private final Downloader downloader;
        private final String url;
        private final File savePath;

        private Into(Downloader downloader, String url, File savePath) {
            this.downloader = downloader;
            this.url = url;
            this.savePath = savePath;
        }

        void start() {
            start(new DownloadListener() {
                @Override
                public void onProgress(DownloadStatus status) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void onError(Exception e) {

                }
            });
        }

        void start(DownloadListener listener) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setUrl(url);
            fileInfo.setSavePath(savePath);
            downloader.downloadManager.enque(fileInfo, listener);
        }
    }
}
