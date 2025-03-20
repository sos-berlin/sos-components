package com.sos.commons.vfs.webdav.commons;

public class WebDAVResource {

    private final String href;
    private final boolean isDirectory;
    private final long size;
    private final long lastModifiedInMillis;

    public WebDAVResource(String href, boolean isDirectory, long size, long lastModifiedInMillis) {
        this.href = href;
        this.isDirectory = isDirectory;
        this.size = size;
        this.lastModifiedInMillis = lastModifiedInMillis;
    }

    public String getHref() {
        return href;
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
        sb.append("href=").append(href);
        sb.append("isDirectory=").append(isDirectory);
        sb.append("size=").append(size);
        sb.append("lastModifiedInMillis=").append(lastModifiedInMillis);
        return sb.toString();
    }
}
