package com.sos.jitl.jobs.examples;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class InfoJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoJobTest.class);

    private static final String BASE64_PATH_VAL = "dGVzdC50eHQ="; //test.txt
    
    @Ignore
    @Test
    public void testJob() throws Exception {

        Map<String, Object> args = new HashMap<>();
        args.put("map_path_values", getMapPaths());
        args.put("map_string_values", getMapStrings());
        args.put("path_argument", "base64:"+BASE64_PATH_VAL);

        // for unit tests only
        UnitTestJobHelper<InfoJobArguments> h = new UnitTestJobHelper<>(new InfoJob(null));
        // creates a new thread for each new onOrderProcess call
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    private Map<String, String> getMapStrings() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("p1", "x");
        m.put("p2", "base64:"+BASE64_PATH_VAL);
        return m;
    }

    private Map<String, Object> getMapPaths() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("p1", Paths.get("x"));
        m.put("p1", "upper:xyz");
        return m;
    }
}
