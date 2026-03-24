package com.sos.yade.engine.handlers.operations.copymove.file.commons;

import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;

/** @apiNote COPY/MOVE operations */
public class YADETargetProviderFile extends YADEProviderFile {

    private long bytesProcessed;
    private int attempt;

    /** Indicates whether the target file was touched (created or partially written) during this transfer operation.
     * 
     * <p>
     * This flag is used for rollback decisions.<br />
     * If a transfer fails, this flag determines whether the target file needs to be cleaned up.<br />
     * A value of {@code true} means the file was affected by this transfer (even if incomplete) and should be deleted during rollback. <br />
     * A value of {@code false} means the file was never touched, so no cleanup is required.
     * </p>
     */
    private boolean touched;
    /** the .md5 file was created/overwritten on the target system */
    private boolean integrityHashFileWritten;

    public YADETargetProviderFile(YADETargetProviderDelegator targetDelegator, String fullPath) {
        super(targetDelegator, fullPath, 0L, 0L, null, false);
    }

    public void updateBytesProcessed(int bytesProcessed) {
        this.bytesProcessed += bytesProcessed;
    }

    public void finalizeFileSize() {
        setSize(bytesProcessed);
        resetBytesProcessed();
    }

    public long getBytesProcessed() {
        return bytesProcessed;
    }

    public void setBytesProcessed(long bytes) {
        bytesProcessed = bytes;
    }

    public void resetBytesProcessed() {
        setBytesProcessed(0L);
    }

    public void addAttempt() {
        attempt++;
    }

    public int getAttempt() {
        return attempt;
    }

    /** Indicates whether the target file was touched (created or partially written) during this transfer operation.
     * 
     * @return */
    public boolean isTouched() {
        return touched;
    }

    public void setTouched() {
        touched = true;
    }

    /** Indicates whether the target integrity hash file (.md5) was written (created/overwritten) during this transfer operation.
     * 
     * @return */
    public boolean isIntegrityHashFileWritten() {
        return integrityHashFileWritten;
    }

    public void setIntegrityHashFileWritten() {
        integrityHashFileWritten = true;
    }
}
