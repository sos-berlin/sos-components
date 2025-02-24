package com.sos.js7.job;

import java.lang.reflect.Field;
import java.sql.Connection;
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
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
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

    protected static final String CANCELABLE_RESOURCE_NAME_HIBERNATE = "hibernate";
    protected static final String CANCELABLE_RESOURCE_NAME_SSH_PROVIDER = "ssh_provider";
    protected static final String CANCELABLE_RESOURCE_NAME_SQL_CONNECTION = "sql_connection";

    private static final String INTERNAL_ORDER_PREPARATION_PARAMETER_JS7_WORKFLOW_PATH = "js7Workflow.path";

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

    protected OrderProcessStep(JobEnvironment<A> jobEnvironment, BlockingInternalJob.Step step) {
        this.jobEnvironment = jobEnvironment;
        this.internalStep = step;
        this.logger = new OrderProcessStepLogger(internalStep);
        this.outcome = new OrderProcessStepOutcome();
        this.resolverPrefixes = JobArgumentValueResolverCache.getResolverPrefixes();
    }

    private OrderProcessStep(JobEnvironment<A> jobEnvironment, OrderProcessStep<?> step) {
        this.jobEnvironment = jobEnvironment;
        this.internalStep = step.getInternalStep();
        this.logger = step.logger;
        this.outcome = step.outcome;
        this.resolverPrefixes = step.resolverPrefixes;
    }

    /** Execute another job<br />
     * 
     * @param clazz Job class
     * @throws Exception */
    public <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz) throws Exception {
        executeJob(clazz, (Map<String, JobArgument<?>>) null, false);
    }

    /** Execute another job<br />
     * 
     * @param clazz Job class
     * @param executeJobArguments Map (key=argument name,value=argument value)
     * @throws Exception */
    public <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz, Map<String, Object> executeJobArguments) throws Exception {
        Map<String, JobArgument<?>> map = null;
        if (executeJobArguments != null && executeJobArguments.size() > 0) {
            map = new HashMap<>();
            for (Map.Entry<String, Object> e : executeJobArguments.entrySet()) {
                map.put(e.getKey(), JobArgument.toExecuteJobArgument(e.getKey(), e.getValue()));
            }
        }
        executeJob(clazz, map, false);
    }

    public <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz, JobArgument<?>... executeJobArguments) throws Exception {
        executeJob(clazz, executeJobArguments == null || executeJobArguments.length == 0 ? null : Arrays.asList(executeJobArguments));
    }

    public <AJ extends JobArguments> void executeJob(Class<? extends Job<AJ>> clazz, Collection<JobArgument<?>> executeJobArguments)
            throws Exception {
        Map<String, JobArgument<?>> map = null;
        if (executeJobArguments != null && executeJobArguments.size() > 0) {
            map = new HashMap<>();
            for (JobArgument<?> arg : executeJobArguments) {
                map.put(arg.getName(), arg.toExecuteJobArgument());
            }
        }
        executeJob(clazz, map, true);
    }

    // to overwrite by UnitTestJobHelper
    protected <AJ extends JobArguments> AJ onExecuteJobCreateArguments(Job<AJ> job, OrderProcessStep<AJ> step, List<JobArgumentException> exceptions)
            throws Exception {
        AJ args = job.onCreateJobArguments(exceptions, step);
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
            step.init(args);

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
                    e.getValue().getExecuteJobBean().getJob().cancelOrderProcessStep(e.getValue());
                } catch (Throwable t) {
                    logger.warn("[cancelExecuteJobs][" + e.getKey() + "]" + e.toString(), e);
                }
            }
            cancelableExecuteJobs.clear();
        }
        cancelableExecuteJobs = null;
    }

    protected void init(A arguments) throws Exception {
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
        init(arguments);
    }

    public void addCancelableResource(SOSHibernateSession session) {
        addCancelableResource(CANCELABLE_RESOURCE_NAME_HIBERNATE, session);
    }

    public void addCancelableResource(SSHProvider provider) {
        addCancelableResource(CANCELABLE_RESOURCE_NAME_SSH_PROVIDER, provider);
    }

    @SuppressWarnings("unused")
    private void addCancelableResource(Connection conn) {
        addCancelableResource(CANCELABLE_RESOURCE_NAME_SQL_CONNECTION, conn);
    }

    private void addCancelableResource(String identifier, Object o) {
        if (cancelableResources == null) {
            cancelableResources = new HashMap<>();
        }
        cancelableResources.put(identifier, o);
    }

    protected Map<String, Object> getCancelableResources() {
        return cancelableResources;
    }

    public OrderProcessStepLogger getLogger() {
        return logger;
    }

    public A getDeclaredArguments() {
        return declaredArguments;
    }

    public List<JobArgument<?>> getAllDeclaredArguments() {
        return allDeclaredArguments;
    }

    public Map<String, Map<String, DetailValue>> getLastOutcomes() {
        if (lastOutcomes == null) {
            lastOutcomes = historicOutcomes2map();
        }
        return lastOutcomes;
    }

    public Map<String, DetailValue> getJobResourcesArgumentsAsNameDetailValueMap() {
        if (jobResourcesValues == null) {
            jobResourcesValues = jobResources2map();
        }
        return jobResourcesValues;
    }

    public Map<String, DetailValue> getLastSucceededOutcomes() {
        return getLastOutcomes().get(OrderOutcome.Succeeded.class.getSimpleName());
    }

    public Map<String, DetailValue> getLastFailedOutcomes() {
        return getLastOutcomes().get(OrderOutcome.Failed.class.getSimpleName());
    }

    protected BlockingInternalJob.Step getInternalStep() {
        return internalStep;
    }

    public JobArgument<?> getDeclaredArgument(String name) {
        if (allDeclaredArguments != null) {
            return allDeclaredArguments.stream().filter(a -> (a.getName() != null && a.getName().equals(name)) || (a.getNameAliases() != null && a
                    .getNameAliases().contains(name))).findAny().orElse(null);
        }
        return null;
    }

    /** JobArgument<?> instead of JobArgument<A> because of dynamicArguments */
    public Object getDeclaredArgumentValue(String name) {
        JobArgument<?> a = getDeclaredArgument(name);
        if (a != null) {
            return a.getValue();
        }
        return null;
    }

    private String getDisplayValue(String name) {
        JobArgument<?> ar = allArguments.get(name);
        if (ar == null) {
            return DisplayMode.UNKNOWN.getValue();
        }
        return ar.getDisplayValue();
    }

    private String getDisplayValue(String name, Object originalValue) {
        JobArgument<?> ar = allArguments.get(name);
        if (ar == null) {
            return DisplayMode.UNKNOWN.getValue();
        }
        return SOSArgumentHelper.getDisplayValue(originalValue, ar.getDisplayMode());
    }

    private void setAllArguments() throws Exception {
        allArguments = new TreeMap<>();

        setOrderPreparationParameterNames();

        // DECLARED Arguments
        setAllDeclaredArguments();
        if (allDeclaredArguments != null && allDeclaredArguments.size() > 0) {
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
        Map<String, DetailValue> lso = getLastSucceededOutcomes();
        if (lso != null && lso.size() > 0) {
            lso.entrySet().stream().forEach(e -> {
                // if (!allArguments.containsKey(e.getKey()) && !JobHelper.NAMED_NAME_RETURN_CODE.equals(e.getKey())) {
                if (!allArguments.containsKey(e.getKey())) {
                    ValueSource vs = new ValueSource(ValueSourceType.LAST_SUCCEEDED_OUTCOME);
                    vs.setSource(e.getValue().getSource());
                    try {
                        allArguments.put(e.getKey(), new JobArgument<>(e.getKey(), e.getValue().getValue(), vs));
                    } catch (Throwable ex) {
                        getLogger().error("[LastSucceededOutcomes][" + e.getKey() + "]" + ex.toString());
                    }
                }
            });
        }
        if (internalStep != null) {
            // Preference 2 - Order Variables (Node Arguments are unknown)
            Map<String, Object> o = JobHelper.asJavaValues(internalStep.order().arguments());
            if (o != null && o.size() > 0) {
                o.entrySet().stream().forEach(e -> {
                    if (!allArguments.containsKey(e.getKey())) {
                        try {
                            allArguments.put(e.getKey(), new JobArgument<>(e.getKey(), e.getValue(), new ValueSource(ValueSourceType.ORDER)));
                        } catch (Throwable ex) {
                            getLogger().error("[OrderVariables][" + e.getKey() + "]" + ex.toString());
                        }
                    }
                });
            }

            // Preference 3 - JobArgument
            Map<String, Object> j = JobHelper.asJavaValues(internalStep.arguments());
            if (j != null && j.size() > 0) {
                j.entrySet().stream().forEach(e -> {
                    if (!allArguments.containsKey(e.getKey())) {
                        try {
                            allArguments.put(e.getKey(), new JobArgument<>(e.getKey(), e.getValue(), new ValueSource(ValueSourceType.JOB)));
                        } catch (Throwable ex) {
                            getLogger().error("[JobArgument][" + e.getKey() + "]" + ex.toString());
                        }
                    }
                });
            }
        }
        // Preference 4 (LOWEST) - JobResources
        Map<String, DetailValue> resources = getJobResourcesArgumentsAsNameDetailValueMap();
        if (resources != null && resources.size() > 0) {
            resources.entrySet().stream().forEach(e -> {
                String name = e.getKey();
                DetailValue dv = e.getValue();
                JobArgument<?> aja = allArguments.get(name);
                // if (!allArguments.containsKey(e.getKey())) {
                if (aja == null || aja.getValue() == null) {// workaround js: job resource changed and declaredArgumens defined
                    try {
                        ValueSource vs = new ValueSource(ValueSourceType.JOB_RESOURCE);
                        vs.setSource(dv.getSource());
                        JobArgument<?> ja = new JobArgument<>(name, dv.getValue(), vs);
                        allArguments.put(name, ja);
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
                    allArguments.put(e.getKey(), new JobArgument<>(e.getKey(), e.getValue(), vs));
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
                        JobArgument<?> ar = new JobArgument<>(name, o, vs);
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
                        allArguments.put(e.getKey(), new JobArgument<>(e.getKey(), e.getValue(), new ValueSource(ValueSourceType.JOB_ARGUMENT)));
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

    public Map<String, JobArgument<?>> getAllArguments() {
        return allArguments;
    }

    public Map<String, JobArgument<?>> getAllArguments(JobArgument.Type type) {
        return allArguments.entrySet().stream().filter(a -> a.getValue().getType().equals(type)).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue));
    }

    public Map<String, Object> getAllArgumentsAsNameValueMap() {
        return JobHelper.asNameValueMap(allArguments);
    }

    public Map<String, Object> getUndeclaredArgumentsAsNameValueMap() {
        return JobHelper.asNameValueMap(getAllArguments(Type.UNDECLARED));
    }

    public Map<String, Object> getOrderArgumentsAsNameValueMap() {
        if (internalStep == null) {
            return Collections.emptyMap();
        }
        return JobHelper.asJavaValues(internalStep.order().arguments());
    }

    @SuppressWarnings("unused")
    private Object getArgumentValue(String name) {
        if (allArguments.containsKey(name)) {
            return allArguments.get(name).getValue();
        }
        try {
            return getNamedValue(name);
        } catch (JobProblemException e) {
            return null;
        }
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
            if (declaredArguments.hasDynamicArgumentFields()) {
                for (JobArgument<?> arg : declaredArguments.getDynamicArgumentFields()) {
                    l.add(arg);
                }
            }
            allDeclaredArguments = l;
        }
    }

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

    public ASOSArguments getIncludedArguments(String clazzKey) throws JobArgumentException {
        if (clazzKey == null) {
            return null;
        }
        switch (clazzKey.toUpperCase()) {
        case CredentialStoreArguments.CLASS_KEY:
            return getIncludedArguments(new CredentialStoreArguments().getClass());
        case SSHProviderArguments.CLASS_KEY:
            return getIncludedArguments(SSHProviderArguments.class);
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

    public Map<String, String> getJobResourcesEnv() throws JobProblemException {
        if (internalStep == null) {
            return null;
        }
        return JobHelper.getFromEither(internalStep.env()).entrySet().stream().filter(e -> e.getValue().isPresent()).collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().get()));
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
                logger.log(logLevel, "    " + a.toString());
            });
        }

        // Declared ORDER or Node arguments
        List<JobArgument<?>> orderOrNode = getDeclaredOrderOrNodeArguments();
        if (orderOrNode != null && orderOrNode.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSourceType.ORDER_OR_NODE.getHeader()));
            orderOrNode.stream().forEach(a -> {
                logger.log(logLevel, "    " + a.toString());
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
            logger.log(logLevel, "    " + a.getValue().toString());
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

    public OrderProcessStepOutcome getOutcome() {
        return outcome;
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
