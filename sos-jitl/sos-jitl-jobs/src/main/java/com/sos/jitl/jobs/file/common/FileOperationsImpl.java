package com.sos.jitl.jobs.file.common;

import java.io.File;

import com.sos.commons.util.loggers.base.ISOSLogger;

public class FileOperationsImpl extends AFileOperations {

    public FileOperationsImpl(ISOSLogger logger) {
        super(logger);
    }

    @Override
    protected boolean handleOneFile(File sourceFile, File targetFile, boolean overwrite, boolean gracious) throws Exception {
        return false;
    }

}
