package com.sos.js7.job;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.sos.commons.exception.ISOSRequiredArgumentMissingException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.js7.job.JobArgument.Type;
import com.sos.js7.job.JobArgument.ValueSource;
import com.sos.js7.job.JobArguments.LogLevel;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.exception.JobProblemException;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.job.JobResourcePath;
import js7.data.order.HistoricOutcome;
import js7.data.order.Outcome;
import js7.data.order.Outcome.Completed;
import js7.data.value.NumberValue;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.launcher.forjava.internal.BlockingInternalJob;
import scala.collection.JavaConverters;

public class OrderProcessStep<A extends JobArguments> {

    protected static final String CANCELABLE_RESOURCE_NAME_HIBERNATE_FACTORY = "hibernate_factory";
    protected static final String CANCELABLE_RESOURCE_NAME_SSH_PROVIDER = "ssh_provider";
    protected static final String CANCELABLE_RESOURCE_NAME_SQL_CONNECTION = "sql_connection";

    private final JobEnvironment<A> jobEnvironment;
    private final BlockingInternalJob.Step internalStep;
    private final OrderProcessStepLogger logger;
    private final OrderProcessStepOutcome outcome;
    private final String threadName;

    /* Arguments of <A extends JobArguments> */
    private A declaredArguments;
    /* declaredArguments + declared includedArguments */
    private List<JobArgument<A>> allDeclaredArguments;
    private Map<String, JobArgument<A>> allArguments;

    private Map<String, Map<String, DetailValue>> lastOutcomes;
    private Map<String, DetailValue> jobResourcesValues;
    private Map<String, Object> unitTestUndeclaredArguments;
    private Map<String, Object> cancelableResources;

    private String controllerId;
    private String orderId;
    private String agentId;
    private String jobInstructionLabel;
    private String jobName;
    private String workflowName;
    private String workflowVersionId;
    private String workflowPosition;
    private String stepInfo;

    protected OrderProcessStep(JobEnvironment<A> jobEnvironment, BlockingInternalJob.Step step) {
        this.jobEnvironment = jobEnvironment;
        this.internalStep = step;
        this.logger = new OrderProcessStepLogger(internalStep);
        this.threadName = Thread.currentThread().getName();
        this.outcome = new OrderProcessStepOutcome();
    }

    protected void init(A arguments) {
        this.declaredArguments = arguments;
        this.logger.init(arguments);

        setAllArguments();
    }

    protected void init(A arguments, Map<String, Object> unitTestUndeclaredArguments) {
        init(arguments);
        this.unitTestUndeclaredArguments = unitTestUndeclaredArguments;
    }

    protected String getThreadName() {
        return threadName;
    }

