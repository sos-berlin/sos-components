package com.sos.commons.vfs.common;

import com.sos.commons.util.SOSPathUtil;

public class ProviderFile {

    private String name;
    private String fullName;
    private long size;

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String val) {
        if (val != null) {
            val = SOSPathUtil.toUnixStylePath(val);
        }
        fullName = val;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long val) {
        size = val;
    }

}
