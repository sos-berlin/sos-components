package com.sos.joc.classes.common;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilenameSanitizerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilenameSanitizerTest.class);
    
    @Test
    public void testFilenameChanged () {
        String value = "1/2\\3";
        LOGGER.info("before check: " + value);
        FilenameSanitizer.check(value);
        LOGGER.info("after check: " + value);
    }
}
