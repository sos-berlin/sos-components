package com.sos.jitl.jobs.common;

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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.ISOSRequiredArgumentMissingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSBase64;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.jitl.jobs.common.JobArgument.ValueSource;
import com.sos.jitl.jobs.common.JobArguments.MockLevel;
import com.sos.jitl.jobs.exception.SOSJobArgumentException;
import com.sos.jitl.jobs.exception.SOSJobProblemException;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.launcher.forjava.internal.BlockingInternalJob;

public abstract class ABlockingInternalJob<A extends JobArguments> implements BlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABlockingInternalJob.class);
    private static final String BASE64_VALUE_PREFIX = "base64:";
    private static final String OPERATION_CANCEL_KILL = "cancel/kill";

    private final JobContext jobContext;

    public ABlockingInternalJob() {
        this(null);
    }

    /** e.g. for jobContext.jobArguments() or getAgentSystemEncoding */
    public ABlockingInternalJob(JobContext jobContext) {
        this.jobContext = jobContext;
    }

    /** to override */
    public void onStart(A args) throws Exception {

    }

    /** to override */
    public void onStop(A args) throws Exception {

    }

    /** to override */
    public abstract JOutcome.Completed onOrderProcess(JobStep<A> step) throws Exception;

    public JobContext getJobContext() {
        return jobContext;
    }

    public Charset getAgentSystemEncoding() {
        return jobContext == null ? null : jobContext.systemEncoding();
    }

    /** engine methods */
    @Override
    public Either<Problem, Void> start() {
        try {
            List<SOSJobArgumentException> exceptions = new ArrayList<SOSJobArgumentException>();
            A args = createJobArguments(exceptions);
            // TODO
            // checkExceptions(exceptions);

            onStart(args);
            return right(null);
        } catch (Throwable e) {
            return left(Problem.fromThrowable(e));
        }
    }

    @Override
    public void stop() {
        try {
            List<SOSJobArgumentException> exceptions = new ArrayList<SOSJobArgumentException>();
            A args = createJobArguments(exceptions);
            // TODO
            // checkExceptions(exceptions);

            onStop(args);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Override
    public OrderProcess toOrderProcess(BlockingInternalJob.Step step) {
        return new OrderProcess() {

            volatile boolean canceled = false;
            AtomicReference<JobStep<A>> jobStepRef = null;

            public JOutcome.Completed run() throws Exception {
                while (!canceled) {
                    MockLevel mockLevel = MockLevel.OFF;
                    JobStep<A> jobStep = new JobStep<A>(getClass().getName(), jobContext, step);
                    jobStepRef = new AtomicReference<>(jobStep);
                    try {
                        List<SOSJobArgumentException> exceptions = new ArrayList<SOSJobArgumentException>();
                        A args = createJobArguments(exceptions, jobStep);
                        jobStep.init(args);

                        mockLevel = jobStep.getDeclaredArguments().getMockLevel().getValue();
                        switch (mockLevel) {
                        case OFF:
                            jobStep.logParameterization(null);
                            checkExceptions(jobStep, exceptions);
                            return onOrderProcess(jobStep);
                        case ERROR:
                            jobStep.logParameterization(String.format("Mock Execution: %s=%s.", jobStep.getDeclaredArguments().getMockLevel()
                                    .getName(), mockLevel));
                            checkExceptions(jobStep, exceptions);
                            return jobStep.success();
                        case INFO:
                        default:
                            jobStep.logParameterization(String.format("Mock Execution: %s=%s.", jobStep.getDeclaredArguments().getMockLevel()
                                    .getName(), mockLevel));
                            return jobStep.success();
                        }
                    } catch (Throwable e) {
                        switch (mockLevel) {
                        case OFF:
                        case ERROR:
                            return jobStep.failed(e.toString(), e);
                        case INFO:
                        default:
                            jobStep.getLogger().info(String.format("Mock Execution: %s=%s, Exception: %s", jobStep.getDeclaredArguments()
                                    .getMockLevel().getName(), mockLevel, e.toString()));
                            return jobStep.success();
                        }
                    }
                }
                return JOutcome.failed("Canceled");
            }

            @Override
            public void cancel(boolean immediately) {
                if (jobStepRef != null && !canceled) {
                    JobStep<A> jobStep = jobStepRef.get();
                    if (jobStep != null) {
                        cancelStep(jobStep);
                        Thread thread = Thread.getAllStackTraces().keySet().stream().filter(t -> t.getName().equals(jobStep.getThreadName())).map(
                                t -> {
                                    return t;
                                }).findAny().orElse(null);
                        if (thread == null) {
                            try {
                                jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "][thread][" + jobStep.getThreadName()
                                        + "][skip interrupt]thread not found");
                            } catch (Throwable e) {
                            }
                        } else {
                            jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "][thread][" + thread.getName() + "]interrupt ...");
                            thread.interrupt();
                        }
                    }
                }
                canceled = true;
            }
        };
    }

    /** can be overwritten */
    public void cancelStep(JobStep<A> jobStep) {
        if (jobStep.getPayload() != null) {
            String jobName = getJobName(jobStep);
            cancelHibernateConnection(jobStep, jobName);
            cancelSQLConnection(jobStep, jobName);
            cancelVFSConnection(jobStep, jobName);
        }
    }

    private void cancelHibernateConnection(JobStep<A> jobStep, String jobName) {
        try {
            Object o = jobStep.getPayload().get(JobStep.PAYLOAD_NAME_HIBERNATE_SESSION);
            if (o != null) {
                SOSHibernateSession s = (SOSHibernateSession) o;
                // step.getLogger().info("cancel ... close session");
                // s.rollback(); s.close() <- does not work because blocked
                if (s.getFactory() != null) {
                    jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "]close factory ...");
                    s.getFactory().close();
                }
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][job name=%s][cancelHibernateConnection]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }

    // TODO currently not used(candidate PLSQLJob) because abort is asynchronous and conn.close() is synchronous (waiting for execution is completed)
    // PLSQLJob is cancelled when the Thread is interrupted.
    private void cancelSQLConnection(JobStep<A> jobStep, String jobName) {
        try {
            Object o = jobStep.getPayload().get(JobStep.PAYLOAD_NAME_SQL_CONNECTION);
            if (o != null) {
                jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "]abort connection ...");
                // ((Connection) o).close();
                ((Connection) o).abort(Runnable::run);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][job name=%s][cancelSQLConnection]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }

    private void cancelVFSConnection(JobStep<A> jobStep, String jobName) {
        try {
            Object o = jobStep.getPayload().get(JobStep.PAYLOAD_NAME_VFS_PROVIDER);
            if (o != null) {
                jobStep.getLogger().info("[" + OPERATION_CANCEL_KILL + "]disconnect ..");
                // ((AProvider<?>) o).disconnect();
                SSHProvider p = (SSHProvider) o;
                p.cancelWithKill();
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][job name=%s][cancelVFSConnection]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }

    private String getJobName(JobStep<A> jobStep) {
        try {
            return jobStep == null ? "unknown" : jobStep.getJobName();
        } catch (Throwable e) {
            return "unknown";
        }
    }

    private void checkExceptions(JobStep<A> jobStep, List<SOSJobArgumentException> exceptions) throws Exception {
        if (exceptions.size() > 0) {
            List<String> l = exceptions.stream().filter(e -> e instanceof ISOSRequiredArgumentMissingException).map(e -> {
                return ((ISOSRequiredArgumentMissingException) e).getArgumentName();
            }).collect(Collectors.toList());
            if (l.size() > 0) {
                jobStep.logParameterizationOnRequiredArgumentMissingException();
                throw new SOSJobRequiredArgumentMissingException(String.join(", ", l));
            } else {
                throw exceptions.get(0);
            }
        }
    }

    private Map<String, Object> mergeJobAndStepArguments(final JobStep<A> step) {
        Map<String, Object> map = null;
        if (step == null) {
            map = JobHelper.convert(jobContext.jobArguments());
        } else {
            Set<Entry<String, Value>> stepArgs = step.getInternalStep().arguments().entrySet();
            Set<Entry<String, Value>> orderArgs = step.getInternalStep().order().arguments().entrySet();

            Stream<Map.Entry<String, Value>> stream = null;
            if (jobContext == null) {
                stream = Stream.concat(stepArgs.stream(), orderArgs.stream());
            } else {
                stream = Stream.concat(Stream.concat(jobContext.jobArguments().entrySet().stream(), stepArgs.stream()), orderArgs.stream());
            }
            map = JobHelper.convert(stream.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (value1, value2) -> value2)));
        }
        return map;
    }

    private A createJobArguments(List<SOSJobArgumentException> exceptions) throws Exception {
        return createJobArguments(exceptions, null);
    }

    @SuppressWarnings({ "rawtypes" })
    private A createJobArguments(List<SOSJobArgumentException> exceptions, final JobStep<A> step) throws Exception {
        A instance = getJobArgumensClass().getDeclaredConstructor().newInstance();
        if (jobContext == null && step == null) {
            return instance;
        }
        Map<String, Object> map = mergeJobAndStepArguments(step);
        Map<String, JobDetailValue> lastSucceededOutcomes = step == null ? null : step.getLastSucceededOutcomes();
        Map<String, JobDetailValue> jobResources = step == null ? null : step.getJobResourcesValues();

        if (instance.getIncludedArguments() != null && instance.getIncludedArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument>> e : instance.getIncludedArguments().entrySet()) {
                for (JobArgument arg : e.getValue()) {
                    arg.setPayload(e.getKey());
                    setJobArgument(step, map, lastSucceededOutcomes, jobResources, arg, null);
                }
            }
        }
        return setJobArguments(exceptions, step, map, lastSucceededOutcomes, jobResources, instance);
    }

    @SuppressWarnings({ "rawtypes" })
    private A setJobArguments(List<SOSJobArgumentException> exceptions, final JobStep<A> step, Map<String, Object> map,
            Map<String, JobDetailValue> lastSucceededOutcomes, Map<String, JobDetailValue> jobResources, A instance) throws Exception {

        List<Field> fields = JobHelper.getJobArgumentFields(instance);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument arg = (JobArgument<?>) field.get(instance);
                if (arg != null) {
                    setJobArgument(step, map, lastSucceededOutcomes, jobResources, arg, field);
                    field.set(instance, arg);
                }
            } catch (SOSJobRequiredArgumentMissingException e) {
                exceptions.add(e);
            } catch (Throwable e) {
                exceptions.add(new SOSJobArgumentException(String.format("[%s.%s][can't get or set field]%s", getClass().getName(), field.getName(), e
                        .toString()), e));
            }
        }
        return instance;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setJobArgument(final JobStep<A> step, Map<String, Object> map, Map<String, JobDetailValue> lastSucceededOutcomes,
            Map<String, JobDetailValue> jobResources, JobArgument arg, Field field) throws Exception {
        if (arg.getName() == null) {// internal usage
            return;
        }
        List<String> allNames = new ArrayList<>(Arrays.asList(arg.getName()));
        if (arg.getNameAliases() != null) {
            allNames.addAll(arg.getNameAliases());
        }

        // preference 1 (HIGHEST) - Succeeded Outcomes
        JobDetailValue jdv = fromMap(lastSucceededOutcomes, allNames);
        if (jdv != null) {
            arg.setValue(getValue(jdv.getValue(), arg, field));
            ValueSource vs = ValueSource.LAST_SUCCEEDED_OUTCOME;
            vs.setDetails("pos=" + jdv.getSource());
            setValueSource(arg, vs);
        } else {
            // preference 2 - Order Variable or Node Argument
            Object val = getNamedValue(step, arg);
            boolean isNamedValue = false;
            if (val == null) {
                val = fromMap(map, allNames);
            } else {
                isNamedValue = true;
            }

            // TODO setValue - currently overrides not empty value of a appArgument SOSArgument
            // - solution 1 - set SOSArgument.setDefaultValue instead of SOSArgument.setValue
            // - solution 2 - handle here ...

            // preference 3 - JobArgument or Argument or Java Default
            if (val == null || SOSString.isEmpty(val.toString())) {
                arg.setValue(arg.getDefaultValue());
            } else {
                arg.setValue(getValue(val, arg, field));
            }
            if (step == null) {
                setValueSource(arg, allNames);
            } else {
                // preference 4 (LOWEST) - JobResources
                setValueSource(step, field, arg, allNames, isNamedValue, jobResources);
            }
            if (arg.isRequired() && arg.getValue() == null) {
                throw new SOSJobRequiredArgumentMissingException(arg.getName(), arg.getName());
            }
        }
    }

    private <T> T fromMap(Map<String, T> map, List<String> list) {
        if (map == null || list == null) {
            return null;
        }
        return list.stream().filter(map::containsKey).findFirst().map(map::get).orElse(null);
    }

    private Object getNamedValue(final JobStep<A> step, final JobArgument<A> arg) throws SOSJobProblemException {
        if (step == null) {
            return null;
        }
        Object val = getNamedValue(step, arg.getName());
        if (val == null && arg.getNameAliases() != null) {
            for (String name : arg.getNameAliases()) {
                val = getNamedValue(step, name);
                if (val != null) {
                    break;
                }
            }
        }
        return val;
    }

    private Object getNamedValue(final JobStep<A> step, final String name) throws SOSJobProblemException {
        Optional<Either<Problem, Value>> opt = step.getInternalStep().namedValue(name);
        if (opt.isPresent()) {
            return JobHelper.getValue(JobHelper.getFromEither(opt.get()));
        }
        return null;
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

    private void setValueSource(JobArgument<A> arg, List<String> allNames) {
        if (arg.getName() == null) {// source Java - internal usage
            return;
        }
        if (jobContext != null && jobContext.jobArguments().containsKey(arg.getName())) {
            setValueSource(arg, JobArgument.ValueSource.JOB_ARGUMENT);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setValueSource(final JobStep<A> step, Field field, JobArgument arg, List<String> allNames, boolean isNamedValue,
            Map<String, JobDetailValue> jobResources) {
        if (arg.getName() == null) {// source Java - internal usage
            return;
        }
        ValueSource source = null;
        Value v = null;
        if (isNamedValue) {// order or node
            v = fromMap(step.getInternalStep().order().arguments(), allNames);
            source = v == null ? JobArgument.ValueSource.ORDER_OR_NODE : JobArgument.ValueSource.ORDER;
        } else {
            if (jobContext != null) {
                v = fromMap(jobContext.jobArguments(), allNames);
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
                JobDetailValue jdv = fromMap(jobResources, allNames);
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
    private Class<A> getJobArgumensClass() throws SOSJobArgumentException {
        try {
            Class<?> clazz = getClass();
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

}
