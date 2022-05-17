package com.sos.commons.util.loganonymizer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSLogAnonymizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLogAnonymizer.class);

    public static void usage() {
        LOGGER.info("\r\n"  
                + "Usage:  log-anonymizer.sh [Options]" + "\r\n" +  "\r\n"  
                + "Options: " + "\r\n"  
                + "  -l | --log-file=<log-file>       | optional: location of log files to be anonymized; a single file, directory or wildcards can be specified" + "\r\n" 
                + "                                               the argument can occur any number of times" +  "\r\n"
                + "  -o | --output-dir=<directory>    | optional: output directory for anonymized log files" +  "\r\n"
                + "  -r | --rules-file=<rules-file>   | optional: path to a YAML file holding rules for anonymization; by default built-in rules will be applied" +  "\r\n" 
                + "  -e | --export-rules=<rules-file> | optional: path to a YAML file to which built-in rules will be exported" +  "\r\n");
        
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
                case "-o":
                case "--output-dir":
                    if (parameters.length > 1) {
                        sosLogAnonymizerExecuter.setOutputdir(parameters[1]);
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
                    usage();
                    System.exit(1);

                }
            }
            sosLogAnonymizerExecuter.executeSubstitution();
            System.exit(exitCode);

        }
    }
}
