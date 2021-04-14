package com.sos.jitl.jobs.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class InfoJob extends ABlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoJob.class);

    public InfoJob(JobContext jobContext) {
        super(jobContext);
        LOGGER.info("[CONSTRUCTOR]jobArguments=" + Job.convert(getJobContext().jobArguments()));
    }

    @Override
    public void onStart() throws Exception {
        LOGGER.info("start");
    }

    @Override
    public void onStop() {
        LOGGER.info("stop");
    }

    @Override
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step) throws Exception {
        LOGGER.info("processOrder: " + step.order().id());

        long result = 1;
        usePrintWriter(step, result);
        // useWriter(step, result);

        return Job.success("info_result", result);
    }

    private void usePrintWriter(BlockingInternalJob.Step step, long result) throws Exception {
        step.out().println("[OUT]----------Workflow-----------------");
        step.out().println("[OUT]name=" + Job.getWorkflowName(step));
        step.out().println("[OUT]versionId=" + Job.getWorkflowVersionId(step));
        step.out().println("[OUT]position=" + Job.getWorkflowPosition(step));

        step.out().println("[OUT]----------ORDER-----------------");
        step.out().println("[OUT]id: " + Job.getOrderId(step));
        step.out().println("[OUT]arguments(scala): " + step.order().arguments());
        step.out().println("[OUT]arguments(java): " + Job.convert(step.order().arguments()));

        step.out().println("[OUT]----------JOB-----------------");
        step.out().println("[OUT]arguments(scala): " + getJobContext().jobArguments());
        step.out().println("[OUT]arguments(java): " + Job.convert(getJobContext().jobArguments()));

        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        step.out().println("[OUT]----------NODE-----------------");
        step.out().println("[OUT]agentId: " + Job.getAgentId(step));
        step.out().println("[OUT]name=" + Job.getJobName(step));
        step.out().println("[OUT]arguments(scala): " + step.arguments());
        step.out().println("[OUT]arguments(java): " + Job.convert(step.arguments()));

        step.err().println("[ERR]position written to err=" + Job.getWorkflowPosition(step));

        step.out().println("[OUT]----------RETURN-----------------");
        step.out().println("[OUT]returns Succeeded and \"info_result\"=" + result);
    }

    @SuppressWarnings("unused")
    private void useWriter(BlockingInternalJob.Step step, long result) throws Exception {
        String newLine = "\n";

        step.outWriter().write("[outWriter]----------Workflow-----------------" + newLine);
        step.outWriter().write("[outWriter]name=" + Job.getWorkflowName(step) + newLine);
        step.outWriter().write("[outWriter]versionId=" + Job.getWorkflowVersionId(step) + newLine);
        step.outWriter().write("[outWriter]position=" + Job.getWorkflowPosition(step) + newLine);

        step.outWriter().write("[outWriter]----------ORDER-----------------" + newLine);
        step.outWriter().write("[outWriter]id: " + Job.getOrderId(step) + newLine);
        step.outWriter().write("[outWriter]arguments(scala): " + step.order().arguments() + newLine);
        step.outWriter().write("[outWriter]arguments(java): " + Job.convert(step.order().arguments()) + newLine);

        step.outWriter().write("[outWriter]----------JOB-----------------" + newLine);
        step.outWriter().write("[outWriter]arguments(scala): " + getJobContext().jobArguments() + newLine);
        step.outWriter().write("[outWriter]arguments(java): " + Job.convert(getJobContext().jobArguments()) + newLine);

        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        step.outWriter().write("[outWriter]----------NODE-----------------" + newLine);
        step.outWriter().write("[outWriter]agentId: " + Job.getAgentId(step) + newLine);
        step.outWriter().write("[outWriter]name=" + Job.getJobName(step) + newLine);
        step.outWriter().write("[outWriter]arguments(scala): " + step.arguments() + newLine);
        step.outWriter().write("[outWriter]arguments(java): " + Job.convert(step.arguments()) + newLine);

        step.errWriter().write("[errWriter]position written to err=" + Job.getWorkflowPosition(step) + newLine);

        step.outWriter().write("[outWriter]----------RETURN-----------------" + newLine);
        step.outWriter().write("[outWriter]returns Succeeded and \"info_result\"=" + result + newLine);
    }
}
