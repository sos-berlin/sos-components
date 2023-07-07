package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobArguments;
import com.sos.jitl.jobs.common.OrderProcessStep;

public class TestJobWithoutJobArgumentsClass extends ABlockingInternalJob<JobArguments> {

    @Override
    public void onOrderProcess(OrderProcessStep<JobArguments> step) throws Exception {
    }
}
