package com.braindigit.downloader.types;

import android.os.Handler;

import com.braindigit.downloader.ChunkInfo;
import com.braindigit.downloader.Dispatcher;
import com.braindigit.downloader.DownloadAction;
import com.braindigit.downloader.DownloadRange;
import com.braindigit.downloader.DownloadStatus;
import com.braindigit.downloader.Downloader;
import com.braindigit.downloader.ExecutorService;
import com.braindigit.downloader.FileInfo;
import com.braindigit.downloader.Utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * Braindigit
 * Created on 11/11/16.
 */

public class DownloadTypeResumable extends DownloadType {
    public static final int EACH_RECORD_SIZE = 16;

    final int RECORD_FILE_TOTAL_SIZE;
    final int MAX_THREADS = ExecutorService.DEFAULT_THREAD_COUNT;

    public DownloadTypeResumable(Downloader downloader, Handler mainThreadHandler,
                                 FileInfo.Destination destination, DownloadAction downloadAction,
                                 Dispatcher dispatcher) {
        super(downloader, mainThreadHandler, destination, downloadAction,dispatcher);
        RECORD_FILE_TOTAL_SIZE = EACH_RECORD_SIZE * MAX_THREADS;
    }

    @Override
    public void prepareDownload() throws IOException, ParseException {

    }

    @Override
    public void startDownload() throws IOException {
        DownloadRange range = getDownloadRange();
        for (int i = 0; i < MAX_THREADS; i++) {
            if (range.start[i] <= range.end[i]) {
                dispatcher.enqueueChunk(new ChunkInfo(downloadAction.getFileInfo(),
                        destination, range.start[i],range.end[i], i, downloadAction));
            }
        }
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
