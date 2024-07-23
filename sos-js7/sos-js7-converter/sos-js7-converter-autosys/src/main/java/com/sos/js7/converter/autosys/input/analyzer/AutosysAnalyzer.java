package com.sos.js7.converter.autosys.input.analyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.config.items.AutosysDiagramConfig;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.input.diagram.AutosysGraphvizDiagramWriter;
import com.sos.js7.converter.commons.input.diagram.AGraphvizDiagramWriter;

public class AutosysAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutosysAnalyzer.class);

    public static final String APP_FOLDER_SEPARATOR = " - ";// <APP> - <FOLDER>
    public static final String PATH_PREFIX = "autosys.input.";

    private Path analyzerDir;
    private ConditionAnalyzer conditionAnalyzer;
    private Map<String, ACommonJob> allJobs;

    private void init(Path reportDir) {
        conditionAnalyzer = new ConditionAnalyzer(reportDir);
        allJobs = new HashMap<>();
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

    private void analyzeJobs(DirectoryParserResult pr, Path reportDir, boolean afterCleanup) {
        for (ACommonJob j : pr.getJobs()) {
            allJobs.put(j.getName(), j);
        }
        conditionAnalyzer.analyze(pr.getJobs());

        Path f = reportDir.resolve("conditions.txt");
        try {
            writeConditionsToFile(f);
        } catch (Throwable e) {
            LOGGER.error("[writeConditionsToFile][" + f + "]" + e.toString(), e);
        }
    }

    private void writeConditionsToFile(Path f) throws Exception {
        SOSPath.deleteIfExists(f);

        SOSPath.appendLine(f, "Conditions by type:");
        for (ConditionType key : conditionAnalyzer.getAllConditionsByType().keySet()) {
            SOSPath.appendLine(f, "  " + key + " = " + conditionAnalyzer.getAllConditionsByType().get(key).size());
        }

        SOSPath.appendLine(f, "");
        SOSPath.appendLine(f, "---- DETAILS ------------------------------------------------------------------");

        SOSPath.appendLine(f, "Conditions by type:");
        for (ConditionType key : conditionAnalyzer.getAllConditionsByType().keySet()) {
            SOSPath.appendLine(f, "  " + key + ":");

            for (String j : conditionAnalyzer.getAllConditionsByType().get(key)) {
                SOSPath.appendLine(f, "    " + j);
            }
        }
    }

    private void createDiagram(DirectoryParserResult pr, AutosysDiagramConfig diagramConfig, Path reportDir, Path analyzerDir, String range)
            throws Exception {
        if (diagramConfig.getGenerate()) {
            String method = "createDiagram";
            LOGGER.info(String.format("[" + method + "] ..."));
            Path outputDir = getExportFoldersMainDir(analyzerDir);

            //String add = diagramConfig.excludeStandalone() ? ".excludeStandalone" : "";
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
                            if (e instanceof StackOverflowError) {
                                Path r = reportDir.resolve("BOX[children_jobs]recursion.txt");
                                SOSPath.appendLine(r, j.getName());
                                LOGGER.error("[createDiagram][BOX][" + j.getName() + "][optimizeBoxDependencies]StackOverflowError...");
                            } else {
                                LOGGER.error("[createDiagram][BOX][" + j.getName() + "][optimizeBoxDependencies]" + e.toString(), e);
                            }
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

    public static String getAppAndFolder(String app, ACommonJob j) {
        return "";
        // return app + APP_FOLDER_SEPARATOR + j.getFullName();
    }

    public ConditionAnalyzer getConditionAnalyzer() {
        return conditionAnalyzer;
    }

    public Map<String, ACommonJob> getAllJobs() {
        return allJobs;
    }

}
