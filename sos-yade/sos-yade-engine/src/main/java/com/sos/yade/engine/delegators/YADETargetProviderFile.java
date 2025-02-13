package com.sos.yade.engine.delegators;

import com.sos.yade.commons.Yade.TransferEntryState;

public class YADETargetProviderFile extends YADEProviderFile {

    private long bytesProcessed;
    private TransferEntryState subState;

    public YADETargetProviderFile(String fullPath) {
        super(fullPath, 0L, 0L, null, false);
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

    public void setSubState(TransferEntryState val) {
        subState = val;
    }

    public TransferEntryState getSubState() {
        return subState;
    }
}
