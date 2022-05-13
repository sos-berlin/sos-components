package com.sos.commons.util.loganonymizer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSLogAnonymizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLogAnonymizer.class);

    public static void usage() {
        LOGGER.info("Usage: --export-rules-file=<export-rules-file> --log-file=<log-file>[,<log-file>[,<log-file>]] --rules-file=<rules-file>");
        LOGGER.info("       -e|--export-rules-file     : optional: a file name where the default rules will be exported.");
        LOGGER.info("       -l|--log-file              : a file name with placeholder like /temp/agent*.log or a folder name");
        LOGGER.info("       -r|--rules-file            : optional: a file with rules.");
    }

    public static void main(String[] args) {
        int exitCode = 0;

        if (args.length == 0 || args[0].matches("-{0,2}h(?:elp)?")) {
            if (args.length == 0) {
                exitCode = 1;
                LOGGER.error("... missing parameter");
            }
            usage();
        } else {

            SOSLogAnonymizerExecuter sosLogAnonymizerExecuter = new SOSLogAnonymizerExecuter();
            for (String arg : args) {
                String[] parameters = arg.split("=");
                String paramName = parameters[0];
                switch (paramName) {
                case "-e":
                case "--export-rules":
                    if (parameters.length > 1) {
                        try {
                            sosLogAnonymizerExecuter.exportRules(parameters[1]);
                        } catch (IOException e) {
                            System.exit(1);
                        }
                    } else {
                        LOGGER.error("Error exporting default rules. No filename specified");
                        System.exit(1);
                    }
                    break;
                case "-l":
                case "--log-file":
                    if (parameters.length > 1) {
                        sosLogAnonymizerExecuter.setLogfiles(parameters[1]);
                    }
                    break;
                case "-r":
                case "--rules-file":
                    if (parameters.length > 1) {
                        sosLogAnonymizerExecuter.setRules(parameters[1]);
                    } else {
                        LOGGER.error("Error exporting reading rules. No filename specified");
                        System.exit(1);
                    }

                    break;
                default:
                    LOGGER.error("... wrong parameter: " + paramName);
                    System.out.println("... wrong parameter: " + paramName);
                    System.exit(1);

                }
            }
            sosLogAnonymizerExecuter.executeSubstitution();
            System.exit(exitCode);

        }
    }
}
