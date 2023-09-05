package com.sos.jitl.jobs.ssh;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class SSHJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHJobTest.class);

    @Ignore
    @Test
    public void testSSHJob() throws Exception {

        Map<String, Object> args = new HashMap<>();
        args.put("host", "localhost");
        args.put("user", "sos");
        args.put("password", "sos");
        args.put("auth_method", "password");
        args.put("command", "echo 123");
        args.put("exit_codes_to_ignore", "0;1");

        // for unit tests only
        UnitTestJobHelper<SSHJobArguments> h = new UnitTestJobHelper<>(new SSHJob());
        // creates a new thread for each new onOrderProcess call
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
