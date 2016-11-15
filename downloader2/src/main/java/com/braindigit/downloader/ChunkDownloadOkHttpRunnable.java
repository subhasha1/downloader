package com.braindigit.downloader;


import android.os.Handler;
import android.util.Log;

import com.braindigit.downloader.network.Header;
import com.braindigit.downloader.types.DownloadTypeResumable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.braindigit.downloader.Utils.stringToLong;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

public class ChunkDownloadOkHttpRunnable implements PriorityRunnable {
    @Override
    public int getPriority() {
        return PRIORITY.LOW.ordinal();
    }

    private static final String TAG = "ChunkDownloadRunnable";

    private final Downloader downloader;
    private final DownloadAction action;
    private final Handler mainThreadHandler;
    private final ChunkInfo chunkInfo;
    private final int EACH_RECORD_SIZE = DownloadTypeResumable.EACH_RECORD_SIZE;
    private final int RECORD_FILE_TOTAL_SIZE = ExecutorService.DEFAULT_THREAD_COUNT *
            DownloadTypeResumable.EACH_RECORD_SIZE;

    public ChunkDownloadOkHttpRunnable(Downloader downloader, DownloadAction action,
                                       Handler mainThreadHandler, ChunkInfo chunkInfo) {
        this.downloader = downloader;
        this.action = action;
        this.mainThreadHandler = mainThreadHandler;
        this.chunkInfo = chunkInfo;
    }


    @Override
    public void run() {
        if (action.isCancelled())
            return;
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(chunkInfo.fileInfo.getUrl())
                .addHeader("Range","bytes=" + chunkInfo.start + "-" + chunkInfo.end)
                .build();
        try {
            RandomAccessFile record = null;
            FileChannel recordChannel = null;

            RandomAccessFile save = null;
            FileChannel saveChannel = null;

            InputStream inStream = null;
            try {
                Log.i(TAG, Thread.currentThread().getName() + " start download from " + chunkInfo.start + " to " + chunkInfo.end + "!");
                int readLen;
                byte[] buffer = new byte[8192];
                DownloadStatus status = new DownloadStatus();

                record = new RandomAccessFile(new File(chunkInfo.destination.tempPath), "rws");
                recordChannel = record.getChannel();
                MappedByteBuffer recordBuffer = recordChannel.map(READ_WRITE, 0, RECORD_FILE_TOTAL_SIZE);
                long totalSize = recordBuffer.getLong(RECORD_FILE_TOTAL_SIZE - 8) + 1;
                status.setTotalSize(totalSize);

                save = new RandomAccessFile(new File(chunkInfo.destination.filePath), "rws");
                saveChannel = save.getChannel();
                MappedByteBuffer saveBuffer = saveChannel.map(READ_WRITE, chunkInfo.start, chunkInfo.end - chunkInfo.start + 1);

                Response response = client.newCall(request).execute();

                inStream = response.body().byteStream();
                while ((readLen = inStream.read(buffer)) != -1) {
                    saveBuffer.put(buffer, 0, readLen);
                    recordBuffer.putLong(chunkInfo.rangeIndex * EACH_RECORD_SIZE, recordBuffer.getLong(chunkInfo.rangeIndex * EACH_RECORD_SIZE) + readLen);

                    status.setDownloadSize(totalSize - getResidue(recordBuffer));
                    if (action.isCancelled())
                        return;
                    else
                        action.onProgress(status);
                }
                Log.i(TAG, Thread.currentThread().getName() + " complete download! Download size is " +
                        stringToLong(response.header(Header.CONTENT_LENGTH) + " bytes"));
                if (status.getTotalSize() == status.getDownloadSize() && !action.isCancelled()) {
                    action.onComplete();
                }
            } finally {
                Utils.close(record);
                Utils.close(recordChannel);
                Utils.close(save);
                Utils.close(saveChannel);
                Utils.close(inStream);
                Log.i(TAG, Thread.currentThread().getName() + " closed thread!");
            }
        } catch (MalformedURLException e) {
            action.onError(e);
        } catch (IOException e) {
            Log.i(TAG, Thread.currentThread().getName() + " Range download stopped! Failed to save range file!");
            action.onError(new Exception(new Throwable("Range download stopped! Failed to save range file!", e)));
        }
    }

    private long getResidue(MappedByteBuffer recordBuffer) {
        long residue = 0;
        for (int j = 0; j < ExecutorService.DEFAULT_THREAD_COUNT; j++) {
            long startTemp = recordBuffer.getLong(j * EACH_RECORD_SIZE);
            long endTemp = recordBuffer.getLong(j * EACH_RECORD_SIZE + 8);
            long temp = endTemp - startTemp + 1;
            residue += temp;
        }
        return residue;
    }
}
