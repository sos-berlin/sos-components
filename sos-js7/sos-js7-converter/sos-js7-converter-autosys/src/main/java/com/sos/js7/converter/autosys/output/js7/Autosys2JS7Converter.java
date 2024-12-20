package com.sos.js7.converter.autosys.output.js7;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobReturnCode;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.ACommonMachineJob;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.JobFW;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.input.AFileParser;
import com.sos.js7.converter.autosys.input.DirectoryParser;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.input.JILJobParser;
import com.sos.js7.converter.autosys.input.XMLJobParser;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.input.analyzer.ConditionAnalyzer.OutConditionHolder;
import com.sos.js7.converter.autosys.output.js7.helper.AdditionalInstructionsHelper;
import com.sos.js7.converter.autosys.output.js7.helper.BoardHelper;
import com.sos.js7.converter.autosys.output.js7.helper.ConverterBOXJobs;
import com.sos.js7.converter.autosys.output.js7.helper.ConverterStandaloneJobs;
import com.sos.js7.converter.autosys.output.js7.helper.LockHelper;
import com.sos.js7.converter.autosys.output.js7.helper.PathResolver;
import com.sos.js7.converter.autosys.output.js7.helper.Report;
import com.sos.js7.converter.autosys.output.js7.helper.RetryHelper;
import com.sos.js7.converter.autosys.output.js7.helper.RunTimeHelper;
import com.sos.js7.converter.autosys.output.js7.helper.bean.Job2Condition;
import com.sos.js7.converter.autosys.output.js7.helper.bean.Resource2Lock;
import com.sos.js7.converter.autosys.output.js7.helper.fork.BOXJobsHelper;
import com.sos.js7.converter.autosys.report.AutosysReport;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.JS7ExportObjects;
import com.sos.js7.converter.commons.agent.JS7AgentConverter;
import com.sos.js7.converter.commons.agent.JS7AgentConverter.JS7AgentConvertType;
import com.sos.js7.converter.commons.agent.JS7AgentHelper;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.config.json.JS7Agent;
import com.sos.js7.converter.commons.output.OutputWriter;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.commons.report.ConverterReportWriter;

/** <br/>
 * TODO Locks<br/>
 * TODO Conditions with operators AND/OR and condition GROUPS<br/>
 * TODO Conditions with lookBack<br/>
 * TODO Box Jobs, Box Child jobs with own run-time<br/>
 * --- all variants<br/>
 * --- admission times<br/>
 * --- post/expected notices - box and box job<br/>
 * , TODO Report<br/>
 */
