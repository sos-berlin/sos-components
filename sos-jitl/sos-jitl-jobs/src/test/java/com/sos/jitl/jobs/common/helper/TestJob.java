package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.Job;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class TestJob extends TestJobSuperClass {

    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, TestJobArguments args) throws Exception {
        return Job.success();
    }
}
