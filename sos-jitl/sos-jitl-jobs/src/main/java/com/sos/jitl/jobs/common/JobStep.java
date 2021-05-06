package com.sos.jitl.jobs.common;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sos.jitl.jobs.common.JobArgument.ValueSource;
import com.sos.jitl.jobs.exception.SOSJobArgumentException;
import com.sos.jitl.jobs.exception.SOSJobProblemException;

import js7.data.job.JobResourcePath;
import js7.data.order.HistoricOutcome;
import js7.data.order.Outcome;
import js7.data.order.Outcome.Completed;
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;
import js7.executor.forjava.internal.BlockingInternalJob.JobContext;
import scala.collection.JavaConverters;

public class JobStep<A> {

    private final String jobClassName;
    private final JobContext jobContext;
    private final BlockingInternalJob.Step internalStep;
    private final JobLogger logger;
    private A arguments;
    private Map<String, Map<String, JobDetailValue>> lastOutcomes;
    private Map<String, JobDetailValue> jobResourcesValues;
    private List<JobArgument<?>> argumentsInfo;

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

    public Map<JobArgument.ValueSource, List<String>> argumentsInfoBySetter() throws Exception {
        List<JobArgument<?>> arguments = getArgumentsInfo();
        if (arguments == null || arguments.size() == 0) {
            return null;
        }
        Map<JobArgument.ValueSource, List<String>> map = new HashMap<JobArgument.ValueSource, List<String>>();
        for (JobArgument<?> arg : arguments) {
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

    public List<JobArgument<?>> getArgumentsInfo() throws Exception {
        if (internalStep == null || arguments == null) {
            return null;
        }
        if (argumentsInfo == null) {
            List<Field> fields = Job.getJobArgumentFields(arguments);
            List<JobArgument<?>> l = new ArrayList<JobArgument<?>>();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    JobArgument<?> arg = (JobArgument<?>) field.get(arguments);
                    if (arg != null) {
                        if (arg.getName() == null) {// internal usage
                            continue;
                        }
                        l.add(arg);
                    }
                } catch (Throwable e) {
                    throw new SOSJobArgumentException(String.format("[%s.%s][can't read field]%s", getClass().getName(), field.getName(), e
                            .toString()), e);
                }
            }
            argumentsInfo = l;
        }
        return argumentsInfo;
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

    public String getOrderId() throws SOSJobProblemException {
        if (internalStep == null) {
            return null;
        }
        return internalStep.order().id().string();
    }

    public String getAgentId() throws SOSJobProblemException {
        if (internalStep == null) {
            return null;
        }
        return Job.getFromEither(internalStep.order().attached()).string();
    }

    public String getJobName() throws SOSJobProblemException {
        if (internalStep == null) {
            return null;
        }
        return Job.getFromEither(internalStep.workflow().checkedJobName(internalStep.order().workflowPosition().position())).toString();
    }

    public String getWorkflowName() {
        if (internalStep == null) {
            return null;
        }
        return internalStep.order().workflowId().path().name();
    }

    public String getWorkflowVersionId() {
        if (internalStep == null) {
            return null;
        }
        return internalStep.order().workflowId().versionId().toString();
    }

    public String getWorkflowPosition() {
        if (internalStep == null) {
            return null;
        }
        return internalStep.order().workflowPosition().position().toString();
    }

    public Object getCurrentValue(final String name) throws SOSJobProblemException {
        if (internalStep == null) {
            return null;
        }
        Optional<Value> op = Job.getFromEither(internalStep.namedValue(name));
        if (op.isPresent()) {
            return Job.getValue(op.get());
        } else {
            return Job.getValue(internalStep.arguments().get(name));
        }
    }

    public JOutcome.Completed success() {
        return JOutcome.succeeded();
    }

    public JOutcome.Completed success(final String returnValueKey, final Object returnValue) {
        if (returnValueKey != null && returnValue != null) {
            return JOutcome.succeeded(convert4engine(Collections.singletonMap(returnValueKey, returnValue)));
        }
        return JOutcome.succeeded();
    }

    public JOutcome.Completed success(final JobReturnVariable<?>... returnValues) {
        return success(getMap(returnValues));
    }

    public JOutcome.Completed success(final Map<String, Object> returnValues) {
        if (returnValues == null || returnValues.size() == 0) {
            return JOutcome.succeeded();
        }
        return JOutcome.succeeded(convert4engine(returnValues));
    }

    public JOutcome.Completed failed(final String msg, final String returnValueKey, final Object returnValue) {
        if (returnValueKey != null && returnValue != null) {
            return failedWithMap(msg, convert4engine(Collections.singletonMap(returnValueKey, returnValue)));
        }
        return failed(msg);
    }

    public JOutcome.Completed failed(final String msg, JobReturnVariable<?>... returnValues) {
        return failed(msg, getMap(returnValues));
    }

    public JOutcome.Completed failed(final String msg, final Map<String, Object> returnValues) {
        if (returnValues == null || returnValues.size() == 0) {
            return failed(msg);
        }
        return failedWithMap(msg, convert4engine(returnValues));
    }

    public JOutcome.Completed failed() {
        logger.failed2slf4j();
        return JOutcome.failed();
    }

    public JOutcome.Completed failed(String msg) {
        logger.failed2slf4j(msg);
        return JOutcome.failed(msg);
    }

    public JOutcome.Completed failed(final String msg, Throwable e) {
        Throwable ex = logger.handleException(e);
        logger.failed2slf4j(e.toString(), ex);
        return JOutcome.failed(logger.err2String(msg, ex));
    }

    private JOutcome.Completed failedWithMap(final String msg, final Map<String, Value> returnValues) {
        logger.failed2slf4j(msg, returnValues);
        return JOutcome.failed(msg, returnValues);
    }

    private Map<String, Object> getMap(JobReturnVariable<?>... returnValues) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (JobReturnVariable<?> arg : returnValues) {
            if (arg.getName() == null || arg.getValue() == null) {
                continue;
            }
            map.put(arg.getName(), arg.getValue());
        }
        return map;
    }

