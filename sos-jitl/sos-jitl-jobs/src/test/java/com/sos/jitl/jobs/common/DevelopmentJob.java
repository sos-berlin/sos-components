package com.sos.jitl.jobs.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sos.commons.util.common.SOSTimeout;

import js7.data_for_java.order.JOutcome;

public class DevelopmentJob<A extends JobArguments> {

    private final ABlockingInternalJob<A> job;

    public DevelopmentJob(ABlockingInternalJob<A> job) {
        this.job = job;
    }

    public JOutcome.Completed onOrderProcess(A args) throws InterruptedException, ExecutionException, TimeoutException {
        return onOrderProcess(args, null);
    }

    public JOutcome.Completed onOrderProcess(A args, SOSTimeout timeout) throws InterruptedException, ExecutionException, TimeoutException {
        if (timeout == null) {
            timeout = new SOSTimeout(2, TimeUnit.MINUTES);
        }
        final JobStep<A> step = new JobStep<A>(job.getClass().getName(), job.getJobContext(), null);
        step.init(args);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.job.onOrderProcess(step);
            } catch (Exception e) {
                return step.failed(e.toString(), e);
            }
        }).get(timeout.getInterval(), timeout.getTimeUnit());
    }

}
