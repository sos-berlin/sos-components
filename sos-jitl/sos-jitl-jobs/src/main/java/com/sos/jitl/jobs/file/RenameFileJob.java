package com.sos.jitl.jobs.file;

import java.util.regex.Pattern;

import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

import js7.data_for_java.order.JOutcome;

public class RenameFileJob extends AFileOperationsJob {

    public RenameFileJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<FileOperationsJobArguments> step) throws Exception {
        FileOperationsJobArguments args = step.getArguments();
        checkArguments(args);

        FileOperationsImpl fo = new FileOperationsImpl(step.getLogger());
        int result = fo.renameFileCnt(args.getSourceFile().getValue(), args.getTargetFile().getValue(), args.getFileSpec().getValue(), args
                .getFlags(), Pattern.CASE_INSENSITIVE, args.getReplacing().getValue(), args.getReplacement().getValue(), args.getMinFileAge()
                        .getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize().getValue(), args.getMaxFileSize().getValue(), args
                                .getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue(), args.getSortCriteria().getValue(), args
                                        .getSortOrder().getValue());

        return handleResult(step, fo.getResultList(), result > 0);
    }

}
