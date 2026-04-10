package com.sos.js7.job.resolver.reference;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.helper.TestJobArguments;

public class ArgumentReferenceResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentReferenceResolverTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        try {
            TestJobArguments args = new TestJobArguments();

            args.getAuthMethods().setValue(Collections.singletonList(SSHAuthMethod.PASSWORD));
            args.getTest().setValue("{a:true, b: %{" + args.getPath().getName() + "},  c: %{" + args.getPath().getName() + "}}");
            LOGGER.info("[before]" + args.getTest().getName() + "=" + args.getTest().getValue());

            Map<String, JobArgument<?>> map = toMap(args);
            ArgumentReferenceResolver.resolve(new SLF4JLogger(), map);
            for (Map.Entry<String, JobArgument<?>> entry : map.entrySet()) {
                LOGGER.info("    [after]" + entry.getValue().toString(true));
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testFailure() throws Exception {
        try {
            TestJobArguments args = new TestJobArguments();

            args.getAuthMethods().setValue(Collections.singletonList(SSHAuthMethod.PASSWORD));
            args.getTest().setValue("{a:true, b: xxx %{" + args.getLogLevel().getName() + "} %{xxx}}");
            LOGGER.info("[before]" + args.getTest().getName() + "=" + args.getTest().getValue());

            Map<String, JobArgument<?>> map = toMap(args);
            ArgumentReferenceResolver.resolve(new SLF4JLogger(), map);
            for (Map.Entry<String, JobArgument<?>> entry : map.entrySet()) {
                LOGGER.info("    [after]" + entry.getValue().toString(true));
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private Map<String, JobArgument<?>> toMap(JobArguments args) {
        Map<String, JobArgument<?>> map = new HashMap<>();

        for (Field field : JobHelper.getJobArgumentFields(args)) {
            try {
                field.setAccessible(true);
                JobArgument<?> arg = (JobArgument<?>) field.get(args);
                if (arg != null) {
                    if (arg.getName() == null) {// internal usage
                        continue;
                    }
                    if (!map.containsKey(arg.getName())) { // if extends -> same argument name
                        arg.setArgumentType();
                        map.put(arg.getName(), arg);
                    }
                }
            } catch (Exception e) {

            }
        }
        return map;

    }

}
