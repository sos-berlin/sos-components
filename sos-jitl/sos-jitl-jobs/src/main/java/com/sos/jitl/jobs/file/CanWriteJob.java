package com.sos.jitl.jobs.file;

import java.io.File;
import java.util.regex.Pattern;

import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class CanWriteJob extends AFileOperationsJob {

    public CanWriteJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, FileOperationsJobArguments args) throws Exception {
        checkArguments(args);

        FileOperationsImpl fo = new FileOperationsImpl(args.isDebugEnabled());
        boolean result = fo.canWrite(step, new File(args.getSourceFile().getValue()), args.getFileSpec().getValue(), Pattern.CASE_INSENSITIVE);
        return handleResult(step, args, fo.getResultList(), result);
    }

}
