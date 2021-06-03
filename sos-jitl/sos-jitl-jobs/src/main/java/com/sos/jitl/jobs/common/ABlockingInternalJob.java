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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.ISOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.jitl.jobs.common.JobArgument.ValueSource;
import com.sos.jitl.jobs.exception.SOSJobArgumentException;
import com.sos.jitl.jobs.exception.SOSJobProblemException;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public abstract class ABlockingInternalJob<A extends JobArguments> implements BlockingInternalJob {

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
        return map;
    }

    private A createJobArguments(List<SOSJobArgumentException> exceptions) throws Exception {
        return createJobArguments(exceptions, null);
    }

    @SuppressWarnings({ "rawtypes" })
    private A createJobArguments(List<SOSJobArgumentException> exceptions, final JobStep<A> step) throws Exception {
        A instance = getJobArgumensClass().newInstance();
        if (jobContext == null && step == null) {
            return instance;
        }
        Map<String, Object> map = mergeJobAndStepArguments(step);
        Map<String, JobDetailValue> lastSucceededOutcomes = step == null ? null : step.getLastSucceededOutcomes();
        Map<String, JobDetailValue> jobResources = step == null ? null : step.getJobResourcesValues();

        if (instance.getAppArguments() != null && instance.getAppArguments().size() > 0) {
            for (Map.Entry<String, List<JobArgument>> e : instance.getAppArguments().entrySet()) {
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

        List<Field> fields = Job.getJobArgumentFields(instance);
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
        Optional<Value> op = Job.getFromEither(step.getInternalStep().namedValue(name));
        if (op.isPresent()) {
            return Job.getValue(op.get());
        }
        return null;
    }

    private Object getValue(Object val, JobArgument<A> arg, Field field) throws ClassNotFoundException {
        if (val instanceof String) {
            val = val.toString().trim();
            setValueType(arg, field);
            Type type = arg.getClazzType();
            if (!type.equals(String.class)) {
                if (type.equals(Path.class)) {
                    val = Paths.get(val.toString());
                } else if (type.equals(URI.class)) {
                    val = URI.create(val.toString());
                } else if (SOSReflection.isList(type)) {
                    boolean asStringList = true;
                    try {
                        Type subType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        if (subType.equals(String.class)) {
                        } else if (SOSReflection.isEnum(subType)) {
                            val = Stream.of(val.toString().split(SOSArgumentHelper.LIST_VALUE_DELIMITER)).map(v -> {
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
                        val = Stream.of(val.toString().split(SOSArgumentHelper.LIST_VALUE_DELIMITER)).map(String::trim).collect(Collectors.toList());
                    }
                } else if (SOSReflection.isEnum(type)) {
                    Object v = SOSReflection.enumIgnoreCaseValueOf(type.getTypeName(), val.toString());
                    if (v == null) {
                        arg.setNotAcceptedValue(val);
                        val = arg.getDefaultValue();
                    } else {
                        val = v;
                    }
                } else if (type.equals(Charset.class)) {
                    try {
                        val = Charset.forName(val.toString());
                    } catch (Throwable e) {
                        arg.setNotAcceptedValue(val);
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
