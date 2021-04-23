package com.sos.commons.credentialstore.keepass;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSKeePassDatabaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeePassDatabaseTest.class);
    private static final String DIR = "src/test/resources";

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    @Test
    public void getMainUriKeePass1CompositeKeyTest() throws Exception {

        String uri = "cs://server/SFTP/my_server@user?file=" + DIR + "/keepassX-test.kdb";

        SOSKeePassDatabase.main(new String[] { uri });
    }

    @Ignore
    @Test
    public void getMainUriPropertyTest() throws Exception {

        String databaseName = "kdbx-p-f.kdbx";

        String uri = "cs://server/SFTP/my_server@my test?file=" + DIR + "/" + databaseName + "&password=test";
        SOSKeePassDatabase.main(new String[] { uri });
    }

    @Ignore
    @Test
    public void getMainUriAttachmentTest() throws Exception {

        String databaseName = "kdbx-p-f.kdbx";

        String uri = "cs://server/SFTP/my_server@attachment?file=" + DIR + "/" + databaseName + "&password=test";
        uri = "cs://server/SFTP/my_server@cloud.jpg?attachment=1&file=" + DIR + "/" + databaseName + "&password=test";
        SOSKeePassDatabase.main(new String[] { uri });
    }

    @Ignore
    @Test
    public void getDefaultKeyFileTest() throws Exception {

        String databaseName = "kdbx-p-f.kdbx";

        Path key = SOSKeePassDatabase.getDefaultKeyFile(Paths.get(DIR).resolve(databaseName));

        LOGGER.info(SOSKeePassDatabase.getFilePath(key));
    }

}
