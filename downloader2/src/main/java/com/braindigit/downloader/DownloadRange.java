package com.braindigit.downloader;

public class DownloadRange {
    public long[] start;
    public long[] end;

    public DownloadRange(long[] start, long[] end) {
        this.start = start;
        this.end = end;
    }
}
