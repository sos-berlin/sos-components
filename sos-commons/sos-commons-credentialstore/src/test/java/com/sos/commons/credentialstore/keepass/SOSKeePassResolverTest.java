package com.sos.commons.credentialstore.keepass;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSKeePassResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeePassResolverTest.class);
    private static final String DIR = "src/test/resources";
    private static final String DATABASE_NAME = "kdbx-p-f.kdbx";

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    @Test
    public void resolveFullPathsSeveralEntriesTest() throws Exception {

        SOSKeePassResolver r = new SOSKeePassResolver();

        String val = r.resolve("cs://server/SFTP/my_server@my test?file=" + DIR + "/" + DATABASE_NAME + "&key_file=" + DIR
                + "/kdbx-p-f.key&password=test");
        LOGGER.info(val);

        val = r.resolve("cs://server/SFTP/my_server_2@url?file=" + DIR + "/" + DATABASE_NAME + "&password=test");
        LOGGER.info(val);
    }

    @Ignore
    @Test
    public void resolveFullPathsSameEntryTest() throws Exception {

        SOSKeePassResolver r = new SOSKeePassResolver();

        String val = r.resolve("cs://server/SFTP/my_server@my test?file=" + DIR + "/" + DATABASE_NAME + "&password=test");
        LOGGER.info(val);

        val = r.resolve("cs://server/SFTP/my_server@url?file=" + DIR + "/" + DATABASE_NAME + "&password=test");
        LOGGER.info(val);
    }

    @Ignore
    @Test
    public void resolveSeveralEntriesTest() throws Exception {

        SOSKeePassResolver r = new SOSKeePassResolver(Paths.get(DIR).resolve(DATABASE_NAME), "test");

        String val = r.resolve("cs://server/SFTP/my_server@my test?file=" + DIR + "/" + DATABASE_NAME + "&password=test");
        LOGGER.info(val);

        val = r.resolve("cs://server/SFTP/my_server_2@url?file=" + DIR + "/" + DATABASE_NAME + "&password=test");
        LOGGER.info(val);
    }

    @Ignore
    @Test
    public void resolveSameEntryTest() throws Exception {

        SOSKeePassResolver r = new SOSKeePassResolver(Paths.get(DIR).resolve(DATABASE_NAME), "test");

        String val = r.resolve("cs://server/SFTP/my_server@my test");
        LOGGER.info(val);

        val = r.resolve("cs://server/SFTP/my_server@url");
        LOGGER.info(val);
    }

    @Ignore
    @Test
    public void resolveSeveralEntries2Test() throws Exception {

        SOSKeePassResolver r = new SOSKeePassResolver(Paths.get(DIR).resolve(DATABASE_NAME), "test");

        String val = r.resolve("cs://server/SFTP/my_server@title");
        LOGGER.info(val);

        val = r.resolve("cs://server/SFTP/my_server_2@url");
        LOGGER.info(val);
    }

    @Ignore
    @Test
    public void resolveSameEntry2Test() throws Exception {

        SOSKeePassResolver r = new SOSKeePassResolver(Paths.get(DIR).resolve(DATABASE_NAME), "test");

        String val = r.resolve("cs://server/SFTP/my_server@title");
        LOGGER.info(val);

        val = r.resolve("cs://server/SFTP/my_server@url");
        LOGGER.info(val);
    }

    @Ignore
    @Test
    public void resolveSeveralEntries3Test() throws Exception {

        SOSKeePassResolver r = new SOSKeePassResolver(Paths.get(DIR).resolve(DATABASE_NAME), "test");
        r.setEntryPath("/server/SFTP/my_server");

        String val = r.resolve("cs://@title");
        LOGGER.info(val);

        val = r.resolve("cs://server/SFTP/my_server_2@url");
        LOGGER.info(val);
    }

    @Ignore
    @Test
    public void resolveSameEntry3Test() throws Exception {

        SOSKeePassResolver r = new SOSKeePassResolver(Paths.get(DIR).resolve(DATABASE_NAME), "test");
        r.setEntryPath("/server/SFTP/my_server");

        String val = r.resolve("cs://@title");
        LOGGER.info(val);

        val = r.resolve("cs://@url");
        LOGGER.info(val);
    }
}
