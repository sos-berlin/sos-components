package com.sos.jitl.jobs.file.common;

import java.io.File;

import com.sos.jitl.jobs.common.OrderProcessStepLogger;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;

public class FileOperationsCopyImpl extends AFileOperations {

    public FileOperationsCopyImpl(OrderProcessStepLogger logger) {
        super(logger);
    }

    @Override
    protected boolean handleOneFile(File sourceFile, File targetFile, boolean overwrite, boolean gracious) throws Exception {
        if (sourceFile.equals(targetFile)) {
            throw new SOSFileOperationsException(String.format("cannot copy file to itself: %s", sourceFile.getCanonicalPath()));
        }

        if (overwrite || !targetFile.exists()) {
            getLogger().info("copy %s to %s", sourceFile.getPath(), targetFile.getPath());
            if (copyFile(sourceFile, targetFile, false)) {
                targetFile.setLastModified(sourceFile.lastModified());
                return true;
            }
        } else if (!gracious) {
            throw new SOSFileOperationsException("file already exists: " + targetFile.getCanonicalPath());
        } else {
            getLogger().info("file already exists: %s", targetFile.getCanonicalPath());
        }
        return false;
    }
}
