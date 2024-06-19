package com.sos.jitl.jobs.examples;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.credentialstore.CredentialStoreArguments.CredentialStoreResolver;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.js7.job.DetailValue;
import com.sos.js7.job.Job;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.OrderProcessStepOutcome;

public class InfoJob extends Job<InfoJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoJob.class);

    public InfoJob(JobContext jobContext) {
        super(jobContext);
        LOGGER.info("[CONSTRUCTOR]arguments=" + getJobEnvironment().getAllArgumentsAsNameValueMap());
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
    public void processOrder(OrderProcessStep<InfoJobArguments> step) throws Exception {
        InfoJobArguments args = step.getDeclaredArguments();

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
        step.getLogger().info("[scala][jobEnvironment.getAllArgumentsAsNameValueMap()]" + getJobEnvironment().getAllArgumentsAsNameValueMap());

        step.getLogger().info("----------Workflow-----------------");
        step.getLogger().info("[java][name]" + step.getWorkflowName());
        step.getLogger().info("[java][versionId]" + step.getWorkflowVersionId());
        step.getLogger().info("[java][position]" + step.getWorkflowPosition());
        // step.getLogger().error("[position written to err]" + step.getWorkflowPosition());

        step.getLogger().info("----------ORDER-----------------");
        step.getLogger().info("[java][id]" + step.getOrderId());
        step.getLogger().info("[java][step.getOrderArgumentsAsNameValueMap]" + step.getOrderArgumentsAsNameValueMap());
        // step.asScala().scope().evaluator().eval(NamedValue.MODULE$.)

        // Either<Problem,Value> checkedValue = step.byJobResourceAndName(JobResourcePath.of("MY-JOB-RESOURCE"), "stringSetting");
        step.getLogger().info("-CONVERTED JOB RESOURCES------------------");
        Map<String, DetailValue> resources = step.getJobResourcesArgumentsAsNameDetailValueMap();
        resources.entrySet().stream().forEach(e -> {
            DetailValue v = e.getValue();
            step.getLogger().info(" %s=%s (job resource=%s)", e.getKey(), v.getValue(), v.getSource());
        });

        step.getLogger().info("-CONVERTED HISTORIC OUTCOME (last var values)-----------------");
        step.getLogger().info("[java][all]" + step.getLastOutcomes());
        step.getLogger().info("[java][succeeded]" + step.getLastSucceededOutcomes());
        step.getLogger().info("[java][failed]" + step.getLastFailedOutcomes());

        step.getLogger().info("----------NODE/STEP-----------------");
        step.getLogger().info("[java][agentId]" + step.getAgentId());
        step.getLogger().info("[java][name]" + step.getJobName());

        step.getLogger().info("----------ALL CURRENT declared/not declared argumens-----------------");
        Map<String, JobArgument<?>> allcmap = step.getAllArguments();
        allcmap.entrySet().stream().forEach(e -> {
            step.getLogger().info("[java][%s][%s=%s]%s", e.getValue().getType(), e.getKey(), e.getValue().getDisplayValue(), SOSString.toString(e
                    .getValue(), true));
        });

        if (args.getShowEnv().getValue()) {
            printEnvs(step.getLogger());
        }

        step.getLogger().info("----------step.getJobResourcesEnv-----------------");
        try {
            Map<String, String> m = step.getJobResourcesEnv();
            step.getLogger().info("[step.getJobResourcesEnv]" + m);
        } catch (Throwable e) {
            step.getLogger().warn(String.format("[step.getJobResourcesEnv]%s", e.toString()), e);
        }

        if (!args.getShellCommand().isEmpty()) {
            step.getLogger().info("----------EXECUTE SHELL COMMAND-----------------");
            step.getLogger().info("  " + args.getShellCommand().getDisplayValue());
            step.getLogger().info("  " + SOSString.toString(SOSShell.executeCommand(args.getShellCommand().getValue())));
        }

        CredentialStoreArguments csArgs = step.getIncludedArguments(CredentialStoreArguments.class);
        step.getLogger().info("----------CREDENTIAL STORE-----------------");
        step.getLogger().info("  file=" + csArgs.getFile());
        CredentialStoreResolver r = csArgs.newResolver();
        step.getLogger().info("  resolve cs://@title=" + r.resolve("cs://@title"));

        step.getLogger().info("----------COLLECTION ARGUMENTS -----------------");
        showCollectionArgument(step, args.getListBigDecimalValues());
        showCollectionArgument(step, args.getListBooleanValues());
        showCollectionArgument(step, args.getListCharsetValues());
        showCollectionArgument(step, args.getListEnumValues());
        showCollectionArgument(step, args.getListFileValues());
        showCollectionArgument(step, args.getListIntegerValues());
        showCollectionArgument(step, args.getListLongValues());
        showCollectionArgument(step, args.getListPathValues());
        showCollectionArgument(step, args.getListStringValues());
        showCollectionArgument(step, args.getListUriValues());
        showCollectionArgument(step, args.getSetIntegerValues());
        showCollectionArgument(step, args.getSetStringValues());

        step.getLogger().info("----------MAP ARGUMENTS -----------------");
        showMapArgument(step, args.getMapStringValues());
        showMapArgument(step, args.getMapIntegerValues());
        showMapArgument(step, args.getMapPathValues());
        showMapArgument(step, args.getMapObjectValues());
        showMapArgument(step, args.getMapWildcardValues());

        if (args.getPathArgument().getValue() != null) {
            step.getLogger().info("----------PATH ARGUMENT -----------------");
            showSingleArgument(step, args.getPathArgument());
        }

        step.getLogger().info("----------RETURN-----------------");
        if (args.getRedefineShowEnv().getValue() || !args.getReturnVariables().isEmpty()) {
            if (args.getRedefineShowEnv().getValue()) {
                step.getOutcome().putVariable(args.getShowEnv().getName(), !args.getShowEnv().getValue());
            }
            if (args.getReturnVariables().getValue() != null) {
                String[] arr = args.getReturnVariables().getValue().split("__");
                for (String val : arr) {
                    String[] valArr = val.trim().split("_");
                    if (valArr.length > 1) {
                        step.getOutcome().putVariable(valArr[0].trim(), valArr[1].trim());
                    }
                }
            }
            step.getLogger().info("[SUCCESS]set step outcome: %s", step.getOutcome().getVariables());
        } else {
            OrderProcessStepOutcome outcome = step.getOutcome();
            outcome.putVariable("test", "my_test");
            outcome.putVariable("test_null_value", null);
            outcome.putVariable("test_boolean_value", true);
            outcome.putVariable("test_Boolean_value", Boolean.FALSE);
            outcome.putVariable("test_int_value", 1);
            outcome.putVariable("test_Integer_value", Integer.valueOf(1));
            outcome.putVariable("test_long_value", Integer.valueOf(1).longValue());
            outcome.putVariable("test_Long_value", Long.valueOf(1));
            outcome.putVariable("test_Double_value", Double.valueOf(1));
            outcome.putVariable("test_BigDecimal_value", new BigDecimal(1));
            outcome.putVariable("test_Date_value", new Date());
            outcome.putVariable("test_Instant_value", Instant.now());
            outcome.putVariable("test_LocalDate_value", LocalDate.now());

            step.getLogger().info("[SUCCESS][java][step outcome]%s", outcome);
            // step.getLogger().info("[SUCCESS]");
            // return step.success();
        }
    }

    private void showSingleArgument(OrderProcessStep<InfoJobArguments> step, JobArgument<?> a) {
        step.getLogger().info(a.getName() + "=" + a.getValue());
        if (a.getValue() != null) {
            step.getLogger().info("  " + a.getValue().getClass());
        }
    }

    private void showCollectionArgument(OrderProcessStep<InfoJobArguments> step, JobArgument<? extends Collection<?>> a) {
        step.getLogger().info(a.getName() + "=" + a.getValue());
        if (a.getValue() != null) {
            step.getLogger().info("  " + a.getValue().getClass());
            for (Object o : a.getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
    }

    private void showMapArgument(OrderProcessStep<InfoJobArguments> step, JobArgument<? extends Map<String, ?>> a) {
        step.getLogger().info(a.getName() + "=" + a.getValue());
        if (a.getValue() != null) {
            step.getLogger().info("  " + a.getValue().getClass());
            a.getValue().entrySet().forEach(e -> {
                step.getLogger().info("    " + e.getKey() + "=" + e.getValue() + "(" + e.getValue().getClass() + ")");
            });
        }
    }

    private void printEnvs(OrderProcessStepLogger logger) {
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
