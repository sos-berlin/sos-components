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

import com.sos.commons.util.SOSBase64;
import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.SOSSerializer;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
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
    private static final String BASE64_VALUE_PREFIX = "base64:";

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
        map.put(o3.getList().getName(), "xxx , yyyy ,");
        map.put(o3.getLinkedList().getName(), "lxxx ; lyyyy ;");
        map.put(o3.getAuthMethods().getName(), "password;publickey");
        // map.put(o3.getTest().getName(), BASE64_VALUE_PREFIX + "aGVsbG8gd2VsdA==");
        map.put(o3.getTest().getName(), BASE64_VALUE_PREFIX + "rO0ABXQACmhlbGxvIHdlbHQ=");

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
        LOGGER.info("authMethods=" + o3.getAuthMethods().getDisplayValue());
        LOGGER.info("test=" + o3.getTest().getDisplayValue());

        LOGGER.info("[1]" + new SOSSerializer<String>().serialize("Hello World"));
        LOGGER.info("[2]" + SOSBase64.encode("Hello World"));
    }

    @Ignore
    @Test
    public void testApp2JobArguments() throws Exception {
        SSHProviderArguments sftpArgs = new SSHProviderArguments();
        JobArguments a = new JobArguments(sftpArgs);

        if (a.getAppArguments() != null && a.getAppArguments().size() > 0) {
            a.getAppArguments().entrySet().stream().forEach(e -> {
                for (JobArgument<?> arg : e.getValue()) {
                    // LOGGER.info(arg.getName());
                    if (arg.getName().equals("protocol")) {
                        LOGGER.info(SOSString.toString(arg));
                    }
                }
            });
        }

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
    private static void setArguments(Map<String, Object> map, JobArguments o) {
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
                        arg.setValue(arg.getDefaultValue());
                    } else {
                        arg.setValue(getValue(field, arg, val));
                    }
                    field.set(o, arg);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", o.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
    }

    private static Object getValue(Field field, JobArgument<?> arg, Object val) throws ClassNotFoundException {
        if (val instanceof String) {
            val = val.toString().trim();
            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            if (type.equals(String.class)) {
                if (((String) val).startsWith(BASE64_VALUE_PREFIX)) {
                    String s = val.toString().substring(BASE64_VALUE_PREFIX.length());
                    try {
                        val = SOSBase64.decode(s);
                    } catch (Throwable e) {
                        LOGGER.info("[BASE64]" + s);
                        LOGGER.error(e.toString(), e);
                        arg.setNotAcceptedValue(val, e);
                    }
                }
            } else {
                LOGGER.debug("       [" + field.getName() + "]type=" + type.getTypeName());
                if (type.equals(Path.class)) {
                    val = Paths.get(val.toString());
                } else if (type.equals(URI.class)) {
                    val = URI.create(val.toString());
                } else if (SOSReflection.isList(type)) {
                    LOGGER.info(arg.getName() + " = " + type);
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
