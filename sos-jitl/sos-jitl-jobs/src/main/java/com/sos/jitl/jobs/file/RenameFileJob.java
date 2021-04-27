package com.sos.jitl.jobs.file;

import java.util.regex.Pattern;

import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class RenameFileJob extends AFileOperationsJob {

    public RenameFileJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, FileOperationsJobArguments args) throws Exception {
        checkArguments(args);

        FileOperationsImpl fo = new FileOperationsImpl(args.isDebugEnabled());
        int result = fo.renameFileCnt(step, args.getSourceFile().getValue(), args.getTargetFile().getValue(), args.getFileSpec().getValue(), args
                .getFlags(), Pattern.CASE_INSENSITIVE, args.getReplacing().getValue(), args.getReplacement().getValue(), args.getMinFileAge()
                        .getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize().getValue(), args.getMaxFileSize().getValue(), args
                                .getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue(), args.getSortCriteria().getValue(), args
                                        .getSortOrder().getValue());

        return handleResult(step, args, fo.getResultList(), result > 0);
    }

}
