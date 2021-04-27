package com.sos.jitl.jobs.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

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
    public JOutcome.Completed onOrderProcess(JobStep step, InfoJobArguments args) throws Exception {

        step.getLogger().info("----------USAGE-----------------");
        step.getLogger().info("declare and set order variables:");
        step.getLogger().info("     \"show_env\"=true (Boolean) to show environment variables");
        step.getLogger().info("     \"redefine_show_env\"=true (Boolean) to redefine \"show_env\" value and see effect in the next step");
        step.getLogger().info("     \"log_level\"=INFO|DEBUG|TRACE (case insensitive)");

        if (step.getLogger().isDebugEnabled()) {
            step.getLogger().debug("-----------------------------------");
            step.getLogger().debug("job DEBUG message");
        }
        if (step.getLogger().isTraceEnabled()) {
            step.getLogger().trace("-----------------------------------");
            step.getLogger().trace("job TRACE message");
        }
        step.getLogger().info("----------JOB Instance-----------------");
        step.getLogger().info("[jobContext.jobArguments()][scala]" + getJobContext().jobArguments());
        step.getLogger().info("[jobContext.jobArguments()][java]" + Job.convert(getJobContext().jobArguments()));

        step.getLogger().info("----------Workflow-----------------");
        step.getLogger().info("[name]" + Job.getWorkflowName(step));
        step.getLogger().info("[versionId]" + Job.getWorkflowVersionId(step));
        step.getLogger().info("[position]" + Job.getWorkflowPosition(step));
        step.getLogger().error("[position written to err]" + Job.getWorkflowPosition(step));

        step.getLogger().info("----------ORDER-----------------");
        step.getLogger().info("[id]" + Job.getOrderId(step));
        step.getLogger().info("[step.order().arguments()][scala]" + step.getInternalStep().order().arguments());
        step.getLogger().info("[step.order().arguments()][java]" + Job.convert(step.getInternalStep().order().arguments()));

        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        step.getLogger().info("----------NODE/STEP-----------------");
        step.getLogger().info("[agentId]" + Job.getAgentId(step));
        step.getLogger().info("[name]" + Job.getJobName(step));
        step.getLogger().info("[step.arguments()][scala]" + step.getInternalStep().arguments());
        step.getLogger().info("[step.arguments()][java]" + Job.convert(step.getInternalStep().arguments()));

        step.getLogger().info("[step.namedValue(%s)]%s", args.getShowEnv().getName(), step.getInternalStep().namedValue(args.getShowEnv().getName()));
        step.getLogger().info("[step.namedValue(%s)]%s", args.getRedefineShowEnv().getName(), step.getInternalStep().namedValue(args
                .getRedefineShowEnv().getName()));
        step.getLogger().info("[step.namedValue(%s)]%s", args.getLogLevel().getName(), step.getInternalStep().namedValue(args.getLogLevel()
                .getName()));

        if (args.getShowEnv().getValue()) {
            printEnvs(step.getLogger());
        }

        step.getLogger().info("----------RETURN-----------------");
        if (args.getRedefineShowEnv().getValue()) {
            step.getLogger().info("[SUCCESS]set step outcome \"%s\"=%s", args.getShowEnv().getName(), !args.getShowEnv().getValue());
            return Job.success(args.getShowEnv().getName(), !args.getShowEnv().getValue());
        } else {
            step.getLogger().info("[SUCCESS]");
            return Job.success();
        }
    }

    private void printEnvs(JobLogger logger) {
        logger.info("----------ENV-----------------");
        logger.info("    JS7");
        System.getenv().entrySet().stream().filter(e -> e.getKey().startsWith("JS7")).forEach(e -> {
            logger.info("        " + e.getKey() + "=" + e.getValue());
        });
        logger.info("    System");
        System.getenv().entrySet().stream().filter(e -> !e.getKey().startsWith("JS7")).forEach(e -> {
            logger.info("        " + e.getKey() + "=" + e.getValue());
        });
    }

}
