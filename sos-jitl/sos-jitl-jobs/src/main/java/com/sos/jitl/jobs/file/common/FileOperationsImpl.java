package com.sos.jitl.jobs.file.common;

import java.io.File;

import com.sos.jitl.jobs.common.OrderProcessStepLogger;

public class FileOperationsImpl extends AFileOperations {

    public FileOperationsImpl(OrderProcessStepLogger logger) {
        super(logger);
    }

    @Override
    protected boolean handleOneFile(File sourceFile, File targetFile, boolean overwrite, boolean gracious)
            throws Exception {
        return false;
    }

}
