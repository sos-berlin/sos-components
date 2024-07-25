package com.sos.js7.converter.autosys.input.analyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.commons.input.diagram.AGraphvizDiagramWriter;

public class AutosysAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutosysAnalyzer.class);

    public static final String PATH_PREFIX = "autosys.input.";

    private Path analyzerDir;
    private ConditionAnalyzer conditionAnalyzer;
    private Map<String, ACommonJob> allJobs;

    private void init(Path reportDir) {
        conditionAnalyzer = new ConditionAnalyzer(reportDir);
        allJobs = new LinkedHashMap<>();
    }

    public void analyzeAndCreateDiagramm(DirectoryParserResult pr, AutosysDiagramConfig diagramConfig, Path reportDir, boolean afterCleanup)
            throws Exception {
        init(reportDir);

        analyzerDir = getAnalyzerDir(reportDir, afterCleanup);
        if (!Files.exists(analyzerDir)) {
            analyzerDir.toAbsolutePath().toFile().mkdirs();
        }

        analyzeJobs(pr, reportDir, afterCleanup);

        String range = getRange(afterCleanup);

        createDiagram(pr, diagramConfig, reportDir, analyzerDir, range);
    }

    private void setAllJobs(List<ACommonJob> jobs) {
        for (ACommonJob j : jobs) {
            allJobs.put(j.getName(), j);
            if (j instanceof JobBOX) {
                setAllJobs(((JobBOX) j).getJobs());
            }
        }
    }

    private void analyzeJobs(DirectoryParserResult pr, Path reportDir, boolean afterCleanup) {
        setAllJobs(pr.getJobs());

        // Jobs reports
        writeJobReports(reportDir);

        // Conditions reports
        conditionAnalyzer.analyze(pr.getJobs());
        writeConditionsReports(reportDir);
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

    private void writeJobReports(Path reportDir) {
        try {
            writeJobReportJobsByType(reportDir);
        } catch (Throwable e) {
            LOGGER.error("[writeJobReportJobsByType]" + e.toString(), e);
        }
    }

    private void writeJobReportJobsByType(Path reportDir) throws Exception {
        Path f = reportDir.resolve(Autosys2JS7Converter.REPORT_FILE_NAME_JOBS_BY_TYPE);

        SOSPath.deleteIfExists(f);

        SOSPath.appendLine(f, "Jobs by type:");

        Map<ConverterJobType, TreeSet<ACommonJob>> mapByType = new LinkedHashMap<>();
        mapJobsByType(mapByType, allJobs.values());

        String msg = "";
        for (ConverterJobType key : mapByType.keySet()) {
            String d = "";
            if (!ConverterJobType.BOX.equals(key)) {
                int standaloneJobs = 0;
                int boxJobs = 0;
                for (ACommonJob j : mapByType.get(key)) {
                    if (j.isStandalone()) {
                        standaloneJobs++;
                    } else {
                        boxJobs++;
                    }
                }
                d = "Standalone jobs=" + standaloneJobs + ", BOX jobs=" + boxJobs;
            }
            msg = String.format("%-20s %-20s  %-50s", key, mapByType.get(key).size(), d);
            SOSPath.appendLine(f, "    " + msg);
        }

        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DETAILS_LINE);

        msg = String.format("%-75s %-50s", "Jobs by type:", "");
        SOSPath.appendLine(f, msg);

        for (ConverterJobType key : mapByType.keySet()) {
            SOSPath.appendLine(f, "    " + key);
            for (ACommonJob j : mapByType.get(key)) {
                String d = "Standalone";
                if (j instanceof JobBOX) {
                    d = "BOX";
                } else if (j.getBox().getBoxName() != null && j.getBox().getBoxName().getValue() != null) {
                    d = "BOX " + j.getBox().getBoxName().getValue();
                }
                msg = String.format("%-80s %-50s", j.getName(), d);
                SOSPath.appendLine(f, "        " + msg);
            }
            SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DELIMETER_LINE);
        }
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

        msg = String.format("%-75s %-50s", "Conditions by type:", "Sorted by Used by job(s):");
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
                String msg2 = String.format("%-80s %-50s", e.getKey().getOriginalValue(), e.getValue());
                try {
                    SOSPath.appendLine(f, "        " + msg2);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });

            SOSPath.appendLine(f, Autosys2JS7Converter.REPORT_DELIMETER_LINE);
        }
    }

    private void createDiagram(DirectoryParserResult pr, AutosysDiagramConfig diagramConfig, Path reportDir, Path analyzerDir, String range)
            throws Exception {
        if (diagramConfig.getGenerate()) {
            String method = "createDiagram";
            LOGGER.info(String.format("[" + method + "] ..."));
            Path outputDir = getExportFoldersMainDir(analyzerDir);

            // String add = diagramConfig.excludeStandalone() ? ".excludeStandalone" : "";
            // outputDir = outputDir.resolve("diagram" + add);

            Map<ConverterJobType, List<ACommonJob>> jobsPerType = pr.getJobs().stream().collect(Collectors.groupingBy(ACommonJob::getConverterJobType,
                    Collectors.toList()));
            int size = 0;
            for (Map.Entry<ConverterJobType, List<ACommonJob>> entry : jobsPerType.entrySet()) {
                ConverterJobType key = entry.getKey();
                List<ACommonJob> value = entry.getValue();
                size = value.size();
                switch (key) {
                case BOX:
                    break;
                default:
                    int i = 0;
                    // one file per application/group
                    try {
                        AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, value);
                    } catch (Throwable e) {
                        LOGGER.error("[createDiagram][standalone][application/group]" + e.toString(), e);
                    }
                    for (ACommonJob j : value) {
                        i++;
                        // single files
                        try {
                            AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, j);
                        } catch (Throwable e) {
                            LOGGER.error("[createDiagram][standalone][" + j + "]" + e.toString(), e);
                        }
                        if (i % 100 == 0) {
                            LOGGER.info(String.format("[createDiagram][standaloneJob]generated %s of %s ...", i, size));
                        }
                    }
                    break;
                }
            }

            if (pr.getBoxJobsCopy().size() > 0) {
                int i = 0;
                for (JobBOX j : pr.getBoxJobsCopy()) {
                    i++;
                    try {
                        AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, j, false);
                    } catch (Throwable e) {
                        LOGGER.error("[createDiagram][BOX][" + j.getName() + "]" + e.toString(), e);
                    }
                    if (diagramConfig.optimizeBoxDependencies()) {
                        // i++;
                        try {
                            AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, j, true);
                        } catch (Throwable e) {
                            LOGGER.error("[createDiagram][BOX][" + j.getName() + "][optimizeBoxDependencies]" + e.toString(), e);
                        }
                    }
                    if (i % 100 == 0) {
                        LOGGER.info(String.format("[createDiagram][boxJobs]generated %s of %s ...", i, size));
                    }
                }
            }

            if (diagramConfig.getGenerate() && diagramConfig.getGraphvizCleanupDotFiles()) {
                AGraphvizDiagramWriter.cleanupDotFiles(outputDir);
            }
        }
    }

    public static String getRange(boolean afterCleanup) {
        return afterCleanup ? "afterCleanup" : "original";
    }

    public static Path getExportFoldersMainDir(Path reportDir, boolean afterCleanup) {
        return getExportFoldersMainDir(getAnalyzerDir(reportDir, afterCleanup));
    }

    public static Path getExportFoldersMainDir(Path analyzerDir) {
        return analyzerDir.resolve("export");
    }

    public static Path getAnalyzerDir(Path reportDir, boolean afterCleanup) {
        return reportDir.resolve(PATH_PREFIX + getRange(afterCleanup));
    }

    public ConditionAnalyzer getConditionAnalyzer() {
        return conditionAnalyzer;
    }

    public Map<String, ACommonJob> getAllJobs() {
        return allJobs;
    }

}
