package com.sos.jitl.jobs.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class InfoJob extends ABlockingInternalJob<InfoJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoJob.class);

    public InfoJob(JobContext jobContext) {
        super(jobContext);
        LOGGER.info("[CONSTRUCTOR]jobArguments=" + Job.convert(getJobContext().jobArguments()));
    }

    @Override
    public void onStart(InfoJobArguments args) throws Exception {
        LOGGER.info("start");
    }

    @Override
    public void onStop(InfoJobArguments args) {
        LOGGER.info("stop");
    }

    @Override
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, InfoJobArguments args) throws Exception {
        Job.info(step, "[OUT]----------JOB Instance-----------------");
        Job.info(step, "[OUT][jobContext.jobArguments()][scala]" + getJobContext().jobArguments());
        Job.info(step, "[OUT][jobContext.jobArguments()][java]" + Job.convert(getJobContext().jobArguments()));

        Job.info(step, "[OUT]----------Workflow-----------------");
        Job.info(step, "[OUT][name]" + Job.getWorkflowName(step));
        Job.info(step, "[OUT][versionId]" + Job.getWorkflowVersionId(step));
        Job.info(step, "[OUT][position]" + Job.getWorkflowPosition(step));

        Job.info(step, "[OUT]----------ORDER-----------------");
        Job.info(step, "[OUT][id]" + Job.getOrderId(step));
        Job.info(step, "[OUT][step.order().arguments()][scala]" + step.order().arguments());
        Job.info(step, "[OUT][step.order().arguments()][java]" + Job.convert(step.order().arguments()));

        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        Job.info(step, "[OUT]----------NODE/STEP-----------------");
        Job.info(step, "[OUT][agentId]" + Job.getAgentId(step));
        Job.info(step, "[OUT][name]" + Job.getJobName(step));
        Job.info(step, "[OUT][step.arguments()][scala]" + step.arguments());
        Job.info(step, "[OUT][step.arguments()][java]" + Job.convert(step.arguments()));

        Job.error(step, "[ERR]position written to err=" + Job.getWorkflowPosition(step));

        Job.info(step, "[OUT]----------RETURN-----------------");
        long result = 1;
        Job.info(step, "[OUT]returns Succeeded and \"info_result\"=" + result);

        printEnvs(step);

        return Job.success("info_result", result);
    }

    private void printEnvs(BlockingInternalJob.Step step) {
        Job.info(step, "[OUT]----------ENV-----------------");
        Job.info(step, "[OUT]    JS7");
        System.getenv().entrySet().stream().filter(e -> e.getKey().startsWith("JS7")).forEach(e -> {
            Job.info(step, "        " + e.getKey() + "=" + e.getValue());
        });
        Job.info(step, "[OUT]    System");
        System.getenv().entrySet().stream().filter(e -> !e.getKey().startsWith("JS7")).forEach(e -> {
            Job.info(step, "        " + e.getKey() + "=" + e.getValue());
        });
    }

}
