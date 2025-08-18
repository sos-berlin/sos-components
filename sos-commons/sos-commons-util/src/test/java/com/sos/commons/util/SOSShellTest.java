package com.sos.commons.util;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSTimeout;

public class SOSShellTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShellTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        LOGGER.info("[ SOSShell.getLocalHostName]" + SOSShell.getLocalHostName());

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
    public void testSystemEncoding() {
        String command = "dir D:";
        command = "echo ÄÜ-ЩЪ-日本語";
        Charset charset = Charset.forName("cp850");

        LOGGER.info("1)Start...");
        SOSCommandResult result = SOSShell.executeCommand(command, charset);

        LOGGER.info(String.format("[command]%s", result.getCommand()));
        LOGGER.info(String.format("[stdOut]%s", result.getStdOut()));
        LOGGER.info(String.format("[stdErr]%s", result.getStdErr()));
        LOGGER.info(String.format("[exitCode]%s", result.getExitCode()));
        LOGGER.info(String.format("[exception]%s", result.getException()));
        LOGGER.info(String.format("[encoding]%s", result.getEncoding()));

        LOGGER.info("---");
        LOGGER.info(String.format("[SOSShell.getConsoleEncoding()]%s", SOSShell.getConsoleEncoding()));
        LOGGER.info(String.format("[Charset.defaultCharset()]%s", Charset.defaultCharset()));
        LOGGER.info(String.format("[System.getProperty(\"file.encoding\")]%s", System.getProperty("file.encoding")));

        // Cp1252
        LOGGER.info("[System-encoding]System.getProperty(\"sun.jnu.encoding\")=" + System.getProperty("sun.jnu.encoding"));
        // US-ASCII
        LOGGER.info("Charset.forName(\"default\")=" + Charset.forName("default"));

        LOGGER.info("2) Start...");
        LOGGER.info("[Console-charset]" + SOSShell.executeCommand("chcp", charset) + "");
        LOGGER.info("[Console-charset]" + SOSShell.executeCommand("dir D:\\_Workspace\\Bilder", charset) + "");

    }

    @Ignore
    @Test
    public void testEncoding() {
        String command = "echo ÄÜ-ЩЪ-日本語";

        LOGGER.info("Start...");
        SOSCommandResult result = SOSShell.executeCommand(command, Charset.forName("CP850"));

        LOGGER.info(String.format("[command]%s", result.getCommand()));
        LOGGER.info(String.format("[stdOut]%s", result.getStdOut()));
        LOGGER.info(String.format("[stdErr]%s", result.getStdErr()));
        LOGGER.info(String.format("[exitCode]%s", result.getExitCode()));
        LOGGER.info(String.format("[exception]%s", result.getException()));
        LOGGER.info(String.format("[encoding]%s", result.getEncoding()));
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

    @Ignore
    @Test
    public void testJava() {
        LOGGER.info(SOSShell.getJavaHome());
    }
}
