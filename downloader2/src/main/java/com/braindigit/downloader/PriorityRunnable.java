package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/11/16.
 */

public interface PriorityRunnable extends Runnable{
    enum PRIORITY {
        LOW,
        HIGH,
        NORMAL
    }

    int getPriority();
}
