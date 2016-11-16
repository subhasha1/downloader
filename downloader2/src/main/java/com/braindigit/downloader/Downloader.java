package com.braindigit.downloader;

import android.os.Environment;
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
    final Map<String, DownloadHunter> downloaderMap;


    static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_PROGRESS:
                    ActionStatus as = (ActionStatus) msg.obj;
                    as.downloadRequest.performProgress(as.downloadStatus);
                    break;
                case DOWNLOAD_COMPLETE:
                    ActionStatus as1 = (ActionStatus) msg.obj;
                    as1.downloadRequest.performComplete();
                    break;
                case DOWNLOAD_FAILED:
                    ActionStatus as2 = (ActionStatus) msg.obj;
                    as2.downloadRequest.performError(as2.e);
                    break;
                default:
                    throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    };

    private Downloader() {
        this.downloaderMap = new HashMap<>();
        this.executorService = new ExecutorService();
        NetworkHelper networkHelper = Utils.createDefaultNetworkHelper();
        this.dispatcher = new Dispatcher(this, executorService, HANDLER, networkHelper);
    }

    void enqueue(DownloadRequest downloadRequest) {
        dispatcher.dispatchSubmit(downloadRequest);
    }

    public static DownloadRequestCreator from(String url) {
        if (downloader == null) {
            downloader = new Downloader();
        }
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new DownloadRequestCreator(downloader, url, path);
    }
}
