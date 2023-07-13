package com.sos.jitl.jobs.inventory;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.OrderProcessStep;
import com.sos.jitl.jobs.inventory.setjobresource.SetJobResource;
import com.sos.jitl.jobs.inventory.setjobresource.SetJobResourceJobArguments;

public class SetJobResourceJob extends ABlockingInternalJob<SetJobResourceJobArguments> {

    public SetJobResourceJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<SetJobResourceJobArguments> step) throws Exception {
        SetJobResource jr = new SetJobResource(step.getLogger(), step.getDeclaredArguments());
        jr.execute();
    }

}