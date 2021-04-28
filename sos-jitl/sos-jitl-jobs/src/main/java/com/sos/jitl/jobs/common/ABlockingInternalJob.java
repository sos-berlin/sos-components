package com.sos.jitl.jobs.common;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.sos.jitl.jobs.exception.SOSJobArgumentException;

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
            onStart(createJobArguments());
            return right(null);
        } catch (Throwable e) {
            return left(Problem.fromThrowable(e));
        }
    }

    @Override
    public void stop() {
        try {
            onStop(createJobArguments());
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Override
    public OrderProcess toOrderProcess(BlockingInternalJob.Step step) {
        return () -> {
            try {
                A args = createJobArguments(step);
                return onOrderProcess(new JobStep<A>(step, new JobLogger(step, args), args));
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                return JobStep.failed(e.toString(), e);
            }
        };
    }

    private Object getNamedValue(final BlockingInternalJob.Step step, final String name) {
        if (step == null) {
            return null;
        }
        Optional<Value> op = step.namedValue(name);
        if (op.isPresent()) {
            return Job.getValue(op.get());
        }
        return null;
    }

    private A createJobArguments() throws Exception {
        return createJobArguments(null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private A createJobArguments(final BlockingInternalJob.Step step) throws Exception {
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
                stream = Stream.of(step.arguments(), step.order().arguments());
            } else {
                stream = Stream.of(jobContext.jobArguments(), step.arguments(), step.order().arguments());
            }
            map = Job.convert(stream.flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        List<Field> fields = Job.getJobArgumentFields(a);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument arg = (JobArgument<?>) field.get(a);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    Object val = getNamedValue(step, arg.getName());
                    if (val == null) {
                        val = map.get(arg.getName());
                    }
                    if (val == null || SOSString.isEmpty(val.toString())) {
                        arg.setValue(arg.getDefault());
                    } else {
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
                                    val = v == null ? arg.getDefault() : v;
                                }
                            }
                        }
                        arg.setValue(val);
                    }
                    field.set(a, arg);
                }
            } catch (Throwable e) {
                throw new SOSJobArgumentException(String.format("[%s.%s][can't get or set field]%s", getClass().getName(), field.getName(), e
                        .toString()), e);
            }
        }
        return a;
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
