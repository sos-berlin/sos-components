package com.sos.jitl.jobs.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.util.common.SOSTimeout;

import js7.data_for_java.order.JOutcome;

public class UnitTestJobHelper<A extends JobArguments> {

    private final ABlockingInternalJob<A> job;

    public UnitTestJobHelper(ABlockingInternalJob<A> job) {
        this.job = job;
    }

    public JOutcome.Completed onOrderProcess(A args) throws InterruptedException, ExecutionException, TimeoutException {
        return onOrderProcess(args, null, null);
    }

    public JOutcome.Completed onOrderProcess(A args, SOSCredentialStoreArguments csArgs) throws InterruptedException, ExecutionException,
            TimeoutException {
        return onOrderProcess(args, csArgs, null);
    }

    public JOutcome.Completed onOrderProcess(A args, SOSTimeout timeout) throws InterruptedException, ExecutionException, TimeoutException {
        return onOrderProcess(args, null, timeout);
    }

    public JOutcome.Completed onOrderProcess(A args, SOSCredentialStoreArguments csArgs, SOSTimeout timeout) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (timeout == null) {
            timeout = new SOSTimeout(2, TimeUnit.MINUTES);
        }
        final JobStep<A> step = newJobStep(args, csArgs);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.job.onOrderProcess(step);
            } catch (Exception e) {
                return step.failed(e.toString(), e);
            }
        }).get(timeout.getInterval(), timeout.getTimeUnit());
    }

    public JobStep<A> newJobStep(A args) {
        return newJobStep(args, null);
    }

    public JobStep<A> newJobStep(A args, SOSCredentialStoreArguments csArgs) {
        JobStep<A> step = new JobStep<A>(job.getClass().getName(), job.getJobContext(), null);
        step.init(args);
        return step;
    }

    public static JobLogger newJobLogger() {
        JobLogger l = new JobLogger(null, null);
        l.init(null);
        return l;
    }
}
