package com.sos.js7.job.resolver;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;
import com.sos.js7.job.helper.TestJob;
import com.sos.js7.job.helper.TestJobArguments;

import js7.data_for_java.order.JOutcome;

public class StandardBase64ResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardBase64ResolverTest.class);

    private static final String BASE64_PATH_VAL = "dGVzdC50eHQ="; // test.txt

    @Ignore
    @Test
    public void testBase64Resolver() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("test", StandardBase64Resolver.getPrefix() + BASE64_PATH_VAL);
        args.put("path", StandardBase64Resolver.getPrefix() + BASE64_PATH_VAL);
        args.put("log_all_arguments", Boolean.valueOf(true));

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
