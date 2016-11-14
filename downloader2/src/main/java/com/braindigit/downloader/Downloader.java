package com.braindigit.downloader;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class Downloader {
    public static final int DOWNLOAD_PROGRESS = 1;
    public static final int DOWNLOAD_COMPLETE = 2;
    public static final int DOWNLOAD_FAILED = 3;

    private static volatile Downloader downloader;

    private final ExecutorService executorService;
    final Dispatcher dispatcher;
    final Map<String, DownloadRunnable> downloaderMap;


    static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_PROGRESS:
                    ActionStatus as = (ActionStatus) msg.obj;
                    as.downloadAction.performProgress(as.downloadStatus);
                    break;
                case DOWNLOAD_COMPLETE:
                    ActionStatus as1 = (ActionStatus) msg.obj;
                    as1.downloadAction.performComplete();
                    break;
                case DOWNLOAD_FAILED:
                    ActionStatus as2 = (ActionStatus) msg.obj;
                    as2.downloadAction.performError(as2.e);
                    break;
                default:
                    throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    };

    private Downloader() {
        this.downloaderMap = new HashMap<>();
        this.executorService = new ExecutorService();
        this.dispatcher = new Dispatcher(this, executorService, HANDLER);
    }

    private void enqueue(DownloadAction downloadAction) {
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

        public DownloadAction start(DownloadListener listener) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setUrl(url);
            fileInfo.setSavePath(savePath);
            fileInfo.setFileName("asster.jpg");
            DownloadAction action = new DownloadAction(HANDLER, fileInfo, listener);
            downloader.enqueue(action);
            return action;
        }
    }
}
