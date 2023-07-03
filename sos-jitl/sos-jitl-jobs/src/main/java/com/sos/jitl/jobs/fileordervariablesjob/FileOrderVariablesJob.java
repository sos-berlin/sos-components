package com.sos.jitl.jobs.fileordervariablesjob;

import java.util.Map;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobHelper;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class FileOrderVariablesJob extends ABlockingInternalJob<FileOrderVariablesJobArguments> {

    public FileOrderVariablesJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<FileOrderVariablesJobArguments> step) throws Exception {
        Map<String, Object> allArgs = step.getAllArgumentsAsNameValueMap();
        if (step.getInternalStep() != null) {
            Object obj = JobHelper.asJavaValue(step.getInternalStep().order().arguments().get("file"));
            if (obj instanceof String) {
                String s = (String) obj;
                step.getDeclaredArguments().setJs7SourceFile(s);
            }
        } else {
            allArgs.put("a", "1235");
        }

        ExecuteFileOrderVariables eov = new ExecuteFileOrderVariables(step.getLogger(), allArgs, step.getDeclaredArguments());
        return step.success(step.newJobStepOutcome(eov.getVariables()));
    }

}