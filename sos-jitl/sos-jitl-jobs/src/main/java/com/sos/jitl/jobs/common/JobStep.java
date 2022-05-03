package com.sos.jitl.jobs.common;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.jitl.jobs.common.JobArgument.ValueSource;
import com.sos.jitl.jobs.common.JobLogger.LogLevel;
import com.sos.jitl.jobs.exception.SOSJobProblemException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.job.JobResourcePath;
import js7.data.order.HistoricOutcome;
import js7.data.order.Outcome;
import js7.data.order.Outcome.Completed;
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.launcher.forjava.internal.BlockingInternalJob;
import js7.launcher.forjava.internal.BlockingInternalJob.JobContext;
import scala.collection.JavaConverters;

public class JobStep<A extends JobArguments> {

    private final String jobClassName;
    private final JobContext jobContext;
    private final BlockingInternalJob.Step internalStep;
    private final JobLogger logger;
    private A arguments;
    private Map<String, Map<String, JobDetailValue>> lastOutcomes;
    private Map<String, JobDetailValue> jobResourcesValues;
    private List<JobArgument<A>> knownArguments;
    private Map<String, JobArgument<A>> allCurrentArguments;

    private String controllerId;
    private String orderId;
    private String agentId;
    private String jobInstructionLabel;
    private String jobName;
    private String workflowName;
    private String workflowVersionId;
    private String workflowPosition;

    protected JobStep(String jobClassName, JobContext jobContext, BlockingInternalJob.Step step) {
        this.jobClassName = jobClassName;
        this.jobContext = jobContext;
        this.internalStep = step;
        this.logger = new JobLogger(internalStep, getStepInfo());
    }

    protected void init(A arguments) {
        this.arguments = arguments;
        this.logger.init(arguments);
    }

    public BlockingInternalJob.Step getInternalStep() {
        return internalStep;
    }

    public JobLogger getLogger() {
        return logger;
    }

    public A getArguments() {
        return arguments;
    }

    public List<HistoricOutcome> getHistoricOutcomes() {
        if (internalStep == null) {
            return null;
        }
        return JavaConverters.asJava(internalStep.order().asScala().historicOutcomes());
    }

    public Map<String, Map<String, JobDetailValue>> getLastOutcomes() {
        if (lastOutcomes == null) {
            lastOutcomes = historicOutcomes2map();
        }
        return lastOutcomes;
    }

    public Map<String, JobDetailValue> getJobResourcesValues() {
        if (jobResourcesValues == null) {
            jobResourcesValues = jobResources2map();
        }
        return jobResourcesValues;
    }

    public Map<String, JobDetailValue> getLastSucceededOutcomes() {
        return getLastOutcomes().get(Outcome.Succeeded.class.getSimpleName());
    }

    public Map<String, JobDetailValue> getLastFailedOutcomes() {
        return getLastOutcomes().get(Outcome.Failed.class.getSimpleName());
    }

    private Map<JobArgument.ValueSource, List<String>> argumentsInfoBySetter() throws Exception {
        setKnownArguments();
        if (knownArguments == null || knownArguments.size() == 0) {
            return null;
        }
        Map<JobArgument.ValueSource, List<String>> map = new HashMap<JobArgument.ValueSource, List<String>>();
        for (JobArgument<?> arg : knownArguments) {
            List<String> l;
            ValueSource key = arg.getValueSource();
            if (map.containsKey(key)) {
                l = map.get(key);
            } else {
                l = new ArrayList<String>();
            }
            l.add(String.format("%s=%s", arg.getName(), arg.getDisplayValue()));
            map.put(key, l);

            if (arg.getNotAcceptedValue() != null) {
                key = arg.getNotAcceptedValue().getSource();
                if (map.containsKey(key)) {
                    l = map.get(key);
                } else {
                    l = new ArrayList<String>();
                }
                l.add(String.format("[not accepted]%s=%s[use default]%s", arg.getName(), arg.getNotAcceptedValue().getDisplayValue(), arg
                        .getDisplayValue()));
                map.put(key, l);
            }

        }
        return map;
    }

    public JobArgument<A> getKnownArgument(String name) {
        setKnownArguments();
        if (knownArguments != null) {
            return knownArguments.stream().filter(a -> a.getName().equals(name) || (a.getNameAliases() != null && a.getNameAliases().contains(name)))
                    .findAny().orElse(null);
        }
        return null;
    }

