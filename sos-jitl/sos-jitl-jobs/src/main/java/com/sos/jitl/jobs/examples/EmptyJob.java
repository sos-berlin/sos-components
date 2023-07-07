package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobArguments;
import com.sos.jitl.jobs.common.OrderProcessStep;

public class EmptyJob extends ABlockingInternalJob<JobArguments> {

    @Override
    public void onOrderProcess(OrderProcessStep<JobArguments> step) throws Exception {
        
    }

}
