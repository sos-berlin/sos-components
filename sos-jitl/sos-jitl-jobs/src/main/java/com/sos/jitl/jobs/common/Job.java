package com.sos.jitl.jobs.common;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.exception.SOSJobProblemException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;
import js7.executor.forjava.internal.BlockingInternalJob.JobContext;

public class Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    private static final String ENV_NAME_AGENT_HOME = "JS7_AGENT_HOME";
    private static final String ENV_NAME_AGENT_CONFIG_DIR = "JS7_AGENT_CONFIG_DIR";
    private static final String ENV_NAME_AGENT_WORK_DIR = "JS7_AGENT_WORK_DIR";

    public static JOutcome.Completed success() {
        return JOutcome.succeeded();
    }

    public static JOutcome.Completed success(final String returnValueKey, final Object returnValue) {
        if (returnValueKey != null && returnValue != null) {
            return JOutcome.succeeded(convert4engine(Collections.singletonMap(returnValueKey, returnValue)));
        }
        return JOutcome.succeeded();
    }

    public static JOutcome.Completed success(final JobReturnVariable<?>... returnValues) {
        return success(getMap(returnValues));
    }

    public static JOutcome.Completed success(final Map<String, Object> returnValues) {
        if (returnValues == null || returnValues.size() == 0) {
            return JOutcome.succeeded();
        }
        return JOutcome.succeeded(convert4engine(returnValues));
    }

    public static JOutcome.Completed failed() {
        return JOutcome.failed();
    }

    public static JOutcome.Completed failed(final String msg, Throwable e) {
        if (e == null) {
            return JOutcome.failed(msg);
        }
        return JOutcome.failed(new StringBuilder(msg).append("\n").append(SOSString.toString(e)).toString());
    }

    public static JOutcome.Completed failed(final String msg, final String returnValueKey, final Object returnValue) {
        if (returnValueKey != null && returnValue != null) {
            return JOutcome.failed(msg, convert4engine(Collections.singletonMap(returnValueKey, returnValue)));
        }
        return JOutcome.failed(msg);
    }

    public static JOutcome.Completed failed(final String msg, JobReturnVariable<?>... returnValues) {
        return failed(msg, getMap(returnValues));
    }

    public static JOutcome.Completed failed(final String msg, final Map<String, Object> returnValues) {
        if (returnValues == null || returnValues.size() == 0) {
            return JOutcome.failed(msg);
        }
        return JOutcome.failed(msg, convert4engine(returnValues));
    }

    @Deprecated
    public static Map<String, Object> mergeArguments(final JobContext jobContext, final BlockingInternalJob.Step step) {
        if (step == null) {
            return convert(jobContext.jobArguments());
        }
        Stream<Map<String, Value>> stream = null;
        if (jobContext == null) {
            stream = Stream.of(step.order().arguments(), step.arguments());
        } else {
            stream = Stream.of(jobContext.jobArguments(), step.order().arguments(), step.arguments());
        }
        return convert(stream.flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> T getArgument(Map<String, Object> args, String name) {
        T val = (T) args.get(name);
        if (val != null && val instanceof String) {
            val = (T) val.toString().trim();
        }
        return val;
    }

    @Deprecated
    public static <T> T getArgument(Map<String, Object> args, String name, T defaultValue) {
        T val = getArgument(args, name);
        if (val == null || val.toString().length() == 0) {
            val = defaultValue;
        }
        return val;
    }

    public static Path getAgentHome() {
        return getPath(System.getenv(ENV_NAME_AGENT_HOME));
    }

    public static Path getAgentConfigDir() {
        return getPath(System.getenv(ENV_NAME_AGENT_CONFIG_DIR));
    }

    public static Path getAgentWorkDir() {
        return getPath(System.getenv(ENV_NAME_AGENT_WORK_DIR));
    }

    public static Path getAgentPrivateConfFile() {
        return getAgentConfigDir().resolve("private").resolve("private.conf").normalize();
    }

    private static Path getPath(String val) {
        return Paths.get(val == null ? "." : val);
    }

    public static SOSParameterSubstitutor getSubstitutor(final Map<String, Object> args) {
        if (args == null) {
            return null;
        }
        SOSParameterSubstitutor s = new SOSParameterSubstitutor();
        args.entrySet().stream().filter(e -> !SOSString.isEmpty(e.getValue().toString())).forEach(e -> {
            s.addKey(e.getKey(), e.getValue().toString());
        });
        return s;
    }

    @SuppressWarnings("rawtypes")
    public static <T> SOSParameterSubstitutor getSubstitutor(final T args) {
        if (args == null) {
            return null;
        }
        SOSParameterSubstitutor s = new SOSParameterSubstitutor();
        List<Field> fields = getJobArgumentFields(args);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument arg = (JobArgument<?>) field.get(args);
                if (arg != null) {
                    if (arg.getName() == null) {
                        continue;// internal usage
                    }
                    s.addKey(arg.getName(), arg.getValue().toString());
                }
            } catch (Throwable e) {
            }
        }
        return s;
    }

    public static List<Field> getJobArgumentFields(Object o) {
        return SOSReflection.getAllDeclaredFields(o.getClass()).stream().filter(f -> f.getType().equals(JobArgument.class)).collect(Collectors
                .toList());
    }

    public static String replaceVars(SOSParameterSubstitutor substitutor, final String val) {
        if (substitutor == null || val == null) {
            return val;
        }
        String result = val;
        if (val.matches("(?s).*\\$\\{[^{]+\\}.*")) {
            substitutor.setOpenTag("${");
            substitutor.setCloseTag("}");
            result = substitutor.replace(val);
        }

        if (result.contains("%")) {
            substitutor.setOpenTag("%");
            substitutor.setCloseTag("%");
            result = substitutor.replace(result);
        }
        return result;
    }

    public static long getTimeAsSeconds(final JobArgument<String> arg) {
        String val = SOSString.isEmpty(arg.getValue()) ? arg.getDefault() : arg.getValue();
        if (SOSString.isEmpty(val)) {
            return 0L;
        }

        int[] num = { 1, 60, 3600, 3600 * 24 };
        int j = 0;
        long seconds = 0L;
        String[] arr = val.split(":");
        for (int i = arr.length - 1; i >= 0; i--) {
            seconds += new Integer(arr[i]) * num[j++];
        }
        return seconds;
    }

    public static Map<String, Object> convert(final Map<String, Value> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getValue(e.getValue())));
    }

    public static void info(final BlockingInternalJob.Step step, final Object msg) {
        step.out().println(msg);
    }

    public static void info(final BlockingInternalJob.Step step, final String format, final Object... msg) {
        info(step, String.format(format, msg));
    }

    public static void debug(final BlockingInternalJob.Step step, final Object msg) {
        step.out().println(String.format("[DEBUG]%s", msg));
    }

    public static void debug(final BlockingInternalJob.Step step, final String format, final Object... msg) {
        debug(step, String.format(format, msg));
    }

    public static void trace(final BlockingInternalJob.Step step, final Object msg) {
        step.out().println(String.format("[TRACE]%s", msg));
    }

    public static void trace(final BlockingInternalJob.Step step, final String format, final Object... msg) {
        trace(step, String.format(format, msg));
    }

    public static void warn(final BlockingInternalJob.Step step, final Object msg) {
        warn(step, msg, null);
    }

    public static void warn(final BlockingInternalJob.Step step, final Object msg, Throwable e) {
        step.out().println(String.format("[WARN]%s", msg));
        LOGGER.warn(String.format("[WARN]%s", msg), e);
    }

    public static void warn(final BlockingInternalJob.Step step, final String format, final Object... msg) {
        warn(step, String.format(format, msg));
    }

    public static void error(final BlockingInternalJob.Step step, final Object msg) {
        error(step, msg, null);
    }

    public static void error(final BlockingInternalJob.Step step, final Object msg, final Throwable e) {
        step.err().println(String.format("[ERROR]%s", msg));
        LOGGER.error(String.format("[ERROR]%s", msg), e);
    }

    public static void error(final BlockingInternalJob.Step step, final String format, final Object... msg) {
        error(step, String.format(format, msg));
    }

    public static <T> T getFromEither(final Either<Problem, T> either) throws SOSJobProblemException {
        if (either.isLeft()) {
            throw new SOSJobProblemException(either.getLeft());
        }
        return either.get();
    }

    public static String getOrderId(final BlockingInternalJob.Step step) throws SOSJobProblemException {
        return step.order().id().string();
    }

    public static String getAgentId(final BlockingInternalJob.Step step) throws SOSJobProblemException {
        return getFromEither(step.order().attached()).string();
    }

    public static String getJobName(final BlockingInternalJob.Step step) throws SOSJobProblemException {
        return getFromEither(step.workflow().checkedJobName(step.order().workflowPosition().position())).toString();
    }

    public static String getWorkflowName(final BlockingInternalJob.Step step) {
        return step.order().workflowId().path().name();
    }

    public static String getWorkflowVersionId(final BlockingInternalJob.Step step) {
        return step.order().workflowId().versionId().toString();
    }

    public static String getWorkflowPosition(final BlockingInternalJob.Step step) {
        return step.order().workflowPosition().position().toString();
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

    public static Object getValue(final Value o) {
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

    private static Map<String, Object> getMap(JobReturnVariable<?>... returnValues) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (JobReturnVariable<?> arg : returnValues) {
            if (arg.getName() == null || arg.getValue() == null) {
                continue;
            }
            map.put(arg.getName(), arg.getValue());
        }
        return map;
    }
}
