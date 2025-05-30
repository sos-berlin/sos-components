package com.sos.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath.SOSPathResult;

public class SOSPathTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPathTest.class);

    @Ignore
    @Test
    public void testCleanupDirectory() throws IOException {
        Path cleanupDir = Paths.get("./src/test/resources/cleanup");
        try {
            if (Files.exists(cleanupDir)) {
                LOGGER.info("[cleanup]start");
                SOSPathResult r = SOSPath.cleanupDirectory(cleanupDir);
                LOGGER.info("[cleanup][end]" + r);
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void test() throws Exception {
        Path source = Paths.get("./src/test/resources/log4j2.xml");
        LOGGER.info("[" + source + "]size=" + SOSPath.getFileSize(source));
        LOGGER.info("[" + source.getParent() + "]size=" + SOSPath.getFileSize(source.getParent()));
        LOGGER.info("basename=" + SOSPath.getBasename(source));
        LOGGER.info("withoutExtension=" + SOSPath.getFileNameWithoutExtension(source));
    }

}
