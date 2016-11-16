package com.braindigit.downloader;

import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * Braindigit
 * Created on 11/16/16.
 */

class DockerResumable extends Docker{
    public static final int EACH_RECORD_SIZE = 16;

    final int RECORD_FILE_TOTAL_SIZE;
    final int MAX_THREADS = ExecutorService.DEFAULT_THREAD_COUNT;
    DockerResumable(DownloadRequest downloadRequest, Downloader downloader,
                    Handler mainThreadHandler, FileInfo.Destination destination,
                    Dispatcher dispatcher, NetworkHelper networkHelper) {
        super(downloadRequest, downloader, mainThreadHandler, destination,
                dispatcher, networkHelper);
        RECORD_FILE_TOTAL_SIZE = EACH_RECORD_SIZE * MAX_THREADS;
    }

    @Override
    void prepareDownload() throws IOException, ParseException {

    }

    @Override
    void startDownload() throws IOException {
        DownloadRange range = getDownloadRange();
        for (int i = 0; i < MAX_THREADS; i++) {
            if (range.start[i] <= range.end[i]) {
                dispatcher.dispatchSubmit(new ChunkInfo(downloadRequest.getFileInfo(),
                        destination, range.start[i],range.end[i], i, downloadRequest));
            }
        }
    }

    @Override
    boolean canPause() {
        return true;
    }

    private DownloadRange getDownloadRange() throws IOException {
        RandomAccessFile record = null;
        FileChannel channel = null;
        try {
            record = new RandomAccessFile(new File(destination.tempPath), "rws");
            channel = record.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);
            long[] startByteArray = new long[ExecutorService.DEFAULT_THREAD_COUNT];
            long[] endByteArray = new long[ExecutorService.DEFAULT_THREAD_COUNT];
            for (int i = 0; i < ExecutorService.DEFAULT_THREAD_COUNT; i++) {
                startByteArray[i] = buffer.getLong();
                endByteArray[i] = buffer.getLong();
            }
            return new DownloadRange(startByteArray, endByteArray);
        } finally {
            Utils.close(channel);
            Utils.close(record);
        }
    }
}
