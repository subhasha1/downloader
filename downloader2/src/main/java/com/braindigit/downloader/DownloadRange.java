package com.braindigit.downloader;

 class DownloadRange {
    public long[] start;
    public long[] end;

     DownloadRange(long[] start, long[] end) {
        this.start = start;
        this.end = end;
    }
}
