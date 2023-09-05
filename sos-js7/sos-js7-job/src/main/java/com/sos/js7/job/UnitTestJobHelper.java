package com.sos.js7.job;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSBase64;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.js7.job.ValueSource.ValueSourceType;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

import js7.data_for_java.order.JOutcome;

public class UnitTestJobHelper<A extends JobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTestJobHelper.class);

    private static final String BASE64_VALUE_PREFIX = "base64:";
    private final Job<A> job;
    private Map<String, String> environment;

    public UnitTestJobHelper(Job<A> job) {
        this.job = job;
        setModifiableEnvironment();
    }

    public void onStart(Map<String, Object> args) throws Exception {
        job.getJobEnvironment().setDeclaredArguments(toArgs(args, null).instance);
        SOSReflection.setDeclaredFieldValue(job.getJobEnvironment(), "allArguments", args);

        job.onStart();
    }

    public void onStop() throws Exception {
        job.onStop();
    }

    public Job<A> getJobs() {
        return job;
    }

    public JOutcome.Completed processOrder(Map<String, Object> args) throws Exception {
        return processOrder(args, null);
    }

    public JOutcome.Completed processOrder(Map<String, Object> args, SOSTimeout timeout) throws Exception {
        if (timeout == null) {
            timeout = new SOSTimeout(2, TimeUnit.MINUTES);
        }

        final OrderProcessStep<A> step = newOrderProcessStep(args);
        return CompletableFuture.supplyAsync(() -> {
            try {
                step.checkAndLogParameterization(null, null);
                this.job.processOrder(step);
                return step.processed();
            } catch (Throwable e) {
                return step.failed(e.toString(), e);
            }
        }).get(timeout.getInterval(), timeout.getTimeUnit());
    }

    public static OrderProcessStepLogger newLogger() {
        OrderProcessStepLogger l = new OrderProcessStepLogger(null);
        l.init(null);
        return l;
    }

    public void setEnvVar(String n, String v) {
        if (environment != null) {
            environment.put(n, v);
        }
    }

    private OrderProcessStep<A> newOrderProcessStep(Map<String, Object> args) throws Exception {
        OrderProcessStep<A> step = new OrderProcessStep<A>(job.getJobEnvironment(), null);
        ArgumentsResult r = toArgs(args, job.onCreateJobArguments(null, step));

        step.init(r.instance, r.undeclared);
        return step;
    }

    @SuppressWarnings("unchecked")
    private void setModifiableEnvironment() {
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Field props = pe.getDeclaredField("theCaseInsensitiveEnvironment");
            props.setAccessible(true);
            environment = (Map<String, String>) props.get(null);
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_HOME, "");
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "");
            setDefaultEnvVar(JobHelper.ENV_NAME_AGENT_WORK_DIR, "");
        } catch (Throwable e) {
            LOGGER.info(String.format(
                    "[can't set env][use 'Run Configurations -> Environment' instead or allow access with '--add-opens java.base/java.util=ALL-UNNAMED']%s",
                    e.toString()));
        }
    }

    private void setDefaultEnvVar(String n, String v) {
        if (environment != null && environment.get(n) == null) {
            environment.put(n, v);
        }
    }

    private ArgumentsResult toArgs(Map<String, Object> args, A instance) throws Exception {
        instance = instance == null ? getJobArgumensClass().getDeclaredConstructor().newInstance() : instance;
        Set<String> declared = setDeclaredJobArguments(args, instance);
        ArgumentsResult r = new ArgumentsResult();
        r.instance = instance;
        r.undeclared = args.entrySet().stream().filter(e -> !declared.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue));
        return r;
    }

    @SuppressWarnings({ "rawtypes" })
    private Set<String> setDeclaredJobArguments(Map<String, Object> args, JobArguments instance) throws Exception {
        Set<String> declared = new HashSet<>();

        if (instance.getIncludedArguments() != null && instance.getIncludedArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument>> e : instance.getIncludedArguments().entrySet()) {
                for (JobArgument arg : e.getValue()) {
                    arg.setPayload(e.getKey());
                    if (arg.getName() == null) {
                        continue;
                    }
                    if (!declared.contains(arg.getName())) {
                        declared.add(arg.getName());
                    }
                    if (args.containsKey(arg.getName())) {
                        setDeclaredJobArgument(args, arg, null, instance);
                    }
                }
            }
        }

        if (instance.hasDynamicArgumentFields()) {
            for (JobArgument arg : instance.getDynamicArgumentFields()) {
                if (arg.getName() == null) {
                    continue;
                }
                if (!declared.contains(arg.getName())) {
                    declared.add(arg.getName());
                }
                setDeclaredJobArgument(args, arg, null, instance);
            }
        }

        List<Field> fields = JobHelper.getJobArgumentFields(instance);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                LOGGER.trace("  [" + field.getName() + "]field type=" + field.getGenericType());
                JobArgument arg = (JobArgument<?>) field.get(instance);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    if (!declared.contains(arg.getName())) {
                        declared.add(arg.getName());
                    }
                    setDeclaredJobArgument(args, arg, field, instance);
                    field.set(instance, arg);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", instance.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
        return declared;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void setDeclaredJobArgument(Map<String, Object> args, JobArgument arg, Field field, JobArguments instance) throws Exception {
        Object val = args.get(arg.getName());
        if (val == null || SOSString.isEmpty(val.toString())) {
            arg.setValue(arg.getDefaultValue());
            setValueType(arg, field, val);
        } else {
            arg.setValue(getValue(val, arg, field));
        }

        if (arg.isRequired() && arg.getValue() == null) {
            throw new JobRequiredArgumentMissingException(arg.getName(), arg.getName());
        }
    }

    private Object getValue(Object val, JobArgument<A> arg, Field field) throws ClassNotFoundException {
        if (val instanceof String) {
            val = val.toString().trim();
            setValueType(arg, field, val);
            Type type = arg.getClazzType();
            if (type.equals(String.class)) {
                if (((String) val).startsWith(BASE64_VALUE_PREFIX)) {
                    try {
                        val = SOSBase64.decode(val.toString().substring(BASE64_VALUE_PREFIX.length()));
                    } catch (Throwable e) {
                        arg.setNotAcceptedValue(val, e);
                    }
                }
            } else {
                if (type.equals(Path.class)) {
                    val = Paths.get(val.toString());
                } else if (type.equals(File.class)) {
                    val = new File(val.toString());
                } else if (type.equals(URI.class)) {
                    val = URI.create(val.toString());
                } else if (SOSReflection.isCollection(type)) {
                    val = getCollectionValue(val, arg, type);
                } else if (SOSReflection.isEnum(type)) {
                    Object v = SOSReflection.enumIgnoreCaseValueOf(type.getTypeName(), val.toString());
                    if (v == null) {
                        arg.setNotAcceptedValue(val, null);
                        arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                        val = arg.getDefaultValue();
                    } else {
                        val = v;
                    }
                } else if (type.equals(Charset.class)) {
                    try {
                        val = Charset.forName(val.toString());
                    } catch (Throwable e) {
                        arg.setNotAcceptedValue(val, e);
                        arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                        val = arg.getDefaultValue();
                    }
                }
            }
        } else if (val instanceof BigDecimal) {
            setValueType(arg, field, val);
            Type type = arg.getClazzType();
            if (type.equals(Integer.class)) {
                val = Integer.valueOf(((BigDecimal) val).intValue());
            } else if (type.equals(Long.class)) {
                val = Long.valueOf(((BigDecimal) val).longValue());
            } else if (type.equals(String.class)) {
                val = val.toString();
            }
        } else if (val instanceof Boolean) {
            setValueType(arg, field, val);
            val = Boolean.valueOf(val.toString());
        } else if (val instanceof List) {
            setValueType(arg, field, val);
            val = (List<?>) val;
        }
        return val;
    }

    private Object getCollectionValue(Object val, JobArgument<A> arg, Type type) {
        String listVal = val.toString();
        Type subType = ((ParameterizedType) type).getActualTypeArguments()[0];

        Function<? super String, ? extends Object> mapFunc = (v) -> {
            return v.trim();
        };

        if (subType.equals(String.class)) {
        } else if (subType.equals(Integer.class)) {
            mapFunc = (v) -> {
                try {
                    return Integer.valueOf(v.trim());
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + listVal, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            };
        } else if (subType.equals(Long.class)) {
            mapFunc = (v) -> {
                try {
                    return Long.valueOf(v.trim());
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + listVal, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            };
        } else if (subType.equals(BigDecimal.class)) {
            mapFunc = (v) -> {
                try {
                    if (listVal.contains(".") || listVal.contains(",")) {
                        return BigDecimal.valueOf(Double.valueOf(v.trim()));
                    }
                    return BigDecimal.valueOf(Long.valueOf(v.trim()));
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + listVal, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            };
        } else if (subType.equals(Path.class)) {
            mapFunc = (v) -> {
                try {
                    return Paths.get(v.trim());
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + listVal, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            };
        } else if (subType.equals(File.class)) {
            mapFunc = (v) -> {
                try {
                    return new File(v.trim());
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + listVal, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            };
        } else if (subType.equals(URI.class)) {
            mapFunc = (v) -> {
                try {
                    return URI.create(v.trim());
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + listVal, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            };
        } else if (subType.equals(Charset.class)) {
            mapFunc = (v) -> {
                try {
                    return Charset.forName(v.trim());
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + listVal, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            };
        } else if (subType.equals(Boolean.class)) {
            mapFunc = (v) -> {
                try {
                    return Boolean.valueOf(v.trim());
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + listVal, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            };
        } else {
            try {
                if (SOSReflection.isEnum(subType)) {
                    mapFunc = (v) -> {
                        try {
                            return SOSReflection.enumIgnoreCaseValueOf(subType.getTypeName(), v.trim());
                        } catch (ClassNotFoundException e) {
                            arg.setNotAcceptedValue(v + " of " + listVal, e);
                            arg.getNotAcceptedValue().setSource(arg.getValueSource());
                            arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                            return null;
                        }
                    };
                }
            } catch (ClassNotFoundException e) {
                arg.setNotAcceptedValue(listVal, e);
                arg.getNotAcceptedValue().setSource(arg.getValueSource());
                arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));

                mapFunc = (v) -> {
                    return null;
                };
            }
        }

        try {
            Stream<? extends Object> stream = Stream.of(listVal.split(SOSArgumentHelper.LIST_VALUE_DELIMITER)).map(mapFunc).filter(Objects::nonNull);
            if (SOSReflection.isSet(type)) {
                val = stream.collect(Collectors.toSet());
            } else {
                val = stream.collect(Collectors.toList());
            }
        } catch (Throwable e) {
            arg.setNotAcceptedValue(listVal, e);
            arg.getNotAcceptedValue().setSource(arg.getValueSource());
            arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
            val = arg.getDefaultValue();
        }
        return val;
    }

    private void setValueType(JobArgument<A> arg, Field field, Object val) {
        if (field == null) {
            setValueType(arg, val);
            return;
        }
        arg.setClazzType(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
    }

    @SuppressWarnings("rawtypes")
    private void setValueType(JobArgument arg, Object val) {
        if (arg.getClazzType() == null) {
            if (arg.getValue() != null) {
                try {
                    arg.setClazzType(arg.getValue().getClass());
                } catch (Throwable e) {
                }
            } else if (val != null) {
                try {
                    arg.setClazzType(val.getClass());
                } catch (Throwable e) {
                }
            }
            if (arg.getClazzType() == null) {
                arg.setClazzType(Object.class);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Class<A> getJobArgumensClass() throws JobArgumentException {
        try {
            Class<?> clazz = job.getClass();
            while (clazz.getSuperclass() != Job.class) {
                clazz = clazz.getSuperclass();
                if (clazz == null)
                    throw new JobArgumentException(String.format("%s super class not found for %s", Job.class.getSimpleName(), getClass()));
            }
            Type gsc = clazz.getGenericSuperclass();
            try {
                return (Class<A>) ((ParameterizedType) gsc).getActualTypeArguments()[0];
            } catch (Throwable e) {
                if (gsc.getTypeName().endsWith(">")) {// com.sos.jitl.jobs.common.ABlockingInternalJob<com.sos.jitl.jobs....Arguments>
                    throw e;
                }
                return (Class<A>) JobArguments.class;// (Class<A>) Object.class;
            }
        } catch (Throwable e) {
            throw new JobArgumentException(String.format("can't evaluate JobArguments class for job %s: %s", getClass().getName(), e.toString()), e);
        }
    }

    private class ArgumentsResult {

        private A instance;
        private Map<String, Object> undeclared;
    }

}
