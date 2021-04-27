package com.sos.jitl.jobs.file;

import java.util.regex.Pattern;

import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.file.common.AFileOperationsJob;
import com.sos.jitl.jobs.file.common.FileOperationsCopyImpl;
import com.sos.jitl.jobs.file.common.FileOperationsJobArguments;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;

import js7.data_for_java.order.JOutcome;

public class CopyFileJob extends AFileOperationsJob {

    public CopyFileJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep step, FileOperationsJobArguments args) throws Exception {
        checkArguments(args);

        FileOperationsCopyImpl fo = new FileOperationsCopyImpl(step.getLogger());

        String[] sourceArr = args.getSourceFile().getValue().split(";");
        String[] targetArr = null;
        if (!SOSString.isEmpty(args.getTargetFile().getValue())) {
            targetArr = args.getTargetFile().getValue().split(";");
            if (sourceArr.length != targetArr.length) {
                throw new SOSFileOperationsException(String.format("length mismatch: %s=%s, %s=%s", args.getSourceFile().getName(), sourceArr.length,
                        args.getTargetFile().getName(), targetArr.length));
            }
        }
        String[] fileSpecsArr = args.getFileSpec().getValue().split(";");
        boolean checkLen = sourceArr.length == fileSpecsArr.length;
        args.setFileSpec(fileSpecsArr[0]);
        int files = 0;
        for (int i = 0; i < sourceArr.length; i++) {
            String source = sourceArr[i];
            String target = null;
            if (!SOSString.isEmpty(args.getTargetFile().getValue())) {
                target = targetArr[i];
            }
            if (!SOSString.isEmpty(args.getFileSpec().getValue()) && checkLen) {
                args.setFileSpec(fileSpecsArr[i]);
            }
            files += doFileOperation(args, fo, source, target);
        }
        return handleResult(step.getLogger(), args, fo.getResultList(), files > 0);
    }

    private int doFileOperation(final FileOperationsJobArguments args, FileOperationsCopyImpl fo, final String strSource, final String strTarget)
            throws Exception {
        return fo.copyFileCnt(strSource, strTarget, args.getFileSpec().getValue(), args.getFlags(), Pattern.CASE_INSENSITIVE, args.getReplacing()
                .getValue(), args.getReplacement().getValue(), args.getMinFileAge().getValue(), args.getMaxFileAge().getValue(), args.getMinFileSize()
                        .getValue(), args.getMaxFileSize().getValue(), args.getSkipFirstFiles().getValue(), args.getSkipLastFiles().getValue(), args
                                .getSortCriteria().getValue(), args.getSortOrder().getValue());
    }

}
