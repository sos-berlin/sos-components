package com.sos.jitl.jobs.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobs.db.oracle.PLSQLJobArguments.ResultSetAs;
import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class SQLExecutorJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLExecutorJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Path resourcesDir = Paths.get("src/test/resources");

        Map<String, Object> args = new HashMap<>();
        args.put("hibernate_configuration_file", resourcesDir.resolve("hibernate.cfg.xml"));
        args.put("command", "select 1");
        args.put("resultset_as", ResultSetAs.XML);
        args.put("result_file", resourcesDir.resolve("SQLExecutorJob_export.xml"));

        UnitTestJobHelper<SQLExecutorJobArguments> h = new UnitTestJobHelper<>(new SQLExecutorJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
