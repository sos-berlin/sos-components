package com.sos.js7.job;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.js7.job.exception.JobArgumentException;

import js7.data_for_java.order.JOutcome;

public class UnitTestJobHelper<A extends JobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTestJobHelper.class);

    private final Job<A> job;
    private final UnitTestStepConfig stepConfig;

    private Map<String, String> environment;

    /** Constructor that defaults to:
     * <ol>
     * <li>Setting the Agent Config Directory system property</li>
     * <li>Resetting SSL system properties</li>
     * </ol>
     * <p>
     * 1) <strong>Setting Agent Config Directory:</strong><br>
     * Sets the system property {@code JS7_AGENT_CONFIG_DIR} to {@code src/test/resources}, so that test jobs can reliably locate configuration files during
     * execution.
     * <p>
     * 2) <strong>Resetting SSL system properties:</strong><br>
     * In some environments (e.g., Eclipse), the JVM may be started with preconfigured SSL settings such as {@code javax.net.ssl.keyStore} or {@code trustStore}
     * for accessing Maven repositories.<br/>
     * These can interfere with test behavior.<br/>
     * This constructor resets those properties to ensure a clean and predictable test environment. */
    public UnitTestJobHelper(Job<A> job) {
        this(job, true);
    }

    /** Constructor that:
     * <ol>
     * <li>Sets the Agent Config Directory system property</li>
     * <li>Optionally resets SSL system properties</li>
     * </ol>
     * <p>
     * 1) <strong>Setting Agent Config Directory:</strong><br>
     * Sets the system property {@code JS7_AGENT_CONFIG_DIR} to {@code src/test/resources}, so that test jobs can reliably locate configuration files during
     * execution.
     * <p>
     * 2) <strong>Resetting SSL system properties:</strong><br>
     * If {@code resetSslSystemProperties} is {@code true}, clears system properties such as {@code javax.net.ssl.keyStore} and {@code trustStore}.<br/>
     * This prevents inherited SSL config (e.g., from IDEs or build tools) from affecting test execution. */
    public UnitTestJobHelper(Job<A> job, boolean resetSslSystemProperties) {
        this.job = job;
        this.stepConfig = new UnitTestStepConfig();

        if (resetSslSystemProperties) {
            resetSslSystemProperties();
        }

        System.setProperty(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, SOSPath.toAbsoluteNormalizedPath("src/test/resources").toString());
        LOGGER.info("[Note Eclipse IDE]use 'Run Configurations -> Environment' to set environment variables if needed");
        // setModifiableEnvironment();
    }

    public void onStart(Map<String, Object> args) throws Exception {
        job.getJobEnvironment().setDeclaredArguments(toArgs(null, args, null).instance);
        SOSReflection.setDeclaredFieldValue(job.getJobEnvironment(), "allArguments", args);

        job.onStart();
    }

    public void onStop() throws Exception {
        job.onStop();
    }

    public JOutcome.Completed processOrder(Map<String, Object> args) throws Exception {
        return processOrder(args, null);
    }

    public JOutcome.Completed processOrder(Map<String, Object> args, SOSTimeout timeout) throws Exception {
        if (timeout == null) {
            timeout = new SOSTimeout(2, TimeUnit.MINUTES);
        }

        final OrderProcessStep<A> step = newOrderProcessStep(args);
        return CompletableFuture.supplyAsync(() -> {
            try {
                ArgumentsResult r = toArgs(step, args, job.beforeCreateJobArguments(null, step));
                step.init4unittest(r.instance, r.undeclared, stepConfig);

                step.checkAndLogParameterization(null, null);
                this.job.processOrder(step);
                return step.processed();
            } catch (Throwable e) {
                return step.failed(e.toString(), e);
            }
        }).get(timeout.getInterval(), timeout.getTimeUnit());
    }

    public void setEnvVar(String n, String v) {
        if (environment != null) {
            environment.put(n, v);
        }
    }

    public Job<A> getJob() {
        return job;
    }

    public UnitTestStepConfig getStepConfig() {
        return stepConfig;
    }

    private static void resetSslSystemProperties() {
        System.setProperty("javax.net.ssl.keyStore", "");
        System.setProperty("javax.net.ssl.keyStorePassword", "");

        System.setProperty("javax.net.ssl.trustStore", "");
        System.setProperty("javax.net.ssl.trustStorePassword", "");
    }

    private OrderProcessStep<A> newOrderProcessStep(Map<String, Object> args) throws Exception {
        // OrderProcessStep<A> step = new OrderProcessStep<A>(job.getJobEnvironment(), null);
        OrderProcessStep<A> step = new OrderProcessStep<A>(job.getJobEnvironment(), null) {

            @Override
            protected <AJ extends JobArguments> AJ onExecuteJobCreateArguments(Job<AJ> job, OrderProcessStep<AJ> step,
                    List<JobArgumentException> exceptions) throws Exception {

                AJ aj = job.beforeCreateJobArguments(exceptions, step);
                aj = job.getJobArgumensClass().getDeclaredConstructor().newInstance();
                aj = job.setDeclaredJobArguments(exceptions, aj, args, step);
                return aj;
            }
        };
        return step;
    }

    private ArgumentsResult toArgs(OrderProcessStep<A> step, Map<String, Object> args, A instance) throws Exception {
        instance = instance == null ? job.getJobArgumensClass().getDeclaredConstructor().newInstance() : instance;
        Set<String> declared = getDeclaredJobArgumentNames(instance);

        List<JobArgumentException> exceptions = new ArrayList<JobArgumentException>();
        instance = job.setDeclaredJobArguments(exceptions, instance, args, step);

        ArgumentsResult r = new ArgumentsResult();
        r.instance = instance;
        r.undeclared = args.entrySet().stream().filter(e -> !declared.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue));
        return r;
    }

    private Set<String> getDeclaredJobArgumentNames(JobArguments instance) throws Exception {
        Set<String> declared = new HashSet<>();

        if (instance.getIncludedArguments() != null && instance.getIncludedArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument<?>>> e : instance.getIncludedArguments().entrySet()) {
                for (JobArgument<?> arg : e.getValue()) {
                    if (arg.getName() == null) {
                        continue;
                    }
                    declared.add(arg.getName());
                }
            }
        }
        if (instance.hasDynamicArgumentFields()) {
            for (JobArgument<?> arg : instance.getDynamicArgumentFields()) {
                if (arg.getName() == null) {
                    continue;
                }
                declared.add(arg.getName());
            }
        }
        List<Field> fields = JobHelper.getJobArgumentFields(instance);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument<?> arg = (JobArgument<?>) field.get(instance);
                if (arg != null) {
                    if (arg.getName() == null) {
                        continue;
                    }
                    declared.add(arg.getName());
                }
            } catch (Throwable e) {
            }
        }
        return declared;
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private void setModifiableEnvironment() {
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Field props = pe.getDeclaredField("theCaseInsensitiveEnvironment");
            props.setAccessible(true);
            environment = (Map<String, String>) props.get(null);
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_HOME, "");
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "");
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_WORK_DIR, "");
        } catch (Throwable e) {
            LOGGER.info(String.format(
                    "[can't set env][use 'Run Configurations -> Environment' instead or allow access with '--add-opens java.base/java.util=ALL-UNNAMED']%s",
                    e.toString()));
        }
    }

    private void setDefaultEnvVar(String n, String v) {
        if (environment != null && environment.get(n) == null) {
            environment.put(n, v);
        }
    }

    private class ArgumentsResult {

        private A instance;
        private Map<String, Object> undeclared;
    }

    public class UnitTestStepConfig {

        private String controllerId;
        private String orderId;
        private String agentId;
        private String jobInstructionLabel;
        private String jobName;
        private String workflowPath;
        private String workflowName;
        private String workflowVersionId;
        private String workflowPosition;

        public String getControllerId() {
            return controllerId == null ? "test-controller" : controllerId;
        }

        public void setControllerId(String val) {
            controllerId = val;
        }

        public String getOrderId() {
            return orderId == null ? "test-order" : orderId;
        }

        public void setOrderId(String val) {
            orderId = val;
        }

        public String getAgentId() {
            return agentId == null ? "test-agent" : agentId;
        }

        public void setAgentId(String val) {
            agentId = val;
        }

        public String getJobInstructionLabel() {
            return jobInstructionLabel;
        }

        public void setJobInstructionLabel(String val) {
            jobInstructionLabel = val;
        }

        public String getJobName() {
            return jobName == null ? "test-job" : jobName;
        }

        public void setJobName(String val) {
            jobName = val;
        }

        public String getWorkflowPath() {
            return workflowPath == null ? "/" + getWorkflowName() : workflowPath;
        }

        public void setWorkflowPath(String val) {
            workflowPath = val;
        }

        public String getWorkflowName() {
            return workflowName == null ? "test-workflow" : workflowName;
        }

        public void setWorkflowName(String val) {
            workflowName = val;
        }

        public String getWorkflowVersionId() {
            return workflowVersionId == null ? "test-versionId-123" : workflowVersionId;
        }

        public void setWorkflowVersionId(String val) {
            workflowVersionId = val;
        }

        public String getWorkflowPosition() {
            return workflowPosition == null ? "0" : workflowPosition;
        }

        public void setWorkflowPosition(String val) {
            workflowPosition = val;
        }

    }
}
