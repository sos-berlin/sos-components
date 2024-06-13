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

public class CustomResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomResolverTest.class);

    /** To load the custom resolvers the environment variable JS7_AGENT_LIBDIR should be set */
    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("test", "");
        args.put("path", "");
        args.put("log_all_arguments", Boolean.valueOf(true));

        UnitTestJobHelper<TestJobArguments> h = new UnitTestJobHelper<>(new TestJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
