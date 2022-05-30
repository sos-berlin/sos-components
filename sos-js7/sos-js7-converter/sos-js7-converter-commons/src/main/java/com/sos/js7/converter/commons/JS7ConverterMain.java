package com.sos.js7.converter.commons;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JS7ConverterMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ConverterMain.class);

    private JS7ConverterConfig doConfig(JS7ConverterConfig config, Path configFile) throws Exception {
        if (configFile == null) {
            config.getGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        } else {
            config.parse(configFile);
        }
        return config;
    }

    public abstract void doConvert(Path input, Path outputDir, Path reportDir) throws Exception;

    public void doMain(JS7ConverterConfig config, String[] args) {
        if (args.length < 4) {
            System.out.println("usage:");
            System.out.println("    <input dir>         - required - directory with XML files");
            System.out.println("    <output dir>        - required - output directory with the JS7 files");
            System.out.println("    <report dir>        - required - report directory");
            System.out.println("    <properties file>   - required - converter configuration properties file");
            return;
        }
        int status = 0;
        try {
            Path input = Paths.get(args[0]);
            if (!Files.exists(input)) {
                throw new Exception("[" + input + "]input not found");
            }
            Path outputDir = Paths.get(args[1]);
            Path reportDir = Paths.get(args[2]);
            Path configFile = Paths.get(args[3].trim());

            System.out.println("------------------------");
            System.out.println("Input   = " + input.toAbsolutePath());
            System.out.println("Output  = " + outputDir.toAbsolutePath());
            System.out.println("Report  = " + reportDir.toAbsolutePath());
            System.out.println("Config  = " + configFile.toAbsolutePath());
            System.out.println("          " + doConfig(config, configFile));
            System.out.println("------------------------");

            doConvert(input, outputDir, reportDir);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            status = 1;
        }
        System.exit(status);
    }

}
