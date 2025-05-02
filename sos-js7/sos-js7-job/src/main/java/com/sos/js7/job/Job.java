package com.sos.js7.job;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.js7.job.JobArguments.MockLevel;
import com.sos.js7.job.ValueSource.ValueSourceType;
import com.sos.js7.job.exception.JobArgumentException;
import com.sos.js7.job.exception.JobException;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;
import com.sos.js7.job.resolver.JobArgumentValueResolverCache;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.launcher.forjava.internal.BlockingInternalJob;

public abstract class Job<A extends JobArguments> implements BlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    public static final String OPERATION_CANCEL_KILL = "cancel/kill";

    private JobEnvironment<A> jobEnvironment;

    static {
        JobArgumentValueResolverCache.initialize();
    }

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
        return new InterruptibleOrderProcess() {

            volatile boolean canceled = false;
            AtomicReference<OrderProcessStep<A>> orderProcessStepRef = null;

            @Override
            public JOutcome.Completed runInterruptible() throws Exception {
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
                try {
                    if (orderProcessStepRef != null && !canceled) {
                        OrderProcessStep<A> orderProcessStep = orderProcessStepRef.get();
                        boolean log = false;
                        if (orderProcessStep != null) {
                            log = true;
                            cancelOrderProcessStep(orderProcessStep);
                        }
                        Thread thread = thread();
                        if (thread == null) {
                            if (log) {
                                orderProcessStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "][thread][skip interrupt]thread not found");
                            }
                        } else {
                            if (log) {
                                orderProcessStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "][thread][" + thread.getName() + "]interrupt ...");
                            }
                            thread.interrupt();
                        }
                    }
                } finally {
                    canceled = true;
                }
            }
        };
    }

    protected void cancelOrderProcessStep(OrderProcessStep<A> jobStep) {
        String jobName = getJobName(jobStep);
        try {
            onOrderProcessCancel(jobStep);
        } catch (Throwable e) {
            jobStep.getLogger().error(String.format("[%s][job name=%s][onOrderProcessCancel]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
        jobStep.cancelExecuteJobs();
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
                arg.setValue(getDeclaredArgumentValue(step, arg, field, a.getValue()));

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
            arg.setValue(getDeclaredArgumentValue(step, arg, field, jdv.getValue()));
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
                arg.setValue(arg.getDefaultValue());
                setDeclaredArgumentValueType(arg, field, arg.getValue());
                // getDeclaredArgumentValue(step, arg, field, val);
            } else {
                arg.setValue(getDeclaredArgumentValue(step, arg, field, val));
            }
            if (step == null) {
                setDeclaredArgumentValueSource(arg, allNames);
            } else {
                // Preference 4 (LOWEST) - JobResources
                setDeclaredArgumentValueSource(step, field, arg, allNames, isNamedValue, jobResources);
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
    private <V> V getDeclaredArgumentValue(final OrderProcessStep<A> step, JobArgument<V> arg, Field field, Object val)
            throws ClassNotFoundException {
        boolean isTraceEnabled = step != null && step.getLogger().isTraceEnabled();
        if (isTraceEnabled) {
            step.getLogger().trace(String.format("[getDeclaredArgumentValue][%s][arg value=%s(%s),defaultValue=%s(%s)]val=%s(%s)", arg.getName(), arg
                    .getValue(), getClassName(arg.getValue()), arg.getDefaultValue(), getClassName(arg.getDefaultValue()), val, getClassName(val)));
        }
        try {
            setDeclaredArgumentValueType(arg, field, val);
            if (isTraceEnabled) {
                step.getLogger().trace(String.format("    [getDeclaredArgumentValue][%s]clazzType=%s,argumentType=%s,argumentFlatType=%s", arg
                        .getName(), arg.getClazzType(), arg.getArgumentType(), arg.getArgumentFlatType()));
            }

            switch (arg.getArgumentType()) {
            case FLAT:
                if (!hasValueStartsWith(step, val)) {
                    val = JobArgument.convertFlatValue(arg, val);
                }
                break;
            case LIST:
                if (val instanceof List) {// e.g. JOC/Agent List type (Singleton maps as values)
                    val = convertListValue(step, arg, val);
                } else {
                    val = convertCollectionStringValue(step, arg, val);// string values separated by delimiter
                }
                break;
            case SET:
                val = convertCollectionStringValue(step, arg, val);
                break;
            case MAP:
                val = convertMapValue(step, arg, val);
                break;
            }
        } catch (Throwable e) {
            arg.setNotAcceptedValue(val, e);
            arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
            val = arg.getDefaultValue();
        }
        return (V) val;
    }

    @SuppressWarnings("unchecked")
    private <V> V convertCollectionStringValue(final OrderProcessStep<A> step, JobArgument<V> arg, Object val) {
        String valAsString = val.toString();
        try {
            Stream<? extends Object> stream = Stream.of(valAsString.split(SOSArgumentHelper.DEFAULT_LIST_VALUE_DELIMITER)).map(v -> {
                try {
                    if (hasValueStartsWith(step, v)) {
                        return v;
                    } else {
                        return JobArgument.convertFlatValue(arg, v);
                    }
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(v + " of " + valAsString, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            }).filter(Objects::nonNull);
            if (arg.isSet()) {
                val = stream.collect(Collectors.toSet());
            } else {
                val = stream.collect(Collectors.toList());
            }
        } catch (Throwable e) {
            arg.setNotAcceptedValue(valAsString, e);
            arg.getNotAcceptedValue().setSource(arg.getValueSource());
            arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
            val = arg.getDefaultValue();
        }
        return (V) val;
    }

    @SuppressWarnings("unchecked")
    private <V> V convertListValue(final OrderProcessStep<A> step, JobArgument<V> arg, Object val) {
        final String valAsString = val.toString();
        try {
            val = ((List<?>) val).stream().map(entry -> {
                try {
                    if (hasValueStartsWith(step, entry)) {
                        return entry;
                    } else {
                        return JobArgument.convertFlatValue(arg, entry);
                    }
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(entry + " of " + valAsString, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Throwable e) {
            arg.setNotAcceptedValue(valAsString, e);
            arg.getNotAcceptedValue().setSource(arg.getValueSource());
            arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
            val = arg.getDefaultValue();
        }
        return (V) val;
    }

    @SuppressWarnings("unchecked")
    private <V> V convertMapValue(final OrderProcessStep<A> step, JobArgument<V> arg, Object val) {
        final String valAsString = val.toString();
        try {
            val = ((Map<String, ?>) val).entrySet().stream().map(entry -> {
                try {
                    if (hasValueStartsWith(step, entry.getValue())) {
                        return Map.entry(entry.getKey(), entry.getValue());
                    } else {
                        return Map.entry(entry.getKey(), JobArgument.convertFlatValue(arg, entry.getValue()));
                    }
                } catch (Throwable e) {
                    arg.setNotAcceptedValue(entry.getValue() + " of " + valAsString, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        } catch (Throwable e) {
            arg.setNotAcceptedValue(valAsString, e);
            arg.getNotAcceptedValue().setSource(arg.getValueSource());
            arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
            val = arg.getDefaultValue();
        }
        return (V) val;
    }

    private <V> boolean hasValueStartsWith(final OrderProcessStep<A> step, Object val) {
        if (step == null) {
            return false;
        }
        String valueAsStr = val.toString();
        return step.getResolverPrefixes().parallelStream().anyMatch(valueAsStr::startsWith);
    }

    private <V> void setDeclaredArgumentValueType(JobArgument<V> arg, Field field, Object val) throws Exception {
        if (field == null) {
            setDeclaredArgumentValueType(arg, val);
            return;
        }
        arg.setClazzType(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
        arg.setArgumentType();
    }

    private <V> void setDeclaredArgumentValueType(JobArgument<V> arg, Object val) throws Exception {
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
        arg.setArgumentType();
    }

    private <V> void setDeclaredArgumentValueSource(JobArgument<V> arg, List<String> allNames) {
        if (arg.getName() == null) {// source Java - internal usage
            return;
        }
        if (jobEnvironment.getAllArgumentsAsNameValueMap().containsKey(arg.getName())) {
            setValueSource(arg, new ValueSource(ValueSourceType.JOB_ARGUMENT));
        }
    }

    private <V> void setDeclaredArgumentValueSource(final OrderProcessStep<A> step, Field field, JobArgument<V> arg, List<String> allNames,
            boolean isNamedValue, Map<String, DetailValue> jobResources) {
        if (arg.getName() == null || step.getInternalStep() == null) {// source Java - internal usage
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
            v = fromMap((Map<String, Value>) step.getInternalStep().arguments(), allNames);
            if (v != null) {
                source = new ValueSource(ValueSourceType.JOB);
            }

            // preference 4 (LOWEST) - JobResources
            if (source == null && arg.getValueSource().isTypeJAVA()) {
                DetailValue dv = fromMap(jobResources, allNames);
                if (dv != null) {
                    try {
                        arg.setValue(getDeclaredArgumentValue(step, arg, field, dv.getValue()));
                        source = new ValueSource(ValueSourceType.JOB_RESOURCE);
                        source.setSource(dv.getSource());
                    } catch (ClassNotFoundException e) {
                        if (step != null) {
                            step.getLogger().error(String.format("[%s]%s", arg.getName(), e.toString()), e);
                        }
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
