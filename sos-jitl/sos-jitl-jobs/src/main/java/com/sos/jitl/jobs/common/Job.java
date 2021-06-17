package com.sos.jitl.jobs.common;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.exception.SOSJobProblemException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;

public class Job {

    public static final String NAMED_NAME_RETURN_CODE = "returnCode";

    public static final Integer DEFAULT_RETURN_CODE_SUCCEEDED = Integer.valueOf(0);
    public static final Integer DEFAULT_RETURN_CODE_FAILED = Integer.valueOf(99);

    public static final String VAR_NAME_CONTROLLER_ID = "js7ControllerId";
    public static final String VAR_NAME_ORDER_ID = "js7OrderId";
    public static final String VAR_NAME_WORKFLOW_PATH = "js7WorkflowPath";
    public static final String VAR_NAME_WORKFLOW_POSITION = "js7WorkflowPosition";
    public static final String VAR_NAME_JOB_INSTRUCTION_LABEL = "js7Label";
    public static final String VAR_NAME_JOB_NAME = "js7JobName";

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

    public static long getTimeAsSeconds(final JobArgument<String> arg) {
        return SOSDate.getTimeAsSeconds(SOSString.isEmpty(arg.getValue()) ? arg.getDefaultValue() : arg.getValue());
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
            return ((NumberValue) o).toJava();
        } else if (o instanceof BooleanValue) {
            return Boolean.parseBoolean(o.convertToString());
        }
        return o;
    }

    public static <A> Map<String, Object> asNameValueMap(Map<String, JobArgument<A>> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().filter(a -> a.getValue().getValue() != null).collect(Collectors.toMap(a -> a.getKey(), a -> a.getValue()
                .getValue()));
    }

    protected static List<Field> getJobArgumentFields(JobArguments o) {
        return SOSReflection.getAllDeclaredFields(o.getClass()).stream().filter(f -> f.getType().equals(JobArgument.class)).collect(Collectors
                .toList());
    }

    private static Path getPath(String val) {
        return Paths.get(val == null ? "." : val);
    }

}
