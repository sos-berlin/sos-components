package com.sos.jitl.jobs.inventory;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.inventory.setjobresource.SetJobResource;
import com.sos.jitl.jobs.inventory.setjobresource.SetJobResourceJobArguments;

import js7.data_for_java.order.JOutcome;

public class SetJobResourceJob extends ABlockingInternalJob<SetJobResourceJobArguments> {

    public SetJobResourceJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<SetJobResourceJobArguments> step) throws Exception {
        SetJobResource jr = new SetJobResource(step.getLogger(), step.getDeclaredArguments());
        jr.execute();
        return step.success();
    }

}