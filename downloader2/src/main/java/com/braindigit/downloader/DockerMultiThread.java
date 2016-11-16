package com.braindigit.downloader;

import android.os.Handler;

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
 * Created on 11/16/16.
 */

class DockerMultiThread extends DockerResumable {
    DockerMultiThread(DownloadRequest downloadRequest, Downloader downloader,
                      Handler mainThreadHandler, FileInfo.Destination destination,
                      Dispatcher dispatcher, NetworkHelper networkHelper) {
        super(downloadRequest, downloader, mainThreadHandler, destination, dispatcher, networkHelper);
    }

    @Override
    void prepareDownload() throws IOException, ParseException {
        final long fileLength = downloadRequest.getFileInfo().getFileLength();
        final int MAX_THREADS = ExecutorService.DEFAULT_THREAD_COUNT;
        writeLastModify(new File(destination.lastModifiedPath),
                downloadRequest.getFileInfo().getLastModify());
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

    @Override
    void startDownload() throws IOException {
        super.startDownload();
    }
}
