package com.sos.js7.converter.autosys.input.analyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.input.diagram.AutosysGraphvizDiagramWriter;
import com.sos.js7.converter.commons.config.items.DiagramConfig;
import com.sos.js7.converter.commons.input.diagram.AGraphvizDiagramWriter;

public class AutosysAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutosysAnalyzer.class);

    public static final String APP_FOLDER_SEPARATOR = " - ";// <APP> - <FOLDER>

    public static final String PATH_PREFIX = "autosys.input.";

    private Path analyzerDir;

    public void analyze(DirectoryParserResult pr, DiagramConfig diagramConfig, Path reportDir, boolean afterCleanup) {
        analyzerDir = getAnalyzerDir(reportDir, afterCleanup);
        if (!Files.exists(analyzerDir)) {
            analyzerDir.toAbsolutePath().toFile().mkdirs();
        }

        String range = getRange(afterCleanup);

        createDiagram(pr, diagramConfig, analyzerDir, range);
    }

    private void createDiagram(DirectoryParserResult pr, DiagramConfig diagramConfig, Path analyzerDir, String range) {
        if (diagramConfig.getGenerate()) {
            String method = "createDiagram";
            LOGGER.info(String.format("[" + method + "] ..."));
            Path outputDir = getExportFoldersMainDir(analyzerDir);

            String add = diagramConfig.excludeStandalone() ? ".excludeStandalone" : "";
            //outputDir = outputDir.resolve("diagram" + add);

            List<ACommonJob> boxJobs = new ArrayList<>();
            Map<ConverterJobType, List<ACommonJob>> jobsPerType = pr.getJobs().stream().collect(Collectors.groupingBy(ACommonJob::getConverterJobType,
                    Collectors.toList()));
            int size = 0;
            for (Map.Entry<ConverterJobType, List<ACommonJob>> entry : jobsPerType.entrySet()) {
                ConverterJobType key = entry.getKey();
                List<ACommonJob> value = entry.getValue();
                size = value.size();
                switch (key) {
                case BOX:
                    boxJobs.addAll(value);
                    break;
                default:
                    int i = 0;
                    for (ACommonJob j : value) {
                        i++;
                        AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, j);
                        if (i % 100 == 0) {
                            LOGGER.info(String.format("[createDiagram][standaloneJob]generated %s of %s ...", i, size));
                        }
                    }
                    break;
                }
            }

            size = boxJobs.size();
            if (size > 0) {
                int i = 0;
                for (ACommonJob j : boxJobs) {
                    i++;
                    AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, outputDir, (JobBOX) j, null);
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

    public static boolean isStandalone(ACommonJob j) {
        return j.getBox() == null;
    }

    public static String getAppAndFolder(String app, ACommonJob j) {
        return app + APP_FOLDER_SEPARATOR + j.getFullName();
    }

}
