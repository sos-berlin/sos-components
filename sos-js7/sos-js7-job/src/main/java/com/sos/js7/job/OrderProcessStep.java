package com.sos.js7.job;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.exception.ISOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.keystore.KeyStoreArguments;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.js7.job.JobArgument.Type;
import com.sos.js7.job.JobArguments.LogLevel;
import com.sos.js7.job.UnitTestJobHelper.UnitTestStepConfig;
import com.sos.js7.job.ValueSource.ValueSourceType;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.exception.JobProblemException;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;
import com.sos.js7.job.resolver.JobArgumentValueResolverCache;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.job.JobResourcePath;
import js7.data.order.HistoricOutcome;
import js7.data.order.OrderOutcome;
import js7.data.order.OrderOutcome.Completed;
import js7.data.value.NumberValue;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.launcher.forjava.internal.BlockingInternalJob;
import js7.launcher.forjava.internal.BlockingInternalJob.JobContext;
import scala.collection.JavaConverters;

public class OrderProcessStep<A extends JobArguments> {

    private static final String INTERNAL_ORDER_PREPARATION_PARAMETER_JS7_WORKFLOW_PATH = "js7Workflow.path";
    private static final String SINGLE_CANCELABLE_RESOURCE_IDENTIFIER = "js7_single_cancelable_resource";

    private final JobEnvironment<A> jobEnvironment;
    private final BlockingInternalJob.Step internalStep;
    private final OrderProcessStepLogger logger;
    private final OrderProcessStepOutcome outcome;

    /* Arguments of <A extends JobArguments> */
    private A declaredArguments;
    /* declaredArguments + declared includedArguments */
    private List<JobArgument<?>> allDeclaredArguments;
    private Map<String, JobArgument<?>> allArguments;
    /* execute another job */
    private ExecuteJobBean executeJobBean;
    @SuppressWarnings("rawtypes")
    private Map<String, OrderProcessStep> cancelableExecuteJobs;

    private Map<String, Map<String, DetailValue>> lastOutcomes;
    private Map<String, DetailValue> jobResourcesValues;
    private Map<String, Object> unitTestUndeclaredArguments;
    private Map<String, Object> cancelableResources;
    private Set<String> orderPreparationParameterNames;
    private List<String> resolverPrefixes;

    private String controllerId;
    private String orderId;
    private String agentId;
    private String jobInstructionLabel;
    private String jobName;
    private String workflowPath;
    private String workflowName;
    private String workflowVersionId;
    private String workflowPosition;
    private String stepInfo;

    protected OrderProcessStep(JobEnvironment<A> jobEnvironment, BlockingInternalJob.Step internalStep) {
        this.jobEnvironment = jobEnvironment;
        this.internalStep = internalStep;
        this.logger = new OrderProcessStepLogger(this.internalStep);
        this.outcome = new OrderProcessStepOutcome();
        this.resolverPrefixes = JobArgumentValueResolverCache.getResolverPrefixes();
    }

    // executeJob
    private OrderProcessStep(JobEnvironment<A> jobEnvironment, OrderProcessStep<?> step) {
        this.jobEnvironment = jobEnvironment;
        this.internalStep = step.getInternalStep();
        this.logger = step.logger;
        this.outcome = step.outcome;
        this.resolverPrefixes = step.resolverPrefixes;
    }

    protected BlockingInternalJob.Step getInternalStep() {
        return internalStep;
    }

