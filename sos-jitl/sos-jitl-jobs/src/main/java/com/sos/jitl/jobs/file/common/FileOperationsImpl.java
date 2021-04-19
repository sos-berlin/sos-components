package com.sos.jitl.jobs.file.common;

import java.io.File;

public class FileOperationsImpl extends AFileOperations {

    @Override
    protected boolean handleOneFile(File sourceFile, File targetFile, boolean overwrite, boolean gracious) throws Exception {
        return false;
    }

}