public class Autosys2JS7Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Autosys2JS7Converter.class);

    public static final boolean NOT_CREATE_NOTICES_IF_JOB_NOT_FOUND = true;

    public static AutosysConverterConfig CONFIG = new AutosysConverterConfig();

    private AutosysAnalyzer analyzer;

    private Map<String, JS7Agent> machine2js7Agent = new HashMap<>();

    public static DirectoryParserResult parseInput(Path input, Path reportDir, boolean isXMLParser) {
        AFileParser parser = isXMLParser ? new XMLJobParser(Autosys2JS7Converter.CONFIG, reportDir) : new JILJobParser(Autosys2JS7Converter.CONFIG,
                reportDir);
        LOGGER.info("[" + input + "][isXMLParser=" + isXMLParser + "]" + parser.getClass().getName());
        return DirectoryParser.parse(CONFIG.getParserConfig(), parser, input);
    }

    private static boolean isXMLInputFiles(Path input) throws IOException {
        boolean r = false;
        if (Files.isDirectory(input)) {
            List<Path> l = SOSPath.getFileList(input, ".*\\.xml$", java.util.regex.Pattern.CASE_INSENSITIVE);
            r = l != null && l.size() > 0;
        } else {
            r = input.getFileName().toString().toLowerCase().endsWith("xml");
        }
        return r;
    }

    public static void convert(Path input, Path outputDir, Path reportDir) throws Exception {

        JS7ConverterHelper.setBoardsConfig(CONFIG);

        String method = "convert";

        // APP start
        Instant appStart = Instant.now();
        LOGGER.info(String.format("[%s][start]...", method));

        boolean isXMLInputFiles = isXMLInputFiles(input);

        OutputWriter.prepareDirectory(outputDir);
        OutputWriter.prepareDirectory(reportDir);

        // 1 - Parse Autosys files
        LOGGER.info(String.format("[%s][parse][start]...", method));
        DirectoryParserResult pr = parseInput(input, reportDir, isXMLInputFiles);

        // 2- Analyze and create Diagram
        AutosysAnalyzer analyzer = new AutosysAnalyzer();
        pr = analyzer.analyzeAndCreateDiagram(pr, input, reportDir);

        LOGGER.info(String.format("[%s][parse][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));

        // 3 - Config Report - after 2 because the reporting files maybe deleted by analyzer.analyzeAndCreateDiagram
        ConverterReportWriter.writeConfigReport(reportDir.resolve("config_errors.csv"), reportDir.resolve("config_warnings.csv"), reportDir.resolve(
                "config_analyzer.csv"));

        // 4 - Parser Reports
        ConverterReportWriter.writeParserReport("Autosys", reportDir.resolve("parser_summary.csv"), reportDir.resolve("parser_errors.csv"), reportDir
                .resolve("parser_warnings.csv"), reportDir.resolve("parser_analyzer.csv"));

        // 5 - Convert to JS7
        Instant start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][convert][start]...", method));

        ConverterResult cr = convert(reportDir, pr, analyzer);
        JS7ConverterResult result = cr.getResult();
        LOGGER.info(String.format("[%s][JS7][convert][end]%s", method, SOSDate.getDuration(start, Instant.now())));
        // 5.1 - Converter Reports
        AutosysReport.analyze(cr.getStandaloneJobs(), cr.getBoxJobs());

        ConverterReportWriter.writeConverterReport(reportDir.resolve("converter_errors.csv"), reportDir.resolve("converter_warnings.csv"), reportDir
                .resolve("converter_analyzer.csv"));

        // 6 - Write JS7 files
        start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][write][start]...", method));
        if (CONFIG.getGenerateConfig().getPseudoWorkflows()) {
            LOGGER.info(String.format("[%s][JS7][write][pseudoWorkflows]...", method));
            Path p = reportDir.resolve("pseudo-workflows");
            OutputWriter.write(p, result.getPseudoWorkflows());
        }

        if (CONFIG.getGenerateConfig().getWorkflows()) {
            LOGGER.info(String.format("[%s][JS7][write][workflows]...", method));
            OutputWriter.write(outputDir, result.getWorkflows());
            ConverterReport.INSTANCE.addSummaryRecord("Workflows", result.getWorkflows().getItems().size());
        }

        if (CONFIG.getGenerateConfig().getAgents()) {
            LOGGER.info(String.format("[%s][JS7][write][Agents]...", method));
            OutputWriter.write(outputDir, result.getAgents());
            long total = result.getAgents().getItems().size();
            long standalone = result.getAgents().getItems().stream().filter(a -> a.getObject().getStandaloneAgent() != null).count();
            ConverterReport.INSTANCE.addSummaryRecord("Agents", total + ", STANDALONE=" + standalone + ", CLUSTER=" + (total - standalone));
        }
        if (CONFIG.getGenerateConfig().getCalendars()) {
            LOGGER.info(String.format("[%s][JS7][write][calendars]...", method));
            OutputWriter.write(outputDir, result.getCalendars());
            ConverterReport.INSTANCE.addSummaryRecord("Calendars", result.getCalendars().getItems().size());
        }
        if (CONFIG.getGenerateConfig().getSchedules()) {
            LOGGER.info(String.format("[%s][JS7][write][schedules]...", method));
            OutputWriter.write(outputDir, result.getSchedules());
            ConverterReport.INSTANCE.addSummaryRecord("Schedules", result.getSchedules().getItems().size());
        }

        if (result.getBoards().getItems().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][write][boards]...", method));
            OutputWriter.write(outputDir, result.getBoards());
            ConverterReport.INSTANCE.addSummaryRecord("Boards", result.getBoards().getItems().size());
        }

        if (result.getLocks().getItems().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][write][locks]...", method));
            OutputWriter.write(outputDir, result.getLocks());
            ConverterReport.INSTANCE.addSummaryRecord("Locks", result.getLocks().getItems().size());
        }

        // TODO all with write(...
        write(outputDir, "FileOrderSources", result.getFileOrderSources(), true, null);

        // 6.1 - Summary Report
        ConverterReportWriter.writeSummaryReport(reportDir.resolve("converter_summary.csv"));

        LOGGER.info(String.format("[%s][[JS7]write][end]%s", method, SOSDate.getDuration(start, Instant.now())));

        // APP end
        LOGGER.info(String.format("[%s][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));
    }

    private static <T> void write(Path outputDir, String title, JS7ExportObjects<T> exportObject, boolean doWrite, String configPropertyName) {
        String logPrefix = String.format("[convert][JS7][write][%s]", title);
        int size = exportObject.getItems().size();
        try {
            if (doWrite) {
                if (size > 0) {
                    LOGGER.info(logPrefix + size + " items ...");
                    OutputWriter.write(outputDir, exportObject);
                } else {
                    LOGGER.info(logPrefix + "[skip]0 items");
                }
            } else {
                if (configPropertyName != null) {
                    LOGGER.info(logPrefix + "[skip]" + configPropertyName + "=false");
                }
            }
        } catch (Throwable e) {
            LOGGER.error(logPrefix + e.toString(), e);
            ConverterReport.INSTANCE.addErrorRecord(null, logPrefix, e);
        } finally {
            ConverterReport.INSTANCE.addSummaryRecord(title, size);
        }
    }

    private static ConverterResult convert(Path reportDir, DirectoryParserResult pr, AutosysAnalyzer analyzer) {
        // ----------------------------
        BoardHelper.clear();
        RunTimeHelper.clear();
        LockHelper.clear();
        BOXJobsHelper.clear();
        ConverterBOXJobs.clear();
        AdditionalInstructionsHelper.clear();
        // -------------------------
        String method = "convert";

        Autosys2JS7Converter c = new Autosys2JS7Converter();
        c.analyzer = analyzer;
        JS7ConverterResult result = new JS7ConverterResult();
        result.getApplications().addAll(pr.getJobs().stream().map(e -> e.getFolder().getApplication().getValue()).filter(Objects::nonNull).distinct()
                .collect(Collectors.toSet()));

        List<ACommonJob> standaloneJobs = new ArrayList<>();
        List<ACommonJob> boxJobs = new ArrayList<>();
        Map<ConverterJobType, List<ACommonJob>> jobsPerType = pr.getJobs().stream().collect(Collectors.groupingBy(ACommonJob::getConverterJobType,
                Collectors.toList()));

        boolean optimize = true;
        if (optimize) {
            for (Map.Entry<ConverterJobType, List<ACommonJob>> entry : jobsPerType.entrySet()) {
                ConverterJobType key = entry.getKey();
                switch (key) {
                case BOX:
                    try {
                        analyzer.getConditionAnalyzer().handleBOXConditions(analyzer, entry.getValue());
                    } catch (Throwable e1) {
                        LOGGER.error("[BOX]" + e1.toString(), e1);
                    }
                    break;
                default:
                    boolean optimizeStandalone = false;
                    if (optimizeStandalone) {
                        try {
                            analyzer.getConditionAnalyzer().handleStandaloneJobsConditions(entry.getValue());
                        } catch (Throwable e1) {
                            LOGGER.error("[standalone]" + e1.toString(), e1);
                        }
                    }
                    break;
                }
            }
        }

        int size = 0;
        for (Map.Entry<ConverterJobType, List<ACommonJob>> entry : jobsPerType.entrySet()) {
            ConverterJobType key = entry.getKey();
            List<ACommonJob> value = entry.getValue();
            size = value.size();

            switch (key) {
            case CMD:
                standaloneJobs.addAll(value);

                ConverterStandaloneJobs converterStandaloneJobs = new ConverterStandaloneJobs(c, result);
                converterStandaloneJobs.convert(standaloneJobs, key);
                break;
            case FW:
                for (ACommonJob j : value) {
                    c.convertStandaloneFW(result, (JobFW) j);
                }
                break;
            case BOX:
                boxJobs.addAll(value);

                ConverterBOXJobs converterBOXJobs = new ConverterBOXJobs(c, result);
                converterBOXJobs.convert(boxJobs);
                break;
            default:
                LOGGER.info(String.format("[%s][%s jobs=%s]not implemented yet", method, key, size));
                for (ACommonJob j : value) {
                    ConverterReport.INSTANCE.addAnalyzerRecord(j.getSource(), j.getName(), j.getJobType().getValue() + ":not implemented yet");
                }
                break;
            }
        }

        // postProcessing(result);
        c.convertBoards(result);
        c.convertAgents(result, reportDir);
        c.convertLocks(result);
        c.convertCalendars(result);

        Report.moveJILReportFiles(reportDir);
        Report.writeJS7Reports(pr, reportDir, analyzer);

        // AutosysReport.analyze(standaloneJobs, boxJobs);
        // return result;
        return new ConverterResult(result, standaloneJobs, boxJobs);
    }

    // generates a dummy workflow that creates a post-notice and a file order source
    // - this allows multiple other workflows to be triggered from a single file job source
    private WorkflowResult convertStandaloneFW(JS7ConverterResult result, JobFW jilJob) {
        try {
            OutConditionHolder h = analyzer.getConditionAnalyzer().getJobOUTConditions(jilJob);
            if (h == null) {
                LOGGER.info("[FW][" + jilJob.getName() + "]IGNORED BECAUSE NOT USED");
                ConverterReport.INSTANCE.addAnalyzerRecord(jilJob.getSource(), jilJob.getName(), jilJob.getJobType().getValue()
                        + "[standalone FW]IGNORED BECAUSE NOT USED");
                return null;
            }

            Map<String, Job2Condition> jobsConditions = new HashMap<>();
            h.getJobConditions().values().forEach(e -> {
                e.values().forEach(c -> {
                    String key = c.getKey();
                    if (!jobsConditions.containsKey(key)) {
                        jobsConditions.put(key, new Job2Condition(null, c));
                    }
                });
            });
            if (jobsConditions.size() == 0) {
                LOGGER.info("[FW][" + jilJob.getName() + "]IGNORED BECAUSE NO JOBS FOUND");
                ConverterReport.INSTANCE.addAnalyzerRecord(jilJob.getSource(), jilJob.getName(), jilJob.getJobType().getValue()
                        + "[standalone FW]IGNORED BECAUSE NO JOBS FOUND");
                return null;
            }

            String runTimeTimezone = jilJob.getRunTime().getTimezone().getValue();
            // WORKFLOW only with a PostNotice
            Workflow w = new Workflow();
            w.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle(jilJob.getDescription().getValue()));
            w.setTimeZone(runTimeTimezone == null ? Autosys2JS7Converter.CONFIG.getWorkflowConfig().getDefaultTimeZone() : runTimeTimezone);

            WorkflowResult wr = new WorkflowResult();
            wr.setName(JS7ConverterHelper.getJS7ObjectName(jilJob.getName()));
            wr.setPath(PathResolver.getJS7WorkflowPath(jilJob, wr.getName()));
            wr.setTimezone(w.getTimeZone(), runTimeTimezone != null);

            List<Instruction> in = new ArrayList<>();
            PostNotices pn = BoardHelper.newPostNotices(analyzer, jilJob, jobsConditions.values().stream().collect(Collectors.toSet()));
            in.add(pn);
            w.setInstructions(in);
            result.add(wr.getPath(), w);

            // FILE ORDER SOURCE
            convertFileOrderSources(result, Collections.singletonList(jilJob), wr, null);
            return wr;
        } catch (Throwable e) {
            LOGGER.error(String.format("[convertStandaloneFW][%s]%s", jilJob.getName(), e.toString()), e);
            return null;
        }

    }

    public void convertFileOrderSources(JS7ConverterResult result, List<ACommonJob> fileOrderSources, WorkflowResult wr, JS7Agent js7Agent) {
        if (fileOrderSources.size() > 0) {
            for (ACommonJob n : fileOrderSources) {
                JobFW j = (JobFW) n;
                if (SOSString.isEmpty(j.getWatchFile().getValue())) {
                    continue;
                }
                Path p = Paths.get(j.getWatchFile().getValue());

                String name = JS7ConverterHelper.getJS7ObjectName(j.getName());
                FileOrderSource fos = new FileOrderSource();
                fos.setWorkflowName(wr.getName());

                JS7Agent fwA = getAgent(j);
                if (fwA == null) {
                    if (js7Agent != null) {
                        fos.setAgentName(js7Agent.getJS7AgentName());
                    }
                } else {
                    fos.setAgentName(fwA.getJS7AgentName());
                }

                fos.setTimeZone(wr.getTimezone());
                fos.setDirectoryExpr(JS7ConverterHelper.quoteValue4JS7(p.getParent().toString().replaceAll("\\\\", "/")));
                fos.setPattern(p.getFileName().toString());
                Long delay = null;
                if (j.getWatchInterval().getValue() != null) {
                    delay = j.getWatchInterval().getValue();
                }
                fos.setDelay(delay);
                result.add(JS7ConverterHelper.getFileOrderSourcePathFromJS7Path(wr.getPath(), name), fos);
            }
        }
    }

    private void convertBoards(JS7ConverterResult result) {
        for (Map.Entry<Condition, Path> entry : BoardHelper.JS7_BOARDS.entrySet()) {
            Path p = entry.getValue().getParent();
            if (p == null) {
                p = Paths.get("");
            }
            String boardName = entry.getValue().getFileName().toString();
            if (!AdditionalInstructionsHelper.convertBoards(result, p, boardName)) {
                JS7ConverterHelper.createNoticeBoardByParentPath(result, p, false, boardName, BoardHelper.getBoardTitle(entry.getKey()), BoardHelper
                        .getLifeTimeInMinutes(entry.getKey()));
            }
        }
    }

    private void convertLocks(JS7ConverterResult result) {
        for (Map.Entry<String, Resource2Lock> entry : LockHelper.LOCKS.entrySet()) {
            Resource2Lock r2l = entry.getValue();
            Path p = entry.getValue().getPath().getParent();
            if (p == null) {
                p = Paths.get("");
            }
            JS7ConverterHelper.createLockByParentPath(result, p, r2l.getJS7Name(), r2l.getCapacity());
        }
    }

    private void convertAgents(JS7ConverterResult result, Path reportDir) {
        if (CONFIG.getGenerateConfig().getAgents()) {
            result = JS7ConverterHelper.convertAgents(result, machine2js7Agent.entrySet().stream().map(e -> e.getValue()).collect(Collectors
                    .toList()));
        }
        Report.writeAgentMappingsConfig(reportDir, machine2js7Agent);
    }

    private void convertCalendars(JS7ConverterResult result) {
        if (CONFIG.getGenerateConfig().getCalendars()) {
            Path rootPath = CONFIG.getCalendarConfig().getForcedFolder() == null ? Paths.get("") : CONFIG.getCalendarConfig().getForcedFolder();
            for (String name : RunTimeHelper.JS7_CALENDARS) {
                result.add(JS7ConverterHelper.getCalendarPath(rootPath, name), JS7ConverterHelper.createDefaultWorkingDaysCalendar());
            }
        }
    }

    public Job getJob(JS7ConverterResult result, JobCMD jilJob) {
        Job j = new Job();
        j.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle(jilJob.getDescription().getValue()));
        j = setFromConfig(j);

        JS7Agent js7Agent = getAgent(jilJob);
        j = JS7AgentHelper.setAgent(j, js7Agent);
        j = setExecutable(j, jilJob, js7Agent.getPlatform());
        j = setJobOptions(j, jilJob);
        return j;
    }

    public void convertSchedule(JS7ConverterResult result, WorkflowResult wr, ACommonJob j) {
        Schedule s = RunTimeHelper.toSchedule(CONFIG, wr, j);
        if (s != null) {
            result.add(JS7ConverterHelper.getSchedulePathFromJS7Path(wr.getPath(), wr.getName(), ""), s);
        }
    }

    public static Job setFromConfig(Job j) {
        if (CONFIG.getJobConfig().getForcedGraceTimeout() != null) {
            j.setGraceTimeout(CONFIG.getJobConfig().getForcedGraceTimeout());
        }
        if (CONFIG.getJobConfig().getForcedParallelism() != null) {
            j.setParallelism(CONFIG.getJobConfig().getForcedParallelism());
        }
        if (CONFIG.getJobConfig().getForcedFailOnErrWritten() != null) {
            j.setFailOnErrWritten(CONFIG.getJobConfig().getForcedFailOnErrWritten());
        }
        if (CONFIG.getJobConfig().getForcedWarnOnErrWritten() != null) {
            j.setWarnOnErrWritten(CONFIG.getJobConfig().getForcedWarnOnErrWritten());
        }
        return j;
    }

    public JS7Agent getAgent(ACommonMachineJob jilJob) {
        String machine = JS7ConverterHelper.getJS7ObjectName(jilJob.getMachine().getValue());
        if (machine != null && machine2js7Agent.containsKey(machine)) {
            return machine2js7Agent.get(machine);
        }

        JS7Agent agent = null;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (CONFIG.getAgentConfig().getForcedAgent() != null) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[getAgent][%s]autosys.machine=%s", JS7AgentConvertType.CONFIG_FORCED.name(), machine));
            }
            agent = convertAgentFrom(JS7AgentConvertType.CONFIG_FORCED, CONFIG.getAgentConfig().getForcedAgent(), CONFIG.getAgentConfig()
                    .getDefaultAgent(), machine);
        } else if (machine != null && CONFIG.getAgentConfig().getMappings().containsKey(machine)) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getAgent][%s]autosys.machine=%s", JS7AgentConvertType.CONFIG_MAPPINGS.name(), machine));
            }
            agent = convertAgentFrom(JS7AgentConvertType.CONFIG_MAPPINGS, CONFIG.getAgentConfig().getMappings().get(machine), CONFIG.getAgentConfig()
                    .getDefaultAgent(), machine);
        } else {
            if (isDebugEnabled) {
                // LOGGER.debug(String.format("[getAgent][%s][autosys.machine=%s]", JS7AgentConvertType.CONFIG_DEFAULT.name(), machine));
            }
            agent = convertAgentFrom(JS7AgentConvertType.CONFIG_DEFAULT, CONFIG.getAgentConfig().getDefaultAgent(), null, machine);
        }
        if (agent != null) {
            machine2js7Agent.put(machine == null ? agent.getJS7AgentName() : machine, agent);
        }
        return agent;
    }

    private JS7Agent convertAgentFrom(final JS7AgentConvertType type, final JS7Agent sourceConf, final JS7Agent defaultConf, String machine) {
        List<SubAgent> subagents = new ArrayList<>();
        JS7Agent source = new JS7Agent();
        com.sos.joc.model.agent.Agent agent = null;
        boolean isStandalone = false;

        if (sourceConf == null) {
            // if (ah != null) {
            agent = new com.sos.joc.model.agent.Agent();
            // isStandalone = ah.js1AgentIsStandalone;
            isStandalone = true;
            // }
        } else {
            source = JS7AgentHelper.copy(sourceConf);
            // AGENT
            if (source.getStandaloneAgent() != null) {
                isStandalone = true;
                agent = source.getStandaloneAgent();
            } else if (source.getAgentCluster() != null) {
                isStandalone = false;
                agent = source.getAgentCluster();
                subagents = source.getAgentCluster().getSubagents();
            }

            if (source.getJS7AgentName() == null && agent != null) {
                switch (type) {
                case CONFIG_FORCED:
                case CONFIG_MAPPINGS:
                    source.setJS7AgentName(agent.getAgentName());
                    break;
                default:
                    break;
                }
            }
        }
        if (agent == null) {
            if (defaultConf != null) {
                agent = JS7AgentHelper.copy(defaultConf.getStandaloneAgent());
                if (agent == null) {
                    agent = JS7AgentHelper.copy(defaultConf.getAgentCluster());
                    if (agent != null) {
                        subagents = JS7AgentHelper.copySubagents(defaultConf.getAgentCluster().getSubagents());
                        isStandalone = false;
                    }
                } else {
                    isStandalone = true;
                }
            }
            if (agent == null) {
                agent = new com.sos.joc.model.agent.Agent();
                // if (ah != null) {
                // isStandalone = ah.js1AgentIsStandalone;
                // } else {
                isStandalone = true;
                // }
            }
        }
        // AGENT_NAME
        if (source.getJS7AgentName() == null) {
            if (machine == null) {
                switch (type) {
                case CONFIG_DEFAULT:
                    if (agent != null && !SOSString.isEmpty(agent.getAgentName())) {
                        source.setJS7AgentName(agent.getAgentName());
                    } else {
                        source.setJS7AgentName(JS7AgentConverter.DEFAULT_AGENT_NAME);
                    }
                    break;
                default:
                    if (defaultConf != null) {
                        if (isStandalone && defaultConf.getStandaloneAgent() != null) {
                            source.setJS7AgentName(defaultConf.getStandaloneAgent().getAgentName());
                        } else if (!isStandalone && defaultConf.getAgentCluster() != null) {
                            source.setJS7AgentName(defaultConf.getAgentCluster().getAgentName());
                        }
                    } else {
                        source.setJS7AgentName(JS7AgentConverter.DEFAULT_AGENT_NAME);
                    }
                    break;
                }
            } else {
                source.setJS7AgentName(machine);
            }
            agent.setAgentName(source.getJS7AgentName());
        }
        if (agent.getAgentName() != null) {
            source.setJS7AgentName(agent.getAgentName());
        }
        if (agent.getAgentId() == null) {
            agent.setAgentId(source.getJS7AgentName());
        }
        if (source.getJS7AgentName() == null) {
            source.setJS7AgentName(agent.getAgentId());
        }
        // CONTROLLER_ID
        if (CONFIG.getAgentConfig().getForcedControllerId() != null) {
            agent.setControllerId(CONFIG.getAgentConfig().getForcedControllerId());
        } else {
            // if (ah != null && ah.processClass.getSpoolerId() != null) {
            // agent.setControllerId(ah.processClass.getSpoolerId());
            // }
            if (agent.getControllerId() == null) {
                if (defaultConf != null) {
                    if (isStandalone && defaultConf.getStandaloneAgent() != null) {
                        agent.setControllerId(defaultConf.getStandaloneAgent().getControllerId());
                    } else if (!isStandalone && defaultConf.getAgentCluster() != null) {
                        agent.setControllerId(defaultConf.getAgentCluster().getControllerId());
                    }
                }
            }
            if (agent.getControllerId() == null) {
                agent.setControllerId(CONFIG.getAgentConfig().getDefaultControllerId());
            }
        }

        if (source.getPlatform() == null) {
            source.setPlatform(Platform.UNIX);
        }

        if (isStandalone) {
            if (agent.getUrl() == null) {
                // if (ah != null) {
                // agent.setUrl(getJS1StandaloneAgentURL(ah.processClass));
                // } else {
                if (defaultConf != null) {
                    if (defaultConf.getStandaloneAgent() != null) {
                        agent.setUrl(defaultConf.getStandaloneAgent().getUrl());
                    }
                } else {
                    agent.setUrl(JS7AgentConverter.DEFAULT_AGENT_URL);
                }
                // }
            }
            source.setStandaloneAgent(agent);
        } else {
            if (subagents == null || subagents.size() == 0) {
                subagents = new ArrayList<>();
                // if (ah != null && ah.processClass.getRemoteSchedulers() != null) {
                // subagents = JS1JS7AgentConverter.convert(ah.processClass.getRemoteSchedulers(), agent.getAgentId());
                // }
            }

            // AgentCluster
            ClusterAgent ca = new ClusterAgent();
            ca.setControllerId(agent.getControllerId());
            ca.setAgentId(agent.getAgentId());
            ca.setAgentName(agent.getAgentName());
            ca.setAgentNameAliases(agent.getAgentNameAliases());
            ca.setTitle(agent.getTitle());
            ca.setUrl(subagents.size() > 0 ? subagents.get(0).getUrl() : agent.getUrl());

            ca.setSubagents(subagents);
            source.setAgentCluster(ca);

            // SubagentCluster
            SubagentCluster subagentCluster = new SubagentCluster();
            subagentCluster.setControllerId(ca.getControllerId());
            subagentCluster.setAgentId(ca.getAgentId());
            subagentCluster.setSubagentClusterId("active-" + subagentCluster.getAgentId());
            subagentCluster.setDeployed(null);
            subagentCluster.setOrdering(null);

            List<SubAgentId> ids = new ArrayList<>();
            int p = 0;
            for (SubAgent subagent : subagents) {
                SubAgentId id = new SubAgentId();
                id.setSubagentId(subagent.getSubagentId());
                id.setPriority(p + "");
                ids.add(id);
                p++;
            }
            subagentCluster.setSubagentIds(ids);

            if (source.getSubagentClusterId() == null) {
                source.setSubagentClusterId(subagentCluster.getSubagentClusterId());
            }
            source.setSubagentClusters(Collections.singletonList(subagentCluster));
        }

        // source.setJS7AgentName("agent_name");
        return source;
    }

    private Job setExecutable(Job j, JobCMD jilJob, String platform) {
        boolean isMock = CONFIG.getMockConfig().hasForcedScript();
        boolean isUnix = platform.equals(Platform.UNIX.name());
        String commentBegin = isUnix ? "# " : "REM ";

        StringBuilder header = new StringBuilder();
        if (isMock) {
            header.append(getScriptBegin("", isUnix)).append(commentBegin).append("Mock mode").append(JS7ConverterHelper.JS7_NEW_LINE);
        }

        String command = jilJob.getCommand().getValue();

        if (header.length() == 0) {
            header.append(getScriptBegin(command, isUnix));
        }
        StringBuilder script = new StringBuilder(header);
        if (!SOSString.isEmpty(jilJob.getProfile().getValue())) {
            if (isMock) {
                script.append(commentBegin);
            }
            String commandPrefix = isUnix ? CONFIG.getJobConfig().getForcedUnixCommandPrefix() : CONFIG.getJobConfig()
                    .getForcedWindowsCommandPrefix();
            if (!SOSString.isEmpty(commandPrefix)) {
                script.append(commandPrefix).append(" ");
            }
            script.append(jilJob.getProfile().getValue()).append(JS7ConverterHelper.JS7_NEW_LINE);
        }
        if (isMock) {
            script.append(commentBegin);
        }
        script.append(command);

        if (isMock) {
            script.append(JS7ConverterHelper.JS7_NEW_LINE);
            String mockScript = isUnix ? CONFIG.getMockConfig().getForcedUnixScript() : CONFIG.getMockConfig().getForcedWindowsScript();
            if (!SOSString.isEmpty(mockScript)) {
                script.append(mockScript);
                script.append(JS7ConverterHelper.JS7_NEW_LINE);
            }
        }

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());
        es.setV1Compatible(CONFIG.getJobConfig().getForcedV1Compatible());
        // TODO Check
        if (jilJob.getFailCodes().getValue() != null) {
            JobReturnCode rc = new JobReturnCode();
            rc.setFailure(JS7ConverterHelper.integerListValue(jilJob.getFailCodes().getValue(), ","));
            es.setReturnCodeMeaning(rc);
        } else {
            if (jilJob.getSuccessCodes().getValue() != null) {
                if (!jilJob.getSuccessCodes().getValue().equals("0")) {
                    JobReturnCode rc = new JobReturnCode();
                    rc.setSuccess(JS7ConverterHelper.integerListValue(jilJob.getFailCodes().getValue(), ","));
                    es.setReturnCodeMeaning(rc);
                }
            } else if (jilJob.getMaxExitSuccess().getValue() != null) {
                try {
                    List<Integer> l = new ArrayList<>();
                    for (int i = 0; i <= jilJob.getMaxExitSuccess().getValue().intValue(); i++) {
                        l.add(Integer.valueOf(i));
                    }
                    if (l.size() > 0) {
                        JobReturnCode rc = new JobReturnCode();
                        rc.setSuccess(l);
                        es.setReturnCodeMeaning(rc);
                    }
                } catch (Throwable e) {
                    LOGGER.error("[" + jilJob + "][getMaxExitSuccess]" + e, e);
                }
            }
        }
        j.setExecutable(es);
        return j;
    }

    private String getScriptBegin(String command, boolean isUnix) {
        if (isUnix) {
            if (command != null && !command.toString().startsWith("#!/")) {
                StringBuilder sb = new StringBuilder();
                if (!SOSString.isEmpty(CONFIG.getJobConfig().getDefaultUnixShebang())) {
                    sb.append(CONFIG.getJobConfig().getDefaultUnixShebang());
                    sb.append(JS7ConverterHelper.JS7_NEW_LINE);
                    return sb.toString();
                }
            }
        }
        return "";
    }

    private static Job setJobOptions(Job j, JobCMD jilJob) {
        if (jilJob.getMaxRunAlarm().getValue() != null) {
            j.setWarnIfLonger(String.valueOf(jilJob.getMaxRunAlarm().getValue() * 60));
        }
        if (jilJob.getMinRunAlarm().getValue() != null) {
            j.setWarnIfShorter(String.valueOf(jilJob.getMinRunAlarm().getValue() * 60));
        }
        if (jilJob.getTermRunTime().getValue() != null) {
            j.setTimeout(jilJob.getTermRunTime().getValue() * 60);
        }
        return j;
    }

    public static NamedJob getNamedJobInstruction(String jobName) {
        NamedJob nj = new NamedJob(jobName);
        nj.setLabel(nj.getJobName());
        return nj;
    }

    // 1) Named Instruction
    // 2) Retry around Named Instruction
    public static List<Instruction> getCommonJobInstructions(ACommonJob j, String js7Name) {
        List<Instruction> in = new ArrayList<>();
        in.add(getNamedJobInstruction(js7Name));
        in = RetryHelper.getRetryInstructions(j, in);
        return in;
    }

    public static List<Instruction> getCommonJobInstructionsWithLock(AutosysAnalyzer analyzer, WorkflowResult wr, ACommonJob j, String js7Name) {
        List<Instruction> in = getCommonJobInstructions(j, js7Name);
        in = LockHelper.getLockInstructions(analyzer, wr, j, in);
        return in;
    }

    public AutosysAnalyzer getAnalyzer() {
        return analyzer;
    }

}
