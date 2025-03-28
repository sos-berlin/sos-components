package com.sos.yade.engine.commons.arguments.parsers.xml;

import java.nio.file.Path;
import java.time.Instant;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;

public class YADEXMLParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEXMLParserTest.class);

    @Ignore
    @Test
    public void testParseFile() {

        try {
            String settingsFile = "xyz";
            String profile = "xyz";

            Instant start = Instant.now();
            YADEXMLParser parser = new YADEXMLParser();
            parser.parse(Path.of(settingsFile), profile);
            LOGGER.info("[duration]" + SOSDate.getDuration(start, Instant.now()));

            ISOSLogger logger = new SLF4JLogger();
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[YADE]", parser.getArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[CLIENT]", parser.getClientArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[SOURCE]", parser.getSourceArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[TARGET]", parser.getTargetArgs()));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
