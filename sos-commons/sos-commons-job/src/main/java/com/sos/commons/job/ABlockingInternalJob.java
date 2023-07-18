package com.sos.commons.job;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.job.JobArgument.ValueSource;
import com.sos.commons.job.JobArguments.MockLevel;
import com.sos.commons.job.exception.JobArgumentException;
import com.sos.commons.job.exception.JobException;
import com.sos.commons.job.exception.JobRequiredArgumentMissingException;
import com.sos.commons.util.SOSBase64;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.vfs.ssh.SSHProvider;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.launcher.forjava.internal.BlockingInternalJob;

public abstract class ABlockingInternalJob<A extends JobArguments> implements BlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABlockingInternalJob.class);
    private static final String BASE64_VALUE_PREFIX = "base64:";
    private static final String OPERATION_CANCEL_KILL = "cancel/kill";

    private final JobEnvironment<A> jobEnvironment;

    public ABlockingInternalJob() {
        this(null);
    }

    /** e.g. for jobContext.jobArguments() or getAgentSystemEncoding */
    public ABlockingInternalJob(JobContext jobContext) {
        jobEnvironment = new JobEnvironment<A>(jobContext);
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
    public abstract void onOrderProcess(OrderProcessStep<A> step) throws Exception;

    public JobEnvironment<A> getJobEnvironment() {
        return jobEnvironment;
    }

    /** engine methods */
    @Override
    public Either<Problem, Void> start() {
        try {
            List<JobArgumentException> exceptions = new ArrayList<JobArgumentException>();
            jobEnvironment.setDeclaredArguments(createJobArguments(exceptions));
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
                        args = createJobArguments(exceptions, step, args);

                        step.init(args);

                        mockLevel = step.getDeclaredArguments().getMockLevel().getValue();
                        switch (mockLevel) {
                        case OFF:
                            step.checkAndLogParameterization(exceptions, null);
                            onOrderProcess(step);
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

    private void cancelOrderProcessStep(OrderProcessStep<A> jobStep) {
        String jobName = getJobName(jobStep);
        try {
            onOrderProcessCancel(jobStep);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][job name=%s][onOrderProcessCancel]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
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

    private A createJobArguments(List<JobArgumentException> exceptions) throws Exception {
        return createJobArguments(exceptions, null, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private A createJobArguments(List<JobArgumentException> exceptions, final OrderProcessStep<A> step, A instance) throws Exception {
        instance = instance == null ? getJobArgumensClass().getDeclaredConstructor().newInstance() : instance;
        if (jobEnvironment.getEngineArguments() == null && step == null) {
            return instance;
        }
        Map<String, Object> map = mergeJobAndStepArguments(step);
        Map<String, DetailValue> lastSucceededOutcomes = step == null ? null : step.getLastSucceededOutcomes();
        Map<String, DetailValue> jobResources = step == null ? null : step.getJobResourcesArgumentsAsNameDetailValueMap();

        if (instance.getIncludedArguments() != null && instance.getIncludedArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument>> e : instance.getIncludedArguments().entrySet()) {
                for (JobArgument arg : e.getValue()) {
                    arg.setPayload(e.getKey());
                    setJobArgument(step, map, lastSucceededOutcomes, jobResources, arg, null);
                }
            }
        }
        if (instance.hasDynamicArgumentFields()) {
            for (JobArgument<String> arg : instance.getDynamicArgumentFields()) {
                setJobArgument(step, map, lastSucceededOutcomes, jobResources, arg, null);
            }
        }
        return setJobArguments(exceptions, step, map, lastSucceededOutcomes, jobResources, instance);
    }

    @SuppressWarnings({ "rawtypes" })
    private A setJobArguments(List<JobArgumentException> exceptions, final OrderProcessStep<A> step, Map<String, Object> map,
            Map<String, DetailValue> lastSucceededOutcomes, Map<String, DetailValue> jobResources, A instance) throws Exception {

        List<Field> fields = JobHelper.getJobArgumentFields(instance);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument arg = (JobArgument<?>) field.get(instance);
                if (arg != null) {
                    setJobArgument(step, map, lastSucceededOutcomes, jobResources, arg, field);
                    field.set(instance, arg);
                }
            } catch (JobRequiredArgumentMissingException e) {
                exceptions.add(e);
            } catch (Throwable e) {
                exceptions.add(new JobArgumentException(String.format("[%s.%s][can't get or set field]%s", getClass().getName(), field.getName(), e
                        .toString()), e));
            }
        }

        // if (instance.hasDynamicArgumentFields()) {
        // for (JobArgument arg : instance.getDynamicArgumentFields()) {
        // setJobArgument(step, map, lastSucceededOutcomes, jobResources, arg, null);
        // }
        // }

        return instance;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setJobArgument(final OrderProcessStep<A> step, Map<String, Object> map, Map<String, DetailValue> lastSucceededOutcomes,
            Map<String, DetailValue> jobResources, JobArgument arg, Field field) throws Exception {
        if (arg.getName() == null) {// internal usage
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
            ValueSource vs = ValueSource.LAST_SUCCEEDED_OUTCOME;
            vs.setDetails("pos=" + jdv.getSource());
            setValueSource(arg, vs);
        } else {
            // Preference 2 - Order Variable or Node Argument
            Object val = step == null ? null : step.getNamedValue(arg);
            boolean isNamedValue = false;
            if (val == null) {
                val = fromMap(map, allNames);
            } else {
                isNamedValue = true;
            }

            // TODO setValue - currently overrides not empty value of a appArgument SOSArgument
            // - solution 1 - set SOSArgument.setDefaultValue instead of SOSArgument.setValue
            // - solution 2 - handle here ...

            // Preference 3 - JobArgument or Argument or Java Default
            if (val == null || SOSString.isEmpty(val.toString())) {
                arg.setValue(arg.getDefaultValue());
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
                arg.setClazzType(String.class);
            }
            return;
        }
        arg.setClazzType(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
    }

    private void setValueSource(JobArgument<A> arg, List<String> allNames) {
        if (arg.getName() == null) {// source Java - internal usage
            return;
        }
        if (jobEnvironment.getAllArgumentsAsNameValueMap().containsKey(arg.getName())) {
            setValueSource(arg, JobArgument.ValueSource.JOB_ARGUMENT);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setValueSource(final OrderProcessStep<A> step, Field field, JobArgument arg, List<String> allNames, boolean isNamedValue,
            Map<String, DetailValue> jobResources) {
        if (arg.getName() == null) {// source Java - internal usage
            return;
        }
        ValueSource source = null;
        Value v = null;
        if (isNamedValue) {// order or node
            v = fromMap(step.getInternalStep().order().arguments(), allNames);
            source = v == null ? JobArgument.ValueSource.ORDER_OR_NODE : JobArgument.ValueSource.ORDER;
        } else {
            if (jobEnvironment.getEngineArguments() != null) {
                v = fromMap(jobEnvironment.getEngineArguments(), allNames);
                if (v != null) {
                    source = JobArgument.ValueSource.JOB_ARGUMENT;
                }
            }
            v = fromMap(step.getInternalStep().arguments(), allNames);
            if (v != null) {
                source = JobArgument.ValueSource.JOB;
            }

            // preference 4 (LOWEST) - JobResources
            if (source == null && arg.getValueSource().equals(ValueSource.JAVA)) {
                DetailValue jdv = fromMap(jobResources, allNames);
                if (jdv != null) {
                    try {
                        arg.setValue(getValue(jdv.getValue(), arg, field));
                        source = ValueSource.JOB_RESOURCE;
                        source.setDetails("resource=" + jdv.getSource());
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

    private void setValueSource(JobArgument<A> arg, ValueSource source) {
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
    private Class<A> getJobArgumensClass() throws JobArgumentException {
        try {
            Class<?> clazz = getClass();
            while (clazz.getSuperclass() != ABlockingInternalJob.class) {
                clazz = clazz.getSuperclass();
                if (clazz == null)
                    throw new JobArgumentException(String.format("%s super class not found for %s", ABlockingInternalJob.class.getSimpleName(),
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
            throw new JobArgumentException(String.format("can't evaluate JobArguments class for job %s: %s", getClass().getName(), e.toString()), e);
        }
    }

}
