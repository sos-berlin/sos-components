package com.sos.js7.converter.autosys.common.v12.job.attr.condition;

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
        String c = "v(app.varA) = \"X\"";
        LOGGER.info(String.format("[%s]%s", c, SOSString.toString(new Condition(c))));

        c = "s(app.jobA)";
        LOGGER.info(String.format("[%s]%s", c, SOSString.toString(new Condition(c))));

        c = "f(jobA^PROD,24)";
        LOGGER.info(String.format("[%s]%s", c, SOSString.toString(new Condition(c))));
    }

}
