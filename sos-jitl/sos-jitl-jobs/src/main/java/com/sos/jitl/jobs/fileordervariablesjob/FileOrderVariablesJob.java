package com.sos.jitl.jobs.fileordervariablesjob;

import java.util.Map;

import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class FileOrderVariablesJob extends Job<FileOrderVariablesJobArguments> {

    public FileOrderVariablesJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<FileOrderVariablesJobArguments> step) throws Exception {
        Map<String, Object> allArgs = step.getAllArgumentsAsNameValueMap();
        Object orderArgFile = step.getOrderArgumentsAsNameValueMap().get("file");
        if (orderArgFile != null && orderArgFile instanceof String) {
            step.getDeclaredArguments().setJs7SourceFile((String) orderArgFile);
        }
        ExecuteFileOrderVariables eov = new ExecuteFileOrderVariables(step.getLogger(), allArgs, step.getDeclaredArguments());
        step.getOutcome().putVariables(eov.getVariables());
    }

}