package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/11/16.
 */

public class ChunkInfo {
    final FileInfo fileInfo;
    final FileInfo.Destination destination;
    final long start;
    final long end;
    final int rangeIndex;
    final DownloadAction downloadAction;

    public ChunkInfo(FileInfo fileInfo,
                     FileInfo.Destination destination,
                     long start, long end, int rangeIndex, DownloadAction downloadAction) {
        this.fileInfo = fileInfo;
        this.destination = destination;
        this.start = start;
        this.end = end;
        this.rangeIndex = rangeIndex;
        this.downloadAction = downloadAction;
    }
}
