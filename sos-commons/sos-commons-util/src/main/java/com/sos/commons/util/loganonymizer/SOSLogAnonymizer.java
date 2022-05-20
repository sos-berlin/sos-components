package com.sos.commons.util.loganonymizer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSLogAnonymizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLogAnonymizer.class);

    public static void usage() {
        StringBuilder s = new StringBuilder(System.lineSeparator());
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().trim().startsWith("windows")) {
            s.append("Usage:  log-anonymizer.cmd [Options]");
        } else {
            s.append("Usage:  log-anonymizer.sh [Options]");
        }
        s.append(System.lineSeparator()).append(System.lineSeparator());
        s.append("Options: ").append(System.lineSeparator());
        s.append("  -l | --log-file=<log-file>       | optional: location of log files to be anonymized; a single file, directory or wildcards can be specified").append(System.lineSeparator());
        s.append("                                               the argument can occur any number of times").append(System.lineSeparator());
        s.append("  -o | --output-dir=<directory>    | optional: output directory for anonymized log files").append(System.lineSeparator());
        s.append("  -r | --rules-file=<rules-file>   | optional: path to a YAML file holding rules for anonymization; by default built-in rules will be applied").append(System.lineSeparator());
        s.append("  -e | --export-rules=<rules-file> | optional: path to a YAML file to which built-in rules will be exported").append(System.lineSeparator());
        LOGGER.info(s.toString());
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
                String[] parameters = arg.split("=", 2);
                String paramName = parameters[0];
                switch (paramName) {
                case "-e":
                case "--export-rules":
                    if (parameters.length > 1 && !parameters[1].isEmpty()) {
                        try {
                            sosLogAnonymizerExecuter.exportRules(parameters[1]);
                        } catch (IOException e) {
                            LOGGER.error("", e.toString());
                            System.exit(1);
                        }
                    } else {
                        LOGGER.error("Error exporting default rules. No filename specified");
                        System.exit(1);
                    }
                    break;
                case "-l":
                case "--log-file":
                    if (parameters.length > 1 && !parameters[1].isEmpty()) {
                        sosLogAnonymizerExecuter.setLogfiles(parameters[1]);
                    }
                    break;
                case "-o":
                case "--output-dir":
                    if (parameters.length > 1 && !parameters[1].isEmpty()) {
                        try {
                            sosLogAnonymizerExecuter.setOutputdir(parameters[1]);
                        } catch (IOException e) {
                            LOGGER.error(e.toString());
                            System.exit(1);
                        }
                    }
                    break;
                case "-r":
                case "--rules-file":
                    if (parameters.length > 1 && !parameters[1].isEmpty()) {
                        try {
                            sosLogAnonymizerExecuter.setRules(parameters[1]);
                        } catch (Exception e) {
                            LOGGER.error("", e.toString());
                            System.exit(1);
                        }
                    }
                    break;
                default:
                    LOGGER.error("... wrong parameter: " + paramName);
                    usage();
                    System.exit(1);

                }
            }
            exitCode = sosLogAnonymizerExecuter.executeSubstitution();
        }
        System.exit(exitCode);
    }
}
