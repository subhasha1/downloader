package com.braindigit.downloader;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Braindigit
 * Created on 11/10/16.
 */

public class ExecutorService extends ThreadPoolExecutor {
    public static final int DEFAULT_THREAD_COUNT = 3;

    public ExecutorService() {
        super(DEFAULT_THREAD_COUNT, DEFAULT_THREAD_COUNT, 0, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(), new Utils.DownloadThreadFactory());
    }

    @Override
    public Future<?> submit(Runnable task) {
        DownloadFutureTask futureTask = new DownloadFutureTask((PriorityRunnable) task);
        execute(futureTask);
        return futureTask;
    }


    private static final class DownloadFutureTask extends FutureTask<PriorityRunnable>
            implements Comparable<DownloadFutureTask> {
        private final PriorityRunnable runnable;

        public DownloadFutureTask(PriorityRunnable runnable) {
            super(runnable, null);
            this.runnable = runnable;
        }

        @Override
        public int compareTo(DownloadFutureTask downloadFutureTask) {
            return downloadFutureTask.runnable.getPriority() - this.runnable.getPriority();
        }
    }
}
