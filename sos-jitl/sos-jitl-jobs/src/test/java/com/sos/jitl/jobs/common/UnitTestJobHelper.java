package com.sos.jitl.jobs.common;

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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSBase64;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.jitl.jobs.exception.SOSJobArgumentException;

import js7.data_for_java.order.JOutcome;

public class UnitTestJobHelper<A extends JobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitTestJobHelper.class);

    private static final String BASE64_VALUE_PREFIX = "base64:";

    private final ABlockingInternalJob<A> job;

    private Map<String, String> environment;

    public UnitTestJobHelper(ABlockingInternalJob<A> job) {
        this.job = job;
        setModifiableEnvironment();
    }

    public void onStart(Map<String, Object> args) throws Exception {
        job.getJobEnvironment().setDeclaredArguments(toArgs(args).instance);
        job.onStart();
    }

    public void onStop() throws Exception {
        job.onStop();
    }

    public JOutcome.Completed onOrderProcess(Map<String, Object> args) throws Exception {
        return onOrderProcess(args, null);
    }

    public JOutcome.Completed onOrderProcess(Map<String, Object> args, SOSTimeout timeout) throws Exception {
        if (timeout == null) {
            timeout = new SOSTimeout(2, TimeUnit.MINUTES);
        }

        final OrderProcessStep<A> step = newOrderProcessStep(args);
        return CompletableFuture.supplyAsync(() -> {
            try {
                step.logParameterization(null);
                this.job.onOrderProcess(step);
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
        ArgumentsResult r = toArgs(args);
        step.init(r.instance, r.notDeclared);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ArgumentsResult toArgs(Map<String, Object> args) throws Exception {
        A instance = getJobArgumensClass().getDeclaredConstructor().newInstance();

        if (instance.getIncludedArguments() != null && instance.getIncludedArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument>> e : instance.getIncludedArguments().entrySet()) {
                for (JobArgument arg : e.getValue()) {
                    arg.setPayload(e.getKey());
                    if (arg.getName() != null && args.containsKey(arg.getName())) {
                        Object val = args.get(arg.getName());
                        if (val == null || SOSString.isEmpty(val.toString())) {
                            arg.setValue(arg.getDefaultValue());
                        } else {
                            arg.setValue(getValue(val, arg, null));
                        }
                    }
                }
            }
        }

        Set<String> declared = setArguments(args, instance);
        ArgumentsResult r = new ArgumentsResult();
        r.instance = instance;
        r.notDeclared = args.entrySet().stream().filter(e -> !declared.contains(e.getKey())).collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue));

        return r;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Set<String> setArguments(Map<String, Object> map, JobArguments o) {
        List<Field> fields = JobHelper.getJobArgumentFields(o);
        Set<String> known = new HashSet<>();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                LOGGER.trace("  [" + field.getName() + "]field type=" + field.getGenericType());
                JobArgument arg = (JobArgument<?>) field.get(o);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    Object val = map.get(arg.getName());
                    if (val == null) {
                        arg.setValue(arg.getDefaultValue());
                    } else {
                        if (!known.contains(arg.getName())) {
                            known.add(arg.getName());
                        }
                        if (SOSString.isEmpty(val.toString())) {
                            arg.setValue(arg.getDefaultValue());
                        } else {
                            arg.setValue(getValue(val, arg, field));
                        }
                    }
                    field.set(o, arg);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", o.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
        return known;
    }

    private Object getValue(Object val, JobArgument<A> arg, Field field) throws ClassNotFoundException {
        if (val instanceof String) {
            val = val.toString().trim();
            setValueType(arg, field);
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
                } else if (type.equals(URI.class)) {
                    val = URI.create(val.toString());
                } else if (SOSReflection.isList(type)) {
                    boolean asStringList = true;
                    String listVal = val.toString();
                    try {
                        Type subType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        if (subType.equals(String.class)) {
                        } else if (SOSReflection.isEnum(subType)) {
                            val = Stream.of(listVal.split(SOSArgumentHelper.LIST_VALUE_DELIMITER)).map(v -> {
                                Object e = null;
                                try {
                                    e = SOSReflection.enumIgnoreCaseValueOf(subType.getTypeName(), v.trim());
                                } catch (ClassNotFoundException ex) {
                                    e = v.trim();
                                }
                                return e;
                            }).collect(Collectors.toList());
                            asStringList = false;
                        }
                    } catch (Throwable e) {
                    }
                    if (asStringList) {
                        val = Stream.of(listVal.split(SOSArgumentHelper.LIST_VALUE_DELIMITER)).map(String::trim).collect(Collectors.toList());
                    }
                } else if (SOSReflection.isEnum(type)) {
                    Object v = SOSReflection.enumIgnoreCaseValueOf(type.getTypeName(), val.toString());
                    if (v == null) {
                        arg.setNotAcceptedValue(val, null);
                        arg.getNotAcceptedValue().setUsedValueSource(JobArgument.ValueSource.JAVA);
                        val = arg.getDefaultValue();
                    } else {
                        val = v;
                    }
                } else if (type.equals(Charset.class)) {
                    try {
                        val = Charset.forName(val.toString());
                    } catch (Throwable e) {
                        arg.setNotAcceptedValue(val, e);
                        arg.getNotAcceptedValue().setUsedValueSource(JobArgument.ValueSource.JAVA);
                        val = arg.getDefaultValue();
                    }
                }
            }
        } else if (val instanceof BigDecimal) {
            setValueType(arg, field);
            Type type = arg.getClazzType();
            if (type.equals(Integer.class)) {
                val = Integer.valueOf(((BigDecimal) val).intValue());
            } else if (type.equals(Long.class)) {
                val = Long.valueOf(((BigDecimal) val).longValue());
            } else if (type.equals(String.class)) {
                val = val.toString();
            }
        }
        return val;
    }

    private void setValueType(JobArgument<A> arg, Field field) {
        if (field == null) {
            if (arg.getClazzType() == null) {
                arg.setClazzType(Object.class);
            }
            return;
        }
        arg.setClazzType(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
    }

    @SuppressWarnings("unchecked")
    private Class<A> getJobArgumensClass() throws SOSJobArgumentException {
        try {
            Class<?> clazz = job.getClass();
            while (clazz.getSuperclass() != ABlockingInternalJob.class) {
                clazz = clazz.getSuperclass();
                if (clazz == null)
                    throw new SOSJobArgumentException(String.format("%s super class not found for %s", ABlockingInternalJob.class.getSimpleName(),
                            getClass()));
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
            throw new SOSJobArgumentException(String.format("can't evaluate JobArguments class for job %s: %s", getClass().getName(), e.toString()),
                    e);
        }
    }

    private class ArgumentsResult {

        private A instance;
        private Map<String, Object> notDeclared;
    }

}
