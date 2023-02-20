package com.sos.jitl.jobs.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSDate;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

import js7.data_for_java.order.JOutcome;

public class FileExistsJob extends AFileOperationsJob {

    public FileExistsJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<FileOperationsJobArguments> step) throws Exception {
        checkArguments(step.getArguments());

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        boolean result = fo.existsFile(step.getArguments().getSourceFile().getValue(), step.getArguments().getFileSpec().getValue(),
                Pattern.CASE_INSENSITIVE, step.getArguments().getMinFileAge().getValue(), step.getArguments().getMaxFileAge().getValue(), step
                        .getArguments().getMinFileSize().getValue(), step.getArguments().getMaxFileSize().getValue(), step.getArguments()
                                .getSkipFirstFiles().getValue(), step.getArguments().getSkipLastFiles().getValue());
        if (result) {
            result = checkSteadyStateOfFiles(step.getLogger(), step.getArguments(), fo.getResultList());
        }
        return handleResult(step, fo.getResultList(), result);
    }

}
