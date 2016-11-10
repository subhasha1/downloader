package com.braindigit.downloader;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class Downloader {
    public static final int DOWNLOAD_PROGRESS = 1;
    public static final int DOWNLOAD_COMPLETE = 2;
    public static final int DOWNLOAD_FAILED = 3;

    private static volatile Downloader downloader;

    private final ExecuterService executerService;
    private final Dispatcher dispatcher;

    static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_PROGRESS:
                    System.out.print("DP");
                    break;
                case DOWNLOAD_COMPLETE:
                    System.out.print("DC");
                    break;
                default:
                    throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    };

    private Downloader() {
        this.executerService = new ExecuterService();
        this.dispatcher = new Dispatcher(this, executerService, HANDLER);
    }

    private void enque(DownloadAction downloadAction) {
        dispatcher.dispatchSubmit(downloadAction);
    }

    public static Downloader.From from(String url) {
        if (downloader == null) {
            downloader = new Downloader();
        }
        return new From(downloader, url);
    }

    public static class From {
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

    public static class Into {
        private final Downloader downloader;
        private final String url;
        private final File savePath;

        private Into(Downloader downloader, String url, File savePath) {
            this.downloader = downloader;
            this.url = url;
            this.savePath = savePath;
        }

        public void start() {
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

        public void start(DownloadListener listener) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setUrl(url);
            fileInfo.setSavePath(savePath);
            fileInfo.setFileName("TEST");
            DownloadAction action = new DownloadAction(fileInfo, listener);
            downloader.enque(action);
        }
    }
}
