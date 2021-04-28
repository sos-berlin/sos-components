package com.sos.jitl.jobs.common;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import js7.executor.forjava.internal.BlockingInternalJob;
import js7.executor.forjava.internal.BlockingInternalJob.JobContext;

public class Job {

    private static final String ENV_NAME_AGENT_HOME = "JS7_AGENT_HOME";
    private static final String ENV_NAME_AGENT_CONFIG_DIR = "JS7_AGENT_CONFIG_DIR";
    private static final String ENV_NAME_AGENT_WORK_DIR = "JS7_AGENT_WORK_DIR";

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

    public static Path getAgentHibernateFile() {
        return Job.getAgentConfigDir().resolve("hibernate.cfg.xml").normalize();
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

    public static <T> T getFromEither(final Either<Problem, T> either) throws SOSJobProblemException {
        if (either.isLeft()) {
            throw new SOSJobProblemException(either.getLeft());
        }
        return either.get();
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

    private static Path getPath(String val) {
        return Paths.get(val == null ? "." : val);
    }

}
