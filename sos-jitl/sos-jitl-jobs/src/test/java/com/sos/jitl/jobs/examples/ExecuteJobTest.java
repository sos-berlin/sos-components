package com.sos.jitl.jobs.examples;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.JobArguments;
import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class ExecuteJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        Map<String, Object> args = new HashMap<>();
        args.put("host", "localhost");
        args.put("user", "sos");
        args.put("password", "sos");
        args.put("auth_method", "password");
        args.put("command", "echo 123");
        args.put("exit_codes_to_ignore", "0;x2");

        // for unit tests only
        UnitTestJobHelper<JobArguments> h = new UnitTestJobHelper<>(new ExecuteJob());
        // creates a new thread for each new onOrderProcess call
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
