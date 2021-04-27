package com.sos.jitl.jobs.common.helper;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class TestJobWithoutJobArgumentsClass extends ABlockingInternalJob<Object> {

    @Override
    public JOutcome.Completed onOrderProcess(JobStep step) throws Exception {
        return Job.success();
    }
}
