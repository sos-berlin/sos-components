package com.sos.jitl.jobs.file;

import java.util.regex.Pattern;

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
    public JOutcome.Completed onOrderProcess(JobStep step, FileOperationsJobArguments args) throws Exception {
        checkArguments(args);

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        boolean result = fo.existsFile(args.getSourceFile().getValue(), args.getFileSpec().getValue(), Pattern.CASE_INSENSITIVE, args.getMinFileAge()
                .getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize().getValue(), args.getMaxFileSize().getValue(), args
                        .getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue());
        if (result) {
            result = checkSteadyStateOfFiles(step.getLogger(), args, fo.getResultList());
        }
        return handleResult(step.getLogger(), args, fo.getResultList(), result);
    }

}
