package com.sos.jitl.jobs.rest;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class JS7RESTClientJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7RESTClientJobTest.class);

    @Ignore
    @Test
    public void test1() throws Exception {
        // The system property 'JS7_AGENT_CONFIG_DIR' is set by default by UnitTestJobHelper to 'src/test/resources'.
        // Redefine here if a different path is required for this test:
        // System.setProperty(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "custom/path");

        Map<String, Object> args = new HashMap<>();
        args.put("js7.api-server.url", "http://localhost:4447");
        args.put("js7.api-server.username", "root");
        args.put("js7.api-server.password", "root");

        args.put("request", SOSPath.readFile(Path.of("src/test/resources/jobs/rest/inventory-read-folder.json")));

        UnitTestJobHelper<RestJobArguments> h = new UnitTestJobHelper<>(new JS7RESTClientJob());
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT] %s", result));
    }

}