    public Object getDisplayValue(String name) {
        JobArgument<?> ar = getKnownArgument(name);
        if (ar == null) {
            return DisplayMode.UNKNOWN.getValue();
        }
        return ar.getDisplayValue();
    }

    public Object getDisplayValue(String name, Object value) {
        JobArgument<?> ar = getKnownArgument(name);
        if (ar == null) {
            return DisplayMode.UNKNOWN.getValue();
        }
        return SOSArgumentHelper.getDisplayValue(value, ar.getDisplayMode());
    }

    public Map<String, JobArgument<A>> getAllCurrentArguments(JobArgument.Type type) {
        getAllCurrentArguments();
        return allCurrentArguments.entrySet().stream().filter(a -> a.getValue().getType().equals(type)).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, JobArgument<A>> getAllCurrentArguments() {
        if (allCurrentArguments != null) {
            return allCurrentArguments;
        }

        allCurrentArguments = new HashMap<>();
        // KNOWN Arguments
        setKnownArguments();
        if (knownArguments != null && knownArguments.size() > 0) {
            knownArguments.stream().forEach(a -> {
                if (!allCurrentArguments.containsKey(a.getName())) {
                    allCurrentArguments.put(a.getName(), a);
                }
                if (a.getNameAliases() != null) {
                    for (String n : a.getNameAliases()) {
                        if (!allCurrentArguments.containsKey(n)) {
                            allCurrentArguments.put(n, a);
                        }
                    }
                }
            });
        }
        // UNKNOWN Arguments
        // for preference see ABlockingJob.createJobArguments
        // preference 1 (HIGHEST) - Succeeded Outcomes
        Map<String, JobDetailValue> lso = getLastSucceededOutcomes();
        if (lso != null && lso.size() > 0) {
            lso.entrySet().stream().forEach(e -> {
                if (!allCurrentArguments.containsKey(e.getKey())) {
                    ValueSource vs = ValueSource.LAST_SUCCEEDED_OUTCOME;
                    vs.setDetails(e.getValue().getSource());
                    allCurrentArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue().getValue(), vs));
                }
            });
        }
        if (internalStep != null) {
            // preference 2 - Order Variables (Node Arguments are unknown)
            Map<String, Object> o = Job.convert(internalStep.order().arguments());
            if (o != null && o.size() > 0) {
                o.entrySet().stream().forEach(e -> {
                    if (!allCurrentArguments.containsKey(e.getKey())) {
                        allCurrentArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue(), ValueSource.ORDER));
                    }
                });
            }
            // preference 3 - JobArgument
            Map<String, Object> j = Job.convert(internalStep.arguments());
            if (j != null && j.size() > 0) {
                j.entrySet().stream().forEach(e -> {
                    if (!allCurrentArguments.containsKey(e.getKey())) {
                        allCurrentArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue(), ValueSource.JOB));
                    }
                });
            }
        }
        // preference 4 (LOWEST) - JobResources
        Map<String, JobDetailValue> resources = getJobResourcesValues();
        if (resources != null && resources.size() > 0) {
            resources.entrySet().stream().forEach(e -> {
                if (!allCurrentArguments.containsKey(e.getKey())) {
                    ValueSource vs = ValueSource.JOB_RESOURCE;
                    vs.setDetails(e.getValue().getSource());
                    allCurrentArguments.put(e.getKey(), new JobArgument(e.getKey(), e.getValue().getValue(), vs));
                }
            });
        }
        return allCurrentArguments;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setKnownArguments() {
        if (arguments == null) {
            knownArguments = null;
            return;
        }
        if (knownArguments == null) {
            List<Field> fields = Job.getJobArgumentFields(arguments);
            List<JobArgument<A>> l = new ArrayList<JobArgument<A>>();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    JobArgument<A> arg = (JobArgument<A>) field.get(arguments);
                    if (arg != null) {
                        if (arg.getName() == null) {// internal usage
                            continue;
                        }
                        l.add(arg);
                    }
                } catch (Throwable e) {
                    logger.warn2allLogger(String.format("[%s.%s][can't read field]%s", getClass().getName(), field.getName(), e.toString()), e);
                }
            }
            if (arguments.getAppArguments() != null && arguments.getAppArguments().size() > 0) {
                for (Map.Entry<String, List<JobArgument>> e : arguments.getAppArguments().entrySet()) {
                    for (JobArgument arg : e.getValue()) {
                        l.add(arg);
                    }
                }
            }
            knownArguments = l;
        }
    }

    @SuppressWarnings("rawtypes")
    public <T extends ASOSArguments> T getAppArguments(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        setKnownArguments();
        T instance = clazz.newInstance();
        if (arguments.getAppArguments() == null) {
            return instance;
        }
        List<SOSArgument> args = knownArguments.stream().filter(a -> a.getPayload() != null && a.getPayload().equals(clazz.getName())).map(
                a -> (SOSArgument) a).collect(Collectors.toList());

        if (args != null) {
            instance.setArguments(args);
        }
        return instance;
    }

    private String getStepInfo() {
        try {
            return String.format("[Order %s][Workflow %s, versionId=%s, pos=%s][Job %s, agent=%s, class=%s]", getOrderId(), getWorkflowName(),
                    getWorkflowVersionId(), getWorkflowPosition(), getJobName(), getAgentId(), jobClassName);
        } catch (SOSJobProblemException e) {
            return String.format("[Workflow %s, versionId=%s, pos=%s][Job class=%s]", getWorkflowName(), getWorkflowVersionId(),
                    getWorkflowPosition(), jobClassName);
        }
    }

    public String getControllerId() {
        if (controllerId == null) {
            if (internalStep == null) {
                return null;
            }
            controllerId = internalStep.controllerId().toString();
        }
        return controllerId;
    }

    public String getOrderId() throws SOSJobProblemException {
        if (orderId == null) {
            if (internalStep == null) {
                return null;
            }
            orderId = internalStep.order().id().string();
        }
        return orderId;
    }

    public String getAgentId() throws SOSJobProblemException {
        if (agentId == null) {
            if (internalStep == null) {
                return null;
            }
            agentId = Job.getFromEither(internalStep.order().attached()).string();
        }
        return agentId;
    }

    public String getJobName() throws SOSJobProblemException {
        if (jobName == null) {
            if (internalStep == null) {
                return null;
            }
            jobName = Job.getFromEither(internalStep.workflow().checkedJobName(internalStep.order().workflowPosition().position())).toString();
        }
        return jobName;
    }

    public String getJobInstructionLabel() throws SOSJobProblemException {
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

    public JOutcome.Completed success() {
        return success(Job.DEFAULT_RETURN_CODE_SUCCEEDED);
    }

    public JOutcome.Completed success(final Integer returnCode) {
        return success(returnCode, new HashMap<String, Object>());
    }

    public JOutcome.Completed success(final String outcomeVarKey, final Object outcomeVarValue) {
        return success(Job.DEFAULT_RETURN_CODE_SUCCEEDED, outcomeVarKey, outcomeVarValue);
    }

    public JOutcome.Completed success(final Integer returnCode, final String outcomeVarKey, final Object outcomeVarValue) {
        Map<String, Object> map = new HashMap<>();
        if (outcomeVarKey != null && outcomeVarValue != null) {
            map.put(outcomeVarKey, outcomeVarValue);
        }
        return success(returnCode, map);
    }

    public JOutcome.Completed success(final JobOutcomeVariable<?>... outcomes) {
        return success(Job.DEFAULT_RETURN_CODE_SUCCEEDED, outcomes);
    }

    public JOutcome.Completed success(final Integer returnCode, final JobOutcomeVariable<?>... outcomes) {
        return success(returnCode, getMap(outcomes));
    }

    public JOutcome.Completed success(final Map<String, Object> outcomes) {
        return success(Job.DEFAULT_RETURN_CODE_SUCCEEDED, outcomes);
    }

    public JOutcome.Completed success(final Integer returnCode, final Map<String, Object> outcomes) {
        if (outcomes == null || outcomes.size() == 0) {
            return JOutcome.succeeded(mapResult(null, returnCode));
        }
        return JOutcome.succeeded(mapResult(convert4engine(outcomes), returnCode));
    }

    public JOutcome.Completed failed(final String msg, final String outcomeVarKey, final Object outcomeVarValue) {
        return failed(Job.DEFAULT_RETURN_CODE_FAILED, msg, outcomeVarKey, outcomeVarValue);
    }

    public JOutcome.Completed failed(final Integer returnCode, final String msg, final String outcomeVarKey, final Object outcomeVarValue) {
        if (outcomeVarKey != null && outcomeVarValue != null) {
            return failedWithMap(returnCode, msg, convert4engine(Collections.singletonMap(outcomeVarKey, outcomeVarValue)));
        }
        return failed(returnCode, msg);
    }

    public JOutcome.Completed failed(final String msg, JobOutcomeVariable<?>... outcomes) {
        return failed(Job.DEFAULT_RETURN_CODE_FAILED, msg, outcomes);
    }

    public JOutcome.Completed failed(final Integer returnCode, final String msg, JobOutcomeVariable<?>... outcomes) {
        return failed(returnCode, msg, getMap(outcomes));
    }

    public JOutcome.Completed failed(final String msg, final Map<String, Object> outcomes) {
        return failed(Job.DEFAULT_RETURN_CODE_FAILED, msg, outcomes);
    }

    public JOutcome.Completed failed(final Integer returnCode, final String msg, final Map<String, Object> outcomes) {
        if (outcomes == null || outcomes.size() == 0) {
            return failed(returnCode, msg);
        }
        return failedWithMap(returnCode, msg, convert4engine(outcomes));
    }

    public JOutcome.Completed failed() {
        return failed(Job.DEFAULT_RETURN_CODE_FAILED);
    }

    public JOutcome.Completed failed(final Integer returnCode) {
        return failed(returnCode, null);
    }

    public JOutcome.Completed failed(final String msg) {
        return failed(Job.DEFAULT_RETURN_CODE_FAILED, msg);
    }

    public JOutcome.Completed failed(final Integer returnCode, final String msg) {
        logger.failed2slf4j(msg);
        return JOutcome.failed(msg, mapResult(null, returnCode));
    }

    public JOutcome.Completed failed(final String msg, Throwable e) {
        return failed(Job.DEFAULT_RETURN_CODE_FAILED, msg, e);
    }

    public JOutcome.Completed failed(final Integer returnCode, final String msg, Throwable e) {
        Throwable ex = logger.handleException(e);
        logger.failed2slf4j(e.toString(), ex);
        return JOutcome.failed(logger.throwable2String(msg, ex), mapResult(null, returnCode));
    }

    private JOutcome.Completed failedWithMap(final Integer returnCode, final String msg, final Map<String, Value> outcomes) {
        logger.failed2slf4j(msg, outcomes);
        return JOutcome.failed(msg, mapResult(outcomes, returnCode));
    }

    private Map<String, Value> mapResult(Map<String, Value> map, Integer returnCode) {
        if (map == null || map.size() == 0) {
            map = Collections.singletonMap(Job.NAMED_NAME_RETURN_CODE, NumberValue.of(returnCode));
        } else {
            // override
            map.put(Job.NAMED_NAME_RETURN_CODE, NumberValue.of(returnCode));
        }
        return map;
    }

    public String replaceVars(Path path) throws Exception {
        Map<String, Object> vars = Job.asNameValueMap(getAllCurrentArguments());
        put(vars, Job.VAR_NAME_CONTROLLER_ID, getControllerId());
        put(vars, Job.VAR_NAME_ORDER_ID, getOrderId());
        put(vars, Job.VAR_NAME_WORKFLOW_PATH, getWorkflowName());
        put(vars, Job.VAR_NAME_WORKFLOW_POSITION, getWorkflowPosition());
        put(vars, Job.VAR_NAME_JOB_INSTRUCTION_LABEL, getJobInstructionLabel());
        put(vars, Job.VAR_NAME_JOB_NAME, getJobName());

        SOSParameterSubstitutor ps = new SOSParameterSubstitutor(true, "${", "}");
        vars.entrySet().forEach(e -> {
            ps.addKey(e.getKey(), e.getValue().toString());
        });
        return ps.replace(path);
    }

    private void put(Map<String, Object> map, String name, Object value) {
        if (value != null) {
            map.put(name, value);
        }
    }

    private Map<String, Object> getMap(JobOutcomeVariable<?>... outcomes) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (JobOutcomeVariable<?> var : outcomes) {
            if (var.getName() == null || var.getValue() == null) {
                continue;
            }
            map.put(var.getName(), var.getValue());
        }
        return map;
    }

    private Map<String, Value> convert4engine(final Map<String, Object> map) {
        Map<String, Value> result = new HashMap<>();
        if (map == null || map.size() == 0) {
            return result;
        }

        // Collectors.toMap throws an exception when duplicate or value is null
        // return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getEngineValue(e.getValue())));
        map.entrySet().forEach(e -> {
            result.put(e.getKey(), getEngineValue(e.getValue()));
        });
        return result;
    }

    private Value getEngineValue(final Object o) {
        if (o == null) {
            return StringValue.of("");
        }
        if (o instanceof Value) {
            return (Value) o;
        } else if (o instanceof String) {
            return StringValue.of((String) o);
        } else if (o instanceof Boolean) {
            return BooleanValue.of((Boolean) o);
        } else if (o instanceof Integer) {
            return NumberValue.of((Integer) o);
        } else if (o instanceof Long) {
            return NumberValue.of((Long) o);
        } else if (o instanceof Double) {
            return NumberValue.of(BigDecimal.valueOf((Double) o));
        } else if (o instanceof BigDecimal) {
            return NumberValue.of((BigDecimal) o);
        } else if (SOSReflection.isEnum(o.getClass())) {
            return StringValue.of(o.toString());
        } else if (SOSReflection.isList(o.getClass())) {
            List<?> l = (List<?>) o;
            String s = (String) l.stream().map(e -> {
                return e.toString();
            }).collect(Collectors.joining(SOSArgumentHelper.LIST_VALUE_DELIMITER));
            return StringValue.of(s);
        }
        return null;
    }

    private Map<String, Map<String, JobDetailValue>> historicOutcomes2map() {
        List<HistoricOutcome> l = getHistoricOutcomes();
        if (l == null || l.size() == 0) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, JobDetailValue>> resultMap = new HashMap<String, Map<String, JobDetailValue>>();
        for (HistoricOutcome ho : l) {
            Outcome outcome = ho.outcome();
            if (outcome instanceof Completed) {
                Completed c = (Completed) outcome;
                if (c.namedValues() != null) {
                    String key = outcome.getClass().getSimpleName();
                    Map<String, JobDetailValue> map = new HashMap<String, JobDetailValue>();
                    if (resultMap.containsKey(key)) {
                        map = resultMap.get(key);
                    } else {
                        map = new HashMap<String, JobDetailValue>();
                    }
                    Map<String, Object> m = Job.convert(JavaConverters.asJava(c.namedValues()));
                    if (m != null) {
                        for (Map.Entry<String, Object> entry : m.entrySet()) {
                            map.remove(entry.getKey());
                            map.put(entry.getKey(), new JobDetailValue(ho.position().toString(), entry.getValue()));
                        }
                    }
                    map.remove(Job.NAMED_NAME_RETURN_CODE);
                    resultMap.put(key, map);
                }
            }
        }
        return resultMap;
    }

    private Map<String, JobDetailValue> jobResources2map() {
        Map<String, JobDetailValue> resultMap = new LinkedHashMap<>();
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
                    resultMap.put(ee.getKey(), new JobDetailValue(resourceName, Job.getValue(ee.getValue().get())));
                }
            });
        });
        return resultMap;
    }

    protected void logParameterization() {
        try {
            logger.info("Job Parameterization:");
            setKnownArguments();
            Map<ValueSource, List<String>> map = argumentsInfoBySetter();
            if (map == null || map.size() == 0) {
                infoResultingArguments();

                if (logger.isDebugEnabled()) {
                    logJobContextArguments(LogLevel.DEBUG);
                    logOutcomes(LogLevel.DEBUG);
                    logAllResultingArguments(LogLevel.DEBUG);
                }
                return;
            }
            infoResultingArguments();

            if (logger.isDebugEnabled()) {
                logArguments(LogLevel.DEBUG);
                logAllResultingArguments(LogLevel.DEBUG);
            }

        } catch (Exception e) {
            logger.error2allLogger(e.toString(), e);
        }
    }

    protected void logParameterizationOnRequiredArgumentMissingException() {
        try {
            setKnownArguments();
            logArguments(LogLevel.INFO);
            logAllResultingArguments(LogLevel.INFO);
        } catch (Exception e) {
            logger.error2allLogger(e.toString(), e);
        }
    }

    private void logArguments(LogLevel logLevel) throws Exception {
        // ORDER Variables
        Map<String, Object> map = Job.convert(internalStep.order().arguments());
        if (map != null && map.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSource.ORDER.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.log(logLevel, "    %s=%s", e.getKey(), getDisplayValue(e.getKey(), e.getValue()));
            });
        }
        // ORDER or Node arguments
        setKnownArguments();
        List<JobArgument<A>> orderOrNode = getOrderOrNodeArguments();
        if (orderOrNode != null && orderOrNode.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSource.ORDER_OR_NODE.getHeader()));
            orderOrNode.stream().forEach(a -> {
                logger.log(logLevel, "    " + a.toString());
            });
        }
        // JOB Arguments
        map = Job.convert(internalStep.arguments());
        if (map != null && map.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSource.JOB.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.log(logLevel, "    %s=%s", e.getKey(), getDisplayValue(e.getKey(), e.getValue()));
            });
        }
        // JOB Resources
        Map<String, JobDetailValue> resources = getJobResourcesValues();
        if (resources != null && resources.size() > 0) {
            logger.log(logLevel, String.format(" %s:", ValueSource.JOB_RESOURCE.getHeader()));
            resources.entrySet().stream().forEach(e -> {
                JobDetailValue v = e.getValue();
                logger.log(logLevel, "    %s=%s (resource=%s)", e.getKey(), getDisplayValue(e.getKey(), v.getValue()), v.getSource());
            });
        }
        logJobContextArguments(logLevel);
        logOutcomes(logLevel);
    }

    private List<JobArgument<A>> getOrderOrNodeArguments() {
        if (knownArguments == null) {
            return null;
        }
        return knownArguments.stream().filter(a -> {
            if (a.getValueSource().equals(ValueSource.ORDER_OR_NODE)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    private void logJobContextArguments(LogLevel logLevel) {
        if (jobContext != null) {
            Map<String, Object> map = Job.convert(jobContext.jobArguments());
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
        Map<String, JobDetailValue> map = getLastSucceededOutcomes();
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

    private void infoResultingArguments() throws Exception {
        if (knownArguments == null || knownArguments.size() == 0) {
            return;
        }
        logger.info(String.format("%s:", ValueSource.JAVA.getHeader()));
        knownArguments.stream().filter(a -> {
            if (a.isDirty()) {
                return true;
            } else {
                if (a.getValueSource().equals(JobArgument.ValueSource.JAVA)) {
                    if (a.getNotAcceptedValue() != null) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        }).forEach(a -> {
            String detail = a.getValueSource().getDetails() == null ? "" : " " + a.getValueSource().getDetails();
            if (a.getPayload() != null) {
                String pd = SOSArgumentHelper.getClassName(a.getPayload().toString());
                if (detail.equals("")) {
                    detail = " " + pd;
                } else {
                    detail = detail + " " + pd;
                }
            }
            if (a.getNotAcceptedValue() == null) {
                logger.info("    %s=%s (source=%s%s)", a.getName(), a.getDisplayValue(), a.getValueSource().name(), detail);
            } else {
                String exception = "";
                if (a.getNotAcceptedValue().getException() != null) {
                    exception = new StringBuilder("(").append(a.getNotAcceptedValue().getException().toString()).append(")").toString();
                }
                logger.info("    %s=%s (source=%s value=%s[not accepted%s, use from source=%s]%s)", a.getName(), a.getDisplayValue(), a
                        .getNotAcceptedValue().getSource().name(), a.getNotAcceptedValue().getDisplayValue(), exception, a.getNotAcceptedValue()
                                .getUsedValueSource().name(), detail);
            }
        });
    }

    private void logAllResultingArguments(LogLevel logLevel) throws Exception {
        if (knownArguments == null || knownArguments.size() == 0) {
            return;
        }
        logger.log(logLevel, String.format(" All %s:", ValueSource.JAVA.getHeader()));
        knownArguments.stream().forEach(a -> {
            logger.log(logLevel, "    " + a.toString());
        });
    }
}