    private Map<String, Value> convert4engine(final Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getEngineValue(e.getValue())));
    }

    private Value getEngineValue(final Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Value) {
            return (Value) o;
        } else if (o instanceof String) {
            return StringValue.of((String) o);
        } else if (o instanceof Boolean) {
            return BooleanValue.of((Boolean) o);
        } else if (o instanceof Integer) {
            return NumberValue.of((Integer) o); // TODO instanceof Number instead of Integer, Long etc
        } else if (o instanceof Long) {
            return NumberValue.of((Long) o);
        } else if (o instanceof Double) {
            return NumberValue.of(BigDecimal.valueOf((Double) o));
        } else if (o instanceof BigDecimal) {
            return NumberValue.of((BigDecimal) o);
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
        Map<JobResourcePath, Map<String, Value>> jobResources = internalStep.jobResourceToNameToValue();
        if (jobResources == null || jobResources.size() == 0) {
            return resultMap;
        }

        jobResources.entrySet().stream().forEach(e -> {
            String resourceName = e.getKey().string();
            e.getValue().entrySet().stream().forEach(ee -> {
                if (!resultMap.containsKey(ee.getKey())) {
                    resultMap.put(ee.getKey(), new JobDetailValue(resourceName, Job.getValue(ee.getValue())));
                }
            });
        });
        return resultMap;
    }

    protected void logParameterization() {
        try {
            logger.info("Job Parameterization:");
            List<JobArgument<?>> allArguments = getArgumentsInfo();
            Map<ValueSource, List<String>> map = argumentsInfoBySetter();
            if (map == null || map.size() == 0) {
                infoResultingArguments(allArguments);

                if (logger.isDebugEnabled()) {
                    debugJobContextArguments(allArguments);
                    debugOutcomes(allArguments);
                    debugAllResultingArguments(allArguments);
                }
                return;
            }
            infoResultingArguments(allArguments);

            if (logger.isDebugEnabled()) {
                debugArguments(allArguments);
                debugAllResultingArguments(allArguments);
            }

        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }

    private void debugArguments(List<JobArgument<?>> allArguments) throws Exception {
        // ORDER Variables
        Map<String, Object> map = Job.convert(internalStep.order().arguments());
        if (map != null && map.size() > 0) {
            logger.debug(String.format(" %s:", ValueSource.ORDER.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.debug("    %s=%s", e.getKey(), getDisplayValue(allArguments, e.getKey(), e.getValue()));
            });
        }
        // ORDER or Node arguments
        List<JobArgument<?>> arguments = getArgumentsInfo();
        if (arguments != null && arguments.size() > 0) {
            logger.debug(String.format(" %s:", ValueSource.ORDER_OR_NODE.getHeader()));
            arguments.stream().filter(a -> {
                if (a.getValueSource().equals(ValueSource.ORDER_OR_NODE)) {
                    return true;
                }
                return false;
            }).forEach(a -> {
                logger.debug("    " + a.toString());
            });
        }
        // JOB Arguments
        map = Job.convert(internalStep.arguments());
        if (map != null && map.size() > 0) {
            logger.debug(String.format(" %s:", ValueSource.JOB.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.debug("    %s=%s", e.getKey(), getDisplayValue(allArguments, e.getKey(), e.getValue()));
            });
        }
        // JOB Resources
        Map<String, JobDetailValue> resources = getJobResourcesValues();
        if (resources != null && resources.size() > 0) {
            logger.debug(String.format(" %s:", ValueSource.JOB_RESOURCE.getHeader()));
            resources.entrySet().stream().forEach(e -> {
                JobDetailValue v = e.getValue();
                logger.debug("    %s=%s (resource=%s)", e.getKey(), getDisplayValue(allArguments, e.getKey(), v.getValue()), v.getSource());
            });
        }
        debugJobContextArguments(allArguments);
        debugOutcomes(allArguments);
    }

    private void debugJobContextArguments(List<JobArgument<?>> allArguments) {
        if (jobContext != null) {
            Map<String, Object> map = Job.convert(jobContext.jobArguments());
            if (map != null && map.size() > 0) {
                logger.debug(String.format(" %s:", ValueSource.JOB_ARGUMENT.getHeader()));
                map.entrySet().stream().forEach(e -> {
                    logger.debug("    %s=%s", e.getKey(), getDisplayValue(allArguments, e.getKey(), e.getValue()));
                });
            }
        }
    }

    private void debugOutcomes(List<JobArgument<?>> allArguments) {
        // OUTCOME succeeded
        Map<String, JobDetailValue> map = getLastSucceededOutcomes();
        if (map != null && map.size() > 0) {
            logger.debug(String.format(" %s:", ValueSource.LAST_SUCCEEDED_OUTCOME.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.debug("    %s=%s (pos=%s)", e.getKey(), getDisplayValue(allArguments, e.getKey(), e.getValue().getValue()), e.getValue()
                        .getSource());
            });
        }
        // OUTCOME failed
        map = getLastFailedOutcomes();
        if (map != null && map.size() > 0) {
            logger.debug(String.format(" %s:", ValueSource.LAST_FAILED_OUTCOME.getHeader()));
            map.entrySet().stream().forEach(e -> {
                logger.debug("    %s=%s (pos=%s)", e.getKey(), getDisplayValue(allArguments, e.getKey(), e.getValue().getValue()), e.getValue()
                        .getSource());
            });
        }
    }

    private void infoResultingArguments(List<JobArgument<?>> allArguments) throws Exception {
        if (allArguments == null || allArguments.size() == 0) {
            return;
        }
        logger.info(String.format("%s:", ValueSource.JAVA.getHeader()));
        allArguments.stream().filter(a -> {
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
            if (a.getNotAcceptedValue() == null) {
                logger.info("    %s=%s (source=%s%s)", a.getName(), a.getDisplayValue(), a.getValueSource().name(), detail);
            } else {
                logger.info("    %s=%s (source=%s value=%s[not accepted, use default from source=%s]%s)", a.getName(), a.getDisplayValue(), a
                        .getNotAcceptedValue().getSource().name(), a.getNotAcceptedValue().getDisplayValue(), a.getValueSource().name(), detail);
            }
        });
    }

    private void debugAllResultingArguments(List<JobArgument<?>> allArguments) throws Exception {
        if (allArguments == null || allArguments.size() == 0) {
            return;
        }
        logger.debug(String.format(" All %s:", ValueSource.JAVA.getHeader()));
        allArguments.stream().forEach(a -> {
            logger.debug("    " + a.toString());
        });
    }

    private Object getDisplayValue(List<JobArgument<?>> allArguments, String name, Object val) {
        if (allArguments == null) {
            return val;
        }
        JobArgument<?> ar = allArguments.stream().filter(a -> a.getName().equals(name)).findAny().orElse(null);
        if (ar == null) {
            return "<hidden>";
        }
        return ar.getDisplayValue();
    }

}
