package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class TestJob extends TestJobSuperClass {

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<TestJobArguments> step) throws Exception {
        step.getLogger().info("info from job onOrderProcess");
        return step.success();
    }
}
