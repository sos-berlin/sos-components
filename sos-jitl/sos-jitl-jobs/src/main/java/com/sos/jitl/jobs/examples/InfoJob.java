package com.sos.jitl.jobs.examples;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobs.common.ABlockingInternalJob;

import js7.data.value.NumberValue;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class InfoJob extends ABlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoJob.class);

    private static JobContext jobContext;

    public InfoJob(JobContext context) {
        jobContext = context;
        LOGGER.info("CONSTRUCTOR: " + jobContext.jobArguments());
    }

    @Override
    public OrderProcess toOrderProcess(BlockingInternalJob.Step step) throws Exception {
        return () -> {
            LOGGER.info("processOrder: " + step.order().id());

            step.out().println("[OUT]----------Workflow-----------------");
            step.out().println("[OUT]name=" + getWorkflowName(step));
            step.out().println("[OUT]versionId=" + getWorkflowVersionId(step));
            step.out().println("[OUT]position=" + getWorkflowPosition(step));

            step.out().println("[OUT]----------ORDER-----------------");
            step.out().println("[OUT]id: " + getOrderId(step));
            step.out().println("[OUT]arguments: " + step.order().arguments());

            step.out().println("[OUT]----------JOB-----------------");
            step.out().println("[OUT]arguments: " + jobContext.jobArguments());

            step.out().println("[OUT]----------NODE-----------------");
            step.out().println("[OUT]agentId: " + getAgentId(step));
            step.out().println("[OUT]name=" + getJobName(step));
            step.out().println("[OUT]arguments: " + step.arguments());

            step.err().println("[ERR]written to err");

            long result = 1;
            step.out().println("[OUT]----------RETURN-----------------");
            step.out().println("[OUT]returns succeeded and info_result=" + result);
            return JOutcome.succeeded(Collections.singletonMap("info_result", NumberValue.of(result)));
        };
    }

}
