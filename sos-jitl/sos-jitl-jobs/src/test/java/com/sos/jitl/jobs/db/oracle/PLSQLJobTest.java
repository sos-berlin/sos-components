package com.sos.jitl.jobs.db.oracle;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class PLSQLJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PLSQLJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        PLSQLJobArguments args = new PLSQLJobArguments();
        args.setCommand("select 1 from dual");
        // args.setCommand("select * from SOS_TEST_SLEEP_VIEW");
        args.setHibernateFile(Paths.get("src/test/resources/hibernate.cfg.xml"));

        // TODO cs support
        SOSCredentialStoreArguments csArgs = new SOSCredentialStoreArguments();
        csArgs.setEntryPath(null);

        PLSQLJob job = new PLSQLJob(null);
        UnitTestJobHelper<PLSQLJobArguments> h = new UnitTestJobHelper<>(job);
        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
