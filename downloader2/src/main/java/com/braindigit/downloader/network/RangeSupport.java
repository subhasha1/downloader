package com.braindigit.downloader.network;

import android.support.annotation.NonNull;

import com.braindigit.downloader.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.braindigit.downloader.Utils.stringToLong;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class RangeSupport {
    public static final String TEST_RANGE_SUPPORT = "bytes=0-";

    public RangeSupport() {

    }

    private HttpURLConnection openConnection(@NonNull String path) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(path).openConnection();
        connection.setRequestMethod("HEAD");
        connection.setRequestProperty("Range", TEST_RANGE_SUPPORT);
        connection.setConnectTimeout(Utils.DEFAULT_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(Utils.DEFAULT_READ_TIMEOUT_MILLIS);
        connection.setRequestProperty("Connection", "close");
        return connection;
    }

    public Header supportsRange(String url) {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(url);
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
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    public Header supportHeaderWithIfRange(String url, String lastModified) {
        return supportHeaderWithIfRangeOkHttp(url,lastModified);
//        HttpURLConnection connection = null;
//        try {
//            connection = openConnection(url);
//            connection.setRequestProperty("If-Range", lastModified);
//            if (connection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
//                    connection.getResponseCode() < HttpURLConnection.HTTP_MULT_CHOICE) {
//                Header header = new Header();
//                header.setContentLength(stringToLong(connection.getHeaderField(Header.CONTENT_LENGTH)));
//                header.setContentRange(connection.getHeaderField(Header.CONTENT_RANGE));
//                header.setLastModified(connection.getHeaderField(Header.LAST_MODIFIED));
//                header.setResponseCode(connection.getResponseCode());
//                return header;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (connection != null)
//                connection.disconnect();
//        }
//        return null;
    }


    public Header supportHeaderWithIfRangeOkHttp(String url, String lastModified) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            Headers responseHeaders = response.headers();
            Header header = new Header();
            header.setContentLength(stringToLong(responseHeaders.get(Header.CONTENT_LENGTH)));
            header.setContentRange(responseHeaders.get(Header.CONTENT_RANGE));
            header.setLastModified(responseHeaders.get(Header.LAST_MODIFIED));
            header.setResponseCode(response.code());
            return header;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
