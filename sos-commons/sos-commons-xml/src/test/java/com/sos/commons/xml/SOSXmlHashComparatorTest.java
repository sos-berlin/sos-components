package com.sos.commons.xml;

import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;

public class SOSXmlHashComparatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSXmlHashComparatorTest.class);

    @Ignore
    @Test
    public void testYade() throws Exception {
        String xml1 = SOSPath.readFile(Path.of("D:/xml_current.xml"));
        String xml2 = SOSPath.readFile(Path.of("D:/xml_release_existing.xml"));

        boolean eguals = SOSXmlHashComparator.equals(xml1, xml2);
        LOGGER.info("eguals=" + eguals);
    }
}
