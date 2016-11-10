package com.braindigit.downloader;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.braindigit.downloader.network.Header;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ThreadFactory;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class Utils {
    static final String THREAD_PREFIX = "LoadTask-";

    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 15*1000;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 15*1000;

    public static void writeLastModify(File lastModifiedFile, String lastModify) throws IOException, ParseException {
        RandomAccessFile record = null;
        try {
            record = new RandomAccessFile(lastModifiedFile, "rws");
            record.setLength(8);
            record.seek(0);
            record.writeLong(GMTToLong(lastModify));
        } finally {
            close(record);
        }
    }

    public static long stringToLong(String s) {
        if (s == null) return -1;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static String longToGMT(long lastModify) {
        Date d = new Date(lastModify);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(d);
    }

    static long GMTToLong(String GMT) throws ParseException {
        if (GMT == null || "".equals(GMT)) {
            return new Date().getTime();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = sdf.parse(GMT);
        return date.getTime();
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    public static boolean supportsRange(Header header) {
        return !TextUtils.isEmpty(header.getContentRange()) || header.getContentLength() > -1;
    }

    static class DownloadThreadFactory implements ThreadFactory {
        @SuppressWarnings("NullableProblems")
        public Thread newThread(Runnable r) {
            return new DownloadThread(r);
        }
    }

    private static class DownloadThread extends Thread {
        public DownloadThread(Runnable r) {
            super(r);
        }

        @Override public void run() {
            Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
            super.run();
        }
    }
}
