package com.sos.jitl.jobs.file;

import java.io.File;
import java.util.regex.Pattern;

import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

import js7.data_for_java.order.JOutcome;

public class CanWriteJob extends AFileOperationsJob {

    public CanWriteJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep step, FileOperationsJobArguments args) throws Exception {
        checkArguments(args);

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        boolean result = fo.canWrite(new File(args.getSourceFile().getValue()), args.getFileSpec().getValue(), Pattern.CASE_INSENSITIVE);
        return handleResult(step.getLogger(), args, fo.getResultList(), result);
    }

}
