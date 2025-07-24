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

    /** Use Run Configurations -> Environment to set environment variables <br/>
     * JS7_AGENT_CONFIG_DIR */
    @Ignore
    @Test
    public void test1() throws Exception {

        Map<String, Object> args = new HashMap<>();
        args.put("js7.api-server.url", "http://localhost:4447");
        args.put("js7.api-server.username", "root");
        args.put("js7.api-server.password", "root");

        args.put("request", SOSPath.readFile(Path.of("src/test/resources/jobs/rest/inventory-read-folder.json")));

        UnitTestJobHelper<JS7RESTClientJobArguments> h = new UnitTestJobHelper<>(new JS7RESTClientJob());
        h.getStepConfig().setControllerId("js7");
        // creates a new thread for each new onOrderProcess call
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT] %s", result));
    }

}
