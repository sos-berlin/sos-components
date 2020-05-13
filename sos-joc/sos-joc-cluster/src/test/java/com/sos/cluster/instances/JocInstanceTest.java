package com.sos.cluster.instances;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;

public class JocInstanceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInstanceTest.class);

    @Ignore
    @Test
    public void testOS() throws Exception {
        LOGGER.info(String.valueOf(SOSShell.IS_WINDOWS));
        LOGGER.info(String.valueOf(SOSShell.OS_NAME));
        LOGGER.info(String.valueOf(SOSShell.OS_VERSION));
        LOGGER.info(String.valueOf(SOSShell.OS_ARCHITECTURE));
        LOGGER.info(String.valueOf(SOSShell.getHostname()));
    }

}
