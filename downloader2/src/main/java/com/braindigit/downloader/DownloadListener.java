package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public abstract class DownloadListener {

    public void onPauseStateIdentified(boolean pausable){

    }

    public void onCancelled() {
//    Implement if needed, not abstracted to simplify the class
    }

    public abstract void onProgress(DownloadStatus status);

    public abstract void onComplete();

    public abstract void onError(Exception e);


}
