package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobArguments;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class TestJobWithoutJobArgumentsClass extends ABlockingInternalJob<JobArguments> {

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<JobArguments> step) throws Exception {
        return step.success();
    }
}
