package com.sos.jitl.jobs.db.oracle;

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
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("hibernate_configuration_file", Paths.get("src/test/resources/hibernate.cfg.xml"));
        args.put("command", "select 1 from dual");
        args.put("resultset_as", ResultSetAs.XML);
        args.put("result_file", Paths.get("src/test/resources/plsqljob_export.xml"));

        // args.put("credential_store_file", "db.kdbx");
        // args.put("credential_store_key_file", "db.kdbx.key");
        // args.put("credential_store_password", "...secret...");
        // args.put("credential_store_entry_path", "/sos/my_path");

        UnitTestJobHelper<PLSQLJobArguments> h = new UnitTestJobHelper<>(new PLSQLJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
