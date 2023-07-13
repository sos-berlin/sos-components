package com.sos.commons.job.helper;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.JobArguments;
import com.sos.commons.job.OrderProcessStep;

public class TestJobWithoutJobArgumentsClass extends ABlockingInternalJob<JobArguments> {

    @Override
    public void onOrderProcess(OrderProcessStep<JobArguments> step) throws Exception {
    }
}
