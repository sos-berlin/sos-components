package com.sos.jitl.jobs.common;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.helper.TestJob;
import com.sos.jitl.jobs.common.helper.TestJobArguments;
import com.sos.jitl.jobs.common.helper.TestJobArgumentsSuperClass;
import com.sos.jitl.jobs.common.helper.TestJobSuperClass;
import com.sos.jitl.jobs.common.helper.TestJobWithoutJobArgumentsClass;
import com.sos.jitl.jobs.db.SQLExecutorJobArguments;
import com.sos.jitl.jobs.examples.JocApiJobArguments;
import com.sos.jitl.jobs.exception.SOSJobArgumentException;

public class JobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobTest.class);

    @Ignore
    @Test
    public void testABlockingJobWithoutArgumentClazzConstructor() throws Exception {
        TestJobSuperClass job = new TestJobSuperClass();

        LOGGER.info("job class=" + job.getClass());
        LOGGER.info("job class(generic super class)=" + job.getClass().getGenericSuperclass());
        LOGGER.info("job super class=" + job.getClass().getSuperclass());
        LOGGER.info("job super class(generic super class)=" + job.getClass().getSuperclass().getGenericSuperclass());

        TestJobArguments args = (TestJobArguments) getJobArgumensClass(job).newInstance();
        LOGGER.info("name(superClass)=" + args.getTestSuperClass().getName());
        LOGGER.info("name=" + args.getTest().getName());
    }

    @Ignore
    @Test
    public void testExtendedABlockingJobWithoutArgumentClazzConstructor() throws Exception {
        TestJob job = new TestJob();

        LOGGER.info("job class=" + job.getClass());
        LOGGER.info("job class(generic super class)=" + job.getClass().getGenericSuperclass());
        LOGGER.info("job super class=" + job.getClass().getSuperclass());
        LOGGER.info("job super class(generic super class)=" + job.getClass().getSuperclass().getGenericSuperclass());

        TestJobArguments args = (TestJobArguments) getJobArgumensClass(job).newInstance();
        LOGGER.info("name(superClass)=" + args.getTestSuperClass().getName());
        LOGGER.info("name=" + args.getTest().getName());
    }

    @Ignore
    @Test
    public void testEJobWithoutJobArgumentsClass() throws Exception {
        TestJobWithoutJobArgumentsClass job = new TestJobWithoutJobArgumentsClass();

        LOGGER.info("job class=" + job.getClass());
        LOGGER.info("job class(generic super class)=" + job.getClass().getGenericSuperclass());
        LOGGER.info("job super class=" + job.getClass().getSuperclass());
        LOGGER.info("job super class(generic super class)=" + job.getClass().getSuperclass().getGenericSuperclass());

        Object args = (Object) getJobArgumensClass(job).newInstance();
        LOGGER.info("args=" + args.getClass());
    }

    @Ignore
    @Test
    public void testJobArguments() throws Exception {
        SQLExecutorJobArguments o1 = new SQLExecutorJobArguments();
        JocApiJobArguments o2 = new JocApiJobArguments();
        TestJobArgumentsSuperClass o3 = new TestJobArgumentsSuperClass();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(o1.getHibernateFile().getName(), "C://myfile");
        map.put(o2.getJocUri().getName(), "http://localhost");
        map.put(o3.getLogLevel().getName(), "xxx");
        map.put(o3.getList().getName(), "xxx ; yyyy ;");
        map.put(o3.getLinkedList().getName(), "lxxx ; lyyyy ;");

        LOGGER.info(o1.getClass().getSimpleName() + "---");
        setArguments(map, o1);
        LOGGER.info("hibernate=" + o1.getHibernateFile().getValue().toFile().toString());

        LOGGER.info(o2.getClass().getSimpleName() + "---");
        setArguments(map, o2);
        LOGGER.info("jocUri=" + o2.getJocUri().getValue().getScheme());
        LOGGER.info(o3.getClass().getSimpleName() + "---");
        setArguments(map, o3);
        LOGGER.info("logLevel=" + o3.getLogLevel().getValue().name());
        LOGGER.info("list=" + o3.getList().getDisplayValue());
        LOGGER.info("linkedList=" + o3.getLinkedList().getDisplayValue());
    }

    private static Class<?> getJobArgumensClass(Object instance) throws SOSJobArgumentException {
        Class<?> clazz = instance.getClass();
        while (clazz.getSuperclass() != ABlockingInternalJob.class) {
            clazz = clazz.getSuperclass();
            if (clazz == null)
                throw new SOSJobArgumentException(String.format("super class not found for %s", instance.getClass()));
        }
        Type gsc = clazz.getGenericSuperclass();
        try {
            return (Class<?>) ((ParameterizedType) gsc).getActualTypeArguments()[0];
        } catch (Throwable e) {
            if (gsc.getTypeName().endsWith(">")) {
                throw e;
            }
            return Object.class;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void setArguments(Map<String, Object> map, Object o) {
        List<Field> fields = Job.getJobArgumentFields(o);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                LOGGER.debug("  [" + field.getName() + "]field type=" + field.getGenericType());
                JobArgument arg = (JobArgument<?>) field.get(o);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    Object val = map.get(arg.getName());
                    if (val == null || SOSString.isEmpty(val.toString())) {
                        arg.setValue(arg.getDefault());
                    } else {
                        arg.setValue(getValue(field, arg, val));
                    }
                    field.set(o, arg);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", this.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
    }

    private Object getValue(Field field, JobArgument<?> arg, Object val) throws ClassNotFoundException {
        if (val instanceof String) {
            val = val.toString().trim();
            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            if (!type.equals(String.class)) {
                LOGGER.debug("       [" + field.getName() + "]type=" + type.getTypeName());
                if (type.equals(Path.class)) {
                    val = Paths.get(val.toString());
                } else if (type.equals(URI.class)) {
                    val = URI.create(val.toString());
                } else if (SOSReflection.isList(type.getTypeName())) {
                    val = Stream.of(val.toString().split(Job.LIST_VALUE_DELIMITER)).map(String::trim).collect(Collectors.toList());
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
        } else if (val instanceof BigDecimal) {
            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            if (type.equals(Integer.class)) {
                val = Integer.valueOf(((BigDecimal) val).intValue());
            } else if (type.equals(Long.class)) {
                val = Long.valueOf(((BigDecimal) val).longValue());
            }
        }
        return val;
    }

}
