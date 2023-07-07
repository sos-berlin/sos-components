package com.sos.jitl.jobs.file;

import java.io.File;
import java.util.regex.Pattern;

import com.sos.jitl.jobs.common.OrderProcessStep;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

public class CanWriteJob extends AFileOperationsJob {

    public CanWriteJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<FileOperationsJobArguments> step) throws Exception {
        checkArguments(step.getDeclaredArguments());

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        boolean result = fo.canWrite(new File(step.getDeclaredArguments().getSourceFile().getValue()), step.getDeclaredArguments().getFileSpec()
                .getValue(), Pattern.CASE_INSENSITIVE);
        handleResult(step, fo.getResultList(), result);
    }

}
