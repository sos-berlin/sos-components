package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class TestJobSuperClass extends ABlockingInternalJob<TestJobArguments> {

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<TestJobArguments> step) throws Exception {
        return step.success();
    }
}
