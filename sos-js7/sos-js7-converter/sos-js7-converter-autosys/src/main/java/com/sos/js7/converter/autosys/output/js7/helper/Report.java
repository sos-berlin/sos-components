package com.sos.js7.converter.autosys.output.js7.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobResource;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.input.JILJobParser;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.AutosysConverterHelper;
import com.sos.js7.converter.autosys.output.js7.helper.fork.BOXJobHelper;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.json.JS7Agent;

public class Report {

    private static final Logger LOGGER = LoggerFactory.getLogger(Report.class);

    private static final String FILE_NAME_AGENT_MAPPINFS_CONFIG = "agent_mappings.config";

    // - JIL
    private static final String FOLDER_NAME_JIL_PARSER_ATTRIBUTES = "report-attributes";
    private static final String FILE_NAME_JIL_PARSER_DUPLICATES = "Report-JIL-Parser[Jobs]duplicates.txt";
    private static final String FILE_NAME_JIL_PARSER_MULTIPLE_ATTRIBUTES = "Report-JIL-Parser[Jobs]multiple-attributes.txt";
    private static Path FILE_JIL_PARSER_DUPLICATES;
    private static Path FILE_JIL_PARSER_MULTIPLE_ATTRIBUTES;

    // - BOX
    private static final String FILE_NAME_BOX_RUNTIME = "Report-BOX[Runtime].txt";
    // different children jobs time zones as by the box
    private static final String FILE_NAME_BOX_RUNTIME_TIMEZONE_CHILDREN_JOBS = "Report-BOX[Runtime][timezone]children_jobs.txt";
    private static final String FILE_NAME_BOX_CHILDREN_JOBS_RECURSION = "Report-BOX[Children-Jobs]recursion.txt";
    private static final String FILE_NAME_BOX_CHILDREN_JOBS_ZERO = "Report-BOX[Children-Jobs]0.txt";
    private static final String FILE_NAME_BOX_CHILDREN_JOBS_BOX_TERMINATOR = "Report-BOX[Children-Jobs]box_terminator.txt";
    private static final String FILE_NAME_BOX_CONDITION_USED_BY_OTHER_JOBS = "Report-BOX[Condition]used_by_other_jobs.txt";
    private static final String FILE_NAME_BOX_CONDITION_REFERS_TO_CHILDREN_JOBS = "Report-BOX[Condition]refers_to_children_jobs.txt";
    private static final String FILE_NAME_BOX_CONDITION_REFERS_TO_BOX_ITSELF = "Report-BOX[Condition]refers_to_box_itself.txt";
    private static final String FILE_NAME_BOX_CONDITIONS_SUCCESS_FAILURE = "Report-BOX[Conditions]box_success,box_failure.txt";

    // - Conditions
    private static final String FILE_NAME_CONDITIONS_BY_TYPE = "Report-Conditions[By-Type].txt";
    private static final String FILE_NAME_CONDITIONS_BY_TYPE_NOTRUNNING = "Report-Conditions[By-Type]notrunning.txt";
    private static final String FILE_NAME_CONDITIONS_WITH_OR = "Report-Conditions[OR].txt";
    private static final String FILE_NAME_CONDITIONS_WITH_GROUP = "Report-Conditions[Groups].txt";
    private static final String FILE_NAME_CONDITIONS_WITH_LOOKBACK = "Report-Conditions[LookBack].txt";
    private static final String FILE_NAME_CONDITIONS_WITH_INSTANCE_TAG = "Report-Conditions[InstanceTag].txt";
    private static final String FILE_NAME_CONDITIONS_JOBS_NOT_FOUND = "Report-Conditions[Jobs]not_found.txt";

    // - Jobs
    public static final String FILE_NAME_JOBS_DUPLICATES = "Report-Jobs[Duplicates].txt";
    private static final String FILE_NAME_JOBS_BY_TYPE = "Report-Jobs[By-Type].txt";
    private static final String FILE_NAME_JOBS_BY_APPLICATION_GROUP = "Report-Jobs[By-Application,Group].txt";
    private static final String FILE_NAME_JOBS_ALL_BY_RUNTIME = "Report-Jobs[By-Runtime].txt";
    private static final String FILE_NAME_JOBS_ALL_BY_RUNTIME_RUN_WINDOW = "Report-Jobs[By-Runtime]run_window.txt";
    private static final String FILE_NAME_JOBS_ALL_BY_RESOURCES = "Report-Jobs[By-Attribute]resources.txt";
    private static final String FILE_NAME_JOBS_ALL_BY_NRETRYS = "Report-Jobs[By-Attribute]n_retrys.txt";
    private static final String FILE_NAME_JOBS_ALL_BY_MAX_RUN_ALARM = "Report-Jobs[By-Attribute]max_run_alarm.txt";
    private static final String FILE_NAME_JOBS_ALL_BY_MIN_RUN_ALARM = "Report-Jobs[By-Attribute]min_run_alarm.txt";
    private static final String FILE_NAME_JOBS_ALL_BY_TERM_RUN_TIME = "Report-Jobs[By-Attribute]term_run_time.txt";
    private static final String FILE_NAME_JOBS_ALL_BY_INTERACTIVE = "Report-Jobs[By-Attribute]interactive.txt";
    private static final String FILE_NAME_JOBS_ALL_BY_JOB_TERMINATOR = "Report-Jobs[By-Attribute]job_terminator.txt";

    // - JS7 Notices
    private static final String FILE_NAME_JS7_CONSUME_NOTICES = "Report-JS7[Consume-Notices].txt";

    // - JS7 BOX Converted
    private static final String FILE_NAME_JS7_BOX_ERROR = "Report-JS7[BOX]ERROR.txt";
    private static final String FILE_NAME_JS7_BOX = "Report-JS7[BOX].txt";

    // - Help lines
    private static final String LINE_DELIMETER =
            "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
    private static final String LINE_DETAILS = "---- DETAILS " + LINE_DELIMETER;

    // - Indent
    private static final String INDENT_JOB_PARENT_PATH = "%-30s";
    private static final String INDENT_JOB_NAME = "%-70s";
    private static final String INDENT_JOB_PATH = INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME;

    public static void writeParserReports(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) {
        writeJobReports(pr, reportDir, analyzer);
        writeConditionsReports(reportDir, analyzer);
    }

    public static void writeJS7Reports(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) {
        writeSummaryJS7ConsumeNoticesReport(reportDir, analyzer);
    }

