package com.sos.jitl.jobs.common;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.exception.SOSJobProblemException;

import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

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

    private static Map<String, Value> convert4engine(final Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getValue(e.getValue())));
    }

    private static Value getValue(final Object o) {
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
}
