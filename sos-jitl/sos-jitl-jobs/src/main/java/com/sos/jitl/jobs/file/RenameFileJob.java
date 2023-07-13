package com.sos.jitl.jobs.file;

import java.util.regex.Pattern;

import com.sos.commons.job.OrderProcessStep;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;
import com.sos.jitl.jobs.file.common.FileOperationsRenameImpl;

public class RenameFileJob extends AFileOperationsJob {

    public RenameFileJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<FileOperationsJobArguments> step) throws Exception {
        FileOperationsJobArguments args = step.getDeclaredArguments();
        checkArguments(args);
        setFlags(args);

        FileOperationsRenameImpl fo = new FileOperationsRenameImpl(step.getLogger());
        int result = fo.renameFileCnt(args.getSourceFile().getValue(), args.getTargetFile().getValue(), args.getFileSpec().getValue(), args
                .getFlags(), Pattern.CASE_INSENSITIVE, args.getReplacing().getValue(), args.getReplacement().getValue(), args.getMinFileAge()
                        .getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize().getValue(), args.getMaxFileSize().getValue(), args
                                .getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue(), args.getSortCriteria().getValue(), args
                                        .getSortOrder().getValue());

        handleResult(step, fo.getResultList(), result > 0);
    }

}
