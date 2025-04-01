package com.sos.yade.engine.commons.arguments.loaders.xml;

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

public class YADEXMLJumpSettingsWriterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEXMLJumpSettingsWriterTest.class);

    @Ignore
    @Test
    public void test() {

        try {
            String settingsFile = "xyz";
            String profile = "xyz";

            Instant start = Instant.now();
            YADEXMLArgumentsLoader argsLoader = new YADEXMLArgumentsLoader();
            argsLoader.load(new SLF4JLogger(), Path.of(settingsFile), profile, System.getenv(), true, true);
            LOGGER.info("[duration]" + SOSDate.getDuration(start, Instant.now()));

            ISOSLogger logger = new SLF4JLogger();
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[YADE]", argsLoader.getArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[CLIENT]", argsLoader.getClientArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[SOURCE]", argsLoader.getSourceArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, "[TARGET]", argsLoader.getTargetArgs()));

            LOGGER.info(YADEXMLJumpSettingsWriter.fromJumpToInternet(argsLoader, "jump_dir", "my_profile", false));

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
