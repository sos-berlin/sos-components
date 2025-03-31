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

public class YADEXMLArgumentsSetterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEXMLArgumentsSetterTest.class);

    @Ignore
    @Test
    public void testParseFile() {

        try {
            String settingsFile = "xyz";
            String profile = "xyz";

            Instant start = Instant.now();
            YADEXMLArgumentsSetter argsSetter = new YADEXMLArgumentsSetter();
            argsSetter.set(new SLF4JLogger(), Path.of(settingsFile), profile);
            LOGGER.info("[duration]" + SOSDate.getDuration(start, Instant.now()));

            ISOSLogger logger = new SLF4JLogger();
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[YADE]", argsSetter.getArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[CLIENT]", argsSetter.getClientArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[SOURCE]", argsSetter.getSourceArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[TARGET]", argsSetter.getTargetArgs()));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