    public void addCancelableResource(SOSHibernateFactory factory) {
        addCancelableResource(CANCELABLE_RESOURCE_NAME_HIBERNATE_FACTORY, factory);
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

    public List<JobArgument<A>> getAllDeclaredArguments() {
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
        return getLastOutcomes().get(Outcome.Succeeded.class.getSimpleName());
    }

    public Map<String, DetailValue> getLastFailedOutcomes() {
        return getLastOutcomes().get(Outcome.Failed.class.getSimpleName());
    }

    protected BlockingInternalJob.Step getInternalStep() {
        return internalStep;
    }

    public JobArgument<A> getDeclaredArgument(String name) {
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
        JobArgument<?> ar = getDeclaredArgument(name);
        if (ar == null) {
            return DisplayMode.UNKNOWN.getValue();
        }
        return ar.getDisplayValue();
    }

    private String getDisplayValue(String name, Object value) {
        JobArgument<?> ar = getDeclaredArgument(name);
        if (ar == null) {
            return DisplayMode.UNKNOWN.getValue();
        }
        return SOSArgumentHelper.getDisplayValue(value, ar.getDisplayMode());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setAllArguments() {
        allArguments = new TreeMap<>();

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

        // for preference see ABlockingJob.createJobArguments/setJobArgument
        // Preference 1 (HIGHEST) - Succeeded Outcomes
        Map<String, DetailValue> lso = getLastSucceededOutcomes();
        if (lso != null && lso.size() > 0) {
            lso.entrySet().stream().forEach(e -> {
                // if (!allArguments.containsKey(e.getKey()) && !JobHelper.NAMED_NAME_RETURN_CODE.equals(e.getKey())) {
                if (!allArguments.containsKey(e.getKey())) {
                    ValueSource vs = ValueSource.LAST_SUCCEEDED_OUTCOME;
                    vs.setDetails(e.getValue().getSource());
                    allArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue().getValue(), vs));
                }
            });
        }
        if (internalStep != null) {
            // Preference 2 - Order Variables (Node Arguments are unknown)
            Map<String, Object> o = JobHelper.asJavaValues(internalStep.order().arguments());
            if (o != null && o.size() > 0) {
                o.entrySet().stream().forEach(e -> {
                    if (!allArguments.containsKey(e.getKey())) {
                        allArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue(), ValueSource.ORDER));
                    }
                });
            }
            // Preference 3 - JobArgument
            Map<String, Object> j = JobHelper.asJavaValues(internalStep.arguments());
            if (j != null && j.size() > 0) {
                j.entrySet().stream().forEach(e -> {
                    if (!allArguments.containsKey(e.getKey())) {
                        allArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue(), ValueSource.JOB));
                    }
                });
            }
        }
        // Preference 4 (LOWEST) - JobResources
        Map<String, DetailValue> resources = getJobResourcesArgumentsAsNameDetailValueMap();
        if (resources != null && resources.size() > 0) {
            resources.entrySet().stream().forEach(e -> {
                if (!allArguments.containsKey(e.getKey())) {
                    ValueSource vs = ValueSource.JOB_RESOURCE;
                    vs.setDetails(e.getValue().getSource());
                    allArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue().getValue(), vs));
                }
            });
        }

        // Preference 5 - JobContext.jobArguments()
        jobEnvironment.getAllArgumentsAsNameValueMap().entrySet().stream().forEach(e -> {
            if (!allArguments.containsKey(e.getKey())) {
                ValueSource vs = ValueSource.JOB_ARGUMENT;
                allArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue(), vs));
            }
        });

        if (unitTestUndeclaredArguments != null) {
            unitTestUndeclaredArguments.entrySet().stream().forEach(e -> {
                if (!allArguments.containsKey(e.getKey())) {
                    allArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue(), ValueSource.JOB_ARGUMENT));
                }
            });
        }
    }

    public Map<String, JobArgument<A>> getAllArguments() {
        return allArguments;
    }

    public Map<String, JobArgument<A>> getAllArguments(JobArgument.Type type) {
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

    protected Object getNamedValue(final JobArgument<A> arg) throws JobProblemException {
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setAllDeclaredArguments() {
        if (declaredArguments == null) {
            allDeclaredArguments = null;
            return;
        }
        if (allDeclaredArguments == null) {
            List<Field> fields = JobHelper.getJobArgumentFields(declaredArguments);
            List<JobArgument<A>> l = new ArrayList<>();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    JobArgument<A> arg = (JobArgument<A>) field.get(declaredArguments);
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
                for (Map.Entry<String, List<JobArgument>> e : declaredArguments.getIncludedArguments().entrySet()) {
                    for (JobArgument arg : e.getValue()) {
                        l.add(arg);
                    }
                }
            }
            if (declaredArguments.hasDynamicArgumentFields()) {
                for (JobArgument<A> arg : declaredArguments.getDynamicArgumentFields()) {
                    l.add(arg);
                }
            }
            allDeclaredArguments = l;
        }
    }

    @SuppressWarnings("rawtypes")
    public <T extends ASOSArguments> T getIncludedArguments(Class<T> clazz) throws JobArgumentException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            if (declaredArguments.getIncludedArguments() == null) {
                return instance;
            }
            List<SOSArgument> args = allDeclaredArguments.stream().filter(a -> a.getPayload() != null && a.getPayload().equals(clazz.getName())).map(
                    a -> (SOSArgument) a).collect(Collectors.toList());
            if (args != null) {
                instance.setArguments(args);
            }
            return instance;
        } catch (Throwable e) {
            throw new JobArgumentException(e.toString(), e);
        }
    }

    private String getStepInfo() {
        if (stepInfo == null) {
            try {
                stepInfo = String.format("[Order %s][Workflow %s, versionId=%s, pos=%s][Job %s, agent=%s, class=%s]", getOrderId(), getWorkflowName(),
                        getWorkflowVersionId(), getWorkflowPosition(), getJobName(), getAgentId(), getClass().getName());
            } catch (JobProblemException e) {
                stepInfo = String.format("[Workflow %s, versionId=%s, pos=%s][Job class=%s]", getWorkflowName(), getWorkflowVersionId(),
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
            }
        }
        return jobInstructionLabel;
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

    protected JOutcome.Completed processed() {
        return outcome.isFailed() ? failed() : success();
    }

    protected JOutcome.Completed success() {
        return JOutcome.succeeded(mapProcessResult(outcome.hasVariables() ? JobHelper.asEngineValues(outcome.getVariables()) : null,
                getReturnCodeSucceeded(outcome.getReturnCode())));
    }

    private JOutcome.Completed failed() {
        String fm = SOSString.isEmpty(outcome.getMessage()) ? "" : outcome.getMessage();
        logger.failed2slf4j(getStepInfo(), fm);
        return JOutcome.failed(fm, mapProcessResult(outcome.hasVariables() ? JobHelper.asEngineValues(outcome.getVariables()) : null,
                getReturnCodeFailed(outcome.getReturnCode())));
    }

    protected JOutcome.Completed failed(final String msg, Throwable e) {
        // return failed(JobHelper.DEFAULT_RETURN_CODE_FAILED, msg, e);
        String fm = SOSString.isEmpty(msg) ? "" : msg;
        Throwable ex = logger.handleException(e);
        logger.failed2slf4j(getStepInfo(), e.toString(), ex);
        return JOutcome.failed(logger.throwable2String(fm, ex), mapProcessResult(null, getReturnCodeFailed(JobHelper.DEFAULT_RETURN_CODE_FAILED)));

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

    public Map<String, String> getEnv() throws JobProblemException {
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
            Outcome outcome = ho.outcome();
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
            if (mockMessage == null) {
                logger.info("Job Parameterization:");
            } else {
                logger.info(mockMessage + " Job Parameterization:");
            }

            logAllDirtyArguments();
            LogLevel ll = LogLevel.DEBUG;
            boolean doLog = logger.isDebugEnabled();
            if (ae != null) {
                ll = LogLevel.INFO;
                doLog = true;
            }

            if (doLog) {
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

    private void logArgumentsBySource(LogLevel logLevel) throws Exception {
        Map<String, Object> map = null;
        if (internalStep != null) {
            // ORDER Variables
            map = JobHelper.asJavaValues(internalStep.order().arguments());
            if (map != null && map.size() > 0) {
                logger.log(logLevel, String.format(" %s:", ValueSource.ORDER.getHeader()));
                map.entrySet().stream().forEach(e -> {
                    logger.log(logLevel, "    %s=%s", e.getKey(), getDisplayValue(e.getKey(), e.getValue()));
                });
            }
        }
        // Declared ORDER or Node arguments
        List<JobArgument<A>> orderOrNode = getDeclaredOrderOrNodeArguments();
        if (orderOrNode != null && orderOrNode.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSource.ORDER_OR_NODE.getHeader()));
            orderOrNode.stream().forEach(a -> {
                logger.log(logLevel, "    " + a.toString());
            });
        }
        if (internalStep != null) {
            // JOB Arguments
            map = JobHelper.asJavaValues(internalStep.arguments());
            if (map != null && map.size() > 0) {
                logger.log(logLevel, String.format(" %s:", ValueSource.JOB.getHeader()));
                map.entrySet().stream().forEach(e -> {
                    logger.log(logLevel, "    %s=%s", e.getKey(), getDisplayValue(e.getKey(), e.getValue()));
                });
            }
        }
        // JOB Resources
        Map<String, DetailValue> resources = getJobResourcesArgumentsAsNameDetailValueMap();
        if (resources != null && resources.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSource.JOB_RESOURCE.getHeader()));
            resources.entrySet().stream().forEach(e -> {
                DetailValue v = e.getValue();
                logger.log(logLevel, "    %s=%s (resource=%s)", e.getKey(), getDisplayValue(e.getKey(), v.getValue()), v.getSource());
            });
        }
        logJobEnvironmentArguments(logLevel);
        logOutcomes(logLevel);
    }

    private List<JobArgument<A>> getDeclaredOrderOrNodeArguments() {
        if (allDeclaredArguments == null) {
            return null;
        }
        return allDeclaredArguments.stream().filter(a -> {
            if (a.getValueSource().equals(ValueSource.ORDER_OR_NODE)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    private void logJobEnvironmentArguments(LogLevel logLevel) {
        if (jobEnvironment != null) {
            Map<String, Object> map = jobEnvironment.getAllArgumentsAsNameValueMap();
            if (map != null && map.size() > 0) {
                logger.log(logLevel, String.format(" %s:", ValueSource.JOB_ARGUMENT.getHeader()));
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
            logger.log(logLevel, String.format(" %s:", ValueSource.LAST_SUCCEEDED_OUTCOME.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.log(logLevel, "    %s=%s (pos=%s)", e.getKey(), getDisplayValue(e.getKey()), e.getValue().getSource());
            });
        }
        // OUTCOME failed
        map = getLastFailedOutcomes();
        if (map != null && map.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSource.LAST_FAILED_OUTCOME.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.log(logLevel, "    %s=%s (pos=%s)", e.getKey(), getDisplayValue(e.getKey()), e.getValue().getSource());
            });
        }
    }

    private void logAllDirtyArguments() throws Exception {
        if (allArguments == null || allArguments.size() == 0) {
            return;
        }
        logger.info(String.format("%s:", ValueSource.JAVA.getHeader()));
        allArguments.entrySet().stream().filter(a -> {
            if (a.getValue().isDirty()) {
                return true;
            } else {
                if (a.getValue().getValueSource().equals(JobArgument.ValueSource.JAVA)) {
                    if (a.getValue().getNotAcceptedValue() != null) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        }).forEach(a -> {
            String detail = a.getValue().getValueSource().getDetails() == null ? "" : " " + a.getValue().getValueSource().getDetails();
            if (a.getValue().getPayload() != null) {
                String pd = SOSArgumentHelper.getClassName(a.getValue().getPayload().toString());
                if (detail.equals("")) {
                    detail = " " + pd;
                } else {
                    detail = detail + " " + pd;
                }
            }
            if (a.getValue().getNotAcceptedValue() == null) {
                logger.info("    %s=%s (source=%s%s)", a.getValue().getName(), a.getValue().getDisplayValue(), a.getValue().getValueSource().name(),
                        detail);
            } else {
                String exception = "";
                if (a.getValue().getNotAcceptedValue().getException() != null) {
                    exception = new StringBuilder("(").append(a.getValue().getNotAcceptedValue().getException().toString()).append(")").toString();
                }
                logger.info("    %s=%s (source=%s value=%s[not accepted%s, use from source=%s]%s)", a.getValue().getName(), a.getValue()
                        .getDisplayValue(), a.getValue().getNotAcceptedValue().getSource().name(), a.getValue().getNotAcceptedValue()
                                .getDisplayValue(), exception, a.getValue().getNotAcceptedValue().getUsedValueSource().name(), detail);
            }
        });
    }

    @SuppressWarnings("unused")
    private void logArgumentsDeprecated(LogLevel logLevel) throws Exception {
        if (unitTestUndeclaredArguments != null && unitTestUndeclaredArguments.size() > 0) {
            logger.log(logLevel, String.format(" All %s(Unit-Test UndeclaredArguments):", ValueSource.JAVA.getHeader()));
            unitTestUndeclaredArguments.entrySet().stream().forEach(a -> {
                logger.log(logLevel, "    " + a.toString());
            });
        }

        if (allDeclaredArguments == null || allDeclaredArguments.size() == 0) {
            return;
        }
        logger.log(logLevel, String.format(" All %s:", ValueSource.JAVA.getHeader()));
        allDeclaredArguments.stream().forEach(a -> {
            logger.log(logLevel, "    " + a.toString());
        });
        // logAllAllArguments(logLevel);
    }

    private void logAllArguments(LogLevel logLevel) throws Exception {
        if (unitTestUndeclaredArguments != null && unitTestUndeclaredArguments.size() > 0) {
            logger.log(logLevel, String.format(" All %s(Unit-Test UndeclaredArguments):", ValueSource.JAVA.getHeader()));
            unitTestUndeclaredArguments.entrySet().stream().forEach(a -> {
                logger.log(logLevel, "    " + a.toString());
            });
        }

        if (allArguments == null || allArguments.size() == 0) {
            return;
        }
        logger.log(logLevel, String.format(" All %s:", ValueSource.JAVA.getHeader()));
        allArguments.entrySet().stream().forEach(a -> {
            logger.log(logLevel, "    " + a.getValue().toString());
        });
    }

    public OrderProcessStepOutcome getOutcome() {
        return outcome;
    }

}
