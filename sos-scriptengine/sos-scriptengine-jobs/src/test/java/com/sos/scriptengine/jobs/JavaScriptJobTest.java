package com.sos.scriptengine.jobs;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.job.JobArguments;
import com.sos.commons.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class JavaScriptJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("script", "src/test/resources/jobs/javascript/JS7Job.js");

        UnitTestJobHelper<JobArguments> h = new UnitTestJobHelper<>(new JavaScriptJob(null));
        h.onStart(args);
        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
        h.onStop();
    }

}
