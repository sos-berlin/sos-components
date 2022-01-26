package com.sos.jitl.jobs.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments.SOSCredentialStoreResolver;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobDetailValue;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.job.JobResourcePath;
import js7.data.order.HistoricOutcome;
import js7.data.value.Value;
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
    public JOutcome.Completed onOrderProcess(JobStep<InfoJobArguments> step) throws Exception {
        InfoJobArguments args = step.getArguments();

        step.getLogger().info("----------USAGE-----------------");
        step.getLogger().info("declare and set order/step variables:");
        step.getLogger().info("     \"%s\"=true (Boolean) to show environment variables", args.getShowEnv().getName());
        step.getLogger().info("     \"%s\"=true (Boolean) to redefine \"%s\" value and see effect in the next step", args.getRedefineShowEnv()
                .getName(), args.getShowEnv().getName());
        step.getLogger().info("     \"%s\"=INFO|DEBUG|TRACE (case insensitive)", args.getLogLevel().getName());
        step.getLogger().info("     \"%s\"=... to show in the ORDER HISTORIC OUTCOME", args.getReturnVariables().getName());
        step.getLogger().info("                e.g.: \"%s\"= 'myvar1_xyz__myvar2_123'", args.getReturnVariables().getName());
        step.getLogger().info("                  means set 2 return variables: 1) myvar1=xyz 2)myvar2=123");
        step.getLogger().info("     \"%s\"='some value'", args.getStringArgument().getName());
        step.getLogger().info("     \"%s\"='some password'", args.getPassword().getName());
        step.getLogger().info("     \"%s\"='entry1;entry2;entry3'", args.getList().getName());
        step.getLogger().info("     \"%s\"='...' any shell command, e.g. dir, ls ...", args.getShellCommand().getName());

        if (step.getLogger().isDebugEnabled()) {
            step.getLogger().debug("-----------------------------------");
            step.getLogger().debug("job DEBUG message");
        }
        if (step.getLogger().isTraceEnabled()) {
            step.getLogger().trace("-----------------------------------");
            step.getLogger().trace("job TRACE message");
        }
        step.getLogger().info("----------JOB Instance-----------------");
        step.getLogger().info("[scala][jobContext.jobArguments()]" + getJobContext().jobArguments());
        step.getLogger().info("[java][jobContext.jobArguments()]" + Job.convert(getJobContext().jobArguments()));

        step.getLogger().info("----------Workflow-----------------");
        step.getLogger().info("[java][name]" + step.getWorkflowName());
        step.getLogger().info("[java][versionId]" + step.getWorkflowVersionId());
        step.getLogger().info("[java][position]" + step.getWorkflowPosition());
        // step.getLogger().error("[position written to err]" + step.getWorkflowPosition());

        step.getLogger().info("----------ORDER-----------------");
        step.getLogger().info("[java][id]" + step.getOrderId());
        step.getLogger().info("[scala][step.order().arguments()]" + step.getInternalStep().order().arguments());
        step.getLogger().info("[java][step.order().arguments()]" + Job.convert(step.getInternalStep().order().arguments()));
        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        step.getLogger().info("----------ORDER JOB RESOURCES-----------------");
        step.getLogger().info("-ENGINE JOB RESOURCES-----------------");
        Map<JobResourcePath, Map<String, Either<Problem, Value>>> jobResources = step.getInternalStep().jobResourceToNameToCheckedValue();
        jobResources.entrySet().stream().forEach(e -> {
            step.getLogger().info(" " + e.getKey().string() + ":");
            e.getValue().entrySet().stream().filter(ee -> ee.getValue().isRight()).forEach(ee -> {
                step.getLogger().info("[scala]%s=%s", ee.getKey(), ee.getValue().get());
            });
        });
        // Either<Problem,Value> checkedValue = step.byJobResourceAndName(JobResourcePath.of("MY-JOB-RESOURCE"), "stringSetting");
        step.getLogger().info("-CONVERTED JOB RESOURCES------------------");
        Map<String, JobDetailValue> resources = step.getJobResourcesValues();
        resources.entrySet().stream().forEach(e -> {
            JobDetailValue v = e.getValue();
            step.getLogger().info(" %s=%s (job resource=%s)", e.getKey(), v.getValue(), v.getSource());
        });

        step.getLogger().info("----------ORDER HISTORIC OUTCOME-----------------");
        step.getLogger().info("-ENGINE HISTORIC OUTCOME-----------------");
        List<HistoricOutcome> list = step.getHistoricOutcomes();
        for (HistoricOutcome ho : list) {
            step.getLogger().info("[scala]" + SOSString.toString(ho));
        }
        step.getLogger().info("-CONVERTED HISTORIC OUTCOME (last var values)-----------------");
        step.getLogger().info("[java][all]" + step.getLastOutcomes());
        step.getLogger().info("[java][succeeded]" + step.getLastSucceededOutcomes());
        step.getLogger().info("[java][failed]" + step.getLastFailedOutcomes());

        step.getLogger().info("----------NODE/STEP-----------------");
        step.getLogger().info("[java][agentId]" + step.getAgentId());
        step.getLogger().info("[java][name]" + step.getJobName());
        step.getLogger().info("[scala][step.arguments()]" + step.getInternalStep().arguments());
        step.getLogger().info("[java][step.arguments()]" + Job.convert(step.getInternalStep().arguments()));

        step.getLogger().info("----------NODE/STEP GET ARGUMENT BY NAME-----------------");
        step.getLogger().info("[scala][step.getInternalStep().namedValue(%s)]%s", args.getStringArgument().getName(), step.getInternalStep()
                .namedValue(args.getStringArgument().getName()));

        step.getLogger().info("----------ALL CURRENT known/unknown argumens-----------------");
        Map<String, JobArgument<InfoJobArguments>> allcmap = step.getAllCurrentArguments();
        allcmap.entrySet().stream().forEach(e -> {
            step.getLogger().info("[java][%s][%s=%s]%s", e.getValue().getType(), e.getKey(), e.getValue().getDisplayValue(), SOSString.toString(e
                    .getValue()));
        });

        if (args.getShowEnv().getValue()) {
            printEnvs(step.getLogger());
        }

        if (!args.getShellCommand().isEmpty()) {
            step.getLogger().info("----------EXECUTE SHELL COMMAND-----------------");
            step.getLogger().info("  " + args.getShellCommand().getDisplayValue());
            step.getLogger().info("  " + SOSString.toString(SOSShell.executeCommand(args.getShellCommand().getValue())));
        }

        SOSCredentialStoreArguments csArgs = step.getAppArguments(SOSCredentialStoreArguments.class);
        step.getLogger().info("----------CREDENTIAL STORE-----------------");
        step.getLogger().info("  file=" + csArgs.getCredentialStoreFile());
        SOSCredentialStoreResolver r = csArgs.newResolver();
        step.getLogger().info("  resolve cs://@title=" + r.resolve("cs://@title"));

        step.getLogger().info("----------RETURN-----------------");
        if (args.getRedefineShowEnv().getValue() || !args.getReturnVariables().isEmpty()) {
            Map<String, Object> map = new HashMap<String, Object>();
            if (args.getRedefineShowEnv().getValue()) {
                map.put(args.getShowEnv().getName(), !args.getShowEnv().getValue());
            }
            if (args.getReturnVariables().getValue() != null) {
                String[] arr = args.getReturnVariables().getValue().split("__");
                for (String val : arr) {
                    String[] valArr = val.trim().split("_");
                    if (valArr.length > 1) {
                        map.put(valArr[0].trim(), valArr[1].trim());
                    }
                }
            }
            step.getLogger().info("[SUCCESS]set step outcome: %s", map);
            return step.success(map);
        } else {
            step.getLogger().info("[SUCCESS]");
            return step.success();
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
