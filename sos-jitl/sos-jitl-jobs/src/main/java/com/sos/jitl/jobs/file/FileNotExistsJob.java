package com.sos.jitl.jobs.file;

import java.util.regex.Pattern;

import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

import js7.data_for_java.order.JOutcome;

public class FileNotExistsJob extends AFileOperationsJob {

    public FileNotExistsJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<FileOperationsJobArguments> step) throws Exception {
        checkArguments(step.getArguments());

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        boolean result = !fo.existsFile(step.getArguments().getSourceFile().getValue(), step.getArguments().getFileSpec().getValue(),
                Pattern.CASE_INSENSITIVE, step.getArguments().getMinFileAge().getValue(), step.getArguments().getMaxFileAge().getValue(), step
                        .getArguments().getMinFileSize().getValue(), step.getArguments().getMaxFileSize().getValue(), step.getArguments()
                                .getSkipFirstFiles().getValue(), step.getArguments().getSkipLastFiles().getValue());
        return handleResult(step, fo.getResultList(), result);
    }

}
