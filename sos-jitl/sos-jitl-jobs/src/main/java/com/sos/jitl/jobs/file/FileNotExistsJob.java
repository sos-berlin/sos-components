package com.sos.jitl.jobs.file;

import java.io.File;
import java.util.regex.Pattern;

import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class FileNotExistsJob extends Job<FileOperationsJobArguments> {

    public FileNotExistsJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<FileOperationsJobArguments> step) throws Exception {
        AFileOperationsJob.checkArguments(step.getDeclaredArguments());

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        FileOperationsJobArguments args = step.getDeclaredArguments();
        boolean result = !fo.existsFile(new File(args.getSourceFile().getValue()), args.getFileSpec().getValue(), args.getRecursive().getValue(),
                Pattern.CASE_INSENSITIVE, args.getMinFileAge().getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize().getValue(), args
                        .getMaxFileSize().getValue(), args.getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue());
        AFileOperationsJob.handleResult(step, fo.getResultList(), result);
    }

}
