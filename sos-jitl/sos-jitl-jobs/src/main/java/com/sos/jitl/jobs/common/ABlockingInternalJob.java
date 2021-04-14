package com.sos.jitl.jobs.common;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public abstract class ABlockingInternalJob implements BlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABlockingInternalJob.class);

    private final JobContext jobContext;

    public ABlockingInternalJob() {
        this(null);
    }

    public ABlockingInternalJob(JobContext jobContext) {
        this.jobContext = jobContext;
    }

    /** to override */
    public void onStart() throws Exception {

    }

    /** to override */
    public void onStop() throws Exception {

    }

    /** to override */
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step) throws Exception {
        return JOutcome.succeeded(Collections.emptyMap());
    }

    public JobContext getJobContext() {
        return jobContext;
    }

    /** engine methods */
    @Override
    public Either<Problem, Void> start() {
        try {
            onStart();
            return right(null);
        } catch (Throwable e) {
            return left(Problem.fromThrowable(e));
        }
    }

    @Override
    public void stop() {
        try {
            onStop();
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Override
    public OrderProcess toOrderProcess(BlockingInternalJob.Step step) throws Exception {
        return () -> {
            try {
                return onOrderProcess(step);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                return JOutcome.failed(e.toString());
            }
        };
    }

}
