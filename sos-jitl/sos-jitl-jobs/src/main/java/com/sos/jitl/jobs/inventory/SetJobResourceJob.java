package com.sos.jitl.jobs.inventory;

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
        SetJobResource jr = new SetJobResource(step.getLogger(), step.getDeclaredArguments());
        jr.execute();
    }

}