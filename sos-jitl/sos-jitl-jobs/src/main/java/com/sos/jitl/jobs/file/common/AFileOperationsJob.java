package com.sos.jitl.jobs.file.common;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSPath;
import com.sos.jitl.jobs.file.exception.SOSFileOperationsException;
import com.sos.js7.job.Job;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepOutcome;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

public abstract class AFileOperationsJob extends Job<FileOperationsJobArguments> {

    public AFileOperationsJob(JobContext jobContext) {
        super(jobContext);
    }

    public static void checkArguments(FileOperationsJobArguments args) throws Exception {
        if (args.getReplacing().isEmpty() && !args.getReplacement().isEmpty()) {
            throw new JobRequiredArgumentMissingException(String.format("'%s' is missing but required for '%s'", args.getReplacing().getName(), args
                    .getReplacement().getName()));
        }
    }

    public void setFlags(FileOperationsJobArguments args) {
        int flags = 0;
        if (args.getCreateFile().getValue()) {
            flags |= AFileOperations.CREATE_DIR;
        }
        if (args.getGracious().getValue()) {
            flags |= AFileOperations.GRACIOUS;
        }
        if (!args.getOverwrite().getValue()) {
            flags |= AFileOperations.NOT_OVERWRITE;
        }
        if (args.getRecursive().getValue()) {
            flags |= AFileOperations.RECURSIVE;
        }
        if (args.getRemoveDir().getValue()) {
            flags |= AFileOperations.REMOVE_DIR;
        }
        args.setFlags(flags);
    }

    public static void handleResult(OrderProcessStep<? extends FileOperationsJobArguments> step, List<File> files, boolean result) throws Exception {

        boolean isDebugEnabled = step.getLogger().isDebugEnabled();
        FileOperationsJobArguments args = step.getDeclaredArguments();
        int size = 0;
        String fileList = "";
        if (files != null && files.size() > 0) {
            size = files.size();
            fileList = files.stream().map(File::getAbsolutePath).collect(Collectors.joining(";"));
        }
        args.getReturnResultSet().setValue(fileList);
        args.getReturnResultSetSize().setValue(size);

        if (isDebugEnabled) {
            step.getLogger().debug("[handleResult]result=" + result);
        }

        if (args.getResultSetFile().getValue() != null && fileList.length() > 0) {
            if (isDebugEnabled) {
                step.getLogger().debug("[handleResult][create/overwrite file]" + args.getResultSetFile().getValue());
            }
            SOSPath.overwriteWithNewLine(args.getResultSetFile().getValue(), fileList);
        }

        OrderProcessStepOutcome outcome = step.getOutcome();
        outcome.putVariable(args.getReturnResultSet());
        outcome.putVariable(args.getReturnResultSetSize());

        if (!args.getRaiseErrorIfResultSetIs().isEmpty()) {
            if (compareIntValues(args.getRaiseErrorIfResultSetIs().getValue(), size, args.getExpectedSizeOfResultSet().getValue())) {
                String msg = String.format("no of hits in result set '%s'  is '%s' expected '%s'", size, args.getRaiseErrorIfResultSetIs().getValue(),
                        args.getExpectedSizeOfResultSet().getValue());

                outcome.setFailed();
                outcome.setMessage(msg);
                return;
            }
        }
        outcome.setReturnCode(result ? JobHelper.DEFAULT_RETURN_CODE_SUCCEEDED : 1);
    }

    private static boolean compareIntValues(final String comparator, final int left, final int right) throws Exception {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("eq", 1);
        map.put("equal", 1);
        map.put("==", 1);
        map.put("=", 1);
        map.put("ne", 2);
        map.put("not equal", 2);
        map.put("!=", 2);
        map.put("<>", 2);
        map.put("lt", 3);
        map.put("less than", 3);
        map.put("<", 3);
        map.put("le", 4);
        map.put("less or equal", 4);
        map.put("<=", 4);
        map.put("ge", 5);
        map.put("greater or equal", 5);
        map.put(">=", 5);
        map.put("gt", 6);
        map.put("greater than", 6);
        map.put(">", 6);

        boolean result = false;
        Integer val = map.get(comparator.toLowerCase());
        if (val != null) {
            switch (val) {
            case 1:
                result = left == right;
                break;
            case 2:
                result = left != right;
                break;
            case 3:
                result = left < right;
                break;
            case 4:
                result = left <= right;
                break;
            case 5:
                result = left >= right;
                break;
            case 6:
                result = left > right;
                break;
            default:
                break;
            }
        } else {
            throw new SOSFileOperationsException(String.format("Compare operator not known: %s", comparator));
        }
        return result;
    }

}
