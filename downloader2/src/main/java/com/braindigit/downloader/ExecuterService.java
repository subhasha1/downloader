package com.braindigit.downloader;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Braindigit
 * Created on 11/10/16.
 */

public class ExecuterService extends ThreadPoolExecutor {
    private static final int DEFAULT_THREAD_COUNT = 3;

    public ExecuterService() {
        super(DEFAULT_THREAD_COUNT, DEFAULT_THREAD_COUNT, 0, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(), new Utils.DownloadThreadFactory());
    }
}
