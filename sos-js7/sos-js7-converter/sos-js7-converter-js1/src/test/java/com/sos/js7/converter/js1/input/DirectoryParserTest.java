package com.sos.js7.converter.js1.input;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.js7.converter.commons.JS7ConverterConfig;
import com.sos.js7.converter.js1.common.Folder;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

public class DirectoryParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryParserTest.class);

    @Ignore
    @Test
    public void testParser() {
        Path input = Paths.get("src/test/resources/input");
        Path outputDir = Paths.get("src/test/resources/output");

        Instant start = Instant.now();
        DirectoryParserResult r = DirectoryParser.parse(new JS7ConverterConfig().getParserConfig(), input, outputDir);
        LOGGER.info("[parse][duration]" + SOSDate.getDuration(start, Instant.now()));

        walk(r.getRoot());
    }

    private void walk(Folder f) {
        LOGGER.info(f.toString());
        for (Folder ff : f.getFolders()) {
            walk(ff);
        }
    }

}
