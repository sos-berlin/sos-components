package com.sos.jitl.jobs.inventory;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.jitl.jobs.inventory.setjobresource.SetJobResource;
import com.sos.jitl.jobs.inventory.setjobresource.SetJobResourceJobArguments;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class SetJobResourceJob extends Job<SetJobResourceJobArguments> {

    public SetJobResourceJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<SetJobResourceJobArguments> step) throws Exception {
        SetJobResource jr = new SetJobResource(step);

        if ((step.getDeclaredArgument("file") == null || step.getDeclaredArgument("file").isEmpty()) && (step.getDeclaredArgument("value") == null
                || step.getDeclaredArgument("value").isEmpty())) {
            throw new SOSMissingDataException("At least one of the parameters 'value' or 'file' is required!");
        }
        
        if (step.getDeclaredArgument("file") != null &&  !step.getDeclaredArgument("file").isEmpty() && step.getDeclaredArgument("value") != null
                && !step.getDeclaredArgument("value").isEmpty()) {
            throw new SOSMissingDataException("Only one of the parameters 'value' or 'file' should be configured!");
        }
        jr.execute();
    }

}