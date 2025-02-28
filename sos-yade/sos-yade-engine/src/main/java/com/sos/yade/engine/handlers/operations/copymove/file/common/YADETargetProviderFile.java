package com.sos.yade.engine.handlers.operations.copymove.file.common;

import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.delegators.YADETargetProviderDelegator;

/** @apiNote COPY/MOVE operations */
public class YADETargetProviderFile extends YADEProviderFile {

    private long bytesProcessed;

    public YADETargetProviderFile(YADETargetProviderDelegator targetDelegator, String fullPath) {
        super(targetDelegator, fullPath, 0L, 0L, null, false);
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
