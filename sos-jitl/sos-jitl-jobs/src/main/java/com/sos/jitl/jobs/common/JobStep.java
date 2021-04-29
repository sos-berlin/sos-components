package com.sos.jitl.jobs.common;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.JobArgument.ValueSource;
import com.sos.jitl.jobs.exception.SOSJobArgumentException;
import com.sos.jitl.jobs.exception.SOSJobProblemException;

import js7.data.order.HistoricOutcome;
import js7.data.order.Outcome;
import js7.data.order.Outcome.Completed;
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;
import scala.collection.JavaConverters;

public class JobStep<A> {

    private final BlockingInternalJob.Step internalStep;
    private final JobLogger logger;
    private final A arguments;
    private Map<String, Map<String, Object>> lastOutcomes;

    protected JobStep(BlockingInternalJob.Step step, JobLogger logger, A arguments) {
        this.internalStep = step;
        this.logger = logger;
        this.arguments = arguments;
        logParameterization();
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

    public Map<String, Map<String, Object>> getLastOutcomes() {
        if (lastOutcomes == null) {
            lastOutcomes = historicOutcomes2map();
        }
        return lastOutcomes;
    }

    public Map<String, Object> getLastSucceededOutcomes() {
        return getLastOutcomes().get(Outcome.Succeeded.class.getSimpleName());
    }

    public Map<String, Object> getLastFailedOutcomes() {
        return getLastOutcomes().get(Outcome.Failed.class.getSimpleName());
    }

    public Map<JobArgument.ValueSource, List<String>> argumentsInfo() throws Exception {
        if (internalStep == null || arguments == null) {
            return null;
        }
        List<Field> fields = Job.getJobArgumentFields(arguments);
        Map<JobArgument.ValueSource, List<String>> map = new HashMap<JobArgument.ValueSource, List<String>>();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument<?> arg = (JobArgument<?>) field.get(arguments);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    List<String> l;
                    if (map.containsKey(arg.getValueSource())) {
                        l = map.get(arg.getValueSource());
                    } else {
                        l = new ArrayList<String>();
                    }
                    l.add(String.format("%s=%s", arg.getName(), arg.getDisplayValue()));
                    map.put(arg.getValueSource(), l);
                }
            } catch (Throwable e) {
                throw new SOSJobArgumentException(String.format("[%s.%s][can't read field]%s", getClass().getName(), field.getName(), e.toString()),
                        e);
            }
        }
        return map;
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

    public Object getCurrentValue(final String name) {
        if (internalStep == null) {
            return null;
        }
        Optional<Value> op = internalStep.namedValue(name);
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

    public JOutcome.Completed failed() {
        return JOutcome.failed();
    }

    protected static JOutcome.Completed failed(final String msg, Throwable e) {
        if (e == null) {
            return JOutcome.failed(msg);
        }
        return JOutcome.failed(new StringBuilder(msg).append("\n").append(SOSString.toString(e)).toString());
    }

    public JOutcome.Completed failed(final String msg, final String returnValueKey, final Object returnValue) {
        if (returnValueKey != null && returnValue != null) {
            return JOutcome.failed(msg, convert4engine(Collections.singletonMap(returnValueKey, returnValue)));
        }
        return JOutcome.failed(msg);
    }

    public JOutcome.Completed failed(final String msg, JobReturnVariable<?>... returnValues) {
        return failed(msg, getMap(returnValues));
    }

    public JOutcome.Completed failed(final String msg, final Map<String, Object> returnValues) {
        if (returnValues == null || returnValues.size() == 0) {
            return JOutcome.failed(msg);
        }
        return JOutcome.failed(msg, convert4engine(returnValues));
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

    private Map<String, Map<String, Object>> historicOutcomes2map() {
        List<HistoricOutcome> l = getHistoricOutcomes();
        if (l == null || l.size() == 0) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, Object>> resultMap = new HashMap<String, Map<String, Object>>();
        for (HistoricOutcome ho : l) {
            Outcome outcome = ho.outcome();
            if (outcome instanceof Completed) {
                Completed c = (Completed) outcome;
                if (c.namedValues() != null) {
                    String key = outcome.getClass().getSimpleName();
                    Map<String, Object> map = new HashMap<String, Object>();
                    if (resultMap.containsKey(key)) {
                        map = resultMap.get(key);
                    } else {
                        map = new HashMap<String, Object>();
                    }
                    Map<String, Object> m = Job.convert(JavaConverters.asJava(c.namedValues()));
                    if (m != null) {
                        for (Map.Entry<String, Object> entry : m.entrySet()) {
                            map.remove(entry.getKey());
                            map.put(entry.getKey(), entry.getValue());
                        }
                    }
                    map.remove(Job.NAMED_NAME_RETURN_CODE);
                    resultMap.put(key, map);
                }
            }
        }
        return resultMap;
    }

    private void logParameterization() {
        try {
            logger.info("Job Parameterization:");
            Map<ValueSource, List<String>> map = argumentsInfo();
            if (map == null || map.size() == 0) {
                logOutcomes();
                return;
            }
            logInfo(map, ValueSource.ORDER);
            logInfo(map, ValueSource.NODE);
            logInfo(map, ValueSource.JOB);
            logInfo(map, ValueSource.JOB_ARGUMENT);

            logOutcomes();

        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }

    // TODO mask password ...
    private void logOutcomes() {
        logInfo("Last Succeeded Outcomes: ", getLastSucceededOutcomes());
        logInfo("Last Failed Outcomes: ", getLastFailedOutcomes());
    }

    private void logInfo(Map<ValueSource, List<String>> map, ValueSource source) {
        List<String> list = map.get(source);
        if (list != null && list.size() > 0) {
            logger.info("%s: %s", source.getValue(), String.join(", ", list));
        }
    }

    private void logInfo(String title, Map<String, Object> map) {
        if (map != null && map.size() > 0) {
            String v = map.toString();
            logger.info("%s: %s", title, v.substring(1, v.length() - 1));
        }
    }
}
