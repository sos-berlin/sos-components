package com.sos.commons.vfs.commons.file;

import java.util.TimeZone;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPathUtils;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.IProvider;

/** A standard class representing a file with its path, size, and last modification time */
public class ProviderFile {

    private String fullPath;
    private String name;
    private long size;
    private long lastModifiedMillis;

    /** file position in the entire files list */
    private int index;

    public ProviderFile(IProvider provider, String fullPath, long size, long lastModifiedMillis) {
        setFullPath(provider, fullPath);
        if (this.fullPath != null) {
            this.size = size;
            this.lastModifiedMillis = lastModifiedMillis;
        }
    }

    public String getName() {
        return name;
    }

    public void setFullPath(String val) {
        fullPath = val;
    }

    public String getFullPath() {
        return fullPath;
    }

    private void setFullPath(IProvider provider, String val) {
        if (val == null) {
            fullPath = null;
            name = null;
            size = AProvider.DEFAULT_FILE_ATTR_VALUE;
            lastModifiedMillis = AProvider.DEFAULT_FILE_ATTR_VALUE;
        } else {
            fullPath = provider.toPathStyle(val);
            name = SOSPathUtils.getName(this.fullPath);
        }
    }

    public long getSize() {
        return size;
    }

    public void setSize(long val) {
        size = val;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int val) {
        index = val;
    }

    public long getLastModifiedMillis() {
        return lastModifiedMillis;
    }

    public void setLastModifiedMillis(long val) {
        lastModifiedMillis = val;
    }

    /** as UTC because the YADE client only knows its own time zone, and the provider's time zone is unknown */
    public String getLastModifiedAsUTCString() {
        return AProvider.isValidModificationTime(lastModifiedMillis) ? SOSDate.tryGetDateTimeAsString(lastModifiedMillis, TimeZone.getTimeZone(
                SOSDate.TIMEZONE_UTC)) : String.valueOf(lastModifiedMillis);
    }
}
