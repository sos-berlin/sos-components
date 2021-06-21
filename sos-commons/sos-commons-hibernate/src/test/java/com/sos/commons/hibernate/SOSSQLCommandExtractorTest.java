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
        SOSHibernateFactory.Dbms dbms = SOSHibernateFactory.Dbms.MSSQL;

        SOSSQLCommandExtractor ex = new SOSSQLCommandExtractor(dbms);
        Path file = Paths.get("src/test/resources/" + dbms.name().toLowerCase() + ".txt");

        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);

        List<String> commands = ex.extractCommands(content);
        for (String command : commands) {
            LOGGER.info(command);
            LOGGER.info("---------------------------");
        }

    }
}
