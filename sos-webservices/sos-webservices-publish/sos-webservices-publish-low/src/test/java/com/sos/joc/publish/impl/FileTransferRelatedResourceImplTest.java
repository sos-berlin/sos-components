package com.sos.joc.publish.impl;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class FileTransferRelatedResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTransferRelatedResourceImplTest.class);

    // Waiting time, as the deployment process internally starts a new thread.
    private static final long WAIT_SECONDS = 10;

    @Ignore
    @Test
    public void testDeploy() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new DeployImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("postDeploy", Paths.get("src/test/resources/ws/publish/impl/filetransfer/request-DeployImpl-draft-folder.json"));

            TimeUnit.SECONDS.sleep(WAIT_SECONDS);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
