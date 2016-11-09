package com.braindigit.downloader.network;

import com.braindigit.downloader.DownloadManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.braindigit.downloader.Utils.stringToLong;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class RangeSupport {

    public RangeSupport() {

    }

    public Header supportsRange(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("Range", DownloadManager.TEST_RANGE_SUPPORT);
            connection.connect();
            if (connection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
                    connection.getResponseCode() < HttpURLConnection.HTTP_MULT_CHOICE) {
                Header header = new Header();
                header.setContentLength(stringToLong(connection.getHeaderField(Header.CONTENT_LENGTH)));
                header.setContentRange(connection.getHeaderField(Header.CONTENT_RANGE));
                header.setLastModified(connection.getHeaderField(Header.LAST_MODIFIED));
                return header;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Header supportHeaderWithIfRange(String url, String lastModified) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("Range", DownloadManager.TEST_RANGE_SUPPORT);
            connection.setRequestProperty("If-Range", lastModified);
            connection.connect();
            if (connection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
                    connection.getResponseCode() < HttpURLConnection.HTTP_MULT_CHOICE) {
                Header header = new Header();
                header.setContentLength(stringToLong(connection.getHeaderField(Header.CONTENT_LENGTH)));
                header.setContentRange(connection.getHeaderField(Header.CONTENT_RANGE));
                header.setLastModified(connection.getHeaderField(Header.LAST_MODIFIED));
                header.setResponseCode(connection.getResponseCode());
                return header;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
