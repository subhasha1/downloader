package com.braindigit.downloader;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.util.LinkedHashMap;
import java.util.Map;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Braindigit
 * Created on 11/10/16.
 */

public class Dispatcher {
    private static final String DISPATCHER_THREAD_NAME = "Dispatcher";

    static final int REQUEST_SUBMIT = 1;
    static final int REQUEST_CANCEL = 2;

    final DispatcherThread dispatcherThread;
    final ExecutorService service;
    final DispatcherHandler handler;
    private final Handler mainThreadHandler;
    private final Downloader downloader;

    public Dispatcher(Downloader downloader, ExecutorService service, Handler mainThreadHandler) {
        this.downloader = downloader;
        this.dispatcherThread = new DispatcherThread();
        this.dispatcherThread.start();
        this.service = service;
        this.mainThreadHandler = mainThreadHandler;
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
    }

    public void dispatchSubmit(DownloadAction downloadAction) {
        handler.sendMessage(handler.obtainMessage(REQUEST_SUBMIT, downloadAction));
    }

    public void enqueueChunk(ChunkInfo chunkInfo) {
        ChunkDownloadRunnable chunkDownloadRunnable =
                new ChunkDownloadRunnable(downloader, chunkInfo.downloadAction,
                        mainThreadHandler, chunkInfo);
        service.submit(chunkDownloadRunnable);
    }

    public void performSubmit(DownloadAction downloadAction) {
        DownloadRunnable downloadRunnable = DownloadRunnable.from(downloader, mainThreadHandler,
                this, downloadAction);
        service.submit(downloadRunnable);
    }

    private static class DispatcherHandler extends Handler {
        private final Dispatcher dispatcher;

        public DispatcherHandler(Looper looper, Dispatcher dispatcher) {
            super(looper);
            this.dispatcher = dispatcher;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case REQUEST_SUBMIT: {
                    DownloadAction action = (DownloadAction) msg.obj;
                    dispatcher.performSubmit(action);
                    break;
                }
                default:
                    Downloader.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            throw new AssertionError("Unknown handler message received: " + msg.what);
                        }
                    });
            }
        }
    }

    static class DispatcherThread extends HandlerThread {
        DispatcherThread() {
            super(Utils.THREAD_PREFIX + DISPATCHER_THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
        }
    }
}
