package com.sos.jitl.jobs.common;

import js7.executor.forjava.internal.BlockingInternalJob;

public class JobStep {

    private final BlockingInternalJob.Step internalStep;
    private final JobLogger logger;

    protected JobStep(BlockingInternalJob.Step step, JobLogger logger) {
        this.internalStep = step;
        this.logger = logger;
    }

    public BlockingInternalJob.Step getInternalStep() {
        return internalStep;
    }

    public JobLogger getLogger() {
        return logger;
    }

}
