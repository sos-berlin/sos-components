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

import com.sos.commons.util.SOSCollection;
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

/** @apiNote An empty (no-argument) constructor will be removed in a future release.<br/>
 *          Subclasses must provide a constructor like the following:
 * 
 *          <pre>
 *          public class MyNewJob extends Job&lt;JobArguments&gt; {
 * 
 *              public MyNewJob(JobContext jobContext) {
 *                  super(jobContext);
 *              }
 *          }
 *          </pre>
 */
public abstract class Job<A extends JobArguments> implements BlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    public static final String OPERATION_CANCEL_KILL = "cancel/kill";

    private JobEnvironment<A> jobEnvironment;

    static {
        JobArgumentValueResolverCache.initialize();
    }

    /** @deprecated This constructor will be removed in a future release.
     *             <p>
     *             Use a constructor like the following in your subclass instead:
     * 
     *             <pre>
     *             public class MyNewJob extends Job&lt;JobArguments&gt; {
     * 
     *                 public MyNewJob(JobContext jobContext) {
     *                     super(jobContext);
     *                 }
     *             }
     *             </pre>
     * 
     *             This no-argument constructor only exists for backward compatibility and will create a legacy-compatible default context, which may lead to
     *             limited or incorrect behavior.
     *
     * @since 2.8.1 */
    @Deprecated
    public Job() {
        // No logging here to avoid noise in customer environments.
        // Fallback to a legacy-compatible context to keep backward compatibility.
        this((JobContext) null);
    }

    /** Uses original JobContext (systemEncoding, etc) provided by the Agent */
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
    public void onProcessOrderCanceled(OrderProcessStep<A> step) throws Exception {

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
            jobEnvironment.setDeclaredArguments(createDeclaredJobArgumentsOnStart(exceptions));
            // TODO
            // checkExceptions(exceptions);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[start]%s", jobEnvironment.getJobKey()));
            }
            onStart();
            return right(null);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }
            return left(Problem.fromThrowable(new JobException("[onStart]" + msg, e)));
        }
    }

    @Override
    public void stop() throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[stop]%s", jobEnvironment.getJobKey()));
        }
        onStop();
    }

    /** A lifecycle hook invoked before the Job API categorizes incoming arguments into declared and undeclared groups.<br/>
     * The default implementation is a no-op and returns {@code null}.<br/>
     * Subclasses may override this method to customize argument initialization or to perform preprocessing steps.
     *
     * <p>
     * This hook can be useful for:
     * <ul>
     * <li>dynamically creating or configuring the {@code declaredArguments} instance,</li>
     * <li>retrieving argument values before they are categorized
     * <ul>
     * <li>see {@link OrderProcessStep#getPreAssignedArgumentValue(String)}),</li>
     * </ul>
     * </li>
     * <li>preparing additional state required to build a successful runtime environment.
     * <ul>
     * <li>see com.sos.js7.scriptengine.jobs.ScriptJob</li>
     * </ul>
     * </li>
     * </ul>
     */
    public A beforeCreateJobArguments(List<JobArgumentException> exceptions, final OrderProcessStep<A> step) throws Exception {
        return null;
    }

    @Override
    public OrderProcess toOrderProcess(BlockingInternalJob.Step internalStep) {
        return new InterruptibleOrderProcess() {

            volatile boolean canceled = false;
            AtomicReference<OrderProcessStep<A>> processOrderStepRef = null;

            @Override
            public JOutcome.Completed runInterruptible() throws Exception {
                while (!canceled) {
                    MockLevel mockLevel = MockLevel.OFF;
                    OrderProcessStep<A> step = new OrderProcessStep<A>(jobEnvironment, internalStep);
                    try {
                        List<JobArgumentException> exceptions = new ArrayList<JobArgumentException>();

                        A args = beforeCreateJobArguments(exceptions, step);
                        args = createDeclaredJobArguments(exceptions, step, args);

                        step.applyArguments(args);
                        processOrderStepRef = new AtomicReference<>(step);

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
                    } catch (Exception e) {
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
                    if (processOrderStepRef != null && !canceled) {
                        OrderProcessStep<A> orderProcessStep = processOrderStepRef.get();
                        boolean log = false;
                        if (orderProcessStep != null) {
                            log = true;
                            cancelProcessOrder(orderProcessStep);
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

    protected void cancelProcessOrder(OrderProcessStep<A> jobStep) {
        String jobName = getJobName(jobStep);
        try {
            onProcessOrderCanceled(jobStep);
        } catch (Exception e) {
            jobStep.getLogger().error(String.format("[%s][job name=%s][onProcessOrderCanceled]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
        jobStep.cancelExecuteJobs();
    }

    private String getJobName(OrderProcessStep<A> jobStep) {
        try {
            return jobStep == null ? "unknown" : jobStep.getJobName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Map<String, Object> mergeJobAndStepArguments(final OrderProcessStep<A> step) {
        Set<Entry<String, Value>> stepArgs = step.getInternalStep().arguments().entrySet();
        Set<Entry<String, Value>> orderArgs = step.getInternalStep().order().arguments().entrySet();

        Stream<Map.Entry<String, Value>> stream = null;
        if (SOSCollection.isEmpty(jobEnvironment.getEngineArguments())) {
            stream = Stream.concat(stepArgs.stream(), orderArgs.stream());
        } else {
            stream = Stream.concat(Stream.concat(jobEnvironment.getEngineArguments().entrySet().stream(), stepArgs.stream()), orderArgs.stream());
        }
        return JobHelper.asJavaValues(stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> value2)));
    }

    private A createDeclaredJobArgumentsOnStart(List<JobArgumentException> exceptions) throws Exception {
        return createDeclaredJobArguments(exceptions, null, null);
    }

    protected A createDeclaredJobArguments(List<JobArgumentException> exceptions, final OrderProcessStep<A> step, A instance) throws Exception {
        instance = instance == null ? getJobArgumensClass().getDeclaredConstructor().newInstance() : instance;
        if (jobEnvironment.getEngineArguments() == null && step == null) {
            return instance;
        }

        if (step == null) {// Job.onStart
            Map<String, Object> map = jobEnvironment.getAllArgumentsAsNameValueMap();
            return setDeclaredJobArguments(exceptions, instance, map, null);
        } else {
            Map<String, Object> map = mergeJobAndStepArguments(step);
            return setDeclaredJobArguments(exceptions, instance, map, step);
        }
    }

    protected A setDeclaredJobArguments(List<JobArgumentException> exceptions, A instance, Map<String, Object> args, final OrderProcessStep<A> step)
            throws Exception {
        if (instance.getIncludedArguments() != null && instance.getIncludedArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument<?>>> e : instance.getIncludedArguments().entrySet()) {
                for (JobArgument<?> arg : e.getValue()) {
                    arg.setPayload(e.getKey());
                    setDeclaredJobArgument(args, arg, null, step);
                }
            }
        }
        if (instance.hasDynamicArguments()) {
            for (JobArgument<?> arg : instance.getDynamicArguments()) {
                arg.reset();
                setDeclaredJobArgument(args, arg, null, step);
            }
        }

        List<Field> fields = JobHelper.getJobArgumentFields(instance);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument<?> arg = (JobArgument<?>) field.get(instance);
                if (arg != null) {
                    setDeclaredJobArgument(args, arg, field, step);
                    field.set(instance, arg);
                }
            } catch (JobRequiredArgumentMissingException e) {
                exceptions.add(e);
            } catch (Exception e) {
                exceptions.add(new JobArgumentException(String.format("[%s.%s][can't get or set field]%s", getClass().getName(), field.getName(), e
                        .toString()), e));
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    protected <V> void setDeclaredJobArgument(Map<String, Object> args, JobArgument<V> arg, Field field, final OrderProcessStep<A> step)
            throws Exception {
        if (arg.getName() == null) {// internal usage
            return;
        }
        if (!arg.isScopeAll()) {
            return;
        }
        if (step == null) { // Job.onStart
            if (arg.getName() != null) { // internal usage
                List<String> allNames = new ArrayList<>(Arrays.asList(arg.getName()));
                if (arg.getNameAliases() != null) {
                    allNames.addAll(arg.getNameAliases());
                }

                Object val = fromMap(jobEnvironment.getAllArgumentsAsNameValueMap(), allNames);
                if (val == null) {
                    arg.setValue(arg.getDefaultValue());
                    setDeclaredArgumentValueType(arg, field, arg.getValue());
                } else {
                    arg.setValue(getDeclaredArgumentValue(step, arg, field, val));
                    setValueSource(arg, new ValueSource(ValueSourceType.JOB_ARGUMENT));
                }
            }
        } else {
            // step calls another java job
            if (step.hasExecuteJobArguments() && step.getExecuteJobBean().getArguments().containsKey(arg.getName())) {
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
                } catch (Exception e) {
                    arg.setNotAcceptedValue(a.getValue(), e);
                }
                return;
            }

            List<String> allNames = new ArrayList<>(Arrays.asList(arg.getName()));
            if (arg.getNameAliases() != null) {
                allNames.addAll(arg.getNameAliases());
            }

            // Preference 1 (HIGHEST) - Succeeded Outcomes
            DetailValue jdv = fromMap(step.getLastSucceededOutcomes(), allNames);
            if (jdv != null) {
                arg.setValue(getDeclaredArgumentValue(step, arg, field, jdv.getValue()));
                ValueSource vs = new ValueSource(ValueSourceType.LAST_SUCCEEDED_OUTCOME);
                vs.setSource(jdv.getSource());
                setValueSource(arg, vs);
            } else {
                // Preference 2 - Order Variable or Node Argument
                Object val = step.getNamedValue(arg);
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

                // Preference 4 (LOWEST) - JobResources
                setDeclaredArgumentValueSource(step, field, arg, allNames, isNamedValue, step.getJobResourcesArgumentsAsNameDetailValueMap());

                if (arg.isRequired() && arg.getValue() == null) {
                    throw new JobRequiredArgumentMissingException(arg.getName(), arg.getName());
                }
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
                } catch (Exception e) {
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
        } catch (Exception e) {
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
                } catch (Exception e) {
                    arg.setNotAcceptedValue(entry + " of " + valAsString, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
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
                } catch (Exception e) {
                    arg.setNotAcceptedValue(entry.getValue() + " of " + valAsString, e);
                    arg.getNotAcceptedValue().setSource(arg.getValueSource());
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        } catch (Exception e) {
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
                } catch (Exception e) {
                }
            } else if (val != null) {
                try {
                    arg.setClazzType(val.getClass());
                } catch (Exception e) {
                }
            }
            if (arg.getClazzType() == null) {
                arg.setClazzType(Object.class);
            }
        }
        arg.setArgumentType();
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
            while (clazz != null && clazz != Object.class) {
                Type genericSuperclass = clazz.getGenericSuperclass();

                if (genericSuperclass instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericSuperclass;
                    Type rawType = pt.getRawType();

                    if (rawType instanceof Class && Job.class.isAssignableFrom((Class<?>) rawType)) {
                        try {
                            return (Class<A>) pt.getActualTypeArguments()[0];
                        } catch (Exception e) {
                            return (Class<A>) JobArguments.class; // Fallback
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
            throw new JobArgumentException(String.format("%s superclass with type parameter not found for %s", Job.class.getSimpleName(),
                    getClass()));
        } catch (Exception e) {
            throw new JobArgumentException(String.format("Can't evaluate JobArguments class for job %s: %s", getClass().getName(), e.toString()), e);
        }
    }

}
