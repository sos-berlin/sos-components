package com.sos.commons.hibernate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSSQLCommandExtractorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSQLCommandExtractorTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Path file = Paths.get("...");// pass file path to process file content

        SOSSQLCommandExtractor ex = new SOSSQLCommandExtractor(SOSHibernateFactory.Dbms.H2);
        List<String> commands = ex.extractCommands(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));

        for (String command : commands) {
            LOGGER.info("---------------------------");
            LOGGER.info(command);
        }

    }
}
