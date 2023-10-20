package com.sos.js7.job;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.js7.job.exception.JobProblemException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.BooleanValue;
import js7.data.value.ListValue;
import js7.data.value.NumberValue;
import js7.data.value.ObjectValue;
import js7.data.value.StringValue;
import js7.data.value.Value;

public class JobHelper {

    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(
                    SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);

    public static final String NAMED_NAME_RETURN_CODE = "returnCode";

    public static final Integer DEFAULT_RETURN_CODE_SUCCEEDED = Integer.valueOf(0);
    public static final Integer DEFAULT_RETURN_CODE_FAILED = Integer.valueOf(99);

    public static final String VAR_NAME_CONTROLLER_ID = "js7ControllerId";
    public static final String VAR_NAME_ORDER_ID = "js7OrderId";
    public static final String VAR_NAME_WORKFLOW_PATH = "js7WorkflowPath";
    public static final String VAR_NAME_WORKFLOW_POSITION = "js7WorkflowPosition";
    public static final String VAR_NAME_JOB_INSTRUCTION_LABEL = "js7Label";
    public static final String VAR_NAME_JOB_NAME = "js7JobName";

    public static final String ENV_NAME_AGENT_HOME = "JS7_AGENT_HOME";
    public static final String ENV_NAME_AGENT_CONFIG_DIR = "JS7_AGENT_CONFIG_DIR";
    public static final String ENV_NAME_AGENT_WORK_DIR = "JS7_AGENT_WORK_DIR";

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
        return getAgentConfigDir().resolve("hibernate.cfg.xml").normalize();
    }

    public static Map<String, Object> asJavaValues(final Map<String, Value> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> asJavaValue(e.getValue())));
    }

    public static Object asJavaValue(final Value o) {
        if (o == null) {
            return null;
        }
        if (o instanceof StringValue) {
            return o.convertToString();
        } else if (o instanceof NumberValue) {
            return ((NumberValue) o).toJava();
        } else if (o instanceof BooleanValue) {
            return Boolean.parseBoolean(o.convertToString());
        } else if (o instanceof ListValue) {
            List<Object> l = new ArrayList<>();
            ((ListValue) o).toJava().forEach(item -> {
                if (item instanceof js7.data.value.ObjectValue) {
                    Map<String, Object> m = new HashMap<>();
                    ((ObjectValue) item).toJava().forEach((k1, v1) -> m.put(k1, v1.toJava()));
                    l.add(m);
                } else {
                    l.add(item.toJava());
                }
            });
            return l;
        }
        return o;
    }

    public static Map<String, Value> asEngineValues(final Map<String, Object> map) {
        Map<String, Value> result = new HashMap<>();
        if (map == null || map.size() == 0) {
            return result;
        }
        // Collectors.toMap throws an exception when duplicate or value is null
        // return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getEngineValue(e.getValue())));
        map.entrySet().forEach(e -> {
            result.put(e.getKey(), asEngineValue(e.getValue()));
        });
        return result;
    }

    private static Value asEngineValue(final Object o) {
        if (o == null) {
            return StringValue.of("");
        }
        if (o instanceof Value) {
            return (Value) o;
        } else if (o instanceof String) {
            return StringValue.of((String) o);
        } else if (o instanceof Boolean) {
            return BooleanValue.of(((Boolean) o).booleanValue());
        } else if (o instanceof Integer) {
            return NumberValue.of((Integer) o);
        } else if (o instanceof Long) {
            return NumberValue.of(((Long) o).longValue());
        } else if (o instanceof Double) {
            return NumberValue.of(BigDecimal.valueOf((Double) o));
        } else if (o instanceof BigDecimal) {
            return NumberValue.of((BigDecimal) o);
        } else if (o instanceof Date) {
            return getDateAsStringValue((Date) o);
        } else if (SOSReflection.isList(o.getClass())) {
            // TODO use ListValue?
            List<?> l = (List<?>) o;
            String s = (String) l.stream().map(e -> {
                return e.toString();
            }).collect(Collectors.joining(SOSArgumentHelper.LIST_VALUE_DELIMITER));
            return StringValue.of(s);
        }
        return StringValue.of(o.toString());
    }

    public static Map<String, Object> asNameValueMap(Map<String, JobArgument<?>> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream().filter(a -> a.getValue().getValue() != null).collect(Collectors.toMap(a -> a.getKey(), a -> a.getValue()
                .getValue()));
    }

    @SuppressWarnings("unused")
    private static Map<String, Object> asNameValueMap(JobArguments o) {
        List<Field> fields = getJobArgumentFields(o);
        Map<String, Object> map = new HashMap<>();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument<?> arg = (JobArgument<?>) field.get(o);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    if (arg.getValue() == null || SOSString.isEmpty(arg.getValue().toString())) {
                        map.put(arg.getName(), arg.getDefaultValue());
                    } else {
                        map.put(arg.getName(), arg.getValue());
                    }
                }
            } catch (Throwable e) {
                //
            }
        }
        return map;
    }

    public static List<Field> getJobArgumentFields(JobArguments o) {
        return SOSReflection.getAllDeclaredFields(o.getClass()).stream().filter(f -> f.getType().equals(JobArgument.class)).collect(Collectors
                .toList());
    }

    @SuppressWarnings("unused")
    private static List<Field> getOrderProcessStepOutcomeVariableFields(JobArguments o) {
        return SOSReflection.getAllDeclaredFields(o.getClass()).stream().filter(f -> f.getType().equals(OrderProcessStepOutcomeVariable.class))
                .collect(Collectors.toList());
    }

    public static <T> T getFromEither(final Either<Problem, T> either) throws JobProblemException {
        if (either.isLeft()) {
            throw new JobProblemException(either.getLeft());
        }
        return either.get();
    }

    private static Path getPath(String val) {
        return Paths.get(val == null ? "." : val);
    }

    private static Value getDateAsStringValue(Date date) {
        try {
            return StringValue.of(SOSDate.getDateTimeAsString(date));
        } catch (Throwable e) {
            return StringValue.of(date == null ? "" : date.toString());
        }
    }

}
