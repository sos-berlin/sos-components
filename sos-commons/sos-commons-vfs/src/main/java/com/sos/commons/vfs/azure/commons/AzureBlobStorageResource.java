package com.sos.commons.vfs.azure.commons;

import com.sos.commons.util.SOSPathUtils;

public class AzureBlobStorageResource {

    private final String fullPath;
    private final String blobPath;
    private final boolean isDirectory;
    private final long size;
    private final long lastModifiedInMillis;

    public AzureBlobStorageResource(String containerName, String blobPath, boolean isDirectory, long size, long lastModifiedInMillis) {
        this.fullPath = SOSPathUtils.appendPath(containerName, blobPath, "/");
        this.blobPath = blobPath;
        this.isDirectory = isDirectory;
        this.size = size;
        this.lastModifiedInMillis = lastModifiedInMillis;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getBlobPath() {
        return blobPath;
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
        sb.append("fullPath=").append(fullPath);
        sb.append(", blobPath=").append(blobPath);
        sb.append(", isDirectory=").append(isDirectory);
        sb.append(", size=").append(size);
        sb.append(", lastModifiedInMillis=").append(lastModifiedInMillis);
        return sb.toString();
    }
}
