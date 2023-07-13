package com.sos.commons.job;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.job.exception.JobArgumentException;
import com.sos.commons.job.helper.TestJob;
import com.sos.commons.job.helper.TestJobArguments;
import com.sos.commons.job.helper.TestJobSuperClass;
import com.sos.commons.job.helper.TestJobWithoutJobArgumentsClass;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;

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
    public void testApp2JobArguments() throws Exception {
        SSHProviderArguments sftpArgs = new SSHProviderArguments();
        JobArguments a = new JobArguments(sftpArgs);

        if (a.getIncludedArguments() != null && a.getIncludedArguments().size() > 0) {
            a.getIncludedArguments().entrySet().stream().forEach(e -> {
                for (JobArgument<?> arg : e.getValue()) {
                    // LOGGER.info(arg.getName());
                    if (arg.getName().equals("protocol")) {
                        LOGGER.info(SOSString.toString(arg));
                    }
                }
            });
        }

    }

    private static Class<?> getJobArgumensClass(Object instance) throws Exception {
        Class<?> clazz = instance.getClass();
        while (clazz.getSuperclass() != ABlockingInternalJob.class) {
            clazz = clazz.getSuperclass();
            if (clazz == null)
                throw new JobArgumentException(String.format("super class not found for %s", instance.getClass()));
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

    @Ignore
    @Test
    public void testTypes() {
        Map<String, Object> m = new HashMap<>();
        m.put("my_boolean", true);

        Object o = m.get("my_boolean");
        if (o instanceof Boolean) {
            LOGGER.info("my_boolean= is Boolean");
        }

        LOGGER.info("my_boolean=" + o.getClass());
    }
}
