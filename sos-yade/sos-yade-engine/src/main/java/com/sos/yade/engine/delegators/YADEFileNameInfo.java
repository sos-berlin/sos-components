package com.sos.yade.engine.delegators;

import com.sos.commons.util.SOSPathUtil;

public class YADEFileNameInfo {

    private String name;
    private String parent;
    private String path;
    private boolean absolutePath;

    public YADEFileNameInfo(final String fileName) {
        name = fileName;
        parent = null;
        path = null;
        absolutePath = false;
    }

    public YADEFileNameInfo(final AYADEProviderDelegator delegator, final String fileNameOrPath) {
        String normalized = delegator.normalizePath(fileNameOrPath);
        if (normalized.contains(String.valueOf(delegator.getPathSeparator()))) {
            name = SOSPathUtil.getName(normalized);
            parent = SOSPathUtil.getParentPath(normalized, delegator.getPathSeparator());
            // parent is not null - due to normalized.contains(...)
            path = SOSPathUtil.appendPath(parent, name, delegator.getPathSeparator());// or normalized...
            absolutePath = delegator.getProvider().isAbsolutePath(parent);
        } else {
            name = normalized;
            parent = null;
            path = null;
            absolutePath = false;
        }
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public boolean needsParent() {
        return parent != null;
    }

    public String getPath() {
        return path;
    }

    public boolean isAbsolutePath() {
        return absolutePath;
    }

}
