package com.sos.jitl.jobs.file;

import java.io.File;
import java.util.regex.Pattern;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

import js7.data_for_java.order.JOutcome;

public class FileNotExistsJob extends ABlockingInternalJob<FileOperationsJobArguments> {

    public FileNotExistsJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<FileOperationsJobArguments> step) throws Exception {
        AFileOperationsJob.checkArguments(step.getArguments());

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        FileOperationsJobArguments args = step.getArguments();
        boolean result = !fo.existsFile(new File(args.getSourceFile().getValue()), args.getFileSpec().getValue(), args.getRecursive().getValue(),
                Pattern.CASE_INSENSITIVE, args.getMinFileAge().getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize().getValue(), args
                        .getMaxFileSize().getValue(), args.getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue());
        return AFileOperationsJob.handleResult(step, fo.getResultList(), result);
    }

}
