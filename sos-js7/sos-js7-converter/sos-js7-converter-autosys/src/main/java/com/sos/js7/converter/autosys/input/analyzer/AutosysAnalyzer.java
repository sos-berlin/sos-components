package com.sos.js7.converter.autosys.input.analyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobCondition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.config.items.AutosysDiagramConfig;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.input.diagram.AutosysGraphvizDiagramWriter;
import com.sos.js7.converter.autosys.input.diagram.AutosysGraphvizDiagramWriter.Range;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.helper.Report;
import com.sos.js7.converter.commons.input.diagram.AGraphvizDiagramWriter;
import com.sos.js7.converter.commons.report.ParserReport;

public class AutosysAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutosysAnalyzer.class);

    public static final String PATH_PREFIX = "autosys.input.";

    private Path reportDir;
    private Path analyzerDir;
    private ConditionAnalyzer conditionAnalyzer;
    private Map<String, ACommonJob> allJobs;

    private void init(Path reportDir) {
        this.reportDir = reportDir;
        conditionAnalyzer = new ConditionAnalyzer(reportDir);
        allJobs = new LinkedHashMap<>();
        analyzerDir = getAnalyzerDir(reportDir);
    }

    private void reset() {
        resetConditionAnalyzer();
        allJobs = new LinkedHashMap<>();
    }

    private void resetConditionAnalyzer() {
        conditionAnalyzer.init();
    }

    public DirectoryParserResult analyzeAndCreateDiagram(DirectoryParserResult pr, Path input, Path reportDir, Path references) throws Exception {
        init(reportDir);
        analyze(pr, reportDir);

        // if the conditions have been optimized, the input files must be parsed again.
        if (createDiagram(pr, input, reportDir, references)) {
            // do not re-parse (do not delete reports already created) if the workflows should not be generated
            if (Autosys2JS7Converter.CONFIG.getGenerateConfig().getWorkflows()) {
                // 1 - reinitialize properties
                // pr.reset();
                // reset();

                // 2 - delete reports already created
                // deleteAllReports(reportDir);

                // 3 - cleanup parser report before re-parse
                // ParserReport.INSTANCE.clear();

                // 4 - re-parse
                // pr = Autosys2JS7Converter.parseInput(input, references, reportDir);
                // analyze(pr, reportDir);

                pr = doReset(pr, input, references);
            }
        }

        // Reports
        Report.writeParserReports(pr, reportDir, this);

        adjustConditions(pr);
        return pr;
    }

    private DirectoryParserResult doReset(DirectoryParserResult pr, Path input, Path references) throws Exception {
        // 1 - reinitialize properties
        // pr.reset();
        // reset();

        // 2 - delete reports already created
        deleteAllReports(reportDir);

        // 3 - cleanup parser report before re-parse
        ParserReport.INSTANCE.clear();

        // 4 - re-parse
        if (pr.isXMLParser() && pr.getInput() != null && Files.exists(pr.getInput())) {
            pr = Autosys2JS7Converter.parseInput(pr.getInput(), references, reportDir);
        } else {
            pr = Autosys2JS7Converter.parseInput(input, references, reportDir);
        }

        init(reportDir);
        analyze(pr, reportDir);
        return pr;
    }

    private void analyze(DirectoryParserResult pr, Path reportDir) {
        Condition.adjustLookBackForJS7 = false;
        setAllJobs(pr.getJobs());
        conditionAnalyzer.analyze(pr.getJobs(), false);
    }

    private void adjustConditions(DirectoryParserResult pr) {
        Condition.adjustLookBackForJS7 = true;

        allJobs.entrySet().stream().forEach(e -> {
            try {
                e.getValue().getCondition().reread();
            } catch (Exception e1) {
                CommonJobCondition c = e.getValue().getCondition();
                String add = c == null ? "condition is null" : c.getOriginalCondition();
                LOGGER.error("[adjustConditions][reread=" + add + "][job=" + e.getValue().getName() + "][exception]" + e1);
            }
        });

        resetConditionAnalyzer();
        conditionAnalyzer.analyze(pr.getJobs(), true);
    }

    private void setAllJobs(List<ACommonJob> jobs) {
        for (ACommonJob j : jobs) {
            allJobs.put(j.getName(), j);
            if (j.isBox()) {
                setAllJobs(((JobBOX) j).getJobs());
            }
        }
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

    private boolean createDiagram(DirectoryParserResult pr, Path input, Path reportDir, Path references) throws Exception {
        boolean resultChanged = false;
        AutosysDiagramConfig diagramConfig = Autosys2JS7Converter.CONFIG.getAutosys().getInputConfig().getDiagramConfig();
        if (!diagramConfig.getGenerate()) {
            return resultChanged;
        }

        Path outputDir = getMainDirDiagram(analyzerDir);
        Map<ConverterJobType, List<ACommonJob>> jobsPerType = pr.getJobs().stream().collect(Collectors.groupingBy(ACommonJob::getConverterJobType,
                Collectors.toList()));

        createDiagram(Range.original, diagramConfig, jobsPerType, outputDir);
        if (diagramConfig.optimizeDependencies()) {
            pr = doReset(pr, input, references);
            // adjustConditions(pr);

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
        Path rangeOutputDir = outputDir.resolve(range.toString());
        // boolean isRangeOriginal = Range.original.equals(range);
        for (Map.Entry<ConverterJobType, List<ACommonJob>> entry : jobsPerType.entrySet()) {
            ConverterJobType key = entry.getKey();
            List<ACommonJob> value = entry.getValue();
            size = value.size();
            switch (key) {
            case BOX:
                int b = 0;

                if (Range.optimizeDependencies.equals(range)) {
                    if (Autosys2JS7Converter.OPTIMIZE_BOX_JOBS_CONDITIONS) {
                        conditionAnalyzer.handleBOXConditions(this, entry.getValue());
                    }
                }

                for (ACommonJob j : value) {
                    b++;
                    AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, rangeOutputDir, (JobBOX) j);

                    if (b % 100 == 0) {
                        LOGGER.info(String.format("[createDiagram][" + range + "][boxJobs]generated %s of %s ...", b, size));
                    }
                }
                break;
            default: // Standalone
                int i = 0;

                if (!Autosys2JS7Converter.HAS_REFERENCES) {
                    // one file per application/group
                    try {
                        AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, rangeOutputDir, value);
                    } catch (Throwable e) {
                        LOGGER.error("[createDiagram][" + range + "][standalone][application/group]" + e.toString(), e);
                    }
                }
                // if (isRangeOriginal) {
                for (ACommonJob j : value) {
                    i++;
                    // single files
                    try {
                        AutosysGraphvizDiagramWriter.createDiagram(diagramConfig, this, range, rangeOutputDir, j);
                    } catch (Throwable e) {
                        LOGGER.error("[createDiagram][" + range + "][standalone][" + j + "]" + e.toString(), e);
                    }
                    if (i % 100 == 0) {
                        LOGGER.info(String.format("[createDiagram][" + range + "][standaloneJob]generated %s of %s ...", i, size));
                    }
                }
                // }
                break;
            }
        }
        LOGGER.info(String.format("[" + method + "][" + range + "]end"));
    }

    public static Path getMainDirDiagram(Path analyzerDir) {
        Path p = analyzerDir.resolve("diagram");
        if (!Files.exists(p)) {
            p.toAbsolutePath().toFile().mkdirs();
        }
        return p;
    }

    public static Path getMainDirSplitConfiguration(Path analyzerDir) {
        Path p = analyzerDir.resolve("config");
        if (!Files.exists(p)) {
            p.toAbsolutePath().toFile().mkdirs();
        }
        return p;
    }

    public static Path getAnalyzerDir(Path reportDir) {
        return reportDir.resolve(PATH_PREFIX + "original");
    }

    public Path getReportDir() {
        return reportDir;
    }

    public ConditionAnalyzer getConditionAnalyzer() {
        return conditionAnalyzer;
    }

    public Map<String, ACommonJob> getAllJobs() {
        return allJobs;
    }

    public Map<String, ACommonJob> getNonReferenceJobs() {
        return allJobs.entrySet().stream().filter(e -> !e.getValue().isReference()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
