package com.sos.yade.engine.addons;

import java.nio.file.Path;
import java.time.Instant;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.xml.YADEXMLArgumentsLoader;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;

public class YADEEngineJumpHostAddonTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEEngineJumpHostAddonTest.class);

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
            LOGGER.info(YADEArgumentsHelper.toString(logger, YADEArguments.LABEL, argsLoader.getArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, YADEClientArguments.LABEL, argsLoader.getClientArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, YADESourceArguments.LABEL, argsLoader.getSourceArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, YADEJumpHostArguments.LABEL, argsLoader.getJumpHostArgs()));
            LOGGER.info(YADEArgumentsHelper.toString(logger, YADETargetArguments.LABEL, argsLoader.getTargetArgs()));

            YADEEngineJumpHostAddon jumpHostAddon = YADEEngineJumpHostAddon.initialize(logger, argsLoader);
            LOGGER.info(SOSString.toString(jumpHostAddon));

        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
