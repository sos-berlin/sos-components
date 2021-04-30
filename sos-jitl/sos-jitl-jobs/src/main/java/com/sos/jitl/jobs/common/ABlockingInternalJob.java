package com.sos.jitl.jobs.common;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.JobArgument.ValueSource;
import com.sos.jitl.jobs.exception.SOSJobArgumentException;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public abstract class ABlockingInternalJob<A> implements BlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABlockingInternalJob.class);

    private final JobContext jobContext;

    public ABlockingInternalJob() {
        this(null);
    }

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
    public JOutcome.Completed onOrderProcess(JobStep<A> step) throws Exception {
        return JOutcome.succeeded(Collections.emptyMap());
    }

    public JobContext getJobContext() {
        return jobContext;
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
        return () -> {
            JobStep<A> jobStep = new JobStep<A>(getClass().getName(), step);
            try {
                List<SOSJobArgumentException> exceptions = new ArrayList<SOSJobArgumentException>();
                A args = createJobArguments(exceptions, jobStep);
                jobStep.init(args);
                jobStep.logParameterization();
                checkExceptions(exceptions);

                return onOrderProcess(jobStep);
            } catch (Throwable e) {
                return jobStep.failed(e.toString(), e);
            }
        };
    }

    private void checkExceptions(List<SOSJobArgumentException> exceptions) throws Exception {
        if (exceptions.size() > 0) {
            List<String> l = exceptions.stream().filter(e -> e instanceof SOSJobRequiredArgumentMissingException).map(e -> {
                return ((SOSJobRequiredArgumentMissingException) e).getArgumentName();
            }).collect(Collectors.toList());
            if (l.size() > 0) {
                throw new SOSJobRequiredArgumentMissingException(String.join(", ", l));
            } else {
                throw exceptions.get(0);
            }
        }
    }

    private Object getNamedValue(final JobStep<A> step, final String name) {
        if (step == null) {
            return null;
        }
        Optional<Value> op = step.getInternalStep().namedValue(name);
        if (op.isPresent()) {
            return Job.getValue(op.get());
        }
        return null;
    }

    private A createJobArguments(List<SOSJobArgumentException> exceptions) throws Exception {
        return createJobArguments(exceptions, null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private A createJobArguments(List<SOSJobArgumentException> exceptions, final JobStep<A> step) throws Exception {
        A a = getJobArgumensClass().newInstance();

        Map<String, Object> map = null;
        if (step == null) {
            if (jobContext == null) {
                return a;
            }
            map = Job.convert(jobContext.jobArguments());
        } else {
            Stream<Map<String, Value>> stream = null;
            if (jobContext == null) {
                stream = Stream.of(step.getInternalStep().arguments(), step.getInternalStep().order().arguments());
            } else {
                stream = Stream.of(jobContext.jobArguments(), step.getInternalStep().arguments(), step.getInternalStep().order().arguments());
            }
            map = Job.convert(stream.flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        Map<String, Object> lastSuccededOutcomes = step == null ? null : step.getLastSucceededOutcomes();
        List<Field> fields = Job.getJobArgumentFields(a);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument arg = (JobArgument<?>) field.get(a);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    if (lastSuccededOutcomes != null && lastSuccededOutcomes.containsKey(arg.getName())) {
                        arg.setValue(getValue(field, arg, lastSuccededOutcomes.get(arg.getName())));
                        setValueSource(arg, ValueSource.LAST_SUCCEEDED_OUTCOME);
                    } else {
                        Object val = getNamedValue(step, arg.getName());
                        boolean isNamedValue = false;
                        if (val == null) {
                            val = map.get(arg.getName());
                        } else {
                            isNamedValue = true;
                        }
                        if (val == null || SOSString.isEmpty(val.toString())) {
                            arg.setValue(arg.getDefault());
                        } else {
                            arg.setValue(getValue(field, arg, val));
                        }
                        if (step != null && arg.isRequired() && arg.getValue() == null) {
                            throw new SOSJobRequiredArgumentMissingException(arg.getName(), arg.getName());
                        }
                        setValueSource(step, arg, isNamedValue);
                    }

                    field.set(a, arg);
                    setReference(fields, a, arg);
                }
            } catch (SOSJobRequiredArgumentMissingException e) {
                exceptions.add(e);
            } catch (Throwable e) {
                exceptions.add(new SOSJobArgumentException(String.format("[%s.%s][can't get or set field]%s", getClass().getName(), field.getName(), e
                        .toString()), e));
            }
        }
        return a;
    }

    private Object getValue(Field field, JobArgument<A> arg, Object val) throws ClassNotFoundException {
        if (val instanceof String) {
            val = val.toString().trim();
            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            if (!type.equals(String.class)) {
                if (type.equals(Path.class)) {
                    val = Paths.get(val.toString());
                } else if (type.equals(URI.class)) {
                    val = URI.create(val.toString());
                } else if (SOSReflection.isEnum(type.getTypeName())) {
                    Object v = SOSReflection.enumIgnoreCaseValueOf(type.getTypeName(), val.toString());
                    if (v == null) {
                        arg.setNotAcceptedValue(val);
                        val = arg.getDefault();
                    } else {
                        val = v;
                    }

                }
            }
        }
        return val;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void setReference(List<Field> fields, A jobArguments, JobArgument<A> arg) throws IllegalArgumentException, IllegalAccessException {
        if (arg.getReference() != null && arg.isDirty()) {
            Field ref = fields.stream().filter(f -> f.getName().equals(arg.getReference().getName())).findAny().orElse(null);
            if (ref != null) {
                ref.setAccessible(true);
                JobArgument ja = (JobArgument<?>) ref.get(jobArguments);
                if (ja != null) {
                    if (!ja.isDirty()) {
                        ja.setValue(arg.getValue());
                        ja.setIsDirty(true);
                    }
                }
            }
        }
    }

    private void setValueSource(final JobStep<A> step, JobArgument<A> arg, boolean isNamedValue) {
        if (arg.getValue() == null || arg.getName() == null) {// source Java or internal usage
            return;
        }
        ValueSource source = null;
        if (isNamedValue) {// order or node
            if (step != null) {
                source = step.getInternalStep().order().arguments().containsKey(arg.getName()) ? JobArgument.ValueSource.ORDER
                        : JobArgument.ValueSource.ORDER_OR_NODE;
            }
        } else {
            if (jobContext != null && jobContext.jobArguments().containsKey(arg.getName())) {
                source = JobArgument.ValueSource.JOB_ARGUMENT;
            }
            if (step != null && step.getInternalStep().arguments().containsKey(arg.getName())) {
                source = JobArgument.ValueSource.JOB;
            }
        }
        if (source != null) {
            setValueSource(arg, source);
        }
    }

    private void setValueSource(JobArgument<A> arg, ValueSource source) {
        if (arg.getNotAcceptedValue() != null) {
            arg.getNotAcceptedValue().setSource(source);
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
                return (Class<A>) Object.class;
            }
        } catch (Throwable e) {
            throw new SOSJobArgumentException(String.format("can't evaluate JobArguments class for job %s: %s", getClass().getName(), e.toString()),
                    e);
        }
    }

}
