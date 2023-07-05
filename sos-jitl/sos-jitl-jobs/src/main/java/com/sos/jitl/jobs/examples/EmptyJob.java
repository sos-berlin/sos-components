package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.DefaultJobArguments;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome.Completed;

public class EmptyJob extends ABlockingInternalJob<DefaultJobArguments> {

    @Override
    public Completed onOrderProcess(JobStep<DefaultJobArguments> step) throws Exception {
        
        return step.success();
    }

}
