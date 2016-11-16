package com.braindigit.downloader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Braindigit
 * Created on 11/16/16.
 */
interface NetworkHelper {
    Header getHeader(String url) throws IOException;

    Header getHeader(String url, String lastModified) throws IOException;

    Response download(String range, String url) throws IOException;

    class Header {
        static final String CONTENT_RANGE = "Content-Range";
        static final String LAST_MODIFIED = "Last-Modified";
        static final String TRANSFER_ENCODING = "Transfer-Encoding";
        static final String CONTENT_LENGTH = "Content-Length";
        static final String CONTENT_DISPOSITION = "Content-Disposition";
        final String contentRange;
        final String lastModified;
        final long contentLength;
        final String transferEncoding;
        final int responseCode;
        final String contentDisposition;

        public Header(String contentRange, String lastModified, long contentLength,
                      String transferEncoding, int responseCode, String contentDisposition) {
            this.contentRange = contentRange;
            this.lastModified = lastModified;
            this.contentLength = contentLength;
            this.transferEncoding = transferEncoding;
            this.responseCode = responseCode;
            this.contentDisposition = contentDisposition;
        }
    }

    class Response{
        final InputStream inputStream;
        final long contentLength;
        final String transferEncoding;

        public Response(InputStream inputStream, long contentLength, String transferEncoding) {
            this.inputStream = inputStream;
            this.contentLength = contentLength;
            this.transferEncoding = transferEncoding;
        }
    }
}
