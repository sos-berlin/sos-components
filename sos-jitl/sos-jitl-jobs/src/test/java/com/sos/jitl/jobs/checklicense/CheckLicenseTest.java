package com.sos.jitl.jobs.checklicense;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class CheckLicenseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckLicenseTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("validityDays", 2);
        
        UnitTestJobHelper<CheckLicenseJobArguments> h = new UnitTestJobHelper<>(new CheckLicenseJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