    public static void writeJS7BOXReport(Path reportDir, JobBOX box, Workflow w) {

        try {
            Path f = reportDir.resolve(FILE_NAME_JS7_BOX_ERROR);
            // SOSPath.deleteIfExists(f);

            Path f2 = reportDir.resolve(FILE_NAME_JS7_BOX);
            // SOSPath.deleteIfExists(f2);

            int totalJobs = box.getJobs().size();
            int wJobs = w.getJobs().getAdditionalProperties().size();

            List<BOXJobHelper> l = ConverterBOXJobs.USED_JOBS_PER_BOX.get(box.getName());
            if (totalJobs != wJobs) {
                String msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(box), box
                        .getName(), "Total BOX ChildrenJobs=" + totalJobs + ", Total WorkflowJobs=" + wJobs);
                SOSPath.appendLine(f, msg);

                List<ACommonJob> nestedBJ = box.getJobs().stream().filter(j -> j.isBox()).collect(Collectors.toList());
                if (nestedBJ.size() > 0) {
                    msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", "", "", "    nested BOX detected:");
                    SOSPath.appendLine(f, msg);
                    for (ACommonJob nj : nestedBJ) {
                        msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", "", "", "        " + nj);
                        SOSPath.appendLine(f, msg);
                    }

                }

                // SOSPath.appendLine(f, LINE_DELIMETER);
            }

            String diffIndent = "%-10s";
            String totalIndent = "%-15s";
            if (l == null) {
                String msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + diffIndent + totalIndent + "%s", PathResolver
                        .getJILJobParentPathNormalized(box), box.getName(), "Diff=" + totalJobs, "(Total jobs=" + totalJobs + ",",
                        "Execute.Named converted=0)");
                SOSPath.appendLine(f, msg);
                SOSPath.appendLine(f, LINE_DELIMETER);
            } else {
                int diff = totalJobs - l.size();
                if (diff != 0) {
                    String msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + diffIndent + totalIndent + "%s", PathResolver
                            .getJILJobParentPathNormalized(box), box.getName(), "Diff=" + diff, "(Total jobs=" + totalJobs + ",",
                            "Execute.Named converted=" + l.size() + ")");
                    SOSPath.appendLine(f, msg);
                    SOSPath.appendLine(f, LINE_DELIMETER);
                } else {
                    String msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(box), box
                            .getName(), "Total jobs=" + totalJobs);
                    SOSPath.appendLine(f2, msg);
                    SOSPath.appendLine(f2, LINE_DELIMETER);
                }
            }

        } catch (Throwable e) {
            LOGGER.error("[writeJS7ErrorBOXReport][box=" + box + "]" + e, e);
        }
    }

    public static void writeAgentMappingsConfig(Path reportDir, Map<String, JS7Agent> machine2js7Agent) {
        if (reportDir == null) {
            return;
        }
        try {
            Path f = reportDir.resolve(FILE_NAME_AGENT_MAPPINFS_CONFIG);
            SOSPath.deleteIfExists(f);

            if (machine2js7Agent == null || machine2js7Agent.size() == 0) {
                return;
            }
            Set<String> set = new TreeSet<>(machine2js7Agent.keySet());
            int maxLength = set.stream().mapToInt(String::length).max().orElse(0);
            maxLength += 5;
            for (String m : set) {
                String msg = String.format("%-" + maxLength + "s%s", m, "= ");
                SOSPath.appendLine(f, msg);
            }

        } catch (Throwable e) {
            LOGGER.error("[writeAgentMappingsConfig]" + e.toString(), e);
        }
    }

    public static void writeAllAttributes(Path reportDir, Properties p, ACommonJob j) {
        try {
            Path d = reportDir.resolve(FOLDER_NAME_JIL_PARSER_ATTRIBUTES);
            if (!Files.exists(d)) {
                try {
                    Files.createDirectory(d);
                } catch (IOException e) {
                    LOGGER.error("[" + d + "]" + e.toString(), e);
                }
            }

            for (Entry<Object, Object> e : p.entrySet()) {
                Path f = d.resolve(e.getKey() + ".txt");

                SOSPath.appendLine(f, e.getKey() + "=" + e.getValue());
                String msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", "", PathResolver
                        .getJILJobParentPathNormalized(j), j.getName(), getDetails(j));
                SOSPath.appendLine(f, msg);
                SOSPath.appendLine(f, LINE_DELIMETER);
            }

        } catch (Throwable e) {
            LOGGER.error("[writeAllAttributes]" + e.toString(), e);
        }
    }

    private static void writeConditionsReports(Path reportDir, AutosysAnalyzer analyzer) {
        try {
            writeSummaryConditionsReportByType(reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeConditionsReportByType]" + e.toString(), e);
        }

        try {
            writeSummaryConditionsReportByTypeNotrunning(reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeSummaryConditionsReportByTypeNotrunning]" + e.toString(), e);
        }

        try {
            writeSummaryConditionsReportBoxSuccessFailure(reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeConditionsReportBoxSuccessFailure]" + e.toString(), e);
        }
        try {
            writeSummaryConditionsReportByLockBack(reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeSummaryConditionsReportByLockBack]" + e.toString(), e);
        }

        try {
            writeSummaryConditionsReportJobsNotFound(reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeSummaryConditionsReportJobsNotFound]" + e.toString(), e);
        }

        try {
            writeSummaryConditionsReportBoxUsedByOtherJobs(reportDir, analyzer);
        } catch (Exception e) {
            LOGGER.error("[writeSummaryConditionsReportBoxUsedByOtherJobs]" + e.toString(), e);
        }
    }

    private static void writeSummaryJS7ConsumeNoticesReport(Path reportDir, AutosysAnalyzer analyzer) {
        if (reportDir == null) {
            return;
        }

        try {
            Path f = reportDir.resolve(FILE_NAME_JS7_CONSUME_NOTICES);
            SOSPath.deleteIfExists(f);
            if (BoardHelper.JS7_CONSUME_NOTICES.size() == 0) {
                return;
            }

            String indentDetails = "%-15s";
            for (Condition c : BoardHelper.JS7_CONSUME_NOTICES) {
                ACommonJob cj = analyzer.getAllJobs().get(c.getJobName());
                String cjp = PathResolver.getJILJobParentPathNormalized(cj);
                String cjn = cj.getName();

                Set<String> jobs = analyzer.getConditionAnalyzer().getINConditionJobs(c);
                String msg = String.format(Report.INDENT_JOB_NAME + Report.INDENT_JOB_PARENT_PATH + Report.INDENT_JOB_NAME + indentDetails + "%s", c
                        .toString(), cjp, cjn, getDetails(cj), "runtime[" + cj.getRunTime() + "]");
                SOSPath.appendLine(f, msg);

                msg = String.format(Report.INDENT_JOB_NAME + "%s", "", "Used as IN by job(s)=" + jobs.size() + ":");
                SOSPath.appendLine(f, msg);

                for (String jn : jobs) {
                    ACommonJob j = analyzer.getAllJobs().get(jn);
                    if (j == null) {
                        msg = String.format(Report.INDENT_JOB_NAME + Report.INDENT_JOB_PARENT_PATH + "%s", "", "!!!NOT FOUND", jn);
                        SOSPath.appendLine(f, msg);
                    } else {
                        String p = PathResolver.getJILJobParentPathNormalized(j);
                        String n = j.getName();
                        msg = String.format(Report.INDENT_JOB_NAME + Report.INDENT_JOB_PARENT_PATH + Report.INDENT_JOB_NAME + indentDetails + "%s",
                                "", p, n, getDetails(j), "runtime[" + j.getRunTime() + "]");
                        SOSPath.appendLine(f, msg);
                    }
                }
                SOSPath.appendLine(f, LINE_DELIMETER);
            }

        } catch (Throwable e) {
            LOGGER.error("[writeJS7ConsumeNoticesReport]" + e, e);
        }
    }

    public static void writePerJobBOXConditionRecursionReport(Path reportDir, JobBOX boxJob, ACommonJob j, Set<String> reportBOXNames) {
        if (reportDir == null) {
            return;
        }

        try {
            Path report = reportDir.resolve(FILE_NAME_BOX_CHILDREN_JOBS_RECURSION);

            String bp = PathResolver.getJILJobParentPathNormalized(boxJob);
            String bn = boxJob.getName();
            if (reportBOXNames.contains(bn)) {
                bp = "";
                bn = "";
            } else {
                // if (reportBOXNames.size() > 0) {
                SOSPath.appendLine(report, Report.LINE_DELIMETER);
                // }
                reportBOXNames.add(bn);
            }
            String msg = String.format(Report.INDENT_JOB_PARENT_PATH + "%s", bp, bn);
            SOSPath.appendLine(report, msg);
            msg = String.format("%-60s" + Report.INDENT_JOB_NAME + "%s", "", j.getName(), j.getCondition().getOriginalCondition());
            SOSPath.appendLine(report, msg);
        } catch (Throwable e) {
            LOGGER.error("[writeBOXConditionRecursionReport]" + e, e);
        }

    }

    public static void writePerJobBOXConditionRefersReports(Path reportDir, JobBOX boxJob,
            Map<String, Condition> toRemoveConditionsRefersToChildrenJobs, Map<String, Condition> toRemoveConditionsRefersToBoxItself) {
        if (reportDir == null) {
            return;
        }

        try {
            if (toRemoveConditionsRefersToChildrenJobs.size() > 0) {
                Path report = reportDir.resolve(FILE_NAME_BOX_CONDITION_REFERS_TO_CHILDREN_JOBS);

                String msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(boxJob), boxJob.getName());
                SOSPath.appendLine(report, msg);
                msg = String.format("%-30s%-20s%-4s%s", "", "condition", ":", boxJob.getCondition().getOriginalCondition());
                SOSPath.appendLine(report, msg);
                msg = String.format("%-30s%-20s%-4s", "", "children jobs parts", ":");
                SOSPath.appendLine(report, msg);

                toRemoveConditionsRefersToChildrenJobs.entrySet().stream().forEach(e -> {
                    String msg2 = String.format("%-54s%s", "", e.getValue().getOriginalValue());
                    try {
                        SOSPath.appendLine(report, msg2);
                    } catch (Throwable ex) {
                    }
                });

                SOSPath.appendLine(report, LINE_DELIMETER);
            }
            if (toRemoveConditionsRefersToBoxItself.size() > 0) {
                Path report = reportDir.resolve(Report.FILE_NAME_BOX_CONDITION_REFERS_TO_BOX_ITSELF);

                String msg = String.format(Report.INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(boxJob), boxJob
                        .getName());
                SOSPath.appendLine(report, msg);
                msg = String.format(Report.INDENT_JOB_PARENT_PATH + "%-20s%-4s%s", "", "condition", ":", boxJob.getCondition()
                        .getOriginalCondition());
                SOSPath.appendLine(report, msg);

                toRemoveConditionsRefersToBoxItself.entrySet().stream().forEach(e -> {
                    String msg2 = String.format(Report.INDENT_JOB_PARENT_PATH + "%-20s%-4s%s", "", "box itself part", ":", e.getValue()
                            .getOriginalValue());
                    try {
                        SOSPath.appendLine(report, msg2);
                    } catch (Throwable ex) {
                    }
                });
                SOSPath.appendLine(report, Report.LINE_DELIMETER);
            }
        } catch (Throwable e) {
            LOGGER.error("[writeBOXConditionRefersReports]" + e, e);
        }
    }

    public static void writePerJobConditionReport(Path reportDir, ACommonJob j) {
        if (reportDir == null) {
            return;
        }

        try {
            String indentDetails = "%-40s";
            indentDetails = INDENT_JOB_PARENT_PATH;
            if (j.hasORConditions()) {
                Path report = reportDir.resolve(FILE_NAME_CONDITIONS_WITH_OR);

                String msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName());
                SOSPath.appendLine(report, msg);
                msg = String.format(indentDetails + "%s", "", j.getCondition().getOriginalCondition());
                SOSPath.appendLine(report, msg);
                SOSPath.appendLine(report, LINE_DELIMETER);
            }
            if (Conditions.containsGroups(j.getCondition().getCondition().getValue())) {
                Path report = reportDir.resolve(FILE_NAME_CONDITIONS_WITH_GROUP);

                String msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName());
                SOSPath.appendLine(report, msg);
                msg = String.format(indentDetails + "%s", "", j.getCondition().getOriginalCondition());
                SOSPath.appendLine(report, msg);
                SOSPath.appendLine(report, LINE_DELIMETER);
            }
            List<Condition> withInstanceTag = Conditions.getConditionsWithInstanceTag(j.getCondition().getCondition().getValue());
            if (withInstanceTag.size() > 0) {
                Path report = reportDir.resolve(FILE_NAME_CONDITIONS_WITH_INSTANCE_TAG);

                String msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName());
                SOSPath.appendLine(report, msg);
                msg = String.format(indentDetails + "%-18s%-4s%s", "", "condition", "=", j.getCondition().getOriginalCondition());
                SOSPath.appendLine(report, msg);
                for (Condition c : withInstanceTag) {
                    msg = String.format(indentDetails + "%-18s%-4s%s", "", "instanceTag part", "=", c.getOriginalValue());
                    SOSPath.appendLine(report, msg);
                }

                SOSPath.appendLine(report, LINE_DELIMETER);
            }
        } catch (Throwable e) {
            LOGGER.error("[writeJobConditionReport]" + e, e);
        }
    }

    private static void writeJobReports(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) {
        try {
            writeJobReportJobsByType(reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsByType]" + e.toString(), e);
        }

        try {
            writeJobReportJobsByApplicationGroup(pr, reportDir);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsByApplicationGroup]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByRuntime(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByRuntime]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByRuntimeRunWindow(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByRuntimeRunWindow]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByResources(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByResources]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByNRetrys(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByNRetrys]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByMaxRunAlarm(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByMaxRunAlarm]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByMinRunAlarm(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByMinRunAlarm]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByTermRunTime(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByTermRunTime]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByInteractive(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByInteractive]" + e.toString(), e);
        }

        try {
            writeJobReportJobsAllByInteractive(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsAllByInteractive]" + e.toString(), e);
        }

        try {
            writeJobReportJobsByJobTerminator(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsByJobTerminator]" + e.toString(), e);
        }

        try {
            writeJobReportJobsBoxByRuntime(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsBoxByRuntime]" + e.toString(), e);
        }

        try {
            writeJobReportJobsBoxChildrenZero(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsBoxChildrenZero]" + e.toString(), e);
        }

        try {
            writeJobReportJobsBoxChildrenBoxTerminator(pr, reportDir, analyzer);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsBoxChildrenBoxTerminator]" + e.toString(), e);
        }
    }

    private static void writeJobReportJobsByApplicationGroup(DirectoryParserResult pr, Path reportDir) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_BY_APPLICATION_GROUP);

        SOSPath.deleteIfExists(f);

        SOSPath.appendLine(f, "Jobs by application/group:       Total    Standalone     BOX      BOX Children Jobs");

        Map<String, TreeSet<ACommonJob>> map = new LinkedHashMap<>();
        for (ACommonJob j : pr.getJobs()) {
            String key = PathResolver.getJILJobParentPathNormalized(j);
            TreeSet<ACommonJob> jobs = map.get(key);
            if (jobs == null) {
                jobs = AutosysConverterHelper.newJobTreeSet();
            }
            if (!jobs.contains(j)) {
                jobs.add(j);
            }
            map.put(key, jobs);
        }

        Set<String> sortedPaths = new TreeSet<>(map.keySet());

        int totalTotal = 0;
        int totalStandalone = 0;
        int totalBoxes = 0;
        int totalBoxChildren = 0;
        for (String key : sortedPaths) {
            try {
                int localTotal = 0;
                int localStandalone = 0;
                int localBoxes = 0;
                int localBoxChildren = 0;
                for (ACommonJob j : map.get(key)) {
                    localTotal++;
                    if (j.isStandalone()) {
                        localStandalone++;
                    } else if (j.isBox()) {
                        int children = ((JobBOX) j).getJobs().size();
                        localBoxes++;
                        localBoxChildren += children;
                        localTotal += children;
                    }
                }

                totalTotal += localTotal;
                totalStandalone += localStandalone;
                totalBoxes += localBoxes;
                totalBoxChildren += localBoxChildren;

                String msg = String.format(INDENT_JOB_PARENT_PATH + "%-10s  %-10s  %-10s %-10s", key, localTotal, localStandalone, localBoxes,
                        localBoxChildren);
                SOSPath.appendLine(f, "    " + msg);

            } catch (Throwable e1) {

            }
        }
        SOSPath.appendLine(f, LINE_DELIMETER);
        String msg = String.format(INDENT_JOB_PARENT_PATH + "%-10s  %-10s  %-10s %-10s", "", totalTotal, totalStandalone, totalBoxes,
                totalBoxChildren);
        SOSPath.appendLine(f, "    " + msg);

        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);
        SOSPath.appendLine(f, "Jobs by application/group:");
        map.keySet().stream().sorted().forEach(e -> {
            try {
                SOSPath.appendLine(f, "    " + e);
                for (ACommonJob j : map.get(e)) {
                    // SOSPath.appendLine(f, " " + j.getName());

                    if (j.isBox()) {
                        JobBOX jb = (JobBOX) j;
                        List<ACommonJob> jobs = jb.getJobs();

                        StringBuilder sb = new StringBuilder();
                        sb.append(", Children Jobs=").append(jobs.size());
                        if (jb.hasCondition()) {
                            sb.append(", condition=").append(j.getCondition().getOriginalCondition());
                        }
                        if (jb.getBoxSuccess().getValue() != null) {
                            sb.append(", ").append(jb.getBoxSuccess().getName()).append("=").append(jb.getBoxSuccess().getValue());
                        }
                        if (jb.getBoxFailure().getValue() != null) {
                            sb.append(", ").append(jb.getBoxFailure().getName()).append("=").append(jb.getBoxFailure().getValue());
                        }
                        String msg2 = String.format(INDENT_JOB_NAME + "%s", j.getName(), getDetails(j) + sb);
                        SOSPath.appendLine(f, "        " + msg2);

                        for (ACommonJob bj : jobs) {
                            msg2 = String.format(INDENT_JOB_NAME + "%s", bj.getName(), bj.getJobType().getValue());
                            SOSPath.appendLine(f, "            " + msg2);
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        if (j.hasCondition()) {
                            sb.append(", condition=").append(j.getCondition().getOriginalCondition());
                        }
                        String msg2 = String.format(INDENT_JOB_NAME + "%s", j.getName(), getDetails(j) + sb);
                        SOSPath.appendLine(f, "        " + msg2);
                    }

                }
                SOSPath.appendLine(f, Report.LINE_DELIMETER);

            } catch (Throwable e1) {

            }
        });

    }

    private static void writeJobReportJobsByType(Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_BY_TYPE);

        SOSPath.deleteIfExists(f);

        // SOSPath.appendLine(f, "Jobs by type:");
        SOSPath.appendLine(f, "Jobs by type:           Total    Standalone     BOX      BOX Children Jobs");

        Map<ConverterJobType, TreeSet<ACommonJob>> mapByType = new LinkedHashMap<>();
        mapJobsByType(mapByType, analyzer.getAllJobs().values());

        String msg = "";
        int totalTotal = 0;
        int totalStandalone = 0;
        int totalBoxChildren = 0;
        for (ConverterJobType key : mapByType.keySet()) {
            int localTotal = 0;
            int localStandalone = 0;
            int localBoxes = 0;
            int localBoxChildren = 0;
            Set<String> boxes = new HashSet<>();
            if (ConverterJobType.BOX.equals(key)) {
                TreeSet<ACommonJob> jobs = mapByType.get(key);
                localBoxes += jobs.size();
                localTotal += jobs.size();

                // for (ACommonJob j : jobs) {
                // JobBOX b = (JobBOX) j;
                // localBoxChildren += b.getJobs().size();
                // }

            } else {
                for (ACommonJob j : mapByType.get(key)) {
                    localTotal++;
                    if (j.isStandalone()) {
                        localStandalone++;
                    } else {
                        localBoxChildren++;
                        if (!boxes.contains(j.getBoxName())) {
                            boxes.add(j.getBoxName());
                        }
                    }
                }
                localBoxes += boxes.size();
            }

            totalTotal += localTotal;
            totalStandalone += localStandalone;
            totalBoxChildren += localBoxChildren;

            String lst = localStandalone + "";
            String lb = localBoxes + "";
            String lbc = localBoxChildren + "";
            if (ConverterJobType.BOX.equals(key)) {
                lst = "";
                lb = "";
                lbc = "";
            }

            msg = String.format("%-19s %-10s  %-10s  %-10s %-10s", key, localTotal, lst, lb, lbc);

            // msg = String.format("%-20s %-20s %-50s", key, mapByType.get(key).size(), d);
            SOSPath.appendLine(f, "    " + msg);
        }

        SOSPath.appendLine(f, LINE_DELIMETER);
        msg = String.format("%-19s %-10s  %-10s  %-10s %-10s", "", totalTotal, totalStandalone, "", totalBoxChildren);
        SOSPath.appendLine(f, "    " + msg);

        SOSPath.appendLine(f, LINE_DETAILS);

        SOSPath.appendLine(f, "Jobs by type:");
        for (ConverterJobType key : mapByType.keySet()) {
            SOSPath.appendLine(f, "    " + key);
            for (ACommonJob j : mapByType.get(key)) {
                msg = String.format("%-8s" + INDENT_JOB_PATH + "%s", "", PathResolver.getJILJobParentPathNormalized(j), j.getName(), getDetails(j));
                SOSPath.appendLine(f, msg);
            }
            SOSPath.appendLine(f, LINE_DELIMETER);
        }
    }

    private static void writeJobReportJobsAllByRuntime(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_RUNTIME);

        SOSPath.deleteIfExists(f);

        // SOSPath.appendLine(f, "Jobs by type:");
        SOSPath.appendLine(f, "Jobs by runtime:      Total Jobs    Without runtime    Single Starts    Cyclic    Runtime without start time");

        String msg = "";
        int jobs = 0;
        int jobsWithoutRuntime = 0;
        Set<ACommonJob> runtimeSingleStarts = AutosysConverterHelper.newJobTreeSet();
        Set<ACommonJob> runtimeCyclic = AutosysConverterHelper.newJobTreeSet();
        Set<ACommonJob> runtimeUnknown = AutosysConverterHelper.newJobTreeSet();
        Map<String, Integer> calendars = new TreeMap<>();
        Map<String, Integer> timezones = new TreeMap<>();
        for (ACommonJob j : analyzer.getAllJobs().values()) {
            jobs++;
            if (!j.hasRunTime()) {
                continue;
            }
            mapCounter(calendars, j.getRunTime().getRunCalendar().getValue());
            mapCounter(timezones, j.getRunTime().getTimezone().getValue());

            if (j.getRunTime().isSingleStarts()) {
                runtimeSingleStarts.add(j);
            } else if (j.getRunTime().isCyclic()) {
                runtimeCyclic.add(j);
            } else {
                runtimeUnknown.add(j);
            }
        }

        SOSPath.appendLine(f, LINE_DELIMETER);
        msg = String.format("%-18s %-14s %-18s %-15s %-10s %-10s", "", jobs, jobsWithoutRuntime, runtimeSingleStarts.size(), runtimeCyclic.size(),
                runtimeUnknown.size());
        SOSPath.appendLine(f, "    " + msg);

        SOSPath.appendLine(f, LINE_DETAILS);
        SOSPath.appendLine(f, "Calendars: " + calendars.size() + "                                       Used by Job(s)");
        for (Map.Entry<String, Integer> e : calendars.entrySet()) {
            msg = String.format("%-50s %-50s", e.getKey(), e.getValue());
            SOSPath.appendLine(f, "    " + msg);
        }
        SOSPath.appendLine(f, LINE_DELIMETER);
        SOSPath.appendLine(f, "Time Zones: " + timezones.size() + "                                       Used by Job(s)");
        for (Map.Entry<String, Integer> e : timezones.entrySet()) {
            msg = String.format("%-50s %-50s", e.getKey(), e.getValue());
            SOSPath.appendLine(f, "    " + msg);
        }
        SOSPath.appendLine(f, LINE_DELIMETER);

        SOSPath.appendLine(f, "Jobs by runtime:");
        writeAllJobsRuntimeDetails(f, runtimeSingleStarts, "Single Starts");
        writeAllJobsRuntimeDetails(f, runtimeCyclic, "Cyclic");
        writeAllJobsRuntimeDetails(f, runtimeUnknown, "Runtime without start time");

    }

    private static void writeJobReportJobsAllByRuntimeRunWindow(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_RUNTIME_RUN_WINDOW);
        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobs = analyzer.getAllJobs().values().stream().filter(j -> j.hasRunTime() && j.getRunTime().getRunWindow()
                .getValue() != null).collect(Collectors.toList());

        if (jobs.size() == 0) {
            return;
        }

        Set<ACommonJob> runtimeSingleStarts = AutosysConverterHelper.newJobTreeSet();
        Set<ACommonJob> runtimeCyclic = AutosysConverterHelper.newJobTreeSet();
        Set<ACommonJob> runtimeUnknown = AutosysConverterHelper.newJobTreeSet();
        for (ACommonJob j : jobs) {
            if (j.getRunTime().isSingleStarts()) {
                runtimeSingleStarts.add(j);
            } else if (j.getRunTime().isCyclic()) {
                runtimeCyclic.add(j);
            } else {
                runtimeUnknown.add(j);
            }
        }
        SOSPath.appendLine(f, "Jobs by runtime run window:");
        writeAllJobsRuntimeDetails(f, runtimeSingleStarts, "Single Starts");
        writeAllJobsRuntimeDetails(f, runtimeCyclic, "Cyclic");
        writeAllJobsRuntimeDetails(f, runtimeUnknown, "Runtime without start time");
    }

    private static void writeJobReportJobsAllByResources(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_RESOURCES);
        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobs = analyzer.getAllJobs().values().stream().filter(j -> j.hasResources()).collect(Collectors.toList());
        if (jobs.size() == 0) {
            return;
        }

        Map<String, List<ACommonJob>> m = new TreeMap<>();
        for (ACommonJob j : jobs) {
            for (CommonJobResource r : j.getResources().getValue()) {
                String key = r.getOriginal();
                List<ACommonJob> l = m.get(key);
                if (l == null) {
                    l = new ArrayList<>();
                }
                l.add(j);
                m.put(key, l);
            }
        }

        SOSPath.appendLine(f, "Resources total: " + m.size() + "                                                    Used by jobs:");
        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, "    " + msg);
            } catch (Exception e1) {
            }
        });
        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);

        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, msg);
                for (ACommonJob j : e.getValue()) {
                    msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                            getDetails(j));
                    SOSPath.appendLine(f, "    " + msg);
                }
                SOSPath.appendLine(f, LINE_DELIMETER);
            } catch (Exception e1) {
            }

        });
    }

    private static void writeJobReportJobsAllByNRetrys(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_NRETRYS);
        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobs = analyzer.getAllJobs().values().stream().filter(j -> j.getNRetrys().getValue() != null).collect(Collectors.toList());
        if (jobs.size() == 0) {
            return;
        }

        Map<Integer, List<ACommonJob>> m = new TreeMap<>();
        for (ACommonJob j : jobs) {
            Integer key = j.getNRetrys().getValue();

            List<ACommonJob> l = m.get(key);
            if (l == null) {
                l = new ArrayList<>();
            }
            l.add(j);
            m.put(key, l);
        }

        SOSPath.appendLine(f, "N_Retrys total: " + m.size() + "                                                    Used by jobs:");
        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, "    " + msg);
            } catch (Exception e1) {
            }
        });
        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);

        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, msg);
                for (ACommonJob j : e.getValue()) {
                    msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                            getDetails(j));
                    SOSPath.appendLine(f, "    " + msg);
                }
                SOSPath.appendLine(f, LINE_DELIMETER);
            } catch (Exception e1) {
            }

        });
    }

    private static void writeJobReportJobsAllByMaxRunAlarm(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_MAX_RUN_ALARM);
        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobs = analyzer.getAllJobs().values().stream().filter(j -> j.getMaxRunAlarm().getValue() != null).collect(Collectors
                .toList());
        if (jobs.size() == 0) {
            return;
        }

        Map<Integer, List<ACommonJob>> m = new TreeMap<>();
        for (ACommonJob j : jobs) {
            Integer key = j.getMaxRunAlarm().getValue();

            List<ACommonJob> l = m.get(key);
            if (l == null) {
                l = new ArrayList<>();
            }
            l.add(j);
            m.put(key, l);
        }

        SOSPath.appendLine(f, "Max Run Alarm total: " + m.size() + "                                                Used by jobs:");
        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, "    " + msg);
            } catch (Exception e1) {
            }
        });
        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);

        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, msg);
                for (ACommonJob j : e.getValue()) {
                    msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                            getDetails(j));
                    SOSPath.appendLine(f, "    " + msg);
                }
                SOSPath.appendLine(f, LINE_DELIMETER);
            } catch (Exception e1) {
            }

        });
    }

    private static void writeJobReportJobsAllByMinRunAlarm(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_MIN_RUN_ALARM);
        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobs = analyzer.getAllJobs().values().stream().filter(j -> j.getMinRunAlarm().getValue() != null).collect(Collectors
                .toList());
        if (jobs.size() == 0) {
            return;
        }

        Map<Integer, List<ACommonJob>> m = new TreeMap<>();
        for (ACommonJob j : jobs) {
            Integer key = j.getMinRunAlarm().getValue();

            List<ACommonJob> l = m.get(key);
            if (l == null) {
                l = new ArrayList<>();
            }
            l.add(j);
            m.put(key, l);
        }

        SOSPath.appendLine(f, "Min Run Alarm total: " + m.size() + "                                                Used by jobs:");
        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, "    " + msg);
            } catch (Exception e1) {
            }
        });
        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);

        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, msg);
                for (ACommonJob j : e.getValue()) {
                    msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                            getDetails(j));
                    SOSPath.appendLine(f, "    " + msg);
                }
                SOSPath.appendLine(f, LINE_DELIMETER);
            } catch (Exception e1) {
            }

        });
    }

    private static void writeJobReportJobsAllByTermRunTime(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_TERM_RUN_TIME);
        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobs = analyzer.getAllJobs().values().stream().filter(j -> j.getTermRunTime().getValue() != null).collect(Collectors
                .toList());
        if (jobs.size() == 0) {
            return;
        }

        Map<Integer, List<ACommonJob>> m = new TreeMap<>();
        for (ACommonJob j : jobs) {
            Integer key = j.getTermRunTime().getValue();

            List<ACommonJob> l = m.get(key);
            if (l == null) {
                l = new ArrayList<>();
            }
            l.add(j);
            m.put(key, l);
        }

        SOSPath.appendLine(f, "Term Run Time total: " + m.size() + "                                                Used by jobs:");
        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, "    " + msg);
            } catch (Exception e1) {
            }
        });
        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);

        m.entrySet().stream().forEach(e -> {
            String msg = String.format(INDENT_JOB_NAME + "%s", e.getKey(), e.getValue().size());
            try {
                SOSPath.appendLine(f, msg);
                for (ACommonJob j : e.getValue()) {
                    msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                            getDetails(j));
                    SOSPath.appendLine(f, "    " + msg);
                }
                SOSPath.appendLine(f, LINE_DELIMETER);
            } catch (Exception e1) {
            }

        });
    }

    private static void writeJobReportJobsAllByInteractive(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_INTERACTIVE);
        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobs = analyzer.getAllJobs().values().stream().filter(j -> j.isInteractive()).collect(Collectors.toList());
        if (jobs.size() == 0) {
            return;
        }

        for (ACommonJob j : jobs) {
            String msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                    getDetails(j));
            SOSPath.appendLine(f, msg);
            SOSPath.appendLine(f, LINE_DELIMETER);
        }
    }

    private static void mapJobsByType(Map<ConverterJobType, TreeSet<ACommonJob>> mapByType, Collection<ACommonJob> jobs) {
        for (ACommonJob job : jobs) {
            ConverterJobType key = job.getConverterJobType();
            TreeSet<ACommonJob> ct = mapByType.get(key);
            if (ct == null) {
                // Comparator<ACommonJob> comp = (o1, o2) -> o1.getName().hashCode() - o2.getName().hashCode();
                // Comparator<ACommonJob> comp = Comparator.comparing(ACommonJob::getName);
                ct = AutosysConverterHelper.newJobTreeSet();
            }
            if (!ct.contains(job)) {
                ct.add(job);
            }
            mapByType.put(key, ct);
        }
    }

    private static void mapCounter(Map<String, Integer> map, String value) {
        if (value != null) {
            Integer c = map.get(value);
            if (c == null) {
                c = Integer.valueOf(0);
            }
            c += 1;
            map.put(value, c);
        }
    }

    public static void writeJobReportJobsBoxByRuntimeTimezoneChildrenJobs(Path reportDir, JobBOX box, Set<String> childrenTimezones) {
        try {
            Path f = reportDir.resolve(FILE_NAME_BOX_RUNTIME_TIMEZONE_CHILDREN_JOBS);
            SOSPath.deleteIfExists(f);

            String timezone = box.getRunTime().getTimezone().getValue();
            int childrenTimezonesSize = childrenTimezones.size();
            if (childrenTimezonesSize > 0) {
                boolean report = false;
                if (childrenTimezonesSize == 1) {
                    String oneOfTheChildrenTimezone = childrenTimezones.iterator().next();
                    if (timezone != null && !timezone.equalsIgnoreCase(oneOfTheChildrenTimezone)) {
                        report = true;
                    }
                } else {
                    report = true;
                }
                if (report) {
                    List<ACommonJob> childrenWithRuntime = box.getJobs().stream().filter(e -> e.hasRunTime()).collect(Collectors.toList());
                    String msg = String.format(INDENT_JOB_PATH + "%s", PathResolver.getJILJobParentPathNormalized(box), box.getName(),
                            "Children(total=" + box.getJobs().size() + ", with runtime=" + childrenWithRuntime.size() + ")", "BOX runtime=" + box
                                    .getRunTime());
                    SOSPath.append(f, msg);
                    for (ACommonJob cj : childrenWithRuntime) {
                        msg = String.format("%-43s%-111s %s", "", cj.getName(), "JOB runtime=" + cj.getRunTime());
                        SOSPath.appendLine(f, msg);
                    }
                    SOSPath.appendLine(f, LINE_DELIMETER);
                }
            }

        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsBoxByRuntimeTimezoneChildrenJobs][box=" + box.getName() + "]" + e, e);
        }
    }

    private static void writeJobReportJobsBoxByRuntime(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_BOX_RUNTIME);
        SOSPath.deleteIfExists(f);

        int boxs = 0;
        Map<JobBOX, Long> runtimeWithOut = AutosysConverterHelper.newJobBoxTreeMap();
        Map<JobBOX, Long> runtimeSingleStarts = AutosysConverterHelper.newJobBoxTreeMap();
        Map<JobBOX, Long> runtimeCyclic = AutosysConverterHelper.newJobBoxTreeMap();
        Map<JobBOX, Long> runtimeUnknown = AutosysConverterHelper.newJobBoxTreeMap();
        Map<String, Integer> calendars = new TreeMap<>();
        Map<String, Integer> timezones = new TreeMap<>();
        for (ACommonJob j : analyzer.getAllJobs().values()) {
            if (!j.isBox()) {
                continue;
            }
            boxs++;

            JobBOX b = (JobBOX) j;

            mapCounter(calendars, j.getRunTime().getRunCalendar().getValue());
            mapCounter(timezones, j.getRunTime().getTimezone().getValue());
            for (ACommonJob cj : b.getJobs()) {
                mapCounter(calendars, cj.getRunTime().getRunCalendar().getValue());
                mapCounter(timezones, cj.getRunTime().getTimezone().getValue());
            }

            if (!b.hasRunTime()) {
                runtimeWithOut.put(b, b.getJobs().stream().filter(e -> e.hasRunTime()).count());
            } else if (j.getRunTime().isSingleStarts()) {
                runtimeSingleStarts.put(b, b.getJobs().stream().filter(e -> e.hasRunTime()).count());
            } else if (j.getRunTime().isCyclic()) {
                runtimeCyclic.put(b, b.getJobs().stream().filter(e -> e.hasRunTime()).count());
            } else {
                runtimeUnknown.put(b, b.getJobs().stream().filter(e -> e.hasRunTime()).count());

            }
        }
        if (boxs == 0) {
            return;
        }

        // SOSPath.appendLine(f, "Jobs by type:");
        SOSPath.appendLine(f, "BOX by runtime:      Total BOX    Without runtime    Single Starts    Cyclic    Runtime without start time");
        // SOSPath.appendLine(f, LINE_DELIMETER);
        String msg = String.format("%-18s %-14s %-18s %-15s %-10s %-10s", "", boxs, runtimeWithOut.size(), runtimeSingleStarts.size(), runtimeCyclic
                .size(), runtimeUnknown.size());
        SOSPath.appendLine(f, "    " + msg);

        SOSPath.appendLine(f, LINE_DETAILS);
        SOSPath.appendLine(f, "Calendars: " + calendars.size() + "                                       Used by Job(s)");
        for (Map.Entry<String, Integer> e : calendars.entrySet()) {
            msg = String.format("%-50s %-50s", e.getKey(), e.getValue());
            SOSPath.appendLine(f, "    " + msg);
        }
        SOSPath.appendLine(f, LINE_DELIMETER);
        SOSPath.appendLine(f, "Time Zones: " + timezones.size() + "                                       Used by Job(s)");
        for (Map.Entry<String, Integer> e : timezones.entrySet()) {
            msg = String.format("%-50s %-50s", e.getKey(), e.getValue());
            SOSPath.appendLine(f, "    " + msg);
        }
        SOSPath.appendLine(f, LINE_DELIMETER);

        SOSPath.appendLine(f, "BOX by runtime:");
        writeBoxRuntimeDetails(f, runtimeWithOut, "Without");
        writeBoxRuntimeDetails(f, runtimeSingleStarts, "Single Starts");
        writeBoxRuntimeDetails(f, runtimeCyclic, "Cyclic");
        writeBoxRuntimeDetails(f, runtimeUnknown, "Runtime without start time");
    }

    private static void writeAllJobsRuntimeDetails(Path f, Set<ACommonJob> l, String title) {
        if (l.size() == 0) {
            return;
        }
        try {
            SOSPath.appendLine(f, "    " + title + ":");
            for (ACommonJob j : l) {
                String msg = String.format(INDENT_JOB_PATH + "%-20s %s", PathResolver.getJILJobParentPathNormalized(j), j.getName(), getDetails(j), j
                        .getRunTime());
                SOSPath.appendLine(f, "        " + msg);
            }
            SOSPath.appendLine(f, LINE_DELIMETER);
        } catch (Throwable e) {
            LOGGER.error("[writeAllJobsRuntimeDetails]" + e, e);
        }
    }

    private static void writeBoxRuntimeDetails(Path f, Map<JobBOX, Long> map, String title) {
        if (map.size() == 0) {
            return;
        }
        try {
            SOSPath.appendLine(f, "    " + title + ":");
            for (JobBOX j : map.keySet()) {
                List<ACommonJob> childrenWithRuntime = j.getJobs().stream().filter(e -> e.hasRunTime()).collect(Collectors.toList());
                String msg = String.format(INDENT_JOB_PATH + "%-40s %s", PathResolver.getJILJobParentPathNormalized(j), j.getName(), "Children(total="
                        + j.getJobs().size() + ", with runtime=" + childrenWithRuntime.size() + ")", "BOX runtime=" + j.getRunTime()
                                + ", BOX condition=" + j.getCondition().getOriginalCondition());
                SOSPath.appendLine(f, "        " + msg);

                for (ACommonJob cj : childrenWithRuntime) {
                    msg = String.format("%-111s %s", cj.getName(), "JOB runtime=" + cj.getRunTime() + ", JOB condition=" + cj.getCondition()
                            .getOriginalCondition());
                    SOSPath.appendLine(f, "                                           " + msg);
                }

            }
            SOSPath.appendLine(f, LINE_DELIMETER);
        } catch (Throwable e) {
            LOGGER.error("[writeBoxByRuntimeDetails]" + e, e);
        }
    }

    private static void writeJobReportJobsBoxChildrenZero(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_BOX_CHILDREN_JOBS_ZERO);

        SOSPath.deleteIfExists(f);

        List<JobBOX> boxes = analyzer.getAllJobs().values().stream().filter(j -> j.isBox()).map(j -> (JobBOX) j).filter(j -> j.getJobs().size() == 0)
                .collect(Collectors.toList());

        if (boxes.size() == 0) {
            return;
        }

        SOSPath.appendLine(f, "BOX without children jobs(Total=" + boxes.size() + "):");
        String msg = "";
        List<JobBOX> notUsed = new ArrayList<>();
        List<JobBOX> asInCond = new ArrayList<>();
        for (JobBOX j : boxes) {
            msg = String.format(INDENT_JOB_PATH + "%-10s %s", PathResolver.getJILJobParentPathNormalized(j), j.getName(), getDetails(j), "condition="
                    + j.getCondition().getOriginalCondition() + ", runtime=" + j.getRunTime());
            SOSPath.appendLine(f, "    " + msg);

            Map<Condition, Set<String>> in = analyzer.getConditionAnalyzer().getINConditionJobs(j);
            if (in == null || in.size() == 0) {
                notUsed.add(j);
            } else {
                asInCond.add(j);
            }
        }
        SOSPath.appendLine(f, LINE_DELIMETER);
        SOSPath.appendLine(f, "Total=" + boxes.size() + " (Used as IN Condition=" + asInCond.size() + ", Not used=" + notUsed.size() + ")");
        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);

        SOSPath.appendLine(f, "BOX without children jobs -  Used as IN Condition for:");
        for (JobBOX b : asInCond) {
            Map<Condition, Set<String>> in = analyzer.getConditionAnalyzer().getINConditionJobs(b);
            msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(b), b.getName());
            SOSPath.appendLine(f, "    " + msg);
            for (Map.Entry<Condition, Set<String>> e : in.entrySet()) {
                msg = String.format("%-80s %s", "", e.getKey());// condition
                SOSPath.appendLine(f, msg);
                for (String jn : e.getValue()) {
                    ACommonJob j = analyzer.getAllJobs().get(jn);
                    if (in == null) {
                        msg = String.format(INDENT_JOB_NAME + "%s", "", jn + " !!! NOT FOUND");
                    } else {
                        String add = "";
                        if (j.isBox()) {
                            add = ",children_jobs=" + ((JobBOX) j).getJobs().size();
                        }
                        msg = String.format("%-84s %-65s %s", "", PathResolver.getJILJobParentPathNormalized(j) + "/" + j.getName(), getDetails(j)
                                + add);
                    }
                    SOSPath.appendLine(f, msg);
                }
            }
        }
        SOSPath.appendLine(f, LINE_DELIMETER);

        SOSPath.appendLine(f, "BOX without children jobs - Not used:");
        for (JobBOX j : notUsed) {
            msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName());
            SOSPath.appendLine(f, "    " + msg);
        }
    }

    private static void writeJobReportJobsBoxChildrenBoxTerminator(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer)
            throws Exception {
        Path f = reportDir.resolve(FILE_NAME_BOX_CHILDREN_JOBS_BOX_TERMINATOR);

        SOSPath.deleteIfExists(f);

        List<ACommonJob> boxTerminators = analyzer.getAllJobs().values().stream().filter(j -> j.isBoxChildJob() && j.getBox().isBoxTerminator())
                .collect(Collectors.toList());

        if (boxTerminators.size() == 0) {
            return;
        }

        SOSPath.appendLine(f, "BOX terminator(Total=" + boxTerminators.size() + "):");
        String msg = "";
        for (ACommonJob j : boxTerminators) {
            msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                    getDetails(j));
            SOSPath.appendLine(f, msg);
            SOSPath.appendLine(f, LINE_DELIMETER);

        }
    }

    private static void writeJobReportJobsByJobTerminator(DirectoryParserResult pr, Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_JOBS_ALL_BY_JOB_TERMINATOR);

        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobTerminators = analyzer.getAllJobs().values().stream().filter(j -> j.isJobTerminator()).collect(Collectors.toList());

        if (jobTerminators.size() == 0) {
            return;
        }

        SOSPath.appendLine(f, "JOB terminator(Total=" + jobTerminators.size() + "):");
        String msg = "";
        for (ACommonJob j : jobTerminators) {
            msg = String.format(INDENT_JOB_PARENT_PATH + INDENT_JOB_NAME + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                    getDetails(j));
            SOSPath.appendLine(f, msg);
            SOSPath.appendLine(f, LINE_DELIMETER);

        }
    }

    private static String getDetails(ACommonJob j) {
        String d = "Standalone";
        if (j.isBox()) {
            d = "BOX";
        } else if (j.getBoxName() != null) {
            d = "BOX " + j.getBoxName();
        }
        return d;
    }

    private static void writeSummaryConditionsReportBoxSuccessFailure(Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_BOX_CONDITIONS_SUCCESS_FAILURE);
        SOSPath.deleteIfExists(f);

        List<JobBOX> boxes = analyzer.getAllJobs().values().stream().filter(j -> j.isBox() && (((JobBOX) j).hasBoxSuccessOrBoxFailure())).map(
                j -> (JobBOX) j).sorted((j1, j2) -> j1.getName().compareTo(j2.getName())).collect(Collectors.toList());

        if (boxes.size() == 0) {
            return;
        }

        for (JobBOX j : boxes) {
            String msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName());
            SOSPath.appendLine(f, msg);
            if (j.getBoxSuccess().getValue() != null) {
                msg = String.format(INDENT_JOB_PARENT_PATH + "%-20s%-4s%s", "", j.getBoxSuccess().getName(), ":", j.getBoxSuccess().getValue());
                SOSPath.appendLine(f, msg);
            }
            if (j.getBoxFailure().getValue() != null) {
                msg = String.format(INDENT_JOB_PARENT_PATH + "%-20s%-4s%s", "", j.getBoxFailure().getName(), ":", j.getBoxFailure().getValue());
                SOSPath.appendLine(f, msg);
            }
            SOSPath.appendLine(f, LINE_DELIMETER);
        }
    }

    private static void writeSummaryConditionsReportBoxUsedByOtherJobs(Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_BOX_CONDITION_USED_BY_OTHER_JOBS);
        SOSPath.deleteIfExists(f);

        List<JobBOX> boxes = analyzer.getAllJobs().values().stream().filter(j -> j.isBox()).map(j -> (JobBOX) j).collect(Collectors.toList());

        if (boxes.size() == 0) {
            return;
        }

        for (JobBOX b : boxes) {
            Map<Condition, Set<String>> m = analyzer.getConditionAnalyzer().getINConditionJobs(b);
            if (m == null || m.size() == 0) {
                continue;
            }

            String msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(b), b.getName());
            SOSPath.appendLine(f, msg);

            for (Map.Entry<Condition, Set<String>> entry : m.entrySet()) {
                msg = String.format("%-4s%s", "", entry.getKey().getOriginalValue());
                SOSPath.appendLine(f, msg);
                for (String jn : entry.getValue()) {
                    ACommonJob j = analyzer.getAllJobs().get(jn);
                    if (j == null) {
                        msg = String.format("%-8s%s", "", "[!!!NOT_FOUND]" + jn);
                        SOSPath.appendLine(f, msg);
                    } else {
                        msg = String.format("%-8s" + INDENT_JOB_PATH + "%s", "", PathResolver.getJILJobParentPathNormalized(j), j.getName(),
                                getDetails(j) + ", condition=" + j.getCondition().getOriginalCondition());
                        SOSPath.appendLine(f, msg);
                    }
                }
            }

            SOSPath.appendLine(f, LINE_DELIMETER);
        }
    }

    private static void writeSummaryConditionsReportByType(Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_CONDITIONS_BY_TYPE);

        SOSPath.deleteIfExists(f);

        SOSPath.appendLine(f, "Conditions by type:");
        String msg = "";
        for (ConditionType key : analyzer.getConditionAnalyzer().getAllConditionsByType().keySet()) {
            msg = String.format("%-20s %-20s", key, analyzer.getConditionAnalyzer().getAllConditionsByType().get(key).size());
            // SOSPath.appendLine(f, " " + key + " = " + conditionAnalyzer.getAllConditionsByType().get(key).size());
            SOSPath.appendLine(f, "    " + msg);
        }

        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);

        msg = String.format("%-68s %-35s %s", "Conditions by type:", "Sorted by used by Job(s):", "Job(s):");
        SOSPath.appendLine(f, msg);

        // String msg = String.format("%-20s %-10s %-10s %-10s", e.getKey(), "Folders", "total=" + e.getValue(), "converted=" + con);
        for (ConditionType key : analyzer.getConditionAnalyzer().getAllConditionsByType().keySet()) {
            SOSPath.appendLine(f, "    " + key);

            Map<Condition, Integer> m = new LinkedHashMap<>();
            for (Condition c : analyzer.getConditionAnalyzer().getAllConditionsByType().get(key)) {
                m.put(c, Integer.valueOf(analyzer.getConditionAnalyzer().getINConditionJobs(c).size()));
            }
            Comparator<Integer> bySize = (Integer o1, Integer o2) -> o1.compareTo(o2);
            m.entrySet().stream().sorted(Map.Entry.<Condition, Integer> comparingByValue(bySize)).forEach(e -> {

                Set<String> jobs = analyzer.getConditionAnalyzer().getINConditionJobs(e.getKey());
                int countStandalone = 0;
                Set<String> boxes = new HashSet<>();
                for (String jn : jobs) {
                    ACommonJob j = analyzer.getAllJobs().get(jn);
                    if (j.isStandalone()) {
                        countStandalone++;
                    } else {
                        String bn = j.getBoxName();
                        if (!boxes.contains(bn)) {
                            boxes.add(bn);
                        }
                    }
                }

                TreeSet<String> sortedJobs = new TreeSet<>();
                sortedJobs.addAll(jobs);

                String msg2 = String.format("%-60s %-3s %-15s %-15s %s", e.getKey().getOriginalValue(), e.getValue(), "(Standalone=" + countStandalone
                        + ",", "BOX=" + boxes.size() + ")", String.join(", ", sortedJobs));
                try {
                    SOSPath.appendLine(f, "        " + msg2);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });

            SOSPath.appendLine(f, LINE_DELIMETER);
        }
    }

    private static void writeSummaryConditionsReportByTypeNotrunning(Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_CONDITIONS_BY_TYPE_NOTRUNNING);

        SOSPath.deleteIfExists(f);

        Set<Condition> set = analyzer.getConditionAnalyzer().getAllConditionsByType().get(ConditionType.NOTRUNNING);
        if (set == null || set.size() == 0) {
            return;
        }

        SOSPath.appendLine(f, "Conditions by type NOTRUNNING: " + set.size());
        SOSPath.appendLine(f, LINE_DETAILS);

        String indentDetails = "%-15s";
        String msg = "";
        for (Condition c : set) {
            SOSPath.appendLine(f, c.getOriginalValue());
            Set<String> jobs = analyzer.getConditionAnalyzer().getINConditionJobs(c);
            if (jobs != null && jobs.size() > 0) {
                for (String jn : jobs) {
                    ACommonJob j = analyzer.getAllJobs().get(jn);
                    if (j == null) {
                        msg = String.format(Report.INDENT_JOB_NAME + Report.INDENT_JOB_PARENT_PATH + "%s", "", "!!!NOT FOUND", jn);
                        SOSPath.appendLine(f, msg);
                    } else {

                        List<Condition> nr = j.conditionsAsList().stream().filter(t -> t.getType().equals(ConditionType.NOTRUNNING)).collect(
                                Collectors.toList());

                        String p = PathResolver.getJILJobParentPathNormalized(j);
                        String n = j.getName();
                        msg = String.format(Report.INDENT_JOB_NAME + Report.INDENT_JOB_PARENT_PATH + Report.INDENT_JOB_NAME + indentDetails + "%s",
                                "", p, n, getDetails(j), "NOTRUNNING=" + nr.size() + ",condition[" + j.getCondition().getOriginalCondition() + "]");
                        SOSPath.appendLine(f, msg);
                    }
                }
            }
            SOSPath.appendLine(f, LINE_DELIMETER);
        }
    }

    private static void writeSummaryConditionsReportByLockBack(Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_CONDITIONS_WITH_LOOKBACK);

        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobsWithLookBack = analyzer.getAllJobs().values().stream().filter(j -> j.hasLookBackConditions()).collect(Collectors
                .toList());
        if (jobsWithLookBack.size() == 0) {
            return;
        }

        Map<ACommonJob, List<Condition>> withLookBackEquals0 = AutosysConverterHelper.newJobConditionsTreeMap();
        Map<ACommonJob, List<Condition>> withLookBackNotEquals0 = AutosysConverterHelper.newJobConditionsTreeMap();

        for (ACommonJob j : jobsWithLookBack) {
            List<Condition> cl = Conditions.getConditionsWithLookBack(j.getCondition().getCondition().getValue());
            for (Condition c : cl) {
                if (c.getLookBack().equals("0")) {
                    List<Condition> l = withLookBackEquals0.get(j);
                    if (l == null) {
                        l = new ArrayList<>();
                    }
                    if (!l.contains(c)) {
                        l.add(c);
                    }
                    withLookBackEquals0.put(j, l);
                } else {
                    List<Condition> l = withLookBackNotEquals0.get(j);
                    if (l == null) {
                        l = new ArrayList<>();
                    }
                    if (!l.contains(c)) {
                        l.add(c);
                    }
                    withLookBackNotEquals0.put(j, l);
                }
            }
        }

        SOSPath.appendLine(f, "Jobs with condition:    lookBack!=0    lookBack=0");
        String msg = String.format("%-28s%-15s%s", "", withLookBackNotEquals0.size(), withLookBackEquals0.size());
        SOSPath.appendLine(f, msg);

        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, LINE_DETAILS);

        String indentDetails = "%-40s";
        if (withLookBackNotEquals0.size() > 0) {
            SOSPath.appendLine(f, "Jobs with condition lookBack!=0 (" + withLookBackNotEquals0.size() + " jobs):");
            SOSPath.appendLine(f, LINE_DELIMETER);
            for (ACommonJob j : withLookBackNotEquals0.keySet()) {
                msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName());
                SOSPath.appendLine(f, msg);
                msg = String.format(indentDetails + "%-18s%-4s%s", "", "condition", "=", j.getCondition().getOriginalCondition());
                SOSPath.appendLine(f, msg);

                List<Condition> cl = withLookBackNotEquals0.get(j);
                for (Condition c : cl) {
                    msg = String.format(indentDetails + "%-18s%-4s%s", "", "lookBack part", "=", c.getOriginalValue());
                    SOSPath.appendLine(f, msg);
                }
            }
        }
        if (withLookBackEquals0.size() > 0) {
            SOSPath.appendLine(f, LINE_DELIMETER);
            SOSPath.appendLine(f, "Jobs with condition lookBack=0 (" + withLookBackEquals0.size() + " jobs):");
            SOSPath.appendLine(f, LINE_DELIMETER);
            for (ACommonJob j : withLookBackEquals0.keySet()) {
                msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName());
                SOSPath.appendLine(f, msg);
                msg = String.format(indentDetails + "%-18s%-4s%s", "", "condition", "=", j.getCondition().getOriginalCondition());
                SOSPath.appendLine(f, msg);

                List<Condition> cl = withLookBackEquals0.get(j);
                for (Condition c : cl) {
                    msg = String.format(indentDetails + "%-18s%-4s%s", "", "lookBack part", "=", c.getOriginalValue());
                    SOSPath.appendLine(f, msg);
                }

            }
            SOSPath.appendLine(f, LINE_DELIMETER);
        }

    }

    private static void writeSummaryConditionsReportJobsNotFound(Path reportDir, AutosysAnalyzer analyzer) throws Exception {
        Path f = reportDir.resolve(FILE_NAME_CONDITIONS_JOBS_NOT_FOUND);

        SOSPath.deleteIfExists(f);

        List<ACommonJob> jobsWithConditions = analyzer.getAllJobs().values().stream().filter(j -> j.hasJobConditions()).collect(Collectors.toList());
        if (jobsWithConditions.size() == 0) {
            return;
        }
        Map<ACommonJob, List<Condition>> jobsRefersToNotFoundJobs = AutosysConverterHelper.newJobConditionsTreeMap();
        Set<String> notFoundJobs = new TreeSet<String>();
        for (ACommonJob j : jobsWithConditions) {
            List<Condition> cl = j.conditionsAsList().stream().filter(c -> c.getJobName() != null && !analyzer.getAllJobs().containsKey(c
                    .getJobName())).collect(Collectors.toList());

            if (cl.size() > 0) {
                for (Condition c : cl) {
                    if (!notFoundJobs.contains(c.getJobName())) {
                        notFoundJobs.add(c.getJobName());
                    }
                }
                jobsRefersToNotFoundJobs.put(j, cl);
            }
        }
        SOSPath.appendLine(f, "Total:");
        String msg = String.format("%-4s%-55s%s", "", "Jobs not found", notFoundJobs.size());
        SOSPath.appendLine(f, msg);
        msg = String.format("%-4s%-55s%s", "", "Jobs with condition that refer to the jobs not found", jobsRefersToNotFoundJobs.size());
        SOSPath.appendLine(f, msg);

        SOSPath.appendLine(f, LINE_DETAILS);
        SOSPath.appendLine(f, "Jobs not found:");
        for (String n : notFoundJobs) {
            SOSPath.appendLine(f, "    " + n);
        }
        SOSPath.appendLine(f, LINE_DELIMETER);

        String indentDetails = "%-40s";
        SOSPath.appendLine(f, "Jobs with condition that refer to the jobs not found:");
        for (ACommonJob j : jobsRefersToNotFoundJobs.keySet()) {
            msg = String.format(INDENT_JOB_PARENT_PATH + "%s", PathResolver.getJILJobParentPathNormalized(j), j.getName());
            SOSPath.appendLine(f, msg);
            msg = String.format(indentDetails + "%-18s%-4s%s", "", "condition", "=", j.getCondition().getOriginalCondition());
            SOSPath.appendLine(f, msg);

            for (Condition c : jobsRefersToNotFoundJobs.get(j)) {
                msg = String.format(indentDetails + "%-18s%-4s%s", "", "not found job", "=", c.getJobName());
                SOSPath.appendLine(f, msg);
            }
            SOSPath.appendLine(f, LINE_DELIMETER);
        }

    }

    public static void writeJILParserDuplicatesReport(Path dir) {
        try {
            FILE_JIL_PARSER_DUPLICATES = null;
            if (dir == null || JILJobParser.INSERT_JOBS == null || JILJobParser.INSERT_JOBS.size() == 0) {
                return;
            }
            Path f = dir.resolve(FILE_NAME_JIL_PARSER_DUPLICATES);
            SOSPath.deleteIfExists(f);
            FILE_JIL_PARSER_DUPLICATES = f;

            int totalDuplicates = 0;
            Set<String> paths = new TreeSet<>();
            for (Map.Entry<String, Map<Path, Integer>> e : JILJobParser.INSERT_JOBS.entrySet()) {
                int counter = 0;
                for (Map.Entry<Path, Integer> v : e.getValue().entrySet()) {
                    counter += v.getValue();
                }
                if (counter > 1) {
                    totalDuplicates += (counter - 1);
                    for (Map.Entry<Path, Integer> v : e.getValue().entrySet()) {
                        String p = JS7ConverterHelper.normalizePath(Paths.get("").resolve(v.getKey().getParent().getFileName()).resolve(v.getKey()
                                .getFileName()).toString());
                        if (!paths.contains(p)) {
                            paths.add(p);
                        }
                    }
                }
            }
            SOSPath.appendLine(f, "TOTAL  duplicates=" + totalDuplicates + ", without duplicates=" + (JILJobParser.COUNTER_INSERT_JOB
                    - totalDuplicates));
            SOSPath.appendLine(f, "TOTAL  files with duplicates=" + paths.size() + " ");
            for (String p : paths) {
                String msg = String.format(INDENT_JOB_NAME + "%s", "", p);
                SOSPath.appendLine(f, msg);
            }

            SOSPath.appendLine(f, LINE_DETAILS);
            for (Map.Entry<String, Map<Path, Integer>> e : JILJobParser.INSERT_JOBS.entrySet()) {
                int counter = 0;
                for (Map.Entry<Path, Integer> v : e.getValue().entrySet()) {
                    counter += v.getValue();
                }
                if (counter > 1) {
                    SOSPath.appendLine(f, e.getKey());
                    for (Map.Entry<Path, Integer> v : e.getValue().entrySet()) {
                        // SOSPath.appendLine(f, " [" + v.getKey() + "]" + v.getValue());

                        // relative path
                        Path p = Paths.get("").resolve(v.getKey().getParent().getFileName()).resolve(v.getKey().getFileName());
                        String msg = String.format(INDENT_JOB_NAME + INDENT_JOB_PARENT_PATH + "%s", "", JS7ConverterHelper.normalizePath(p
                                .toString()), v.getValue());
                        SOSPath.appendLine(f, msg);
                    }
                    SOSPath.appendLine(f, LINE_DELIMETER);
                }
            }

        } catch (Throwable e) {
            LOGGER.error("writeJILParserDuplicatesReport" + e, e);
        }
    }

    public static void writeJILParserMultipleAttributes(Path dir) {
        try {
            FILE_JIL_PARSER_MULTIPLE_ATTRIBUTES = null;
            if (dir == null || JILJobParser.MULTIPLE_ATTRIBUTES == null || JILJobParser.MULTIPLE_ATTRIBUTES.size() == 0) {
                return;
            }
            Path f = dir.resolve(FILE_NAME_JIL_PARSER_MULTIPLE_ATTRIBUTES);
            SOSPath.deleteIfExists(f);

            FILE_JIL_PARSER_MULTIPLE_ATTRIBUTES = f;
            for (Map.Entry<String, Map<String, List<String>>> e : JILJobParser.MULTIPLE_ATTRIBUTES.entrySet()) {
                SOSPath.appendLine(f, e.getKey());
                for (Map.Entry<String, List<String>> v : e.getValue().entrySet()) {
                    SOSPath.appendLine(f, "   [" + v.getKey() + "][hits=" + v.getValue().size() + "]" + v.getValue());
                }
                SOSPath.appendLine(f, LINE_DELIMETER);
            }

        } catch (Throwable e) {
            LOGGER.error("writeJILParserMultipleAttributes" + e, e);
        }
    }

    public static void moveJILReportFiles(Path reportDir) {
        if (FILE_JIL_PARSER_DUPLICATES != null && Files.exists(FILE_JIL_PARSER_DUPLICATES)) {
            try {
                SOSPath.renameTo(FILE_JIL_PARSER_DUPLICATES, reportDir.resolve(FILE_JIL_PARSER_DUPLICATES.getFileName()));
            } catch (IOException e) {
                LOGGER.error("[moveJILReportFiles][" + FILE_JIL_PARSER_DUPLICATES + "]" + e, e);
            }
        }
        if (FILE_JIL_PARSER_MULTIPLE_ATTRIBUTES != null && Files.exists(FILE_JIL_PARSER_MULTIPLE_ATTRIBUTES)) {
            try {
                SOSPath.renameTo(FILE_JIL_PARSER_MULTIPLE_ATTRIBUTES, reportDir.resolve(FILE_JIL_PARSER_MULTIPLE_ATTRIBUTES.getFileName()));
            } catch (IOException e) {
                LOGGER.error("[moveJILReportFiles][" + FILE_JIL_PARSER_MULTIPLE_ATTRIBUTES + "]" + e, e);
            }
        }
    }
}
