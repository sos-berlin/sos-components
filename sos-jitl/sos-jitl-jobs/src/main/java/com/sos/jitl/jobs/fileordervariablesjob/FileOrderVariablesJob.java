package com.sos.jitl.jobs.fileordervariablesjob;

import java.util.Map;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.OrderProcessStep;

public class FileOrderVariablesJob extends ABlockingInternalJob<FileOrderVariablesJobArguments> {

    public FileOrderVariablesJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<FileOrderVariablesJobArguments> step) throws Exception {
        Map<String, Object> allArgs = step.getAllArgumentsAsNameValueMap();
        Object orderArgFile = step.getOrderArgumentsAsNameValueMap().get("file");
        if (orderArgFile != null && orderArgFile instanceof String) {
            step.getDeclaredArguments().setJs7SourceFile((String) orderArgFile);
        }
        ExecuteFileOrderVariables eov = new ExecuteFileOrderVariables(step.getLogger(), allArgs, step.getDeclaredArguments());
        step.getOutcome().getVariables().putAll(eov.getVariables());
    }

}