package com.sos.js7.job;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSBase64;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.js7.job.JobArguments.MockLevel;
import com.sos.js7.job.ValueSource.ValueSourceType;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.exception.JobException;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.launcher.forjava.internal.BlockingInternalJob;

public abstract class Job<A extends JobArguments> implements BlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);
    private static final String BASE64_VALUE_PREFIX = "base64:";
    private static final String OPERATION_CANCEL_KILL = "cancel/kill";

    private JobEnvironment<A> jobEnvironment;

    public Job() {
        this((JobContext) null);
    }

    /** e.g. for jobContext.jobArguments() or getAgentSystemEncoding */
    public Job(JobContext jobContext) {
        jobEnvironment = new JobEnvironment<A>(jobContext);
    }

    protected void setJobEnvironment(JobEnvironment<A> je) {
        jobEnvironment = je;
    }

    /** to override */
    public void onStart() throws Exception {

    }

    /** to override */
    public void onStop() throws Exception {

    }

    /** to override */
    public void onOrderProcessCancel(OrderProcessStep<A> step) throws Exception {

    }

    /** to override */
    public abstract void processOrder(OrderProcessStep<A> step) throws Exception;

    public JobEnvironment<A> getJobEnvironment() {
        return jobEnvironment;
    }

    /** engine methods */
    @Override
    public Either<Problem, Void> start() {
        try {
            List<JobArgumentException> exceptions = new ArrayList<JobArgumentException>();
            jobEnvironment.setDeclaredArguments(createDeclaredJobArguments(exceptions));
            // TODO
            // checkExceptions(exceptions);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[start]%s", jobEnvironment.getJobKey()));
            }
            onStart();
            return right(null);
        } catch (Throwable e) {
            return left(Problem.fromThrowable(new JobException("[onStart]" + e.toString(), e)));
        }
    }

    @Override
    public void stop() throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[stop]%s", jobEnvironment.getJobKey()));
        }
        onStop();
    }

    public A onCreateJobArguments(List<JobArgumentException> exceptions, final OrderProcessStep<A> step) throws Exception {
        return null;
    }

    @Override
    public OrderProcess toOrderProcess(BlockingInternalJob.Step internalStep) {
        return new OrderProcess() {

            volatile boolean canceled = false;
            AtomicReference<OrderProcessStep<A>> orderProcessStepRef = null;

            public JOutcome.Completed run() throws Exception {
                while (!canceled) {
                    MockLevel mockLevel = MockLevel.OFF;
                    OrderProcessStep<A> step = new OrderProcessStep<A>(jobEnvironment, internalStep);
                    orderProcessStepRef = new AtomicReference<>(step);
                    try {
                        List<JobArgumentException> exceptions = new ArrayList<JobArgumentException>();

                        A args = onCreateJobArguments(exceptions, step);
                        args = createDeclaredJobArguments(exceptions, step, args);

                        step.init(args);

                        mockLevel = step.getDeclaredArguments().getMockLevel().getValue();
                        switch (mockLevel) {
                        case OFF:
                            step.checkAndLogParameterization(exceptions, null);
                            processOrder(step);
                            return step.processed();
                        case ERROR:
                            step.checkAndLogParameterization(exceptions, String.format("Mock Execution: %s=%s.", step.getDeclaredArguments()
                                    .getMockLevel().getName(), mockLevel));
                            return step.success();
                        case INFO:
                        default:
                            step.checkAndLogParameterization(null, String.format("Mock Execution: %s=%s.", step.getDeclaredArguments().getMockLevel()
                                    .getName(), mockLevel));
                            return step.success();
                        }
                    } catch (Throwable e) {
                        switch (mockLevel) {
                        case OFF:
                        case ERROR:
                            return step.failed(e.toString(), e);
                        case INFO:
                        default:
                            step.getLogger().info(String.format("Mock Execution: %s=%s, Exception: %s", step.getDeclaredArguments().getMockLevel()
                                    .getName(), mockLevel, e.toString()));
                            return step.success();
                        }
                    }
                }
                return JOutcome.failed("Canceled");
            }

            @Override
            public void cancel(boolean immediately) {
                if (orderProcessStepRef != null && !canceled) {
                    OrderProcessStep<A> orderProcessStep = orderProcessStepRef.get();
                    if (orderProcessStep != null) {
                        cancelOrderProcessStep(orderProcessStep);
                        Thread thread = Thread.getAllStackTraces().keySet().stream().filter(t -> t.getName().equals(orderProcessStep.getThreadName()))
                                .map(t -> {
                                    return t;
                                }).findAny().orElse(null);
                        if (thread == null) {
                            try {
                                orderProcessStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "][thread][" + orderProcessStep.getThreadName()
                                        + "][skip interrupt]thread not found");
                            } catch (Throwable e) {
                            }
                        } else {
                            orderProcessStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "][thread][" + thread.getName() + "]interrupt ...");
                            thread.interrupt();
                        }
                    }
                }
                canceled = true;
            }
        };
    }

    protected void cancelOrderProcessStep(OrderProcessStep<A> jobStep) {
        String jobName = getJobName(jobStep);
        try {
            onOrderProcessCancel(jobStep);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][job name=%s][onOrderProcessCancel]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }

        jobStep.cancelExecuteJobs();

        if (jobStep.getCancelableResources() != null) {
            cancelHibernateFactory(jobStep, jobName);
            cancelSSHProvider(jobStep, jobName);
            // cancelSQLConnection(jobStep, jobName);
        }
    }

    private void cancelHibernateFactory(OrderProcessStep<A> jobStep, String jobName) {
        try {
            Object o = jobStep.getCancelableResources().get(OrderProcessStep.CANCELABLE_RESOURCE_NAME_HIBERNATE_FACTORY);
            if (o != null) {
                // SOSHibernateSession s = (SOSHibernateSession) o;
                // jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "]close session ...");
                // s.rollback(); s.close() <- does not work because blocked

                // TODO restore factory if initialized onStart ? ...
                SOSHibernateFactory f = (SOSHibernateFactory) o;
                if (f != null) {
                    jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "]close hibernate factory ...");
                    f.close();
                }
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][job name=%s][cancelHibernateFactory]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }

    private void cancelSSHProvider(OrderProcessStep<A> jobStep, String jobName) {
        try {
            Object o = jobStep.getCancelableResources().get(OrderProcessStep.CANCELABLE_RESOURCE_NAME_SSH_PROVIDER);
            if (o != null) {
                jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "]ssh cancelWithKill ...");
                SSHProvider p = (SSHProvider) o;
                p.cancelWithKill();
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][job name=%s][cancelSSHProvider]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }

    // TODO currently not used(candidate PLSQLJob) because abort is asynchronous and conn.close() is synchronous (waiting for execution is completed)
    // PLSQLJob is cancelled when the Thread is interrupted.
    @SuppressWarnings("unused")
    private void cancelSQLConnection(OrderProcessStep<A> jobStep, String jobName) {
        try {
            Object o = jobStep.getCancelableResources().get(OrderProcessStep.CANCELABLE_RESOURCE_NAME_SQL_CONNECTION);
            if (o != null) {
                jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "]abort sql connection ...");
                // ((Connection) o).close();
                ((Connection) o).abort(Runnable::run);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][job name=%s][cancelSQLConnection]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }

    private String getJobName(OrderProcessStep<A> jobStep) {
        try {
            return jobStep == null ? "unknown" : jobStep.getJobName();
        } catch (Throwable e) {
            return "unknown";
        }
    }

    private Map<String, Object> mergeJobAndStepArguments(final OrderProcessStep<A> step) {
        Map<String, Object> map = null;
        if (step == null) {
            map = jobEnvironment.getAllArgumentsAsNameValueMap();
        } else {
            Set<Entry<String, Value>> stepArgs = step.getInternalStep().arguments().entrySet();
            Set<Entry<String, Value>> orderArgs = step.getInternalStep().order().arguments().entrySet();

            Stream<Map.Entry<String, Value>> stream = null;
            if (jobEnvironment.getEngineArguments() == null) {
                stream = Stream.concat(stepArgs.stream(), orderArgs.stream());
            } else {
                stream = Stream.concat(Stream.concat(jobEnvironment.getEngineArguments().entrySet().stream(), stepArgs.stream()), orderArgs.stream());
            }
            map = JobHelper.asJavaValues(stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> value2)));
        }
        return map;
    }

    private A createDeclaredJobArguments(List<JobArgumentException> exceptions) throws Exception {
        return createDeclaredJobArguments(exceptions, null, null);
    }

    protected A createDeclaredJobArguments(List<JobArgumentException> exceptions, final OrderProcessStep<A> step, A instance) throws Exception {
        instance = instance == null ? getJobArgumensClass().getDeclaredConstructor().newInstance() : instance;
        if (jobEnvironment.getEngineArguments() == null && step == null) {
            return instance;
        }
        Map<String, Object> map = mergeJobAndStepArguments(step);
        Map<String, DetailValue> lastSucceededOutcomes = step == null ? null : step.getLastSucceededOutcomes();
        Map<String, DetailValue> jobResources = step == null ? null : step.getJobResourcesArgumentsAsNameDetailValueMap();

        return setDeclaredJobArguments(exceptions, step, map, lastSucceededOutcomes, jobResources, instance);
    }

    protected A setDeclaredJobArguments(List<JobArgumentException> exceptions, final OrderProcessStep<A> step, Map<String, Object> args,
            Map<String, DetailValue> lastSucceededOutcomes, Map<String, DetailValue> jobResources, A instance) throws Exception {

        if (instance.getIncludedArguments() != null && instance.getIncludedArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument<?>>> e : instance.getIncludedArguments().entrySet()) {
                for (JobArgument<?> arg : e.getValue()) {
                    arg.setPayload(e.getKey());
                    setDeclaredJobArgument(step, args, lastSucceededOutcomes, jobResources, arg, null);
                }
            }
        }
        if (instance.hasDynamicArgumentFields()) {
            for (JobArgument<?> arg : instance.getDynamicArgumentFields()) {
                arg.reset();
                setDeclaredJobArgument(step, args, lastSucceededOutcomes, jobResources, arg, null);
            }
        }

        List<Field> fields = JobHelper.getJobArgumentFields(instance);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument<?> arg = (JobArgument<?>) field.get(instance);
                if (arg != null) {
                    setDeclaredJobArgument(step, args, lastSucceededOutcomes, jobResources, arg, field);
                    field.set(instance, arg);
                }
            } catch (JobRequiredArgumentMissingException e) {
                exceptions.add(e);
            } catch (Throwable e) {
                exceptions.add(new JobArgumentException(String.format("[%s.%s][can't get or set field]%s", getClass().getName(), field.getName(), e
                        .toString()), e));
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    protected <V> void setDeclaredJobArgument(final OrderProcessStep<A> step, Map<String, Object> args,
            Map<String, DetailValue> lastSucceededOutcomes, Map<String, DetailValue> jobResources, JobArgument<V> arg, Field field) throws Exception {
        if (arg.getName() == null) {// internal usage
            return;
        }
        if (!arg.isScopeAll()) {
            return;
        }

        // step calls another java job
        if (step != null && step.hasExecuteJobArguments() && step.getExecuteJobBean().getArguments().containsKey(arg.getName())) {
            JobArgument<?> a = step.getExecuteJobBean().getArguments().get(arg.getName());
            try {
                arg.setValue(getValue(a.getValue(), arg, field));

                if (step.getExecuteJobBean().updateDeclaredArgumentsDefinition()) {
                    arg.setRequired(a.isRequired());
                    arg.setDefaultValue((V) a.getDefaultValue());
                    arg.setDisplayMode(a.getDisplayMode());
                    arg.setValueSource(a.getValueSource());
                }
                if (arg.isRequired() && arg.getValue() == null) {
                    throw new JobRequiredArgumentMissingException(arg.getName(), arg.getName());
                }
            } catch (Throwable e) {
                arg.setNotAcceptedValue(a.getValue(), e);
            }
            return;
        }

        List<String> allNames = new ArrayList<>(Arrays.asList(arg.getName()));
        if (arg.getNameAliases() != null) {
            allNames.addAll(arg.getNameAliases());
        }

        // Preference 1 (HIGHEST) - Succeeded Outcomes
        DetailValue jdv = fromMap(lastSucceededOutcomes, allNames);
        if (jdv != null) {
            arg.setValue(getValue(jdv.getValue(), arg, field));
            ValueSource vs = new ValueSource(ValueSourceType.LAST_SUCCEEDED_OUTCOME);
            vs.setSource(jdv.getSource());
            setValueSource(arg, vs);
        } else {
            // Preference 2 - Order Variable or Node Argument
            Object val = step == null ? null : step.getNamedValue(arg);
            boolean isNamedValue = false;
            if (val == null) {
                val = fromMap(args, allNames);
            } else {
                isNamedValue = true;
            }

            // TODO setValue - currently overrides not empty value of a appArgument SOSArgument
            // - solution 1 - set SOSArgument.setDefaultValue instead of SOSArgument.setValue
            // - solution 2 - handle here ...

            // Preference 3 - JobArgument or Argument or Java Default
            if (val == null || SOSString.isEmpty(val.toString())) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("[setDeclaredJobArgument][%s]val=%s(%s),defaultVal=%s(%s)", arg.getName(), arg.getValue(),
                            getClassName(arg.getValue()), arg.getDefaultValue(), getClassName(arg.getDefaultValue())));
                }
                arg.setValue(arg.getDefaultValue());
                setValueType(arg, field, arg.getValue());
            } else {
                arg.setValue(getValue(val, arg, field));
            }
            if (step == null) {
                setValueSource(arg, allNames);
            } else {
                // Preference 4 (LOWEST) - JobResources
                setValueSource(step, field, arg, allNames, isNamedValue, jobResources);
            }
            if (arg.isRequired() && arg.getValue() == null) {
                throw new JobRequiredArgumentMissingException(arg.getName(), arg.getName());
            }
        }
    }

    private <T> T fromMap(Map<String, T> map, List<String> list) {
        if (map == null || list == null) {
            return null;
        }
        return list.stream().filter(map::containsKey).findFirst().map(map::get).orElse(null);
    }

    private String getClassName(Object val) {
        if (val == null) {
            return null;
        }
        try {
            return val.getClass().getName();
        } catch (Throwable e) {
            return e.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private <V> V getValue(Object val, JobArgument<V> arg, Field field) throws ClassNotFoundException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[getValue][%s]val=%s(%s),defaultVal=%s(%s)", arg.getName(), arg.getValue(), getClassName(arg.getValue()), arg
                    .getDefaultValue(), getClassName(arg.getDefaultValue())));
        }
        if (val instanceof String) {
            val = val.toString().trim();
            setValueType(arg, field, val);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[getValue][%s][type=%s][fromString]val=%s(%s),defaultVal=%s(%s)", arg.getName(), arg.getClazzType(), arg
                        .getValue(), getClassName(arg.getValue()), arg.getDefaultValue(), getClassName(arg.getDefaultValue())));
            }
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
            } else if (SOSReflection.isCollection(type)) {
                val = getCollectionValue(val, arg, type);
            }
        } else if (val instanceof Boolean) {
            setValueType(arg, field, val);
            Type type = arg.getClazzType();
            if (type.equals(Boolean.class)) {
                val = Boolean.valueOf(val.toString());
            } else if (SOSReflection.isCollection(type)) {
                val = getCollectionValue(val, arg, type);
            }
        } else if (val instanceof List) {
            setValueType(arg, field, val);
            val = (List<?>) val;
        }
        return (V) val;
    }

    @SuppressWarnings("unchecked")
    private <V> V getCollectionValue(Object val, JobArgument<V> arg, Type type) {
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
        return (V) val;
    }

    private <V> void setValueType(JobArgument<V> arg, Field field, Object val) {
        if (field == null) {
            setValueType(arg, val);
            return;
        }
        arg.setClazzType(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
    }

    private <V> void setValueType(JobArgument<V> arg, Object val) {
        if (arg.getClazzType() == null) {
            LOGGER.info("arg=" + arg.getName());

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

    private <V> void setValueSource(JobArgument<V> arg, List<String> allNames) {
        if (arg.getName() == null) {// source Java - internal usage
            return;
        }
        if (jobEnvironment.getAllArgumentsAsNameValueMap().containsKey(arg.getName())) {
            setValueSource(arg, new ValueSource(ValueSourceType.JOB_ARGUMENT));
        }
    }

    private <V> void setValueSource(final OrderProcessStep<A> step, Field field, JobArgument<V> arg, List<String> allNames, boolean isNamedValue,
            Map<String, DetailValue> jobResources) {
        if (arg.getName() == null) {// source Java - internal usage
            return;
        }
        ValueSource source = null;
        Value v = null;
        if (isNamedValue) {// order or node
            v = fromMap(step.getInternalStep().order().arguments(), allNames);
            source = v == null ? new ValueSource(ValueSourceType.ORDER_OR_NODE) : new ValueSource(ValueSourceType.ORDER);
        } else {
            if (jobEnvironment.getEngineArguments() != null) {
                v = fromMap(jobEnvironment.getEngineArguments(), allNames);
                if (v != null) {
                    source = new ValueSource(ValueSourceType.JOB_ARGUMENT);
                }
            }
            v = fromMap(step.getInternalStep().arguments(), allNames);
            if (v != null) {
                source = new ValueSource(ValueSourceType.JOB);
            }

            // preference 4 (LOWEST) - JobResources
            if (source == null && arg.getValueSource().isTypeJAVA()) {
                DetailValue dv = fromMap(jobResources, allNames);
                if (dv != null) {
                    try {
                        arg.setValue(getValue(dv.getValue(), arg, field));
                        source = new ValueSource(ValueSourceType.JOB_RESOURCE);
                        source.setSource(dv.getSource());
                    } catch (ClassNotFoundException e) {
                        LOGGER.error(String.format("[%s]%s", arg.getName(), e.toString()), e);
                    }
                }
            }
        }
        if (source != null) {
            setValueSource(arg, source);
        }
    }

    private <V> void setValueSource(JobArgument<V> arg, ValueSource source) {
        if (arg.getNotAcceptedValue() != null) {
            arg.getNotAcceptedValue().setSource(source);
            if (arg.getNotAcceptedValue().getUsedValueSource() == null) {
                arg.getNotAcceptedValue().setUsedValueSource(source);
            }
        } else {
            arg.setValueSource(source);
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<A> getJobArgumensClass() throws JobArgumentException {
        try {
            Class<?> clazz = getClass();
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

}
