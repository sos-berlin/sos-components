package com.sos.jitl.jobs.common;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.jitl.jobs.exception.JobProblemException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class Job {

    public static JOutcome.Completed success() {
        return success(null);
    }

    public static JOutcome.Completed success(String returnValueKey, Object returnValue) {
        Map<String, Object> map = null;
        if (returnValueKey != null && returnValue != null) {
            map = Collections.singletonMap(returnValueKey, returnValue);
        }
        return JOutcome.succeeded(convert4engine(map));
    }

    public static JOutcome.Completed success(Map<String, Object> returnValues) {
        return JOutcome.succeeded(convert4engine(returnValues));
    }

    public static JOutcome.Completed failed() {
        return failed(null);
    }

    public static JOutcome.Completed failed(String msg) {
        return JOutcome.failed(msg);
    }

    public static Map<String, Object> convert(Map<String, Value> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getValue(e.getValue())));
    }

    public static <T> T getFromEither(Either<Problem, T> either) throws JobProblemException {
        if (either.isLeft()) {
            throw new JobProblemException(either.getLeft());
        }
        return either.get();
    }

    public static String getOrderId(BlockingInternalJob.Step step) throws JobProblemException {
        return step.order().id().string();
    }

    public static String getAgentId(BlockingInternalJob.Step step) throws JobProblemException {
        return getFromEither(step.order().attached()).string();
    }

    public static String getJobName(BlockingInternalJob.Step step) throws JobProblemException {
        return getFromEither(step.workflow().checkedJobName(step.order().workflowPosition().position())).toString();
    }

    public static String getWorkflowName(BlockingInternalJob.Step step) {
        return step.order().workflowId().path().name();
    }

    public static String getWorkflowVersionId(BlockingInternalJob.Step step) {
        return step.order().workflowId().versionId().toString();
    }

    public static String getWorkflowPosition(BlockingInternalJob.Step step) {
        return step.order().workflowPosition().position().toString();
    }

    private static Map<String, Value> convert4engine(Map<String, Object> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getValue(e.getValue())));
    }

    private static Value getValue(Object o) {
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
            return NumberValue.of((Integer) o); // TODO o instanceof Number instead of Integer, Long etc
        } else if (o instanceof Long) {
            return NumberValue.of((Long) o);
        } else if (o instanceof Double) {
            return NumberValue.of(BigDecimal.valueOf((Double) o));
        } else if (o instanceof BigDecimal) {
            return NumberValue.of((BigDecimal) o);
        }
        return null;
    }

    private static Object getValue(Value o) {
        if (o == null) {
            return null;
        }
        if (o instanceof StringValue) {
            return o.convertToString();
        } else if (o instanceof NumberValue) {
            return ((NumberValue) o).toJava();// TODO Integer etc
        } else if (o instanceof BooleanValue) {
            return Boolean.parseBoolean(o.convertToString());
        }
        return o;
    }
}
