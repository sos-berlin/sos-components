package com.sos.js7.converter.autosys.input;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;

public class DirectoryParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParserTest.class);

    @Ignore
    @Test
    public void testXMLParser() {
        Path dir = Paths.get("src/test/resources/input/xml");

        Instant start = Instant.now();
        DirectoryParserResult r = DirectoryParser.parse(new XMLJobParser(), dir);
        LOGGER.info("[parse][duration]" + SOSDate.getDuration(start, Instant.now()));

        log(r);
    }

    private void log(DirectoryParserResult r) {
        LOGGER.info(String.format("[JOBS]%s", r.getJobs().size()));
        for (ACommonJob job : r.getJobs()) {
            LOGGER.info(job.getInsertJob().getValue() + "=" + SOSString.toString(job));
        }
        LOGGER.info(String.format("[JOBS]%s", r.getJobs().size()));
    }

}
