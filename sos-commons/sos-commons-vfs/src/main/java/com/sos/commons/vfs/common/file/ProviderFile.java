package com.sos.commons.vfs.common.file;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.vfs.common.AProvider;

/** A standard class representing a file with its path, size, and last modification time */
public class ProviderFile {

    private String fullPath;
    private String name;
    private long size;
    private long lastModifiedMillis;

    public ProviderFile(String fullPath, long size, long lastModifiedMillis) {
        setFullPath(fullPath);
        if (this.fullPath != null) {
            this.size = size;
            this.lastModifiedMillis = lastModifiedMillis;
        }
    }

    public String getName() {
        return name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String val) {
        if (val == null) {
            fullPath = null;
            name = null;
            size = AProvider.DEFAULT_FILE_ATTR_VALUE;
            lastModifiedMillis = AProvider.DEFAULT_FILE_ATTR_VALUE;
        } else {
            fullPath = SOSPathUtil.toUnixPath(val);
            name = SOSPathUtil.getFileName(this.fullPath);
        }
    }

    public long getSize() {
        return size;
    }

    public void setSize(long val) {
        size = val;
    }

    public long getLastModifiedMillis() {
        return lastModifiedMillis;
    }

    public void setLastModifiedMillis(long val) {
        lastModifiedMillis = val;
    }
}
