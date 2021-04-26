package com.sos.jitl.jobs.file.common;

import java.io.File;

import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;

import js7.executor.forjava.internal.BlockingInternalJob;

public class FileOperationsCopyImpl extends AFileOperations {

    public FileOperationsCopyImpl() {
        super();
    }

    @Override
    protected boolean handleOneFile(BlockingInternalJob.Step step, File sourceFile, File targetFile, boolean overwrite, boolean gracious)
            throws Exception {
        if (sourceFile.equals(targetFile)) {
            throw new SOSFileOperationsException(String.format("cannot copy file to itself: %s", sourceFile.getCanonicalPath()));
        }

        if (overwrite || !targetFile.exists()) {
            if (copyFile(step, sourceFile, targetFile, false)) {
                targetFile.setLastModified(sourceFile.lastModified());
                Job.info(step, "copy %s to %s", sourceFile.getPath(), targetFile.getPath());
                return true;
            }
        } else if (!gracious) {
            throw new SOSFileOperationsException("file already exists: " + targetFile.getCanonicalPath());
        } else {
            Job.info(step, "file already exists: %s", targetFile.getCanonicalPath());
        }
        return false;
    }
}
