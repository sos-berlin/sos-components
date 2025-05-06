package com.sos.commons.vfs.webdav.commons;

import java.net.URI;

public class WebDAVResource {

    private final String uri;
    private final boolean isDirectory;
    private final long size;
    private final long lastModifiedInMillis;

    public WebDAVResource(URI uri, boolean isDirectory, long size, long lastModifiedInMillis) {
        this.uri = uri.toString();
        this.isDirectory = isDirectory;
        this.size = size;
        this.lastModifiedInMillis = lastModifiedInMillis;
    }

    public String getURI() {
        return uri;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }

    public long getLastModifiedInMillis() {
        return lastModifiedInMillis;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("uri=").append(uri);
        sb.append("isDirectory=").append(isDirectory);
        sb.append("size=").append(size);
        sb.append("lastModifiedInMillis=").append(lastModifiedInMillis);
        return sb.toString();
    }
}
