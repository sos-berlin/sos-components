package com.sos.yade.engine.common;

import com.sos.commons.vfs.common.AProviderContext;

public class YADEProviderContext extends AProviderContext {

    private final boolean source;
    private final String logPrefix;

    public YADEProviderContext(boolean val) {
        source = val;
        logPrefix = source ? "[Source]" : "[Target]";
    }

    public boolean isSource() {
        return source;
    }

    @Override
    public String getLogPrefix() {
        return logPrefix;
    }

}
