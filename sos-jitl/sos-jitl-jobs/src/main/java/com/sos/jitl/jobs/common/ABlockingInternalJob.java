package com.sos.jitl.jobs.common;

import com.sos.jitl.jobs.exception.JobProblemException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.executor.forjava.internal.BlockingInternalJob;

public abstract class ABlockingInternalJob implements BlockingInternalJob {

    public ABlockingInternalJob() {

    }

    public ABlockingInternalJob(JobContext jobContext) {

    }

    @Override
    public OrderProcess toOrderProcess(BlockingInternalJob.Step step) throws Exception {
        return null;
    }

    public <T> T getFromEither(Either<Problem, T> either) throws JobProblemException {
        if (either.isLeft()) {
            throw new JobProblemException(either.getLeft());
        }
        return either.get();
    }

    public String getOrderId(BlockingInternalJob.Step step) throws JobProblemException {
        return step.order().id().string();
    }

    public String getAgentId(BlockingInternalJob.Step step) throws JobProblemException {
        return getFromEither(step.order().attached()).string();
    }

    public String getJobName(BlockingInternalJob.Step step) throws JobProblemException {
        return getFromEither(step.workflow().checkedJobName(step.order().workflowPosition().position())).toString();
    }

    public String getWorkflowName(BlockingInternalJob.Step step) {
        return step.order().workflowId().path().name();
    }

    public String getWorkflowVersionId(BlockingInternalJob.Step step) {
        return step.order().workflowId().versionId().toString();
    }

    public String getWorkflowPosition(BlockingInternalJob.Step step) {
        return step.order().workflowPosition().position().toString();
    }
}
