package com.sos.commons.util;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSTimeout;

public class SOSShellTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShellTest.class);

    @Ignore
    @Test
    public void test() {
        String command = "type C:\\test.txt";

        LOGGER.info("Start...");
        SOSCommandResult result = SOSShell.executeCommand(command);

        LOGGER.info(String.format("[command]%s", result.getCommand()));
        LOGGER.info(String.format("[stdOut]%s", result.getStdOut()));
        LOGGER.info(String.format("[stdErr]%s", result.getStdErr()));
        LOGGER.info(String.format("[exitCode]%s", result.getExitCode()));
        LOGGER.info(String.format("[exception]%s", result.getException()));
    }

    @Ignore
    @Test
    public void testCharset() {
        String command = "dir D:";

        LOGGER.info("Start...");
        SOSCommandResult result = SOSShell.executeCommand(command);

        LOGGER.info(String.format("[command]%s", result.getCommand()));
        LOGGER.info(String.format("[stdOut]%s", result.getStdOut()));
        LOGGER.info(String.format("[stdErr]%s", result.getStdErr()));
        LOGGER.info(String.format("[exitCode]%s", result.getExitCode()));
        LOGGER.info(String.format("[exception]%s", result.getException()));
        LOGGER.info(String.format("[charset]%s", result.getCharset()));
    }

    @Ignore
    @Test
    public void testTimeout() {
        String command = "ping www.google.de -n 5";

        LOGGER.info("Start...");
        SOSCommandResult result = SOSShell.executeCommand(command, new SOSTimeout(1, TimeUnit.SECONDS));

        LOGGER.info(String.format("[command]%s", result.getCommand()));
        LOGGER.info(String.format("[stdOut]%s", result.getStdOut()));
        LOGGER.info(String.format("[stdErr]%s", result.getStdErr()));
        LOGGER.info(String.format("[exitCode]%s", result.getExitCode()));
        LOGGER.info(String.format("[exception]%s", result.getException()));
        LOGGER.info(String.format("[timeoutExeeded]%s", result.isTimeoutExeeded()));
    }
}
