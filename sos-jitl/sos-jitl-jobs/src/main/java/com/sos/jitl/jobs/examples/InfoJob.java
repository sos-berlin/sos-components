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
        boolean isDebugEnabled = args.isDebugEnabled();
        boolean isTraceEnabled = args.isTraceEnabled();

        Job.info(step, "----------USAGE-----------------");
        Job.info(step, "declare and set order variables:");
        Job.info(step, "     \"show_env\"=true (Boolean) to show environment variables");
        Job.info(step, "     \"redefine_show_env\"=true (Boolean) to redefine \"show_env\" value and see effect in the next step");
        Job.info(step, "     \"log_level\"=INFO|DEBUG|TRACE (case insensitive)");

        if (isDebugEnabled) {
            Job.debug(step, "-----------------------------------");
            Job.debug(step, "job DEBUG message");
        }
        if (isTraceEnabled) {
            Job.trace(step, "-----------------------------------");
            Job.trace(step, "job TRACE message");
        }
        Job.info(step, "----------JOB Instance-----------------");
        Job.info(step, "[jobContext.jobArguments()][scala]" + getJobContext().jobArguments());
        Job.info(step, "[jobContext.jobArguments()][java]" + Job.convert(getJobContext().jobArguments()));

        Job.info(step, "----------Workflow-----------------");
        Job.info(step, "[name]" + Job.getWorkflowName(step));
        Job.info(step, "[versionId]" + Job.getWorkflowVersionId(step));
        Job.info(step, "[position]" + Job.getWorkflowPosition(step));
        Job.error(step, "[position written to err]" + Job.getWorkflowPosition(step));

        Job.info(step, "----------ORDER-----------------");
        Job.info(step, "[id]" + Job.getOrderId(step));
        Job.info(step, "[step.order().arguments()][scala]" + step.order().arguments());
        Job.info(step, "[step.order().arguments()][java]" + Job.convert(step.order().arguments()));

        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        Job.info(step, "----------NODE/STEP-----------------");
        Job.info(step, "[agentId]" + Job.getAgentId(step));
        Job.info(step, "[name]" + Job.getJobName(step));
        Job.info(step, "[step.arguments()][scala]" + step.arguments());
        Job.info(step, "[step.arguments()][java]" + Job.convert(step.arguments()));

        Job.info(step, "[step.namedValue(%s)]%s", args.getShowEnv().getName(), step.namedValue(args.getShowEnv().getName()));
        Job.info(step, "[step.namedValue(%s)]%s", args.getRedefineShowEnv().getName(), step.namedValue(args.getRedefineShowEnv().getName()));
        Job.info(step, "[step.namedValue(%s)]%s", args.getLogLevel().getName(), step.namedValue(args.getLogLevel().getName()));

        if (args.getShowEnv().getValue()) {
            printEnvs(step);
        }

        Job.info(step, "----------RETURN-----------------");
        if (args.getRedefineShowEnv().getValue()) {
            Job.info(step, "[SUCCESS]set step outcome \"%s\"=%s", args.getShowEnv().getName(), !args.getShowEnv().getValue());
            return Job.success(args.getShowEnv().getName(), !args.getShowEnv().getValue());
        } else {
            Job.info(step, "[SUCCESS]");
            return Job.success();
        }
    }

    private void printEnvs(BlockingInternalJob.Step step) {
        Job.info(step, "----------ENV-----------------");
        Job.info(step, "    JS7");
        System.getenv().entrySet().stream().filter(e -> e.getKey().startsWith("JS7")).forEach(e -> {
            Job.info(step, "        " + e.getKey() + "=" + e.getValue());
        });
        Job.info(step, "    System");
        System.getenv().entrySet().stream().filter(e -> !e.getKey().startsWith("JS7")).forEach(e -> {
            Job.info(step, "        " + e.getKey() + "=" + e.getValue());
        });
    }

}
