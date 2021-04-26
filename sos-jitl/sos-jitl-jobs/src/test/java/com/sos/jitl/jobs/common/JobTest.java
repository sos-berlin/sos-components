package com.sos.jitl.jobs.common;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jitl.jobs.common.helper.TestJob;
import com.sos.jitl.jobs.common.helper.TestJobArguments;
import com.sos.jitl.jobs.common.helper.TestJobSuperClass;
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
    public void testJobArguments() {
        SQLExecutorJobArguments o1 = new SQLExecutorJobArguments();
        JocApiJobArguments o2 = new JocApiJobArguments();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(o1.getHibernateFile().getName(), "C://myfile");
        map.put(o2.getJocUri().getName(), "http://localhost");

        setArguments(map, o1);
        LOGGER.info("hibernate=" + o1.getHibernateFile().getValue().toFile().toString());

        setArguments(map, o2);
        LOGGER.info("jocUri=" + o2.getJocUri().getValue().getScheme());
    }

    private static Class<?> getJobArgumensClass(Object instance) throws SOSJobArgumentException {
        Class<?> clazz = instance.getClass();
        while (clazz.getSuperclass() != ABlockingInternalJob.class) {
            clazz = clazz.getSuperclass();
            if (clazz == null)
                throw new SOSJobArgumentException(String.format("super class not found for %s", instance.getClass()));
        }
        return (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void setArguments(Map<String, Object> map, Object o) {
        List<Field> fields = Job.getJobArgumentFields(o);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                LOGGER.debug("  field type=" + field.getGenericType());
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
                            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            LOGGER.debug("    field parameter type=" + type);
                            val = val.toString().trim();
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
    }
}
