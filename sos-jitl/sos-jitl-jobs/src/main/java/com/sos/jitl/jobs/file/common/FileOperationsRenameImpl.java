package com.sos.jitl.jobs.file.common;

import java.io.File;

import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;

public class FileOperationsRenameImpl extends AFileOperations {

    public FileOperationsRenameImpl(JobLogger logger) {
        super(logger);
    }

    @Override
    protected boolean handleOneFile(File sourceFile, File targetFile, boolean overwrite, boolean gracious) throws Exception {
        if (sourceFile.equals(targetFile)) {
            throw new SOSFileOperationsException(String.format("cannot rename file to itself: %s", sourceFile.getCanonicalPath()));
        }

        if (!overwrite && targetFile.exists()) {
            if (!gracious) {
                throw new SOSFileOperationsException(String.format("file already exists: %s", targetFile.getCanonicalPath()));
            } else {
                getLogger().info("file already exists: %s", targetFile.getCanonicalPath());
                return false;
            }
        } else {
            if (targetFile.exists() && !targetFile.delete()) {
                throw new SOSFileOperationsException(String.format("cannot overwrite %s", targetFile.getCanonicalPath()));
            }

            getLogger().info("rename %s to %s", sourceFile.getPath(), targetFile.getPath());
            if (!sourceFile.renameTo(targetFile)) {
                boolean rc = copyFile(sourceFile, targetFile, false);
                if (rc) {
                    rc = sourceFile.delete();
                    if (!rc) {
                        rc = targetFile.delete();
                        throw new SOSFileOperationsException(String.format("cannot rename file from %s to %s", sourceFile.getCanonicalPath(),
                                targetFile.getCanonicalPath()));
                    }
                } else {
                    throw new SOSFileOperationsException(String.format("cannot rename file from %s to %s", sourceFile.getCanonicalPath(), targetFile
                            .getCanonicalPath()));
                }
            }
        }
        return true;
    }
}
