package com.sos.joc.syslog;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SyslogTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SyslogTest.class);

    @BeforeClass
    public static void setUpBeforeClass() {
        try {
            UDPServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() {
        UDPServer.shutdown();
    }

    @Test
    public void test() {
        LOGGER.info("This is a first test message");
        LOGGER.info("This is a second test message");
        try {
            throw new Exception("This is an exception");
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

}