    /** Executes another job of the specified job class.
     * <p>
     * All arguments from the current job step are automatically populated to the job being executed.<br/>
     *
     * @param clazz the job class to execute
     * @param <AJ> the generic type of the job's arguments (used internally for type safety)
     * @throws Exception if the job cannot be executed */
    public <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz) throws Exception {
        executeJob(clazz, (Map<String, JobArgument<?>>) null, false);
    }

    /** Executes another job of the specified job class with additional arguments.
     * <p>
     * This method behaves like {@link #executeJob(Class)}, which automatically populates all arguments from the current job step.<br/>
     * Additionally, extra arguments can be provided via the {@code executeJobArguments} map.<br />
     * This can be helpful when some arguments are not set in the current job step or need to be calculated/recalculated for the job being executed.
     *
     * @param clazz the job class to execute
     * @param executeJobArguments a map of additional argument names and values to pass to the job
     * @param <AJ> the generic type of the job's arguments (used internally for type safety)
     * @throws Exception if the job cannot be executed */
    public <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz, Map<String, Object> executeJobArguments) throws Exception {
        Map<String, JobArgument<?>> map = null;
        if (executeJobArguments != null && executeJobArguments.size() > 0) {
            map = new HashMap<>();
            for (Map.Entry<String, Object> e : executeJobArguments.entrySet()) {
                map.put(e.getKey(), JobArgument.toUndeclaredExecuteJobArgument(e.getKey(), e.getValue()));
            }
        }
        executeJob(clazz, map, false);
    }

    /** Executes another job of the specified job class with additional arguments.
     * <p>
     * This method behaves like {@link #executeJob(Class)}, which automatically populates all arguments from the current job step.<br/>
     * Additionally, extra arguments can be provided via the {@code executeJobArguments} varargs.<br/>
     * This can be helpful when some arguments are not set in the current job step or need to be calculated/recalculated for the job being executed.
     *
     * @param clazz the job class to execute
     * @param executeJobArguments additional {@link JobArgument} objects to pass to the job
     * @param <AJ> the generic type of the job's arguments (used internally for type safety)
     * @throws Exception if the job cannot be executed */
    public <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz, JobArgument<?>... executeJobArguments) throws Exception {
        executeJob(clazz, executeJobArguments == null || executeJobArguments.length == 0 ? null : Arrays.asList(executeJobArguments));
    }

    /** Executes another job of the specified job class with additional arguments.
     * <p>
     * This method behaves like {@link #executeJob(Class)}, which automatically populates all arguments from the current job step.<br/>
     * Additionally, extra arguments can be provided via the {@code executeJobArguments} collection.<br />
     * This can be helpful when some arguments are not set in the current job step or need to be calculated/recalculated for the job being executed.
     *
     * @param clazz the job class to execute
     * @param executeJobArguments a map of additional argument names and values to pass to the job
     * @param <AJ> the generic type of the job's arguments (used internally for type safety)
     * @throws Exception if the job cannot be executed */
    public <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz, Collection<JobArgument<?>> executeJobArguments)
            throws Exception {
        Map<String, JobArgument<?>> map = null;
        if (executeJobArguments != null && executeJobArguments.size() > 0) {
            map = new HashMap<>();
            for (JobArgument<?> arg : executeJobArguments) {
                map.put(arg.getName(), arg.toUndeclaredExecuteJobArgument());
            }
        }
        executeJob(clazz, map, true);
    }

    // to overwrite by UnitTestJobHelper
    protected <AJ extends JobArguments> AJ onExecuteJobCreateArguments(Job<AJ> job, OrderProcessStep<AJ> step, List<JobArgumentException> exceptions)
            throws Exception {
        AJ args = job.beforeCreateJobArguments(exceptions, step);
        return job.createDeclaredJobArguments(exceptions, step, args);
    }

    private <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz, Map<String, JobArgument<?>> executeJobArguments,
            boolean updateDeclaredArgumentsDefinition) throws Exception {
        Job<AJ> job = null;
        try {
            job = clazz.getDeclaredConstructor(JobContext.class).newInstance((JobContext) null);
        } catch (Throwable e) {
            job = clazz.getDeclaredConstructor().newInstance();
        }

        JobEnvironment<AJ> je = new JobEnvironment<AJ>(clazz.getSimpleName(), jobEnvironment);
        job.setJobEnvironment(je);

        if (cancelableExecuteJobs == null) {
            cancelableExecuteJobs = new ConcurrentHashMap<>();
        }
        try {
            job.onStart();

            OrderProcessStep<AJ> step = new OrderProcessStep<>(je, this);
            step.executeJobBean = step.new ExecuteJobBean(job, executeJobArguments, updateDeclaredArgumentsDefinition);

            List<JobArgumentException> exceptions = new ArrayList<JobArgumentException>();
            // AJ args = job.onCreateJobArguments(exceptions, step);
            // args = job.createDeclaredJobArguments(exceptions, step, args);
            AJ args = onExecuteJobCreateArguments(job, step, exceptions);
            step.applyArguments(args);

            if (step.getLogger().isDebugEnabled()) {
                step.logJobKey();
                // step.getLogger().debug(job.getClass().getSimpleName() + " Arguments:");
                // logArgumentsBySource(LogLevel.DEBUG);
                step.logAllArguments(LogLevel.DEBUG);
            }

            cancelableExecuteJobs.put(je.getJobKey(), step);
            job.processOrder(step);
        } catch (Throwable e) {
            throw e;
        } finally {
            job.onStop();
            cancelableExecuteJobs.remove(je.getJobKey());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void cancelExecuteJobs() {
        if (cancelableExecuteJobs != null && cancelableExecuteJobs.size() > 0) {
            for (Map.Entry<String, OrderProcessStep> e : cancelableExecuteJobs.entrySet()) {
                try {
                    e.getValue().getExecuteJobBean().getJob().cancelProcessOrder(e.getValue());
                } catch (Throwable t) {
                    logger.warn("[cancelExecuteJobs][" + e.getKey() + "]" + e.toString(), e);
                }
            }
            cancelableExecuteJobs.clear();
        }
        cancelableExecuteJobs = null;
    }

    protected void applyArguments(A arguments) throws Exception {
        this.declaredArguments = arguments;
        this.logger.init(arguments);

        setAllArguments();
    }

    @SuppressWarnings("rawtypes")
    protected void init4unittest(A arguments, Map<String, Object> unitTestUndeclaredArguments, UnitTestStepConfig stepConfig) throws Exception {
        this.unitTestUndeclaredArguments = unitTestUndeclaredArguments;
        if (stepConfig != null) {
            this.controllerId = stepConfig.getControllerId();
            this.orderId = stepConfig.getOrderId();
            this.agentId = stepConfig.getAgentId();
            this.jobInstructionLabel = stepConfig.getJobInstructionLabel();
            this.jobName = stepConfig.getJobName();
            this.workflowPath = stepConfig.getWorkflowPath();
            this.workflowName = stepConfig.getWorkflowName();
            this.workflowVersionId = stepConfig.getWorkflowVersionId();
            this.workflowPosition = stepConfig.getWorkflowPosition();
        }
        applyArguments(arguments);
    }

    /** Single Cancelable Resource. Sets a resource object (e.g. a database {@code Connection}) that can be invoked when the current job step is canceled.
     * <p>
     * The assigned resource will be returned via {@link #getCancelableResource()} and can react appropriately when the job step's {@code cancel} handling is
     * triggered. <br/>
     * Implementations of this job should override the corresponding cancellation callback method ({@link Job#onProcessOrderCanceled(OrderProcessStep)})<br/>
     * and retrieve the resource via {@code getCancelableResource()} in order to perform cleanup or other cancellation-related actions.
     * </p>
     * 
     * @param resource the resource object to be handled on job step cancellation */
    public void setCancelableResource(Object cancelableResource) {
        getCancelableResources().put(SINGLE_CANCELABLE_RESOURCE_IDENTIFIER, cancelableResource);
    }

    /** Single Cancelable Resource.
     * 
     * @return Cancelable Resource. */
    public Object getCancelableResource() {
        return getCancelableResources().get(SINGLE_CANCELABLE_RESOURCE_IDENTIFIER);
    }

    /** Multiple Cancelable Resources
     * 
     * @param identifier Cancelable Resource identifier
     * @param cancelableResource */
    public void addCancelableResource(String identifier, Object cancelableResource) {
        getCancelableResources().put(identifier, cancelableResource);
    }

    public synchronized Map<String, Object> getCancelableResources() {
        if (cancelableResources == null) {
            cancelableResources = new HashMap<>();
        }
        return cancelableResources;
    }

    /** Returns the logger instance.
     * 
     * @return the {@link OrderProcessStepLogger} instance */
    public OrderProcessStepLogger getLogger() {
        return logger;
    }

    /** Returns the output writer used by this step.<br/>
     * If no internal step is available (JUnit), a dummy {@link PrintWriter} backed by a {@link java.io.StringWriter} is returned.
     *
     * @return the {@link PrintWriter} for this step */
    public PrintWriter getOut() {
        if (internalStep == null) {
            return new PrintWriter(new StringWriter());
        }
        return internalStep.out();
    }

    /** Returns the error writer used by this step.<br/>
     * If no internal step is available (JUnit), a dummy {@link PrintWriter} backed by a {@link java.io.StringWriter} is returned.
     *
     * @return the {@link PrintWriter} for error output */
    public PrintWriter getErr() {
        if (internalStep == null) {
            return new PrintWriter(new StringWriter());
        }
        return internalStep.err();
    }

    /** Returns an object containing all declared arguments for this job.
     * <p>
     * The returned object extends {@link JobArguments}.
     *
     * @return the object holding all declared arguments */
    public A getDeclaredArguments() {
        return declaredArguments;
    }

    /** Returns all declared arguments for this job as a list.
     * <p>
     * This is an alternative to {@link #getDeclaredArguments()}, providing the arguments as a {@link List} of {@link JobArgument} instances.
     *
     * @return a {@link List} containing all {@link JobArgument} instances declared for this job */
    public List<JobArgument<?>> getAllDeclaredArguments() {
        return allDeclaredArguments;
    }

    /** Returns the arguments of all used job resources as a map.
     * <p>
     * The returned map contains resource argument names as keys and their corresponding {@link DetailValue} objects as values, each containing the name of the
     * job resource and the resource argument's value.
     *
     * @return a {@link Map} of resource argument names to {@link DetailValue} objects */
    public Map<String, DetailValue> getJobResourcesArgumentsAsNameDetailValueMap() {
        if (jobResourcesValues == null) {
            jobResourcesValues = jobResources2map();
        }
        return jobResourcesValues;
    }

    /** Returns the arguments of all used job resources as a simple name-value map.
     * <p>
     * Unlike {@link #getJobResourcesArgumentsAsNameDetailValueMap()}, this method returns a map where the values are the plain job resource argument values
     * extracted from {@link DetailValue} objects, ignoring their source. The keys are the job resource argument names.
     *
     * @return a {@link Map} of job resource argument names to their plain values */
    public Map<String, Object> getJobResourcesArgumentsAsNameValueMap() {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, DetailValue> entry : getJobResourcesArgumentsAsNameDetailValueMap().entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            result.put(key, entry.getValue().getValue());
        }
        return result;
    }

    /** Returns the arguments of all used job resources as a simple name-value map with string values.
     * <p>
     * Unlike {@link #getJobResourcesArgumentsAsNameValueMap()}, which returns plain values as {@link Object}, this method converts the job resource argument
     * values from {@link DetailValue} objects into {@link String}, ignoring their source. The keys are the job resource argument names.
     *
     * @return a {@link Map} of job resource argument names to their string values */
    public Map<String, String> getJobResourcesArgumentsAsNameStringValueMap() {
        return JobHelper.asNameStringValueMapFromMapWithObjectValue(getJobResourcesArgumentsAsNameValueMap());
    }

    /** Returns the environment variables declared in the job resources used by this job step.
     * <p>
     * The returned map contains the environment variable names as keys and their corresponding values as {@link String}.
     *
     * @return a {@link Map} of environment variable names to their values
     * @throws JobProblemException if the job resources environment variables cannot be retrieved */
    public Map<String, String> getJobResourcesEnv() throws JobProblemException {
        if (internalStep == null) {
            return null;
        }
        return JobHelper.getFromEither(internalStep.env()).entrySet().stream().filter(e -> e.getValue().isPresent()).collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().get()));
    }

    /** Returns the last outcomes of previous jobs as a nested map.
     * <p>
     * The outer map's keys are outcome statuses (e.g., "Succeeded" or "Failed").<br/>
     * Each value is an inner map where the key is the outcome variable name and the value is a {@link DetailValue} object containing the outcome's variable
     * source and its value.
     *
     * @return a {@link Map} of outcome status to a map of outcome variable names to {@link DetailValue} objects */
    public Map<String, Map<String, DetailValue>> getLastOutcomes() {
        if (lastOutcomes == null) {
            lastOutcomes = historicOutcomes2map();
        }
        return lastOutcomes;
    }

    /** Returns the last succeeded outcomes of previous jobs as a map.
     * <p>
     * The returned map contains outcome variable names as keys and their corresponding {@link DetailValue} objects as values.
     * <p>
     * This is a shortcut for retrieving the "Succeeded" entries from {@link #getLastOutcomes()}.
     *
     * @return a {@link Map} of outcome variable names to {@link DetailValue} objects for succeeded outcomes */
    public Map<String, DetailValue> getLastSucceededOutcomes() {
        return getLastOutcomes().get(OrderOutcome.Succeeded.class.getSimpleName());
    }

    /** Returns the last succeeded outcomes of previous jobs as a simple name-value map.
     * <p>
     * Unlike {@link #getLastSucceededOutcomes()}, this method returns a map where the values are the plain outcome values extracted from {@link DetailValue}
     * objects, ignoring their source. The keys are the outcome variable names.
     *
     * @return a {@link Map} of outcome variable names to their plain values for succeeded outcomes */
    public Map<String, Object> getLastSucceededOutcomesAsNameValueMap() {
        return JobHelper.asNameValueMapFromMapWithDetailValue(getLastSucceededOutcomes());
    }

    /** Returns the last succeeded outcomes of previous jobs as a simple name-value map with string values.
     * <p>
     * Unlike {@link #getLastSucceededOutcomesAsNameValueMap()}, which returns plain values as {@link Object}, this method converts the outcome values from
     * {@link DetailValue} objects into {@link String}, ignoring their source. The keys are the outcome variable names.
     *
     * @return a {@link Map} of outcome variable names to their string values for succeeded outcomes */
    public Map<String, String> getLastSucceededOutcomesAsNameStringValueMap() {
        return JobHelper.asNameStringValueMapFromMapWithDetailValue(getLastSucceededOutcomes());
    }

    /** Returns the last failed outcomes of previous jobs as a map.
     * <p>
     * The returned map contains outcome variable names as keys and their corresponding {@link DetailValue} objects as values.
     * <p>
     * This is a shortcut for retrieving the "Failed" entries from {@link #getLastOutcomes()}.
     *
     * @return a {@link Map} of outcome variable names to {@link DetailValue} objects for failed outcomes */
    public Map<String, DetailValue> getLastFailedOutcomes() {
        return getLastOutcomes().get(OrderOutcome.Failed.class.getSimpleName());
    }

    /** Returns the last failed outcomes of previous jobs as a simple name-value map.
     * <p>
     * Unlike {@link #getLastFailedOutcomes()}, this method returns a map where the values are the plain outcome values extracted from {@link DetailValue}
     * objects, ignoring their source. The keys are the outcome variable names.
     *
     * @return a {@link Map} of outcome variable names to their plain values for failed outcomes */
    public Map<String, Object> getLastFailedOutcomesAsNameValueMap() {
        return JobHelper.asNameValueMapFromMapWithDetailValue(getLastFailedOutcomes());
    }

    /** Returns the last failed outcomes of previous jobs as a simple name-value map with string values.
     * <p>
     * Unlike {@link #getLastFailedOutcomesAsNameValueMap()}, which returns plain values as {@link Object}, this method converts the outcome values from
     * {@link DetailValue} objects into {@link String}, ignoring their source. The keys are the outcome variable names.
     *
     * @return a {@link Map} of outcome variable names to their string values for failed outcomes */
    public Map<String, String> getLastFailedOutcomesAsNameStringValueMap() {
        return JobHelper.asNameStringValueMapFromMapWithDetailValue(getLastFailedOutcomes());
    }

    /** Returns the declared argument with the specified name.
     *
     * @param name the name of the argument to retrieve
     * @return the {@link JobArgument} corresponding to the given name, or {@code null} if no such argument exists */
    public JobArgument<?> getDeclaredArgument(String name) {
        if (allDeclaredArguments != null) {
            return allDeclaredArguments.stream().filter(a -> (a.getName() != null && a.getName().equals(name)) || (a.getNameAliases() != null && a
                    .getNameAliases().contains(name))).findAny().orElse(null);
        }
        return null;
    }

    /** Returns the value of the declared argument with the specified name.
     *
     * @param name the name of the argument whose value is to be retrieved
     * @return the value of the argument as an {@link Object}, or {@code null} if no such argument exists */
    public Object getDeclaredArgumentValue(String name) {
        // JobArgument<?> instead of JobArgument<A> because of dynamicArguments
        JobArgument<?> arg = getDeclaredArgument(name);
        return arg == null ? null : arg.getValue();
    }

    /** Returns the value of the declared argument with the specified name as a {@link String}.
     * <p>
     * Unlike {@link #getDeclaredArgumentValue()}, which returns the value as an {@link Object}, this method converts the argument value into a {@link String}.
     *
     * @param name the name of the argument whose value is to be retrieved
     * @return the argument value as a {@link String}, or {@code null} if no such argument exists */
    public String getDeclaredArgumentValueAsString(String name) {
        Object obj = getDeclaredArgumentValue(name);
        return obj == null ? null : obj.toString();
    }

    /** Returns the value of the argument with the specified name, searching all available arguments.
     * <p>
     * Unlike {@link #getDeclaredArgumentValue(String)}, which only looks at declared arguments, this method searches through all arguments of the job,
     * including declared, outcome, resource-provided arguments, etc.
     *
     * @param name the name of the argument whose value is to be retrieved
     * @return the argument value as an {@link Object}, or {@code null} if no such argument exists */
    public Object getArgumentValue(String name) {
        if (allArguments == null) {
            return null;
        }
        JobArgument<?> arg = allArguments.get(name);
        return arg == null ? null : arg.getValue();
    }

    /** Returns the value of the declared argument with the specified name as a {@link String}, searching all available arguments.
     * <p>
     * Unlike {@link #getArgumentValue()}, which returns the value as an {@link Object}, this method converts the argument value into a {@link String}.
     *
     * @param name the name of the argument whose value is to be retrieved
     * @return the argument value as a {@link String}, or {@code null} if no such argument exists */
    public String getArgumentValueAsString(String name) {
        Object obj = getArgumentValue(name);
        return obj == null ? null : obj.toString();
    }

    /** Retrieves the value of the specified argument **before it has been categorized or finalized**.
     * 
     * <p>
     * This method is intended to be called only during the {@link Job#beforeCreateJobArguments(List, OrderProcessStep)} phase,<br/>
     * i.e., before the Job API has classified arguments into declared or undeclared categories.<br />
     * 
     * Processing priority: see {@link #setAllArguments()} */
    public Object getPreAssignedArgumentValue(String argName) {
        // Preference 1 (HIGHEST) - Succeeded Outcomes
        Map<String, DetailValue> lastSucceededOutcomes = getLastSucceededOutcomes();
        if (!SOSCollection.isEmpty(lastSucceededOutcomes)) {
            DetailValue v = lastSucceededOutcomes.get(argName);
            if (v != null) {
                return v.getValue();
            }
        }
        if (internalStep != null) {
            // Preference 2 - Order Variables (Node Arguments are unknown)
            Map<String, Object> map = JobHelper.asJavaValues(internalStep.order().arguments());
            if (!SOSCollection.isEmpty(map)) {
                if (map.containsKey(argName)) {
                    return map.get(argName);
                }
            }

            // Preference 3 - JobArgument
            map = JobHelper.asJavaValues(internalStep.arguments());
            if (!SOSCollection.isEmpty(map)) {
                if (map.containsKey(argName)) {
                    return map.get(argName);
                }
            }
        }

        // Preference 4 (LOWEST) - JobResources
        Map<String, DetailValue> resources = getJobResourcesArgumentsAsNameDetailValueMap();
        if (!SOSCollection.isEmpty(resources)) {
            DetailValue v = resources.get(argName);
            if (v != null) {
                return v.getValue();
            }
        }

        // Preference 5 - JobContext.jobArguments()
        if (jobEnvironment.getAllArgumentsAsNameValueMap().containsKey(argName)) {
            return jobEnvironment.getAllArgumentsAsNameValueMap().get(argName);
        }

        setOrderPreparationParameterNames();
        // order preparation default values
        if (orderPreparationParameterNames != null && orderPreparationParameterNames.contains(argName)) {
            for (String name : orderPreparationParameterNames) {
                try {
                    return getNamedValue(name);
                } catch (Exception e1) {
                }
            }
        }

        if (unitTestUndeclaredArguments != null) {
            if (unitTestUndeclaredArguments.containsKey(argName)) {
                return unitTestUndeclaredArguments.get(argName);
            }
        }
        return null;
    }

    private void setAllArguments() throws Exception {
        allArguments = new TreeMap<>();

        setOrderPreparationParameterNames();

        // DECLARED Arguments
        setAllDeclaredArguments();
        if (!SOSCollection.isEmpty(allDeclaredArguments)) {
            allDeclaredArguments.stream().forEach(a -> {
                if (!allArguments.containsKey(a.getName())) {
                    allArguments.put(a.getName(), a);
                }
                if (a.getNameAliases() != null) {
                    for (String n : a.getNameAliases()) {
                        if (!allArguments.containsKey(n)) {
                            allArguments.put(n, a);
                        }
                    }
                }
            });
        }

        if (hasExecuteJobArguments()) {
            executeJobBean.arguments.entrySet().stream().forEach(e -> {
                if (!allArguments.containsKey(e.getKey())) {
                    allArguments.put(e.getKey(), e.getValue());
                }
            });
        }

        // for preference see ABlockingJob.createJobArguments/setJobArgument
        // Preference 1 (HIGHEST) - Succeeded Outcomes
        Map<String, DetailValue> lastSucceededOutcomes = getLastSucceededOutcomes();
        if (!SOSCollection.isEmpty(lastSucceededOutcomes)) {
            lastSucceededOutcomes.entrySet().stream().forEach(e -> {
                // if (!allArguments.containsKey(e.getKey()) && !JobHelper.NAMED_NAME_RETURN_CODE.equals(e.getKey())) {
                if (!allArguments.containsKey(e.getKey())) {
                    ValueSource vs = new ValueSource(ValueSourceType.LAST_SUCCEEDED_OUTCOME);
                    vs.setSource(e.getValue().getSource());
                    try {
                        allArguments.put(e.getKey(), JobArgument.createUndeclaredArgument(e.getKey(), e.getValue().getValue(), vs));
                    } catch (Throwable ex) {
                        getLogger().error("[LastSucceededOutcomes][" + e.getKey() + "]" + ex.toString());
                    }
                }
            });
        }
        if (internalStep != null) {
            // Preference 2 - Order Variables (Node Arguments are unknown)
            Map<String, Object> map = JobHelper.asJavaValues(internalStep.order().arguments());
            if (!SOSCollection.isEmpty(map)) {
                map.entrySet().stream().forEach(e -> {
                    if (!allArguments.containsKey(e.getKey())) {
                        try {
                            allArguments.put(e.getKey(), JobArgument.createUndeclaredArgument(e.getKey(), e.getValue(), new ValueSource(
                                    ValueSourceType.ORDER)));
                        } catch (Throwable ex) {
                            getLogger().error("[OrderVariables][" + e.getKey() + "]" + ex.toString());
                        }
                    }
                });
            }

            // Preference 3 - JobArgument
            map = JobHelper.asJavaValues(internalStep.arguments());
            if (!SOSCollection.isEmpty(map)) {
                map.entrySet().stream().forEach(e -> {
                    if (!allArguments.containsKey(e.getKey())) {
                        try {
                            allArguments.put(e.getKey(), JobArgument.createUndeclaredArgument(e.getKey(), e.getValue(), new ValueSource(
                                    ValueSourceType.JOB)));
                        } catch (Throwable ex) {
                            getLogger().error("[JobArgument][" + e.getKey() + "]" + ex.toString());
                        }
                    }
                });
            }
        }
        // Preference 4 (LOWEST) - JobResources
        Map<String, DetailValue> resources = getJobResourcesArgumentsAsNameDetailValueMap();
        if (!SOSCollection.isEmpty(resources)) {
            resources.entrySet().stream().forEach(e -> {
                String name = e.getKey();
                DetailValue dv = e.getValue();
                JobArgument<?> aja = allArguments.get(name);
                // if (!allArguments.containsKey(e.getKey())) {
                // TODO - clarify what it means ???: workaround js: job resource changed and declaredArgumens defined
                if (aja == null || aja.getValue() == null) {// workaround js: job resource changed and declaredArgumens defined
                    try {
                        ValueSource vs = new ValueSource(ValueSourceType.JOB_RESOURCE);
                        vs.setSource(dv.getSource());
                        if (aja == null) { // create an undeclared
                            allArguments.put(name, JobArgument.createUndeclaredArgument(name, dv.getValue(), vs));
                        } else {
                            // aja.setValueSource(vs);
                        }
                        // allArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue().getValue(), vs));
                    } catch (Throwable ex) {
                        getLogger().error("[JobResources][" + dv.getSource() + "][" + e.getKey() + "]" + ex.toString());
                    }
                }
            });
        }

        // Preference 5 - JobContext.jobArguments()
        jobEnvironment.getAllArgumentsAsNameValueMap().entrySet().stream().forEach(e -> {
            if (!allArguments.containsKey(e.getKey())) {
                try {
                    ValueSource vs = new ValueSource(ValueSourceType.JOB_ARGUMENT);
                    allArguments.put(e.getKey(), JobArgument.createUndeclaredArgument(e.getKey(), e.getValue(), vs));
                } catch (Throwable ex) {
                    getLogger().error("[JobEnvironment.JobArgument][" + e.getKey() + "]" + ex.toString());
                }
            }
        });

        // order preparation default values
        if (orderPreparationParameterNames != null) {
            for (String name : orderPreparationParameterNames) {
                if (!allArguments.containsKey(name)) {
                    ValueSource vs = new ValueSource(ValueSourceType.ORDER_PREPARATION);
                    try {
                        Object o = getNamedValue(name);
                        JobArgument<?> ar = JobArgument.createUndeclaredArgument(name, o, vs);
                        // ar.setIsDirty(false);
                        allArguments.put(name, ar);
                    } catch (Throwable e1) {
                        getLogger().error("[orderPreparation][" + name + "]" + e1.toString());
                    }
                }
            }
        }

        if (unitTestUndeclaredArguments != null) {
            unitTestUndeclaredArguments.entrySet().stream().forEach(e -> {
                if (!allArguments.containsKey(e.getKey())) {
                    try {
                        allArguments.put(e.getKey(), JobArgument.createUndeclaredArgument(e.getKey(), e.getValue(), new ValueSource(
                                ValueSourceType.JOB_ARGUMENT)));
                    } catch (Throwable ex) {
                        getLogger().error("[unitTestUndeclaredArguments][" + e.getKey() + "]" + ex.toString(), e);
                    }
                }
            });
        }

        resolveArgumentValues();
    }

    private void resolveArgumentValues() throws Exception {
        if (allArguments != null) {
            Map<String, List<JobArgument<?>>> groupedArguments = allArguments.values().parallelStream().flatMap(arg -> getResolverPrefixes()
                    .parallelStream().filter(prefix -> arg.hasValueStartsWith(prefix)).<Map.Entry<String, JobArgument<?>>> map(
                            prefix -> new AbstractMap.SimpleEntry<>(prefix, arg))).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors
                                    .mapping(Map.Entry::getValue, Collectors.toList())));
            for (Map.Entry<String, List<JobArgument<?>>> entry : groupedArguments.entrySet()) {
                try {
                    JobArgumentValueResolverCache.resolve(entry.getKey(), logger, entry.getValue(), allArguments);
                } catch (Throwable e) {
                    Throwable ex = e.getCause() == null ? e : e.getCause();
                    throw new JobArgumentException(String.format("[%s]%s", JobArgumentValueResolverCache.getResolverClassName(entry.getKey()), ex
                            .toString()), ex);
                }
            }
        }
    }

    /** Returns all available arguments as a map.
     * <p>
     * The map contains argument names as keys and their corresponding {@link JobArgument} objects as values.<br/>
     * This includes declared arguments as well as arguments from outcomes, job resources, or other sources.
     *
     * @return a {@link Map} of argument names to {@link JobArgument} objects */
    public Map<String, JobArgument<?>> getAllArguments() {
        return allArguments;
    }

    /** Returns all available arguments of the specified type as a map.
     * <p>
     * The map contains argument names as keys and their corresponding {@link JobArgument} objects as values.<br />
     * The {@code type} parameter filters which arguments to include,<br/>
     * e.g., {@link JobArgument.Type#DECLARED} or {@link JobArgument.Type#UNDECLARED}.
     *
     * @param type the type of arguments to include in the returned map
     * @return a {@link Map} of argument names to {@link JobArgument} objects of the specified type */
    public Map<String, JobArgument<?>> getAllArguments(JobArgument.Type type) {
        return allArguments.entrySet().stream().filter(a -> a.getValue().getType().equals(type)).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue));
    }

    /** Returns all available arguments as a simple name-value map.
     * <p>
     * The map contains argument names as keys and their corresponding values as {@link Object}.<br/>
     * This includes declared arguments as well as arguments from outcomes, job resources, or other sources.
     *
     * @return a {@link Map} of argument names to their values */
    public Map<String, Object> getAllArgumentsAsNameValueMap() {
        return JobHelper.asNameValueMap(allArguments);
    }

    /** Returns all available arguments as a simple name-value map with string values.
     * <p>
     * Unlike {@link #getAllArgumentsAsNameValueMap()}, which returns values as {@link Object}, this method converts all argument values into {@link String}.
     * The map contains argument names as keys and their string values as values.
     *
     * @return a {@link Map} of argument names to their string values */
    public Map<String, String> getAllArgumentsAsNameStringValueMap() {
        return JobHelper.asNameStringValueMap(allArguments);
    }

    /** Returns all undeclared arguments as a simple name-value map.
     * <p>
     * The map contains argument names as keys and their corresponding values as {@link Object}.<br/>
     * This includes undeclared arguments as well as arguments from outcomes, job resources, or other sources.
     *
     * @return a {@link Map} of undeclared argument names to their values */
    public Map<String, Object> getUndeclaredArgumentsAsNameValueMap() {
        return JobHelper.asNameValueMap(getAllArguments(Type.UNDECLARED));
    }

    /** Returns all undeclared arguments as a simple name-value map with string values.
     * <p>
     * Unlike {@link #getUndeclaredArgumentsAsNameValueMap()}, which returns values as {@link Object}, this method converts all argument values into
     * {@link String}. The map contains argument names as keys and their string values as values.
     *
     * @return a {@link Map} of undeclared argument names to their string values */
    public Map<String, String> getUndeclaredArgumentsAsNameStringValueMap() {
        return JobHelper.asNameStringValueMap(getAllArguments(Type.UNDECLARED));
    }

    /** Returns the arguments provided by the order as a simple name-value map.
     * <p>
     * The map contains argument names as keys and their corresponding values as {@link Object}.<br/>
     * These arguments are sourced specifically from the order.
     *
     * @return a {@link Map} of order argument names to their values */
    public Map<String, Object> getOrderArgumentsAsNameValueMap() {
        if (internalStep == null) {
            return Collections.emptyMap();
        }
        return JobHelper.asJavaValues(internalStep.order().arguments());
    }

    /** Returns the arguments provided by the order as a simple name-value map with string values.
     * <p>
     * Unlike {@link #getOrderArgumentsAsNameValueMap()}, which returns values as {@link Object}, this method converts all argument values into {@link String}.
     * The map contains order argument names as keys and their string values as values.
     *
     * @return a {@link Map} of order argument names to their string values */
    public Map<String, String> getOrderArgumentsAsNameStringValueMap() {
        return JobHelper.asNameStringValueMapFromMapWithObjectValue(getOrderArgumentsAsNameValueMap());
    }

    /** Returns the step outcome object used to define the result of this job step.
     * <p>
     * The returned {@link OrderProcessStepOutcome} can be used, for example, to set the return code and assign outcome variables.<br/>
     * Both the return code and the outcome variables are automatically propagated to and available for subsequent jobs in the workflow.
     *
     * @return the {@link OrderProcessStepOutcome} for this job step */
    public OrderProcessStepOutcome getOutcome() {
        return outcome;
    }

    protected Object getNamedValue(final JobArgument<?> arg) throws JobProblemException {
        if (internalStep == null) {
            return null;
        }
        Object val = getNamedValue(arg.getName());
        if (val == null && arg.getNameAliases() != null) {
            for (String name : arg.getNameAliases()) {
                val = getNamedValue(name);
                if (val != null) {
                    break;
                }
            }
        }
        return val;
    }

    private Object getNamedValue(final String name) throws JobProblemException {
        Optional<Either<Problem, Value>> opt = internalStep.namedValue(name);
        if (opt.isPresent()) {
            return JobHelper.asJavaValue(JobHelper.getFromEither(opt.get()));
        }
        return null;
    }

    private void setAllDeclaredArguments() {
        if (declaredArguments == null) {
            allDeclaredArguments = null;
            return;
        }
        if (allDeclaredArguments == null) {
            List<Field> fields = JobHelper.getJobArgumentFields(declaredArguments);
            List<JobArgument<?>> l = new ArrayList<>();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    JobArgument<?> arg = (JobArgument<?>) field.get(declaredArguments);
                    if (arg != null) {
                        if (arg.getName() == null) {// internal usage
                            continue;
                        }
                        l.add(arg);
                    }
                } catch (Throwable e) {
                    logger.warn2allLoggers(getStepInfo(), String.format("[%s.%s][can't read field]%s", getClass().getName(), field.getName(), e
                            .toString()), e);
                }
            }
            if (declaredArguments.getIncludedArguments() != null && declaredArguments.getIncludedArguments().size() > 0) {
                for (Map.Entry<String, List<JobArgument<?>>> e : declaredArguments.getIncludedArguments().entrySet()) {
                    for (JobArgument<?> arg : e.getValue()) {
                        l.add(arg);
                    }
                }
            }
            if (declaredArguments.hasDynamicArguments()) {
                for (JobArgument<?> arg : declaredArguments.getDynamicArguments()) {
                    l.add(arg);
                }
            }
            allDeclaredArguments = l;
        }
    }

    /** Returns an object containing all declared arguments of the specified class.
     * <p>
     * These arguments are automatically populated as declared in the current job step instance.<br/>
     * The returned object extends {@link ASOSArguments}.
     * <p>
     * Example:
     * 
     * <pre>
     * 
     * CredentialStoreArguments csArgs = js7Step.getIncludedArguments(CredentialStoreArguments.class);
     * </pre>
     *
     * @param clazz the class of the arguments to retrieve
     * @return the object holding all declared arguments of the specified class
     * @throws JobArgumentException if the arguments cannot be retrieved or populated */
    public <T extends ASOSArguments> T getIncludedArguments(Class<T> clazz) throws JobArgumentException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            if (declaredArguments.getIncludedArguments() == null) {
                return instance;
            }
            List<SOSArgument<?>> args = allDeclaredArguments.stream().filter(a -> a.getPayload() != null && a.getPayload().equals(clazz.getName()))
                    .map(a -> (SOSArgument<?>) a).collect(Collectors.toList());
            if (args != null) {
                instance.setArguments(args);
            }
            return instance;
        } catch (Throwable e) {
            throw new JobArgumentException(e.toString(), e);
        }
    }

    /** Returns an object containing all declared arguments of the specified class, identified by its class key.
     * <p>
     * This method is intended for use in GraalVM jobs or scenarios where the class type is not directly available, allowing a simpler call using a string key
     * instead of a Java {@code Class} object.
     * <p>
     * Internally, this method calls {@link #getIncludedArguments(Class)}.<br />
     * The returned object extends {@link ASOSArguments}.
     * <p>
     * Example GraalVM JavaScript:
     * 
     * <pre>
     * 
     * var csArgs = js7Step.getIncludedArguments("CREDENTIAL_STORE");
     * </pre>
     *
     * @param clazzKey the class key of the arguments to retrieve
     * @return the object holding all declared arguments of the specified class
     * @throws JobArgumentException if the arguments cannot be retrieved or populated */
    public ASOSArguments getIncludedArguments(String clazzKey) throws JobArgumentException {
        if (clazzKey == null) {
            return null;
        }
        switch (clazzKey.toUpperCase()) {
        case CredentialStoreArguments.CLASS_KEY:
            return getIncludedArguments(new CredentialStoreArguments().getClass());
        case SSHProviderArguments.CLASS_KEY:
            return getIncludedArguments(SSHProviderArguments.class);
        case KeyStoreArguments.CLASS_KEY:
            return getIncludedArguments(KeyStoreArguments.class);
        case ProxyConfigArguments.CLASS_KEY:
            return getIncludedArguments(ProxyConfigArguments.class);
        default:
            return null;
        }

    }

    private String getStepInfo() {
        if (stepInfo == null) {
            try {
                stepInfo = String.format("[Order %s][Workflow %s, versionId=%s, pos=%s][Job %s, agent=%s, class=%s]", getOrderId(), getWorkflowPath(),
                        getWorkflowVersionId(), getWorkflowPosition(), getJobName(), getAgentId(), getClass().getName());
            } catch (JobProblemException e) {
                stepInfo = String.format("[Workflow %s, versionId=%s, pos=%s][Job class=%s]", getWorkflowPath(), getWorkflowVersionId(),
                        getWorkflowPosition(), getClass().getName());
            }
        }
        return stepInfo;
    }

    public String getControllerId() {
        if (controllerId == null) {
            if (internalStep == null) {
                return null;
            }
            controllerId = internalStep.controllerId().string();
        }
        return controllerId;
    }

    public String getOrderId() {
        if (orderId == null) {
            if (internalStep == null) {
                return null;
            }
            orderId = internalStep.order().id().string();
        }
        return orderId;
    }

    public String getAgentId() throws JobProblemException {
        if (agentId == null) {
            if (internalStep == null) {
                return null;
            }
            agentId = JobHelper.getFromEither(internalStep.order().attached()).string();
        }
        return agentId;
    }

    public String getJobName() throws JobProblemException {
        if (jobName == null) {
            if (internalStep == null) {
                return null;
            }
            jobName = JobHelper.getFromEither(internalStep.workflow().checkedJobName(internalStep.order().workflowPosition().position())).toString();
        }
        return jobName;
    }

    public String getJobInstructionLabel() {
        if (jobInstructionLabel == null) {
            if (internalStep == null) {
                return null;
            }
            try {
                jobInstructionLabel = internalStep.instructionLabel().get().string();
            } catch (Throwable e) {
                getLogger().error(String.format("[getJobInstructionLabel]%s", e.toString()));
            }
        }
        return jobInstructionLabel;
    }

    public String getWorkflowPath() {
        if (workflowPath == null) {
            if (internalStep == null) {
                return null;
            }
            try {
                workflowPath = (String) getNamedValue(INTERNAL_ORDER_PREPARATION_PARAMETER_JS7_WORKFLOW_PATH);
            } catch (Throwable e) {
                getLogger().error(String.format("[getWorkflowPath][%s]%s", INTERNAL_ORDER_PREPARATION_PARAMETER_JS7_WORKFLOW_PATH, e.toString()));
            }
        }
        return workflowPath;
    }

    public String getWorkflowName() {
        if (workflowName == null) {
            if (internalStep == null) {
                return null;
            }
            workflowName = internalStep.order().workflowId().path().name();
        }
        return workflowName;
    }

    public String getWorkflowVersionId() {
        if (workflowVersionId == null) {
            if (internalStep == null) {
                return null;
            }
            workflowVersionId = internalStep.order().workflowId().versionId().string();
        }
        return workflowVersionId;
    }

    public String getWorkflowPosition() {
        if (workflowPosition == null) {
            if (internalStep == null) {
                return null;
            }
            workflowPosition = internalStep.order().workflowPosition().position().toString();
        }
        return workflowPosition;
    }

    private String getDisplayValue(String name) {
        JobArgument<?> ar = allArguments.get(name);
        if (ar == null) {
            return DisplayMode.UNKNOWN.getValue();
        }
        if (logger.isDebugEnabled()) {
            return SOSArgumentHelper.getDisplayValueIgnoreUnknown(ar.getValue(), ar.getDisplayMode());
        }
        return ar.getDisplayValue();
    }

    private String getDisplayValue(String name, Object originalValue) {
        if (allArguments == null) {
            if (logger.isDebugEnabled()) {
                return SOSArgumentHelper.getDisplayValue(originalValue, DisplayMode.UNMASKED);
            }
            return DisplayMode.UNKNOWN.getValue();
        }
        JobArgument<?> ar = allArguments.get(name);
        if (ar == null) {
            return DisplayMode.UNKNOWN.getValue();
        }
        if (logger.isDebugEnabled()) {
            return SOSArgumentHelper.getDisplayValueIgnoreUnknown(originalValue, ar.getDisplayMode());
        }
        return SOSArgumentHelper.getDisplayValue(originalValue, ar.getDisplayMode());
    }

    private void setOrderPreparationParameterNames() {
        if (orderPreparationParameterNames == null) {
            if (internalStep == null) {
                orderPreparationParameterNames = new HashSet<>();
                return;
            }
            orderPreparationParameterNames = JavaConverters.asJava(internalStep.asScala().workflow().orderPreparation().parameterList()
                    .nameToParameter()).entrySet().stream().filter(e -> !e.getKey().equals(INTERNAL_ORDER_PREPARATION_PARAMETER_JS7_WORKFLOW_PATH))
                    .map(e -> e.getKey()).collect(Collectors.toSet());
        }
    }

    protected JOutcome.Completed processed() {
        return outcome.isFailed() ? failed() : success();
    }

    protected JOutcome.Completed success() {
        return JOutcome.succeeded(mapProcessResult(getOutcomeVariables(), getReturnCodeSucceeded(outcome.getReturnCode())));
    }

    private JOutcome.Completed failed() {
        String fm = SOSString.isEmpty(outcome.getMessage()) ? "" : outcome.getMessage();
        logger.failed2slf4j(getStepInfo(), fm);
        return JOutcome.failed(fm, mapProcessResult(getOutcomeVariables(), getReturnCodeFailed(outcome.getReturnCode())));
    }

    protected JOutcome.Completed failed(final String msg, Throwable e) {
        String fm = SOSString.isEmpty(msg) ? "" : msg;
        Throwable ex = logger.handleException(e);
        logger.failed2slf4j(getStepInfo(), e.toString(), ex);
        logger.error(logger.throwable2String(fm, ex));
        return JOutcome.failed(getJOutcomeFailed(fm, ex), mapProcessResult(getOutcomeVariables(), getReturnCodeFailed(
                JobHelper.DEFAULT_RETURN_CODE_FAILED)));
    }

    private String getJOutcomeFailed(final String msg, Throwable e) {
        if (e == null) {
            return msg;
        }
        String em = e.getMessage() == null ? e.toString() : e.getMessage();
        return msg + em;
    }

    private Map<String, Value> getOutcomeVariables() {
        return outcome.hasVariables() ? JobHelper.asEngineValues(outcome.getVariables()) : null;
    }

    private Map<String, Value> mapProcessResult(Map<String, Value> map, Integer returnCode) {
        if (map == null || map.size() == 0) {
            map = Collections.singletonMap(JobHelper.NAMED_NAME_RETURN_CODE, NumberValue.of(returnCode));
        } else {
            // override
            map.put(JobHelper.NAMED_NAME_RETURN_CODE, NumberValue.of(returnCode));
        }
        return map;
    }

    private Integer getReturnCodeSucceeded(Integer returnCode) {
        if (returnCode == null) {
            return JobHelper.DEFAULT_RETURN_CODE_SUCCEEDED;
        }
        return returnCode;
    }

    private Integer getReturnCodeFailed(Integer returnCode) {
        if (returnCode == null) {
            return JobHelper.DEFAULT_RETURN_CODE_FAILED;
        }
        return returnCode;
    }

    private List<HistoricOutcome> getEngineHistoricOutcomes() {
        if (internalStep == null) {
            return null;
        }
        return JavaConverters.asJava(internalStep.order().asScala().historicOutcomes());
    }

    private Map<String, Map<String, DetailValue>> historicOutcomes2map() {
        List<HistoricOutcome> l = getEngineHistoricOutcomes();
        if (l == null || l.size() == 0) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, DetailValue>> resultMap = new TreeMap<>();
        for (HistoricOutcome ho : l) {
            OrderOutcome outcome = ho.outcome();
            if (outcome instanceof Completed) {
                Completed c = (Completed) outcome;
                if (c.namedValues() != null) {
                    String key = outcome.getClass().getSimpleName();
                    Map<String, DetailValue> map = new TreeMap<>();
                    if (resultMap.containsKey(key)) {
                        map = resultMap.get(key);
                    } else {
                        map = new HashMap<String, DetailValue>();
                    }
                    Map<String, Object> m = JobHelper.asJavaValues(JavaConverters.asJava(c.namedValues()));
                    if (m != null) {
                        for (Map.Entry<String, Object> entry : m.entrySet()) {
                            map.remove(entry.getKey());
                            map.put(entry.getKey(), new DetailValue(ho.position().toString(), entry.getValue()));
                        }
                    }
                    // map.remove(JobHelper.NAMED_NAME_RETURN_CODE);
                    resultMap.put(key, map);
                }
            }
        }
        return resultMap;
    }

    private Map<String, DetailValue> jobResources2map() {
        Map<String, DetailValue> resultMap = new TreeMap<>();
        if (internalStep == null) {
            return resultMap;
        }
        Map<JobResourcePath, Map<String, Either<Problem, Value>>> jobResources = internalStep.jobResourceToNameToCheckedValue();
        if (jobResources == null || jobResources.size() == 0) {
            return resultMap;
        }

        jobResources.entrySet().stream().forEach(e -> {
            String resourceName = e.getKey().string();
            e.getValue().entrySet().stream().forEach(ee -> {
                if (!resultMap.containsKey(ee.getKey()) && ee.getValue().isRight()) {
                    resultMap.put(ee.getKey(), new DetailValue(resourceName, JobHelper.asJavaValue(ee.getValue().get())));
                }
            });
        });
        return resultMap;
    }

    protected void checkAndLogParameterization(List<JobArgumentException> exceptions, String mockMessage) throws Exception {
        Exception ae = null;
        try {
            if (exceptions != null && exceptions.size() > 0) {
                List<String> l = exceptions.stream().filter(e -> e instanceof ISOSRequiredArgumentMissingException).map(e -> {
                    return ((ISOSRequiredArgumentMissingException) e).getArgumentName();
                }).collect(Collectors.toList());
                if (l.size() > 0) {
                    ae = new JobRequiredArgumentMissingException(String.join(", ", l));
                } else {
                    ae = exceptions.get(0);
                }
            }
            LogLevel ll = LogLevel.DEBUG;
            boolean logDetails = logger.isDebugEnabled();
            if (ae != null) {
                ll = LogLevel.INFO;
                logDetails = true;
            }

            String header = "Job Parameterization";
            logAllDirtyArguments(mockMessage == null ? header : mockMessage + " " + header, logDetails);

            if (logDetails) {
                logJobKey();
                logArgumentsBySource(ll);
                logAllArguments(ll);
            }
        } catch (Throwable e) {
            logger.error2allLoggers(getStepInfo(), e.toString(), e);
        } finally {
            if (ae != null) {
                throw ae;
            }
        }
    }

    private void logJobKey() {
        if (logger.isDebugEnabled()) {
            logger.debug("JobKEY=" + jobEnvironment.getJobKey());
        }
    }

    private void logArgumentsBySource(LogLevel logLevel) throws Exception {
        logOutcomes(logLevel);

        Map<String, Object> map = null;
        if (internalStep != null) {
            // ORDER Variables
            map = JobHelper.asJavaValues(internalStep.order().arguments());
            if (map != null && map.size() > 0) {
                logger.log(logLevel, String.format(" %s:", ValueSourceType.ORDER.getHeader()));
                map.entrySet().stream().forEach(e -> {
                    logger.log(logLevel, "    %s=%s", e.getKey(), getDisplayValue(e.getKey(), e.getValue()));
                });
            }
        }

        List<JobArgument<?>> orderPreparation = getOrderPreparationArguments();
        if (orderPreparation != null && orderPreparation.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSourceType.ORDER_PREPARATION.getHeader()));
            orderPreparation.stream().forEach(a -> {
                logger.log(logLevel, "    " + a.toString(logger.isDebugEnabled()));
            });
        }

        // Declared ORDER or Node arguments
        List<JobArgument<?>> orderOrNode = getDeclaredOrderOrNodeArguments();
        if (orderOrNode != null && orderOrNode.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSourceType.ORDER_OR_NODE.getHeader()));
            orderOrNode.stream().forEach(a -> {
                logger.log(logLevel, "    " + a.toString(logger.isDebugEnabled()));
            });
        }
        if (internalStep != null) {
            // JOB Arguments
            map = JobHelper.asJavaValues(internalStep.arguments());
            if (map != null && map.size() > 0) {
                logger.log(logLevel, String.format(" %s:", ValueSourceType.JOB.getHeader()));
                map.entrySet().stream().forEach(e -> {
                    logger.log(logLevel, "    %s=%s", e.getKey(), getDisplayValue(e.getKey(), e.getValue()));
                });
            }
        }

        logJobEnvironmentArguments(logLevel);

        // JOB Resources
        Map<String, DetailValue> resources = getJobResourcesArgumentsAsNameDetailValueMap();
        if (resources != null && resources.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSourceType.JOB_RESOURCE.getHeader()));
            resources.entrySet().stream().forEach(e -> {
                DetailValue v = e.getValue();
                logger.log(logLevel, "    %s=%s (resource=%s)", e.getKey(), getDisplayValue(e.getKey(), v.getValue()), v.getSource());
            });
        }

    }

    private List<JobArgument<?>> getOrderPreparationArguments() {
        if (allArguments == null) {
            return null;
        }
        return allArguments.entrySet().stream().filter(e -> {
            if (e.getValue().getValueSource().isTypeOrderPreparation()) {
                return true;
            }
            return false;
        }).map(e -> e.getValue()).collect(Collectors.toList());
    }

    private List<JobArgument<?>> getDeclaredOrderOrNodeArguments() {
        if (allDeclaredArguments == null) {
            return null;
        }
        return allDeclaredArguments.stream().filter(a -> {
            if (a.getValueSource().isTypeOrderOrNode()) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    private void logJobEnvironmentArguments(LogLevel logLevel) {
        if (jobEnvironment != null) {
            Map<String, Object> map = jobEnvironment.getAllArgumentsAsNameValueMap();
            if (map != null && map.size() > 0) {
                logger.log(logLevel, String.format(" %s:", ValueSourceType.JOB_ARGUMENT.getHeader()));
                map.entrySet().stream().forEach(e -> {
                    logger.log(logLevel, "    %s=%s", e.getKey(), getDisplayValue(e.getKey(), e.getValue()));
                });
            }
        }
    }

    private void logOutcomes(LogLevel logLevel) {
        // OUTCOME succeeded
        Map<String, DetailValue> map = getLastSucceededOutcomes();
        if (map != null && map.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSourceType.LAST_SUCCEEDED_OUTCOME.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.log(logLevel, "    %s=%s (pos=%s)", e.getKey(), getDisplayValue(e.getKey()), e.getValue().getSource());
            });
        }
        // OUTCOME failed
        map = getLastFailedOutcomes();
        if (map != null && map.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSourceType.LAST_FAILED_OUTCOME.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.log(logLevel, "    %s=%s (pos=%s)", e.getKey(), getDisplayValue(e.getKey()), e.getValue().getSource());
            });
        }
    }

    private void logAllDirtyArguments(String header, boolean logDetails) throws Exception {
        if (allArguments == null || allArguments.size() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (logDetails) {
            logger.info(header + ":");
        } else {
            sb.append(header).append(", ");
        }
        sb.append(ValueSourceType.JAVA.getHeader()).append(":");
        String prefix = "";
        for (Map.Entry<String, JobArgument<?>> e : allArguments.entrySet()) {
            JobArgument<?> a = e.getValue();
            if (!a.isDirty()) {
                if (a.getNotAcceptedValue() == null) {
                    continue;
                }
            }

            String detail = a.getValueSource().getSource() == null ? "" : " " + a.getValueSource().getSource();
            if (a.getPayload() != null) {
                String pd = SOSArgumentHelper.getClassName(a.getPayload().toString());
                if (detail.equals("")) {
                    detail = " " + pd;
                } else {
                    detail = detail + " " + pd;
                }
            }

            sb.append(prefix).append(" ");
            if (a.getNotAcceptedValue() == null) {
                String vsn = a.getValueSource().getType() == null ? "" : a.getValueSource().getType().name();

                sb.append(a.getName()).append("=").append(a.getDisplayValue());
                sb.append("(").append(vsn).append(detail).append(")");
            } else {
                String exception = "";
                if (a.getNotAcceptedValue().getException() != null) {
                    exception = new StringBuilder("(").append(a.getNotAcceptedValue().getException().toString()).append(")").toString();
                }
                String nvsn = a.getNotAcceptedValue().getSource() == null || a.getNotAcceptedValue().getSource().getType() == null ? "" : a
                        .getNotAcceptedValue().getSource().getType().name() + " ";
                sb.append(a.getName()).append("=").append(a.getDisplayValue());
                sb.append("(");
                sb.append(nvsn).append("value=").append(a.getNotAcceptedValue().getDisplayValue());
                sb.append(" ignored").append(exception);
                sb.append(detail);
                sb.append(")");
            }
            prefix = ",";
        }
        logger.info(sb);
    }

    private void logAllArguments(LogLevel logLevel) throws Exception {
        if (allArguments == null || allArguments.size() == 0) {
            return;
        }
        logger.log(logLevel, String.format(" All %s:", ValueSourceType.JAVA.getHeader()));
        allArguments.entrySet().stream().forEach(a -> {
            logger.log(logLevel, "    " + a.getValue().toString(logger.isDebugEnabled()));
        });
    }

    protected ExecuteJobBean getExecuteJobBean() {
        return executeJobBean;
    }

    protected boolean hasExecuteJobArguments() {
        return executeJobBean != null && executeJobBean.arguments != null && executeJobBean.arguments.size() > 0;
    }

    protected List<String> getResolverPrefixes() {
        if (resolverPrefixes == null) {
            resolverPrefixes = new ArrayList<>();
        }
        return resolverPrefixes;
    }

    protected class ExecuteJobBean {

        private final Job<?> job;
        /* execute another job arguments */
        private final Map<String, JobArgument<?>> arguments;
        private final boolean updateDeclaredArgumentsDefinition;

        private ExecuteJobBean(Job<?> job, Map<String, JobArgument<?>> arguments, boolean updateDeclaredArgumentsDefinition) {
            this.job = job;
            this.arguments = arguments;
            this.updateDeclaredArgumentsDefinition = updateDeclaredArgumentsDefinition;
        }

        protected Job<?> getJob() {
            return job;
        }

        protected Map<String, JobArgument<?>> getArguments() {
            return arguments;
        }

        protected boolean updateDeclaredArgumentsDefinition() {
            return updateDeclaredArgumentsDefinition;
        }
    }

}
