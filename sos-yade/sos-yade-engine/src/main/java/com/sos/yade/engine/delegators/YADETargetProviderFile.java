package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.IProvider;

public class YADETargetProviderFile extends YADEProviderFile {

    private long bytesProcessed;

    public YADETargetProviderFile(IProvider provider, String fullPath) {
        super(provider, fullPath, 0L, 0L, null, false);
    }

    public void updateProgressSize(int bytesProcessed) {
        this.bytesProcessed += bytesProcessed;
    }

    public void finalizeFileSize() {
        setSize(bytesProcessed);
        bytesProcessed = 0L;
    }

    public long getBytesProcessed() {
        return bytesProcessed;
    }

}
