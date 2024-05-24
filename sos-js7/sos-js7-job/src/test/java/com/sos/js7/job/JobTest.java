package com.sos.js7.job;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.js7.job.helper.TestJob;
import com.sos.js7.job.helper.TestJobArguments;
import com.sos.js7.job.resolver.DefaultBase64ValueResolver;
import com.sos.js7.job.resolver.DefaultEncryptionResolver;

import js7.data_for_java.order.JOutcome;

public class JobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobTest.class);

    @Ignore
    @Test
    public void testBase64Resolver() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("test", DefaultBase64ValueResolver.getPrefix() + "xxx");
        args.put("path", DefaultBase64ValueResolver.getPrefix() + "yyy");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testEncryptionResolver() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_CERTIFICATE, "xxx");
        args.put(DefaultEncryptionResolver.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, "yyy");

        args.put("test", DefaultEncryptionResolver.getPrefix() + "zzz");

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
