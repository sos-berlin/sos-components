package com.sos.js7.converter.js1.common.jobstreams.condition;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;

public class ConditionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        convert("fileexist:test.txt");
        convert("fileexistX:test.txt");
        convert("rc:0-255");
        convert("jobChain:my_jobchain.isStartedToday");
    }

    private static void convert(String val) {
        LOGGER.info(val);
        LOGGER.info("    " + SOSString.toString(new Condition(val)));

    }

}
