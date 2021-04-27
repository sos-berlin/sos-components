package com.sos.jitl.jobs.file.common;

import java.io.File;

import js7.executor.forjava.internal.BlockingInternalJob;

public class FileOperationsImpl extends AFileOperations {

    public FileOperationsImpl(boolean isDebugEnabled) {
        super(isDebugEnabled);
    }

    @Override
    protected boolean handleOneFile(BlockingInternalJob.Step step, File sourceFile, File targetFile, boolean overwrite, boolean gracious)
            throws Exception {
        return false;
    }

}
