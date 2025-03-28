package com.sos.jitl.jobs.yade;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class YADEJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEJobTest.class);

    @Ignore
    @Test
    public void testJob() throws Exception {

        Map<String, Object> args = new HashMap<>();
        args.put("settings", Path.of("xyz"));
        args.put("profile", "xyz");

        // overrides settings
        boolean overrides = false;
        if (overrides) {
            args.put("source_dir", "xyz");
            args.put("source_file_path", "xyz");
            args.put("source_file_spec", "xyz");
            args.put("source_file_list", Path.of("xyz"));
            args.put("target_dir", "xyz");
        }
        // for unit tests only
        UnitTestJobHelper<YADEJobArguments> h = new UnitTestJobHelper<>(new YADEJob());
        h.getStepConfig().setControllerId("js7");
        // creates a new thread for each new onOrderProcess call
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT] %s", result));
    }

}
