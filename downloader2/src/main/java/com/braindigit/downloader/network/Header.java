package com.braindigit.downloader.network;

/**
 * Braindigit
 * Created on 11/9/16.
 */

public class Header {
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String CONTENT_LENGTH = "Content-Length";
    private String contentRange;
    private String lastModified;
    private long contentLength;
    private String transferEncoding;
    private int responseCode;

    public String getContentRange() {
        return contentRange;
    }

    public void setContentRange(String contentRange) {
        this.contentRange = contentRange;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }


    public String getTransferEncoding() {
        return transferEncoding;
    }

    public void setTransferEncoding(String transferEncoding) {
        this.transferEncoding = transferEncoding;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
