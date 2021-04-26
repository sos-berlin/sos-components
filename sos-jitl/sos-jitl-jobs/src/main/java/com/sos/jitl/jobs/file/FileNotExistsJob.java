package com.sos.jitl.jobs.file;

import java.util.regex.Pattern;

import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class FileNotExistsJob extends AFileOperationsJob {

    public FileNotExistsJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, FileOperationsJobArguments args) throws Exception {
        checkArguments(args);

        FileOperationsImpl fo = new FileOperationsImpl();
        boolean result = !fo.existsFile(step, args.getSourceFile().getValue(), args.getFileSpec().getValue(), Pattern.CASE_INSENSITIVE, args
                .getMinFileAge().getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize().getValue(), args.getMaxFileSize().getValue(), args
                        .getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue());
        return handleResult(step, args, fo.getResultList(), result);
    }

}
