package com.sos.yade.engine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.arguments.loaders.xml.YADEXMLArgumentsLoader;

public class YADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEEngineTest.class);

    @Ignore
    @Test
    public void test() {
        Path settings = Path.of("xyz");
        String profile = "xyz";

        Map<String, String> map = System.getenv();
        boolean settingsReplacerCaseSensitive = true;
        boolean settingsReplacerKeepUnresolved = true;
        int parallelism = 10;
        try {
            ISOSLogger logger = new SLF4JLogger();

            // Load Arguments from Settings XML
            AYADEArgumentsLoader argsLoader = new YADEXMLArgumentsLoader().load(logger, settings, profile, map, settingsReplacerCaseSensitive,
                    settingsReplacerKeepUnresolved);

            // Set YADE parallelism from the Job Argument
            argsLoader.getArgs().getParallelism().setValue(parallelism);

            // Execute YADE Transfer
            YADEEngine engine = new YADEEngine();
            List<ProviderFile> files = engine.execute(logger, argsLoader, true);
            LOGGER.info("[files]" + files);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testMain() {
        List<String> args = new ArrayList<>();
        args.add("--settings=xyz");
        args.add("--profile=xyz");
        args.add("--help");
        YADEEngineMain.main(args.toArray(new String[0]));
    }

}
