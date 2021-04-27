package com.sos.jitl.jobs.common;

import js7.executor.forjava.internal.BlockingInternalJob;

public class JobStep<A> {

    private final BlockingInternalJob.Step internalStep;
    private final JobLogger logger;
    private final A arguments;

    protected JobStep(BlockingInternalJob.Step step, JobLogger logger, A arguments) {
        this.internalStep = step;
        this.logger = logger;
        this.arguments = arguments;
    }

    public BlockingInternalJob.Step getInternalStep() {
        return internalStep;
    }

    public JobLogger getLogger() {
        return logger;
    }

    public A getArguments() {
        return arguments;
    }
}
