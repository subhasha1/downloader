package com.braindigit.downloader;

/**
 * Braindigit
 * Created on 11/11/16.
 */

 class ChunkInfo {
    final FileInfo fileInfo;
    final FileInfo.Destination destination;
    final long start;
    final long end;
    final int rangeIndex;
    final DownloadRequest downloadRequest;

     ChunkInfo(FileInfo fileInfo,
                     FileInfo.Destination destination,
                     long start, long end, int rangeIndex, DownloadRequest downloadRequest) {
        this.fileInfo = fileInfo;
        this.destination = destination;
        this.start = start;
        this.end = end;
        this.rangeIndex = rangeIndex;
        this.downloadRequest = downloadRequest;
    }
}
