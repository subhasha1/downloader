package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/11/16.
 */

interface PriorityRunnable extends Runnable{
    enum PRIORITY {
        LOW,
        HIGH,
        NORMAL
    }

    int getPriority();
}
