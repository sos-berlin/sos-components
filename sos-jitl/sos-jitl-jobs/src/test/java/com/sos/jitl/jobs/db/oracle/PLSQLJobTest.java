package com.sos.jitl.jobs.db.oracle;

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

public class PLSQLJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PLSQLJobTest.class);

    @Ignore
    @Test
    public void testEncryption() throws Exception {
        Path resourcesDir = Paths.get("src/test/resources/encryption");

        Map<String, Object> args = new HashMap<>();
        args.put("hibernate_configuration_file", resourcesDir.resolve("hibernate.cfg.xml"));
        args.put("command", "select 1 from dual");
        args.put("resultset_as", ResultSetAs.XML);
        args.put("result_file", resourcesDir.resolve("plsqljob_export.xml"));

        UnitTestJobHelper<PLSQLJobArguments> h = new UnitTestJobHelper<>(new PLSQLJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testPlain() throws Exception {
        Path resourcesDir = Paths.get("src/test/resources");

        Map<String, Object> args = new HashMap<>();
        args.put("db_url", "jdbc:oracle:thin:@localhost:1521:XE");
        args.put("db_user", "scheduler");
        args.put("db_password", "scheduler");

        args.put("command", "select 1 from dual");
        args.put("resultset_as", ResultSetAs.XML);
        args.put("result_file", resourcesDir.resolve("plsqljob_export.xml"));

        UnitTestJobHelper<PLSQLJobArguments> h = new UnitTestJobHelper<>(new PLSQLJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testCredentialStoreDbParams() throws Exception {
        Path resourcesDir = Paths.get("src/test/resources/cs");

        Map<String, Object> args = new HashMap<>();
        args.put("db_url", "cs://server/Oracle/localhost@url");
        args.put("db_user", "scheduler");
        args.put("db_password", "scheduler");

        args.put("credential_store_file", resourcesDir.resolve("kdbx-p-f.kdbx"));
        args.put("credential_store_key_file", resourcesDir.resolve("kdbx-p-f.key"));
        args.put("credential_store_password", "test");

        args.put("command", "select 1 from dual");
        args.put("resultset_as", ResultSetAs.XML);
        args.put("result_file", resourcesDir.resolve("plsqljob_export.xml"));

        UnitTestJobHelper<PLSQLJobArguments> h = new UnitTestJobHelper<>(new PLSQLJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testCredentialStoreHibernate() throws Exception {
        Path resourcesDir = Paths.get("src/test/resources/cs");

        Map<String, Object> args = new HashMap<>();
        args.put("hibernate_configuration_file", resourcesDir.resolve("hibernate.cfg.oracle.cs.xml"));

        args.put("credential_store_file", resourcesDir.resolve("kdbx-p-f.kdbx"));
        args.put("credential_store_key_file", resourcesDir.resolve("kdbx-p-f.key"));
        args.put("credential_store_password", "test");

        args.put("command", "select 1 from dual");
        args.put("resultset_as", ResultSetAs.XML);
        args.put("result_file", resourcesDir.resolve("plsqljob_export.xml"));

        UnitTestJobHelper<PLSQLJobArguments> h = new UnitTestJobHelper<>(new PLSQLJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }
}
