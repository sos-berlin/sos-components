package com.sos.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.reports.classes.ReportArguments;

// 1. top n frequently failed workflows
// 2. top n frequently failed jobs
// 3. top n agents with most parallel execution
// 4. top n periods of low and high parallism jof job executions
// 5. top n high criticality failed jobs
// 6. top n frequently failed workflows with cancelled orders
// 7. top n workflows with the longest execution time
// 8. top n jobs with the longest execution time
// 9. top n periods during which mostly workflows executed.
// 10. top n periods during which mostly jobs executed.

public class ReportGenerator {

    private static final String UTC = "UTC";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerator.class);

    public static void usage() {
        StringBuilder s = new StringBuilder(System.lineSeparator());
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().trim().startsWith("windows")) {
            s.append("Usage:  run-report.cmd [Options]");
        } else {
            s.append("Usage:  run-report.sh [Options]");
        }
        s.append(System.lineSeparator()).append(System.lineSeparator());
        s.append("Options: ").append(System.lineSeparator());
        s.append("  -r | --report=<report-ID> | required: id of the report that should be generated").append(System.lineSeparator());
        s.append("  -i | --inputDirectory=<inputDirectory>    | required: Input directory from where data files will be taken").append(System
                .lineSeparator());
        s.append(
                "  -p | --frequencies=<frequencies>   | required: Processing frequencies (comma-separated, e.g., every3months, every6months, monthly)")
                .append(System.lineSeparator());
        s.append("  -o | --outputDirectory=<outputDirectory> | required: Output directory where final reports will be created").append(System
                .lineSeparator());
        s.append("  -c | --controllerId=<controllerId> | required: Controller Id for which the report needs to be generated").append(System
                .lineSeparator());
        s.append("  -s | --monthFrom=<monthFrom> | required: Month from for input file selection e.g. YYYY-MM").append(System.lineSeparator());
        s.append("  -e | --monthTo=<monthTo> | optional: Month to for input file selection e.g. YYYY-MM;default month before now").append(System
                .lineSeparator());
        s.append("  -a | --periodLength=<periodLength> | optional: Length of period in minutes. Default=5").append(System.lineSeparator());
        s.append("  -b | --periodStep=<periodStep> | optional: Step for next period in minutes. Default=5").append(System
                .lineSeparator());
        s.append("  -n | --hits=<hits> | optional: Define the hits of report;default=10").append(System.lineSeparator());
        s.append("  -d | --logDir=<directory> | optional: Specify the log directory").append(System.lineSeparator());
        System.out.println(s);
    }

    public static void main(String[] args) throws SOSException {
        TimeZone.setDefault(TimeZone.getTimeZone(UTC));
        int exitCode = 0;
        String paramFrequency = null;

        if (args.length == 0 || args[0].matches("-{0,2}h(?:elp)?")) {
            if (args.length == 0) {
                exitCode = 1;
                LOGGER.error("... missing parameter");
                System.err.println("... missing parameter");
            }
            usage();
        } else {

            ReportGeneratorExecuter reportGeneratorExecuter = new ReportGeneratorExecuter();

            List<String> argsList = new ArrayList<String>();
            Map<String, String> optsList = new HashMap<String, String>();
            Map<String, String> double2SingleOpt = new HashMap<String, String>();
            double2SingleOpt.put("--report", "-r");
            double2SingleOpt.put("--inputDirectory", "-i");
            double2SingleOpt.put("--frequencies", "-p");
            double2SingleOpt.put("--outputDirectory", "-o");
            double2SingleOpt.put("--controllerId", "-c");
            double2SingleOpt.put("--monthFrom", "-s");
            double2SingleOpt.put("--monthTo", "-e");
            double2SingleOpt.put("--periodLength", "-a");
            double2SingleOpt.put("--periodStep", "-b");
            double2SingleOpt.put("--periodLength", "-a");
            double2SingleOpt.put("--periodStep", "-b");
            double2SingleOpt.put("--hits", "-n");
            double2SingleOpt.put("--logDir", "-d");

            for (int i = 0; i < args.length; i++) {
                switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].length() < 2) {
                        throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                    }
                    if (args[i].charAt(1) == '-') {
                        if (args[i].length() < 3)
                            throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                        // --opt
                        String opt = double2SingleOpt.get(args[i]);
                        if (opt == null) {
                            throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                        }
                        optsList.put(opt, args[i + 1]);
                    } else {
                        if (args.length - 1 == i) {
                            throw new IllegalArgumentException("Expected arg after: " + args[i]);
                        }
                        // -opt
                        optsList.put(args[i], args[i + 1]);
                        i++;
                    }
                    break;
                default:
                    // arg
                    argsList.add(args[i]);
                    break;
                }
            }

            ReportArguments reportArguments = new ReportArguments();

            for (Entry<String, String> arg : optsList.entrySet()) {
                String paramName = arg.getKey();
                String paramValue = arg.getValue();
                switch (paramName) {
                case "-r":
                case "--report":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setReportId(paramValue);
                    }
                    break;
                case "-i":
                case "--inputDirectory":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setInputDirectory(paramValue);
                    }

                    break;
                case "-p":
                case "--frequencies":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        paramFrequency = paramValue;
                    }
                    break;
                case "-o":
                case "--outputDirectory":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setOutputDirectory(paramValue);
                    }
                    break;
                case "-c":
                case "--controllerId":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setControllerId(paramValue);
                    }
                    break;
                case "-s":
                case "--monthFrom":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setMonthFrom(paramValue);
                    }
                    break;
                case "-e":
                case "--monthTo":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setMonthTo(paramValue);
                    }
                    break;
                case "-a":
                case "--periodLength":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setPeriodLength(paramValue);
                    }
                    break;
                case "-b":
                case "--periodStep":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setPeriodStep(paramValue);
                    }
                    break;
                case "-n":
                case "--hits":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        try {
                            reportArguments.setHits(paramValue);
                        } catch (NumberFormatException e) {
                            LOGGER.error("Error wrong parameter value for <-n --hits>. Integer value expected." + "\n" + e.getMessage() + ":" + e
                                    .getCause());
                            System.err.println("Error wrong parameter value for <-n --hits>. Integer value expected." + "\n" + e.getMessage() + ":"
                                    + e.getCause());
                            System.exit(1);
                        }
                    }
                    break;
                case "-d":
                case "--logDir":
                    if (paramValue != null && !paramValue.isEmpty()) {
                        reportArguments.setLogDir(paramValue);
                    }
                    break;
                default:
                    LOGGER.error("... wrong parameter: " + paramName);
                    System.err.println("... wrong parameter: " + paramName);
                    usage();
                    System.exit(1);

                }
            }

            if (paramFrequency != null && !paramFrequency.isEmpty()) {
                reportArguments.setReqportFrequency(paramFrequency);
            }

            try {
                reportArguments.checkRequired();
                exitCode = reportGeneratorExecuter.execute(reportArguments);
            } catch (IOException | SOSRequiredArgumentMissingException e) {
                                e.printStackTrace(System.err);
                System.exit(1);
            }

        }

        System.exit(exitCode);

    }

}
