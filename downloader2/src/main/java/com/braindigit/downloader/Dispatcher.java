package com.braindigit.downloader;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Braindigit
 * Created on 11/10/16.
 */

final class Dispatcher {
    private static final String DISPATCHER_THREAD_NAME = "Dispatcher";

    static final int REQUEST_SUBMIT = 1;
    static final int REQUEST_CANCEL = 2;
    static final int REQUEST_CHUNK_SUBMIT = 3;

    final DispatcherThread dispatcherThread;
    final ExecutorService service;
    final DispatcherHandler handler;
    private final Handler mainThreadHandler;
    private final Downloader downloader;
    final NetworkHelper networkHelper;

    Dispatcher(Downloader downloader, ExecutorService service, Handler mainThreadHandler, NetworkHelper networkHelper) {
        this.downloader = downloader;
        this.networkHelper = networkHelper;
        this.dispatcherThread = new DispatcherThread();
        this.dispatcherThread.start();
        this.service = service;
        this.mainThreadHandler = mainThreadHandler;
        this.handler = new DispatcherHandler(dispatcherThread.getLooper(), this);
    }

    void dispatchSubmit(DownloadRequest downloadRequest) {
        handler.sendMessage(handler.obtainMessage(REQUEST_SUBMIT, downloadRequest));
    }

    public void dispatchSubmit(ChunkInfo chunkInfo) {
        handler.sendMessage(handler.obtainMessage(REQUEST_CHUNK_SUBMIT, chunkInfo));
    }

    private void performSubmit(DownloadRequest downloadRequest) {
        DownloadHunter downloadHunter = DownloadHunter.from(downloader, mainThreadHandler,
                this, downloadRequest, networkHelper);
        service.submit(downloadHunter);
    }

    private void performSubmit(ChunkInfo chunkInfo) {
        DockerMultiThreadMinion minion =
                new DockerMultiThreadMinion(downloader, chunkInfo.downloadRequest,
                        mainThreadHandler, chunkInfo, networkHelper);
        service.submit(minion);
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
                    DownloadRequest action = (DownloadRequest) msg.obj;
                    dispatcher.performSubmit(action);
                    break;
                }
                case REQUEST_CHUNK_SUBMIT: {
                    ChunkInfo chunkInfo = (ChunkInfo) msg.obj;
                    dispatcher.performSubmit(chunkInfo);
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

    private static class DispatcherThread extends HandlerThread {
        DispatcherThread() {
            super(Utils.THREAD_PREFIX + DISPATCHER_THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
        }
    }
}
