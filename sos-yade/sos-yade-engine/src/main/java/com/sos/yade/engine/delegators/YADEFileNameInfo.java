package com.sos.yade.engine.delegators;

import com.sos.commons.util.SOSPathUtil;

public class YADEFileNameInfo {

    private String name;
    private String parent;
    private String path;
    private boolean absolutePath;

    public YADEFileNameInfo(final AYADEProviderDelegator delegator, final String fileNameOrPath) {
        String formatted = delegator.getProvider().toPathStyle(fileNameOrPath);
        if (formatted.contains(delegator.getProvider().getPathSeparator())) {
            name = SOSPathUtil.getName(formatted);
            parent = SOSPathUtil.getParentPath(formatted, delegator.getProvider().getPathSeparator());
            // parent is not null - due to normalized.contains(...)
            path = SOSPathUtil.appendPath(parent, name, delegator.getProvider().getPathSeparator());// or normalized...
            absolutePath = path.startsWith(delegator.getProvider().getPathSeparator()); // delegator.getProvider().isAbsolutePath(parent);
        } else {
            name = formatted;
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
