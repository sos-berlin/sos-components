package com.sos.jitl.jobs.checkhistory;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class CheckHistoryJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("query", "isStarted(startedFrom=-1d,startedTo=0d) or isCompleted or isCompleted");
        args.put("query", "isCompletedSuccessful(startedFrom=2023-01-22T01:00:00+02:00) ");
        args.put("query", "isCompletedSuccessful(startedFrom=2022-05-05T01:00:00.000Z)");
        args.put("query", "isCompletedSuccessful(startedFrom=2023-01-22T01:00:00+02:00) ");
        args.put("query", "isStarted(startedFrom=-1d,startedTo=0d) oR isCompleted Or isCompleted");
        // args.put("query","isCompleted(startedFrom=-100d, count>5)");
        // args.put("query","lastCompletedSuccessful");

        // args.put("job","job2");
        args.put("workflow", "exercise1");

        UnitTestJobHelper<CheckHistoryJobArguments> h = new UnitTestJobHelper<>(new CheckHistoryJob(null));
        // h.setEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "/");
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }
}
