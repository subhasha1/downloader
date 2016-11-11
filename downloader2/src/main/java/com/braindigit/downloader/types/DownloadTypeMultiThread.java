package com.braindigit.downloader.types;

import android.os.Handler;

import com.braindigit.downloader.Dispatcher;
import com.braindigit.downloader.DownloadAction;
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

import static com.braindigit.downloader.Utils.writeLastModify;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * Braindigit
 * Created on 11/11/16.
 */

public class DownloadTypeMultiThread extends DownloadTypeResumable {

    public DownloadTypeMultiThread(Downloader downloader, Handler mainThreadHandler,
                                   FileInfo.Destination destination, DownloadAction downloadAction,
                                   Dispatcher dispatcher) {
        super(downloader, mainThreadHandler, destination, downloadAction,dispatcher);
    }

    @Override
    public void prepareDownload() throws IOException, ParseException {
        final long fileLength = downloadAction.getFileInfo().getFileLength();
        final int MAX_THREADS = ExecutorService.DEFAULT_THREAD_COUNT;
        writeLastModify(new File(destination.lastModifiedPath),
                downloadAction.getFileInfo().getLastModify());
        RandomAccessFile rFile = null;
        RandomAccessFile rRecord = null;
        FileChannel channel = null;
        try {
            rFile = new RandomAccessFile(new File(destination.filePath), "rws");
            rFile.setLength(fileLength);

            rRecord = new RandomAccessFile(new File(destination.tempPath), "rws");
            rRecord.setLength(RECORD_FILE_TOTAL_SIZE);

            channel = rRecord.getChannel();
            MappedByteBuffer buffer = channel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);

            long start;
            long end;
            int eachSize = (int) (fileLength / MAX_THREADS);
            for (int i = 0; i < MAX_THREADS; i++) {
                if (i == MAX_THREADS - 1) {
                    start = i * eachSize;
                    end = fileLength - 1;
                } else {
                    start = i * eachSize;
                    end = (i + 1) * eachSize - 1;
                }
                buffer.putLong(start);
                buffer.putLong(end);
            }
        } finally {
            Utils.close(channel);
            Utils.close(rRecord);
            Utils.close(rFile);
        }
    }
}
