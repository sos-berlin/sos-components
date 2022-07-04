package com.sos.js7.converter.commons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.SOSGzip.SOSGzipResult;
import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.commons.output.ZipCompress;

public abstract class JS7ConverterMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ConverterMain.class);

    public abstract String getProductAndVersion();

    public abstract void doConvert(Path input, Path outputDir, Path reportDir) throws Exception;

    private void printUsage() {
        System.out.println("Usage:");
        System.out.println("  Options:");
        System.out.println("    --input=<location of input file or directory>                   | required argument");
        System.out.println("    --output-dir=<location of output directory>                     | default: ./output");
        System.out.println("    --report-dir=<location of report directory>                     | default: ./report");
        System.out.println("    --archive=<location of resulting .zip archive for JS7 import>   | default: ./js7_converted.tar.gz | .zip");
        System.out.println("    --config=<location of config file>                              | default: ./js7_convert.config");
        System.out.println("    --help                                                          | displays usage");
    }

    public void doMain(JS7ConverterConfig config, String[] args) {
        String argInput = null;
        String argOutputDir = null;
        String argReportDir = null;
        String argArhive = null;
        String argConfig = null;
        String argHelp = null;

        if (args == null) {
            argHelp = "help";
        } else {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i].trim();
                if (arg.startsWith("--input=")) {
                    argInput = getOptionValue(arg);
                } else if (arg.startsWith("--output-dir=")) {
                    argOutputDir = getOptionValue(arg);
                } else if (arg.startsWith("--report-dir=")) {
                    argReportDir = getOptionValue(arg);
                } else if (arg.startsWith("--archive=")) {
                    argArhive = getOptionValue(arg);
                } else if (arg.startsWith("--config=")) {
                    argConfig = getOptionValue(arg);
                } else if (arg.startsWith("--help")) {
                    argHelp = "help";
                }
            }
        }
        int status = 0;

        LOGGER.info("------------------------ JS7 Converter " + getProductAndVersion());
        if (argHelp != null) {
            printUsage();
        } else if (argInput == null) {
            printUsage();
            status = 1;
        } else {
            try {
                Path input = SOSPath.toAbsolutePath(argInput);
                if (!Files.exists(input)) {
                    throw new Exception("[" + input + "]input file or directory not found");
                }
                Path outputDir = SOSPath.toAbsolutePath(getValue(argOutputDir, "output"));
                Path reportDir = SOSPath.toAbsolutePath(getValue(argReportDir, "report"));
                Path archive = SOSPath.toAbsolutePath(getValue(argArhive, "js7_converted.tar.gz"));
                Path configFile = SOSPath.toAbsolutePath(getValue(argConfig, "js7_convert.config"));

                LOGGER.info(SOSDate.getCurrentDateTimeAsString());
                LOGGER.info("--input       = " + input);
                LOGGER.info("--output-dir  = " + outputDir);
                LOGGER.info("--report-dir  = " + reportDir);
                LOGGER.info("--archive     = " + archive);
                LOGGER.info("--config      = " + configFile);
                LOGGER.info("                " + doConfig(config, configFile));

                doConvert(input, outputDir, reportDir);
                createArchiveFile(outputDir, archive);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                status = 1;
            }
        }
        LOGGER.info("------------------------");
        System.exit(status);
    }

    public static void createArchiveFile(Path outputDir, Path archive) {
        if (Files.exists(archive)) {
            try {
                Files.delete(archive);
                LOGGER.info("[" + archive + "]old archive file deleted");
            } catch (IOException e) {
                LOGGER.warn("[" + archive + "][old archive file can not be deleted]" + e.toString(), e);
            }
        }
        String fn = archive.getFileName().toString().toLowerCase();
        boolean isTarGZ = fn.endsWith(".tar.gz");
        boolean isZIP = fn.endsWith(".zip");
        if (!isTarGZ && !isZIP) {
            archive = Paths.get(archive.toString() + ".tar.gz");
            LOGGER.info("[archive]" + archive);
            isTarGZ = true;
        }

        if (isTarGZ) {
            try {
                SOSGzipResult r = SOSGzip.compress(outputDir, false);
                Files.write(archive, r.getCompressed(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                LOGGER.info("[archive][" + archive + "][written]" + r.toString());
            } catch (Throwable e) {
                LOGGER.error("[" + archive + "][tar.gz compressing]" + e.toString(), e);
            }
        } else {
            ZipCompress.compress(outputDir, archive);
            LOGGER.info("[archive][" + archive + "]file written");
        }

    }

    private JS7ConverterConfig doConfig(JS7ConverterConfig config, Path configFile) throws Exception {
        if (configFile == null) {
            config.getGenerateConfig().withWorkflows(true).withSchedules(true).withLocks(true).withCyclicOrders(false);
        } else {
            config.parse(configFile);
        }
        return config;
    }

    private String getOptionValue(String argVal) {
        String[] arr = argVal.split("=");
        return arr.length < 2 ? null : arr[1].trim();
    }

    private String getValue(String val, String defaultVal) {
        return val == null ? defaultVal : val;
    }
}
