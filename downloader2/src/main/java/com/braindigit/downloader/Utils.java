package com.braindigit.downloader;

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

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class Utils {

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
}
