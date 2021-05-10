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
import java.util.Arrays;
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
import com.sos.jitl.jobs.exception.SOSJobProblemException;
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
    public abstract JOutcome.Completed onOrderProcess(JobStep<A> step) throws Exception;

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
            JobStep<A> jobStep = new JobStep<A>(getClass().getName(), jobContext, step);
            try {
                List<SOSJobArgumentException> exceptions = new ArrayList<SOSJobArgumentException>();
                A args = createJobArguments(exceptions, jobStep);
                jobStep.init(args);
                jobStep.logParameterization();
                checkExceptions(jobStep, exceptions);

                return onOrderProcess(jobStep);
            } catch (Throwable e) {
                return jobStep.failed(e.toString(), e);
            }
        };
    }

    private void checkExceptions(JobStep<A> jobStep, List<SOSJobArgumentException> exceptions) throws Exception {
        if (exceptions.size() > 0) {
            List<String> l = exceptions.stream().filter(e -> e instanceof SOSJobRequiredArgumentMissingException).map(e -> {
                return ((SOSJobRequiredArgumentMissingException) e).getArgumentName();
            }).collect(Collectors.toList());
            if (l.size() > 0) {
                jobStep.logParameterizationOnRequiredArgumentMissingException();
                throw new SOSJobRequiredArgumentMissingException(String.join(", ", l));
            } else {
                throw exceptions.get(0);
            }
        }
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
        Map<String, JobDetailValue> lastSucceededOutcomes = step == null ? null : step.getLastSucceededOutcomes();
        Map<String, JobDetailValue> jobResources = step == null ? null : step.getJobResourcesValues();
        List<Field> fields = Job.getJobArgumentFields(a);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument arg = (JobArgument<?>) field.get(a);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    List<String> allNames = new ArrayList<>(Arrays.asList(arg.getName()));
                    if (arg.getNameAliases() != null) {
                        allNames.addAll(arg.getNameAliases());
                    }
                    // preference 1 (HIGHEST) - Succeeded Outcomes
                    JobDetailValue jdv = fromMap(lastSucceededOutcomes, allNames);
                    if (jdv != null) {
                        arg.setValue(getValue(field, arg, jdv.getValue()));
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
                        // preference 3 - JobArgument or Argument or Java Default
                        if (val == null || SOSString.isEmpty(val.toString())) {
                            arg.setValue(arg.getDefault());
                        } else {
                            arg.setValue(getValue(field, arg, val));
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
                    field.set(a, arg);
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
        Optional<Value> op = Job.getFromEither(step.getInternalStep().namedValue(name));
        if (op.isPresent()) {
            return Job.getValue(op.get());
        }
        return null;
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

    private void setValueSource(JobArgument<A> arg, List<String> allNames) {
        if (arg.getName() == null) {// source Java - internal usage
            return;
        }
        if (jobContext != null && jobContext.jobArguments().containsKey(arg.getName())) {
            setValueSource(arg, JobArgument.ValueSource.JOB_ARGUMENT);
        }
    }

    @SuppressWarnings("unchecked")
    private void setValueSource(final JobStep<A> step, Field field, JobArgument<A> arg, List<String> allNames, boolean isNamedValue,
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
                        arg.setValue((A) getValue(field, arg, jdv.getValue()));
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
