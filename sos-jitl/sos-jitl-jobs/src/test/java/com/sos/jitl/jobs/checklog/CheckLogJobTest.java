package com.sos.jitl.jobs.checklog;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.JobHelper;
import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class CheckLogJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckLogJobTest.class);

    @Ignore
    @Test
    public void testMonitoringJob() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("job", "job1");
        args.put("label", "job1a");
        args.put("pattern", ".*error.*");
  //      args.put("groupSeparator", "|");
        args.put("multiline", true);
        args.put("unix_lines", false);
        args.put("case_insensitive", false);

        UnitTestJobHelper<CheckLogJobArguments> h = new UnitTestJobHelper<>(new CheckLogJob(null));

        // h.setEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "...");
        LOGGER.info(System.getenv(JobHelper.ENV_NAME_AGENT_CONFIG_DIR));

        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
