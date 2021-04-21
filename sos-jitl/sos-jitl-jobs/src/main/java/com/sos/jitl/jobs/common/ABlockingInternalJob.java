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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public abstract class ABlockingInternalJob<A> implements BlockingInternalJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ABlockingInternalJob.class);

    private final JobContext jobContext;
    private final Class<A> argumentClazz;

    public ABlockingInternalJob() {
        this(null, null);
    }

    public ABlockingInternalJob(JobContext jobContext, Class<A> argumentClazz) {
        this.jobContext = jobContext;
        this.argumentClazz = argumentClazz;
    }

    /** to override */
    public void onStart(A args) throws Exception {

    }

    /** to override */
    public void onStop(A args) throws Exception {

    }

    /** to override */
    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, A args) throws Exception {
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
    public OrderProcess toOrderProcess(BlockingInternalJob.Step step) throws Exception {
        return () -> {
            try {
                return onOrderProcess(step, createJobArguments(step));
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                return JOutcome.failed(e.toString());
            }
        };
    }

    private A createJobArguments() {
        return createJobArguments(null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private A createJobArguments(final BlockingInternalJob.Step step) {
        A o;
        try {
            o = argumentClazz.newInstance();
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            return null;
        }

        Map<String, Object> map = null;
        if (step == null) {
            map = Job.convert(jobContext.jobArguments());
        } else {
            map = Job.convert(Stream.of(jobContext.jobArguments(), step.order().arguments(), step.arguments()).flatMap(m -> m.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        List<Field> fields = Job.getJobArgumentFields(o);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                JobArgument arg = (JobArgument<?>) field.get(o);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    Object val = map.get(arg.getName());
                    if (val == null || SOSString.isEmpty(val.toString())) {
                        arg.setValue(arg.getDefault());
                    } else {
                        if (val instanceof String) {
                            val = val.toString().trim();
                            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            if (type.equals(Path.class)) {
                                val = Paths.get(val.toString());
                            } else if (type.equals(URI.class)) {
                                val = URI.create(val.toString());
                            }
                        }
                        arg.setValue(val);
                    }
                    field.set(o, arg);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", this.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
        return o;
    }

}
