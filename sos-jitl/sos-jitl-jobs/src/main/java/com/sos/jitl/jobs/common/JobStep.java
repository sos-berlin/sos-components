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

    protected JobStep(BlockingInternalJob.Step step, JobLogger logger, A arguments) {
        this.internalStep = step;
        this.logger = logger;
        this.arguments = arguments;
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

    public Map<String, Object> historicOutcomes2map() {
        return historicOutcomes2map(Outcome.Completed.class);
    }

    public Map<String, Object> succeededHistoricOutcomes2map() {
        return historicOutcomes2map(Outcome.Succeeded.class);
    }

    public Map<String, Object> failedHistoricOutcomes2map() {
        return historicOutcomes2map(Outcome.Failed.class);
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

    private Map<String, Object> historicOutcomes2map(Class<? extends Completed> clazz) {
        List<HistoricOutcome> l = getHistoricOutcomes();
        if (l == null || l.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<String, Object>();
        for (HistoricOutcome ho : l) {
            Map<String, Object> m = map2java(ho, clazz);
            if (m != null) {
                m.entrySet().forEach(e -> {
                    map.remove(e.getKey());
                    map.put(e.getKey(), e.getValue());
                });
            }
        }
        map.remove(Job.NAMED_NAME_RETURN_CODE);
        return map;
    }

    @SuppressWarnings("unchecked")
    private <T extends Completed> Map<String, Object> map2java(HistoricOutcome ho, Class<T> clazz) {
        Outcome outcome = ho.outcome();
        if (outcome == null) {
            return null;
        }
        if (clazz.isInterface() || clazz.isInstance(outcome)) {
            T c = (T) outcome;
            if (c.namedValues() != null) {
                return Job.convert(JavaConverters.asJava(c.namedValues()));
            }
        }
        return null;
    }
}
