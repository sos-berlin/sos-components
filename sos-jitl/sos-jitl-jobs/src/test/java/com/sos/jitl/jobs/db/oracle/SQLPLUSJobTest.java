package com.sos.jitl.jobs.db.oracle;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class SQLPLUSJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLPLUSJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        // args.put("db_url", "ora12c");
        args.put("db_url", "//localhost:1521/XE");
        args.put("db_user", "scheduler");
        args.put("db_password", "scheduler");

        args.put("shell_command", "sqlplus");
        // args.put("command_script_file", "c:/temp/1.sql");
        args.put("command", "select 1 from dual;\n exit;");

        // args.put("credential_store_file", "D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/profiles/sos.kdbx");
        // args.put("credential_store_key_file", "D:/documents/sos-berlin.com/scheduler_joc_cockpit/config/profiles/sos.key");
        // args.put("credential_store_password", "...secret...");
        // args.put("credential_store_entry_path", "/sos/my_path");

        UnitTestJobHelper<SQLPlusJobArguments> h = new UnitTestJobHelper<>(new SQLPLUSJob(null));
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
