package com.sos.jitl.jobs.examples;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
                    .getValue()));
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
        step.getLogger().info(args.getListBigDecimalValues().getName() + "=" + args.getListBigDecimalValues().getValue());
        if (args.getListBigDecimalValues().getValue() != null) {
            step.getLogger().info("  " + args.getListBigDecimalValues().getValue().getClass());
            for (Object o : args.getListBigDecimalValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListBooleanValues().getName() + "=" + args.getListBooleanValues().getValue());
        if (args.getListBooleanValues().getValue() != null) {
            step.getLogger().info("  " + args.getListBooleanValues().getValue().getClass());
            for (Object o : args.getListBooleanValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListCharsetValues().getName() + "=" + args.getListCharsetValues().getValue());
        if (args.getListCharsetValues().getValue() != null) {
            step.getLogger().info("  " + args.getListCharsetValues().getValue().getClass());
            for (Object o : args.getListCharsetValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListEnumValues().getName() + "=" + args.getListEnumValues().getValue());
        if (args.getListEnumValues().getValue() != null) {
            step.getLogger().info("  " + args.getListEnumValues().getValue().getClass());
            for (Object o : args.getListEnumValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListFileValues().getName() + "=" + args.getListFileValues().getValue());
        if (args.getListFileValues().getValue() != null) {
            step.getLogger().info("  " + args.getListFileValues().getValue().getClass());
            for (Object o : args.getListFileValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListIntegerValues().getName() + "=" + args.getListIntegerValues().getValue());
        if (args.getListIntegerValues().getValue() != null) {
            step.getLogger().info("  " + args.getListIntegerValues().getValue().getClass());
            for (Object o : args.getListIntegerValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListLongValues().getName() + "=" + args.getListLongValues().getValue());
        if (args.getListLongValues().getValue() != null) {
            step.getLogger().info("  " + args.getListLongValues().getValue().getClass());
            for (Object o : args.getListLongValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListPathValues().getName() + "=" + args.getListPathValues().getValue());
        if (args.getListPathValues().getValue() != null) {
            step.getLogger().info("  " + args.getListPathValues().getValue().getClass());
            for (Object o : args.getListPathValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListStringValues().getName() + "=" + args.getListStringValues().getValue());
        if (args.getListStringValues().getValue() != null) {
            step.getLogger().info("  " + args.getListStringValues().getValue().getClass());
            for (Object o : args.getListStringValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getListUriValues().getName() + "=" + args.getListUriValues().getValue());
        if (args.getListUriValues().getValue() != null) {
            step.getLogger().info("  " + args.getListUriValues().getValue().getClass());
            for (Object o : args.getListUriValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }

        step.getLogger().info(args.getSetIntegerValues().getName() + "=" + args.getSetIntegerValues().getValue());
        if (args.getSetIntegerValues().getValue() != null) {
            step.getLogger().info("  " + args.getSetIntegerValues().getValue().getClass());
            for (Object o : args.getSetIntegerValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
        }
        step.getLogger().info(args.getSetStringValues().getName() + "=" + args.getSetStringValues().getValue());
        if (args.getSetStringValues().getValue() != null) {
            step.getLogger().info("  " + args.getSetStringValues().getValue().getClass());
            for (Object o : args.getSetStringValues().getValue()) {
                step.getLogger().info("    " + o + "(" + o.getClass() + ")");
            }
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
