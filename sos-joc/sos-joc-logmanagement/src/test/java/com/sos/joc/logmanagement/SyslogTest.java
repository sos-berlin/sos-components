package com.sos.joc.logmanagement;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.log4j2.NotificationAppender;

@Ignore
public class SyslogTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SyslogTest.class);
    private static UDPServer udpServer;

    @BeforeClass
    public static void setUpBeforeClass() {
        try {
            udpServer = new UDPServer();
            udpServer.forceStart();
            System.setProperty("jocId", "joc#0");
            System.setProperty("log4j2.contextSelector", "classOf[org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
            System.setProperty("log4j2.asyncLoggerWaitStrategy", "Block");
            System.setProperty("js7.log4j.immediateFlush", "false");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws InterruptedException {
        udpServer.stop();
        TimeUnit.SECONDS.sleep(60);
    }

    @Test
    public void test() throws InterruptedException {
        LOGGER.info("This is a first test message: こんにちは");
        TimeUnit.SECONDS.sleep(3);
        NotificationAppender.doNotify = true;
        LOGGER.info("This is a second test message");
        try {
            throw new Exception("This is an exception");
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        LOGGER.warn("This is a third test message");
        LOGGER.warn("This is a fourth test message");
        LOGGER.info("This is a fifth test message");
        TimeUnit.SECONDS.sleep(2);
        LOGGER.warn("This is a sixth test message");
        LOGGER.warn("This is a seventh test message");
        LOGGER.info("This is a eighth test message");
        TimeUnit.SECONDS.sleep(1);
        LOGGER.warn("This is a ninth test message");
        LOGGER.info("This is a tenth test message");
    }

}
