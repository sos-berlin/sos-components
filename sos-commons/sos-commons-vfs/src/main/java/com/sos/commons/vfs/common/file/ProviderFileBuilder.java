package com.sos.commons.vfs.common.file;

/** Builder class to construct ProviderFile objects */
public class ProviderFileBuilder {

    private String fullPath;
    private long size;
    private long lastModifiedMillis;

    public ProviderFileBuilder fullPath(String val) {
        fullPath = val;
        return this;
    }

    public ProviderFileBuilder size(long val) {
        size = val;
        return this;
    }

    public ProviderFileBuilder lastModifiedMillis(long val) {
        lastModifiedMillis = val;
        return this;
    }

    public String getFullPath() {
        return fullPath;
    }

    public long getSize() {
        return size;
    }

    public long getLastModifiedMillis() {
        return lastModifiedMillis;
    }

    public ProviderFile build() {
        return new ProviderFile(fullPath, size, lastModifiedMillis);
    }
}
