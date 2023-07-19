package com.sos.scriptengine.jobs;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.job.JobArguments;
import com.sos.commons.job.UnitTestJobHelper;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSReflection;

import js7.data_for_java.order.JOutcome;

public class JavaScriptJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("my_arg1", "xyz");
        args.put("my_arg2", "xyz");
        String script = SOSPath.readFile(Paths.get("src/test/resources/jobs/javascript/JS7Job.js"));

        UnitTestJobHelper<JobArguments> h = new UnitTestJobHelper<>(new JavaScriptJob(null));
        SOSReflection.setDeclaredFieldValue(h.getJobs(), "script", script);
        h.onStart(args);

        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
        h.onStop();
    }

}
