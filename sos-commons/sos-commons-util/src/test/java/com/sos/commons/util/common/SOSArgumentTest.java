package com.sos.commons.util.common;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.helper.TestArguments;

public class SOSArgumentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSArgumentTest.class);

    @Ignore
    @Test
    public void test() {
        TestArguments ta = new TestArguments();
        ta.getMyPathArray().setValue(new Path[] { Paths.get("xxx") });

        LOGGER.info("[value][string][masked]" + ta.getMyPassword().getDisplayValue());
        LOGGER.info("[value][array]" + ta.getMyPathArray().getDisplayValue());

        LOGGER.info("[isEmpty][array]" + ta.getMyPathArray().isEmpty());
        LOGGER.info("[isEmpty][map]" + ta.getMyMap().isEmpty());
        LOGGER.info("[isEmpty][list]" + ta.getMyList().isEmpty());
    }

}
