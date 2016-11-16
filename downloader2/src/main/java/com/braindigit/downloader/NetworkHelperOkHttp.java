package com.braindigit.downloader;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.braindigit.downloader.Utils.stringToLong;

/**
 * Braindigit
 * Created on 11/16/16.
 */

class NetworkHelperOkHttp implements NetworkHelper {
    private static final String TEST_RANGE_SUPPORT = "bytes=0-";

    private final OkHttpClient client;

    NetworkHelperOkHttp() {
        this.client = new OkHttpClient.Builder().build();
    }

    private Header from(okhttp3.Response response) {
        Headers responseHeaders = response.headers();
        long contentLength = stringToLong(responseHeaders.get(Header.CONTENT_LENGTH));
        String contentRange = responseHeaders.get(Header.CONTENT_RANGE);
        String lastModified = responseHeaders.get(Header.LAST_MODIFIED);
        String contentDisposition = responseHeaders.get(Header.CONTENT_DISPOSITION);
        return new Header(contentRange, lastModified, contentLength,
                responseHeaders.get(Header.TRANSFER_ENCODING), response.code(), contentDisposition);
    }

    @Override
    public Header getHeader(String url) throws IOException {
        Request request = new Request.Builder()
                .head()
                .header("Range", TEST_RANGE_SUPPORT)
                .url(url).build();
        return from(client.newCall(request).execute());
    }

    @Override
    public Header getHeader(String url, String lastModified) throws IOException {
        Request request = new Request.Builder()
                .head()
                .header("If-Range", lastModified)
                .header("Range", TEST_RANGE_SUPPORT)
                .url(url).build();
        return from(client.newCall(request).execute());
    }

    @Override
    public Response download(String range, String url) throws IOException {
        Request request = new Request.Builder().header("Range", range).url(url).build();
        okhttp3.Response okResponse = client.newCall(request).execute();
        return new Response(okResponse.body().byteStream(),
                okResponse.body().contentLength(),Utils.transferEncoding(okResponse));
    }
}
