package com.sos.js7.converter.autosys.input.analyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.config.items.AutosysDiagramConfig;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.input.diagram.AutosysGraphvizDiagramWriter;
import com.sos.js7.converter.autosys.input.diagram.AutosysGraphvizDiagramWriter.Range;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.input.diagram.AGraphvizDiagramWriter;
import com.sos.js7.converter.commons.report.ParserReport;

public class AutosysAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutosysAnalyzer.class);

    public static final String PATH_PREFIX = "autosys.input.";

    private Path analyzerDir;
    private ConditionAnalyzer conditionAnalyzer;
    private Map<String, ACommonJob> allJobs;

    private void init(Path reportDir) {
        conditionAnalyzer = new ConditionAnalyzer(reportDir);
        allJobs = new LinkedHashMap<>();
        analyzerDir = getAnalyzerDir(reportDir);
        if (!Files.exists(analyzerDir)) {
            analyzerDir.toAbsolutePath().toFile().mkdirs();
        }
    }

    public DirectoryParserResult analyzeAndCreateDiagram(DirectoryParserResult pr, Path input, Path reportDir) throws Exception {
        init(reportDir);
        analyze(pr, reportDir);

        // if the conditions have been optimized, the input files must be parsed again.
        if (createDiagram(pr, input, reportDir)) {
            // do not re-parse (do not delete reports already created) if the workflows should not be generated
            if (Autosys2JS7Converter.CONFIG.getGenerateConfig().getWorkflows()) {
                // 1- cleanup, delete reports already created
                pr.reset();
                reset(reportDir);

                // 2- re-parse
                pr = Autosys2JS7Converter.parseInput(input, reportDir, pr.isXMLParser());
                analyze(pr, reportDir);
            }
        }

        // Reports
        writeJobReports(pr, reportDir);
        writeConditionsReports(reportDir);

        return pr;
    }

    private void analyze(DirectoryParserResult pr, Path reportDir) {
        setAllJobs(pr.getJobs());
        conditionAnalyzer.analyze(pr.getJobs());
    }

    private void setAllJobs(List<ACommonJob> jobs) {
        for (ACommonJob j : jobs) {
            allJobs.put(j.getName(), j);
            if (j.isBox()) {
                setAllJobs(((JobBOX) j).getJobs());
            }
        }
    }

    private void reset(Path reportDir) {
        conditionAnalyzer.init();
        allJobs = new LinkedHashMap<>();
        ParserReport.INSTANCE.clear();
        deleteAllReports(reportDir);
    }

    private void deleteAllReports(Path reportDir) {
        try {
            List<Path> l = SOSPath.getFileList(reportDir);
            for (Path p : l) {
                SOSPath.delete(p);
            }
        } catch (Throwable e) {
            LOGGER.error("[deleteAllReports][" + reportDir + "]" + e.toString(), e);
        }
    }

    private void mapJobsByType(Map<ConverterJobType, TreeSet<ACommonJob>> mapByType, Collection<ACommonJob> jobs) {
        for (ACommonJob job : jobs) {
            ConverterJobType key = job.getConverterJobType();
            TreeSet<ACommonJob> ct = mapByType.get(key);
            if (ct == null) {
                // Comparator<ACommonJob> comp = (o1, o2) -> o1.getName().hashCode() - o2.getName().hashCode();
                // Comparator<ACommonJob> comp = Comparator.comparing(ACommonJob::getName);
                ct = new TreeSet<>(Comparator.comparing(ACommonJob::getName, Collator.getInstance(Locale.ENGLISH)));
            }
            if (!ct.contains(job)) {
                ct.add(job);
            }
            mapByType.put(key, ct);
        }
    }

    private void writeJobReports(DirectoryParserResult pr, Path reportDir) {
        try {
            writeJobReportJobsByType(reportDir);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsByType]" + e.toString(), e);
        }

        try {
            writeJobReportJobsApplicationGroup(pr, reportDir);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsApplicationGroup]" + e.toString(), e);
        }
    }

    private void writeJobReportJobsApplicationGroup(DirectoryParserResult pr, Path reportDir) throws Exception {
        Path f = reportDir.resolve(Autosys2JS7Converter.REPORT_FILE_NAME_JOBS_BY_APPLICATION_GROUP);

        SOSPath.deleteIfExists(f);

        SOSPath.appendLine(f, "Jobs by application/group:                            Total    Standalone     BOX      BOX Children Jobs");

        Map<Path, TreeSet<ACommonJob>> map = new LinkedHashMap<>();
        for (ACommonJob j : pr.getJobs()) {
            Path key = j.getJobFullPathFromJILDefinition().getParent();
            key = key == null ? Paths.get("") : key;
            TreeSet<ACommonJob> jobs = map.get(key);
            if (jobs == null) {
                jobs = new TreeSet<>(Comparator.comparing(ACommonJob::getName, Collator.getInstance(Locale.ENGLISH)));
            }
            if (!jobs.contains(j)) {
                jobs.add(j);
            }
            map.put(key, jobs);
        }

        Set<Path> sortedPaths = new TreeSet<>(map.keySet());

        int totalTotal = 0;
        int totalStandalone = 0;
        int totalBoxes = 0;
        int totalBoxChildren = 0;
        for (Path key : sortedPaths) {
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

                String msg = String.format("%-50s %-10s  %-10s  %-10s %-10s", JS7ConverterHelper.normalizePath(key.toString()), localTotal,
                        localStandalone, localBoxes, localBoxChildren);
                SOSPath.appendLine(f, "    " + msg);

            } catch (Throwable e1) {

            }
        }
        SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DELIMETER_LINE);
        String msg = String.format("%-50s %-10s  %-10s  %-10s %-10s", "", totalTotal, totalStandalone, totalBoxes, totalBoxChildren);
        SOSPath.appendLine(f, "    " + msg);

        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DETAILS_LINE);
        SOSPath.appendLine(f, "Jobs by application/group:");
        map.keySet().stream().sorted().forEach(e -> {
            try {
                SOSPath.appendLine(f, "    " + JS7ConverterHelper.normalizePath(e.toString()));
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
                        String msg2 = String.format("%-80s %-50s", j.getName(), getDetails(j) + sb);
                        SOSPath.appendLine(f, "        " + msg2);

                        for (ACommonJob bj : jobs) {
                            msg2 = String.format("%-80s %-50s", bj.getName(), bj.getJobType().getValue());
                            SOSPath.appendLine(f, "            " + msg2);
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        if (j.hasCondition()) {
                            sb.append(", condition=").append(j.getCondition().getOriginalCondition());
                        }
                        String msg2 = String.format("%-80s %-50s", j.getName(), getDetails(j) + sb);
                        SOSPath.appendLine(f, "        " + msg2);
                    }

                }
                SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DELIMETER_LINE);

            } catch (Throwable e1) {

            }
        });

    }

    private void writeJobReportJobsByType(Path reportDir) throws Exception {
        Path f = reportDir.resolve(Autosys2JS7Converter.REPORT_FILE_NAME_JOBS_BY_TYPE);

        SOSPath.deleteIfExists(f);

        // SOSPath.appendLine(f, "Jobs by type:");
        SOSPath.appendLine(f, "Jobs by type:           Total    Standalone     BOX      BOX Children Jobs");

        Map<ConverterJobType, TreeSet<ACommonJob>> mapByType = new LinkedHashMap<>();
        mapJobsByType(mapByType, allJobs.values());

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

        SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DELIMETER_LINE);
        msg = String.format("%-19s %-10s  %-10s  %-10s %-10s", "", totalTotal, totalStandalone, "", totalBoxChildren);
        SOSPath.appendLine(f, "    " + msg);

        SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DETAILS_LINE);

        msg = String.format("%-75s %-50s", "Jobs by type:", "");
        SOSPath.appendLine(f, msg);

        for (ConverterJobType key : mapByType.keySet()) {
            SOSPath.appendLine(f, "    " + key);
            for (ACommonJob j : mapByType.get(key)) {
                msg = String.format("%-80s %-50s", j.getName(), getDetails(j));
                SOSPath.appendLine(f, "        " + msg);
            }
            SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DELIMETER_LINE);
        }
    }

    private String getDetails(ACommonJob j) {
        String d = "Standalone";
        if (j.isBox()) {
            d = "BOX";
        } else if (j.getBoxName() != null) {
            d = "BOX " + j.getBoxName();
        }
        return d;
    }

    private void writeConditionsReports(Path reportDir) {
        try {
            writeConditionsReportByType(reportDir);
        } catch (Throwable e) {
            LOGGER.error("[writeConditionsReportByType]" + e.toString(), e);
        }
        try {
            writeConditionsReportBoxSuccessFailure(reportDir);
        } catch (Throwable e) {
            LOGGER.error("[writeConditionsReportBoxSuccessFailure]" + e.toString(), e);
        }
    }

    // TODO JobBox - box_success, box_failure
    private void writeConditionsReportBoxSuccessFailure(Path reportDir) throws Exception {
        Path f = reportDir.resolve(Autosys2JS7Converter.REPORT_FILE_NAME_BOX_CONDITIONS_SUCCESS_FAILURE);
        SOSPath.deleteIfExists(f);

        List<JobBOX> boxes = allJobs.values().stream().filter(j -> j.isBox() && (((JobBOX) j).hasBoxSuccessOrBoxFailure())).map(j -> (JobBOX) j)
                .sorted((j1, j2) -> j1.getName().compareTo(j2.getName())).collect(Collectors.toList());

        if (boxes.size() == 0) {
            return;
        }

        for (JobBOX j : boxes) {
            SOSPath.appendLine(f, j.getName() + ":");
            if (j.getBoxSuccess().getValue() != null) {
                String msg = String.format("%-15s %s", j.getBoxSuccess().getName(), j.getBoxSuccess().getValue());
                SOSPath.appendLine(f, "    " + msg);
            }
            if (j.getBoxFailure().getValue() != null) {
                String msg = String.format("%-15s %s", j.getBoxFailure().getName(), j.getBoxFailure().getValue());
                SOSPath.appendLine(f, "    " + msg);
            }
            SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DELIMETER_LINE);
        }
    }

    private void writeConditionsReportByType(Path reportDir) throws Exception {
        Path f = reportDir.resolve(Autosys2JS7Converter.REPORT_FILE_NAME_CONDITIONS_BY_TYPE);

        SOSPath.deleteIfExists(f);

        SOSPath.appendLine(f, "Conditions by type:");
        String msg = "";
        for (ConditionType key : conditionAnalyzer.getAllConditionsByType().keySet()) {
            msg = String.format("%-20s %-20s", key, conditionAnalyzer.getAllConditionsByType().get(key).size());
            // SOSPath.appendLine(f, " " + key + " = " + conditionAnalyzer.getAllConditionsByType().get(key).size());
            SOSPath.appendLine(f, "    " + msg);
        }

        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DETAILS_LINE);

        msg = String.format("%-68s %-35s %s", "Conditions by type:", "Sorted by used by Job(s):", "Job(s):");
        SOSPath.appendLine(f, msg);

        // String msg = String.format("%-20s %-10s %-10s %-10s", e.getKey(), "Folders", "total=" + e.getValue(), "converted=" + con);
        for (ConditionType key : conditionAnalyzer.getAllConditionsByType().keySet()) {
            SOSPath.appendLine(f, "    " + key);

            Map<Condition, Integer> m = new LinkedHashMap<>();
            for (Condition c : conditionAnalyzer.getAllConditionsByType().get(key)) {
                m.put(c, Integer.valueOf(conditionAnalyzer.getInConditionJobs(c).size()));
            }
            Comparator<Integer> bySize = (Integer o1, Integer o2) -> o1.compareTo(o2);
            m.entrySet().stream().sorted(Map.Entry.<Condition, Integer> comparingByValue(bySize)).forEach(e -> {

                Set<String> jobs = conditionAnalyzer.getInConditionJobs(e.getKey());
                int countStandalone = 0;
                Set<String> boxes = new HashSet<>();
                for (String jn : jobs) {
                    ACommonJob j = allJobs.get(jn);
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

            SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DELIMETER_LINE);
        }
    }

    private boolean createDiagram(DirectoryParserResult pr, Path input, Path reportDir) throws Exception {
        boolean resultChanged = false;
        AutosysDiagramConfig diagramConfig = Autosys2JS7Converter.CONFIG.getAutosys().getInputConfig().getDiagramConfig();
        if (!diagramConfig.getGenerate()) {
            return resultChanged;
        }

        Path outputDir = getExportFoldersMainDir(analyzerDir);
        Map<ConverterJobType, List<ACommonJob>> jobsPerType = pr.getJobs().stream().collect(Collectors.groupingBy(ACommonJob::getConverterJobType,
                Collectors.toList()));

        createDiagram(Range.original, diagramConfig, jobsPerType, outputDir);
        if (diagramConfig.optimizeDependencies()) {
            createDiagram(Range.optimizeDependencies, diagramConfig, jobsPerType, outputDir);
            resultChanged = true;
        }

        if (diagramConfig.getGenerate() && diagramConfig.getGraphvizCleanupDotFiles()) {
            AGraphvizDiagramWriter.cleanupDotFiles(outputDir);
        }

        return resultChanged;
    }

    private void createDiagram(Range range, AutosysDiagramConfig diagramConfig, Map<ConverterJobType, List<ACommonJob>> jobsPerType, Path outputDir)
            throws Exception {

        String method = "createDiagram";
        LOGGER.info(String.format("[" + method + "][" + range + "]..."));

        int size = 0;
        boolean isRangeOriginal = Range.original.equals(range);
        for (Map.Entry<ConverterJobType, List<ACommonJob>> entry : jobsPerType.entrySet()) {
            ConverterJobType key = entry.getKey();
            List<ACommonJob> value = entry.getValue();
            size = value.size();
            switch (key) {
            case BOX:
                int b = 0;
                for (ACommonJob j : value) {
                    b++;
                    AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, (JobBOX) j);

                    if (b % 100 == 0) {
                        LOGGER.info(String.format("[createDiagram][" + range + "][boxJobs]generated %s of %s ...", b, size));
                    }
                }
                break;
            default: // Standalone
                int i = 0;
                // one file per application/group
                try {
                    AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, value);
                } catch (Throwable e) {
                    LOGGER.error("[createDiagram][" + range + "][standalone][application/group]" + e.toString(), e);
                }
                if (isRangeOriginal) {
                    for (ACommonJob j : value) {
                        i++;
                        // single files
                        try {
                            AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, j);
                        } catch (Throwable e) {
                            LOGGER.error("[createDiagram][" + range + "][standalone][" + j + "]" + e.toString(), e);
                        }
                        if (i % 100 == 0) {
                            LOGGER.info(String.format("[createDiagram][" + range + "][standaloneJob]generated %s of %s ...", i, size));
                        }
                    }
                }
                break;
            }
        }
        LOGGER.info(String.format("[" + method + "][" + range + "]end"));
    }

    public static Path getExportFoldersMainDir(Path analyzerDir) {
        return analyzerDir.resolve("export");
    }

    public static Path getAnalyzerDir(Path reportDir) {
        return reportDir.resolve(PATH_PREFIX + "original");
    }

    public ConditionAnalyzer getConditionAnalyzer() {
        return conditionAnalyzer;
    }

    public Map<String, ACommonJob> getAllJobs() {
        return allJobs;
    }

}
