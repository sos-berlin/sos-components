package com.sos.js7.converter.js1.output.js7;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.Fail;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.RetryCatch;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.inventory.model.job.notification.JobNotificationMail;
import com.sos.inventory.model.job.notification.JobNotificationType;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.OrderPositions;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.BranchWorkflow;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Parameters;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.agent.transfer.Agent;
import com.sos.js7.converter.commons.JS7AgentHelper;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.JS7ExportObjects.JS7ExportObject;
import com.sos.js7.converter.commons.config.JS7ConverterConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.config.json.JS7Agent;
import com.sos.js7.converter.commons.output.OutputWriter;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.commons.report.ConverterReportWriter;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.Folder;
import com.sos.js7.converter.js1.common.Include;
import com.sos.js7.converter.js1.common.Params;
import com.sos.js7.converter.js1.common.job.ACommonJob;
import com.sos.js7.converter.js1.common.job.ACommonJob.DelayAfterError;
import com.sos.js7.converter.js1.common.job.OrderJob;
import com.sos.js7.converter.js1.common.job.OrderJob.DelayOrderAfterSetback;
import com.sos.js7.converter.js1.common.job.StandaloneJob;
import com.sos.js7.converter.js1.common.jobchain.JobChain;
import com.sos.js7.converter.js1.common.jobchain.JobChainOrder;
import com.sos.js7.converter.js1.common.jobchain.node.AJobChainNode;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNode;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNodeFileOrderSink;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNodeFileOrderSource;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendar;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendars;
import com.sos.js7.converter.js1.common.processclass.ProcessClass;
import com.sos.js7.converter.js1.common.runtime.RunTime;
import com.sos.js7.converter.js1.input.DirectoryParser;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.js1.output.js7.JS7AgentConverter.JS7AgentConvertType;
import com.sos.js7.converter.js1.output.js7.JS7JobHelper.JavaJITLJobHelper;
import com.sos.js7.converter.js1.output.js7.JS7JobHelper.ShellJobHelper;

/** <br/>
 * TODO Locks<br/>
 * --- current JS7 state - 1 Lock support - generate 1 Lock, ignore/report following<br/>
 * TODO JobChain .config.xml - <br/>
 * --- params as node instructions<br/>
 * --- ... substitute variables <br/>
 * TODO JobChainNodes:<br/>
 * ---- job_chain_node.job_chain<br/>
 * ---- file_order_sink - generate a fileOrderSing JITL Job(Move,Remove)<br/>
 * -------- use ${file}<br/>
 * -------- WARN if the file not exists(grace argument?)<br/>
 * -------- otherwise exception (can't be moved/removed: permission denied etc)<br/>
 * ---------- move - we have only a copy file job ....<br/>
 * ---- job_chain_node.end - ignore/report<br/>
 * ---- multiple file_order_source with different next_state ???<br/>
 * ------- generate a File Order Source per file_order_source for th given workflow<br/>
 * ------- current JS7 state - workflow position not supported - will be implemented later ...<br/>
 * ---- exit workflow e.g. next_state=error, error_state=error...<br/>
 * TODO Schedule substitute - ignore/report<br/>
 * TODO Schedule - create one schedule for multiple workflows<br/>
 * TODO Cyclic Workflows Instructions<br/>
 * TODO Job (order) AdmissionTimes<br/>
 * ---- RunTime - without calendars or job chain jobs with a run time ...<br/>
 * TODO Java: Split/Join - done<br/>
 * TODO Java: Synchronizer<br/>
 * TODO Order state - Schedule start position<br/>
 * TODO YADE without settings - generate jobresource ... ? -<br/>
 */
public class JS7Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7Converter.class);

    public static JS7ConverterConfig CONFIG = new JS7ConverterConfig();

    private static final int INITIAL_DUPLICATE_COUNTER = 1;
    private static final String NAME_CONCAT_CHARACTER = "-";
    private static final String DUPLICATE_PREFIX = NAME_CONCAT_CHARACTER + "copy";

    private static final String MOVE_JOB_NAME = "fileOrderSinkMove";
    private static final String REMOVE_JOB_NAME = "fileOrderSinkRemove";
    private static final String VAR_CURRENT_FILE_JS7 = "${file}";
    private static final String VAR_CURRENT_FILE_JS1 = "${scheduler_file_path}";
    private static final String ENV_VAR_JS1_PREFIX = "SCHEDULER_PARAM_";
    private static final String DEFAULT_AGENT_NAME = "default_agent";
    private static final String DEFAULT_AGENT_URL = "http://localhost:4445";

    private static final String YADE_MAIN_CONFIGURATION_JOBRESOURCE = "yade";
    private static final String YADE_JOB_SETTINGS_ARG = "yadeXml";
    private static final String YADE_JOB_SETTINGS_ENV = "YADE_XML";
    private static final String YADE_JOB_PROFILE_ENV = "YADE_PROFILE";

    private ConverterObjects converterObjects;
    private DirectoryParserResult pr;
    private String inputDirPath;
    private Map<Path, String> jobResources = new HashMap<>();
    private Map<String, Integer> jobResourcesDuplicates = new HashMap<>();
    private Map<Path, OrderJob> orderJobs = new HashMap<>();
    private Map<Path, AgentHelper> js1Agents = new HashMap<>();
    private List<JobChainOrder> orders = new ArrayList<>();

    private Map<String, List<ACommonJob>> js1JobsByLanguage = new HashMap<>();
    private Map<String, List<RunTime>> js1Calendars = new HashMap<>();
    private Map<String, ScheduleHelper> js1Schedules = new HashMap<>();
    private List<ACommonJob> js1JobsWithMonitors = new ArrayList<>();
    private List<ProcessClassFirstUsageHelper> js7Agents2js1ProcessClassResult = new ArrayList<>();

    private Map<String, String> js7Calendars = new HashMap<>();
    private Map<String, AgentHelper> js7Agents = new HashMap<>();
    private Map<String, String> js7StandaloneAgents = new HashMap<>();
    private Map<Path, JS7Agent> js7Agents2js1ProcessClass = new HashMap<>();
    private Map<Path, List<BoardHelper>> js7BoardHelpers = new HashMap<>();
    private Map<Path, String> sinkJobs = new HashMap<>();
    private Path defaultProcessClassPath = null;
    private int sinkJobsDuplicateCounter = 0;

    private int js7ConverterNameCounter = 0;

    public static void convert(Path input, Path outputDir, Path reportDir) throws IOException {

        String method = "convert";

        // APP start
        Instant appStart = Instant.now();
        LOGGER.info(String.format("[%s][start]...", method));

        OutputWriter.prepareDirectory(reportDir);
        OutputWriter.prepareDirectory(outputDir);

        // 1 - Config Report
        ConverterReportWriter.writeConfigReport(reportDir.resolve("config_errors.csv"), reportDir.resolve("config_warnings.csv"), reportDir.resolve(
                "config_analyzer.csv"));

        // 2 - Parse JS1 files
        LOGGER.info(String.format("[%s][JIL][parse][start]...", method));
        DirectoryParserResult pr = DirectoryParser.parse(CONFIG.getParserConfig(), input, outputDir);
        LOGGER.info(String.format("[%s][JIL][parse][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));

        // 3 - Convert to JS7
        Instant start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][convert][start]...", method));
        JS7ConverterResult result = convert(pr);
        LOGGER.info(String.format("[%s][JS7][convert][end]%s", method, SOSDate.getDuration(start, Instant.now())));

        // 3.1 - Parser Reports
        ConverterReportWriter.writeParserReport(reportDir.resolve("parser_summary.csv"), reportDir.resolve("parser_errors.csv"), reportDir.resolve(
                "parser_warnings.csv"), reportDir.resolve("parser_analyzer.csv"));
        // 3.2 - Converter Reports
        ConverterReportWriter.writeConverterReport(reportDir.resolve("converter_errors.csv"), reportDir.resolve("converter_warnings.csv"), reportDir
                .resolve("converter_analyzer.csv"));

        // 4 - Write JS7 files
        start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][write][start]...", method));
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

        if (result.getJobResources().getItems().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][write][jobResources]...", method));
            OutputWriter.write(outputDir, result.getJobResources());
            ConverterReport.INSTANCE.addSummaryRecord("JobResources", result.getJobResources().getItems().size());
        }
        if (result.getFileOrderSources().getItems().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][write][fileOrderSources]...", method));
            OutputWriter.write(outputDir, result.getFileOrderSources());
            ConverterReport.INSTANCE.addSummaryRecord("FileOrderSources", result.getFileOrderSources().getItems().size());
        }
        if (result.getBoards().getItems().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][write][boards]...", method));
            OutputWriter.write(outputDir, result.getBoards());
            ConverterReport.INSTANCE.addSummaryRecord("Boards", result.getBoards().getItems().size());
        }
        // 4.1 - Summary Report
        ConverterReportWriter.writeSummaryReport(reportDir.resolve("converter_summary.csv"));
        LOGGER.info(String.format("[%s][[JS7][write][end]%s", method, SOSDate.getDuration(start, Instant.now())));

        // APP end
        LOGGER.info(String.format("[%s][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));
    }

    private static JS7ConverterResult convert(DirectoryParserResult pr) {
        JS7ConverterResult result = new JS7ConverterResult();

        JS7Converter c = new JS7Converter();
        c.pr = pr;
        c.inputDirPath = pr.getRoot().getPath().toString();
        c.converterObjects = c.getConverterObjects(pr.getRoot());
        c.js1Agents = c.getJS1Agents();

        c.convertYade(result);
        c.convertStandalone(result);
        c.convertJobChains(result);
        c.addJobResources(result);
        c.addSchedulesBasedOnJS1Schedule(result);
        c.convertAgents(result);
        c.postProcessing(result);

        c.parserSummaryReport();
        c.analyzerReport();

        return result;
    }

    private String getJS1StandaloneAgentURL(ProcessClass p) {
        if (p == null || !p.isAgent()) {
            return null;
        }
        return p.getRemoteScheduler() == null ? p.getRemoteSchedulers().getRemoteScheduler().get(0).getRemoteScheduler() : p.getRemoteScheduler();
    }

    private void convertAgents(JS7ConverterResult result) {
        if (CONFIG.getGenerateConfig().getAgents()) {
            List<JS7Agent> agents = js7Agents.entrySet().stream().map(e -> e.getValue().js7Agent).collect(Collectors.toList());
            for (JS7Agent agent : agents) {
                Agent a = null;
                if (agent.getStandaloneAgent() != null) {
                    a = new Agent();
                    a.setStandaloneAgent(JS7AgentConverter.convertStandaloneAgent(agent));
                } else if (agent.getAgentCluster() != null) {
                    a = new Agent();
                    a.setAgentCluster(JS7AgentConverter.convertAgentCluster(agent));
                    a.setSubagentClusters(JS7AgentConverter.convertSubagentClusters(agent));
                }
                if (a == null) {
                    ConverterReport.INSTANCE.addErrorRecord("[agent=" + agent.getJS7AgentName()
                            + "][cannot be converted]missing standalone or agentCluster");
                } else {
                    result.add(Paths.get(agent.getJS7AgentName() + ".agent.json"), a);
                }
            }
        }
    }

    private void postProcessing(JS7ConverterResult result) {
        for (Map.Entry<Path, List<BoardHelper>> e : js7BoardHelpers.entrySet()) {
            for (BoardHelper h : e.getValue()) {
                @SuppressWarnings("rawtypes")
                JS7ExportObject eo = result.getExportObjectWorkflowByPath(h.workflowPath);
                if (eo == null) {
                    LOGGER.error(String.format("[postProcessing][boards][%s]workflow not found", h.workflowPath));
                    ConverterReport.INSTANCE.addErrorRecord("[postProcessing][boards][workflow not found]" + h.workflowPath);
                } else {
                    Workflow workflow = (Workflow) eo.getObject();
                    try {
                        String w = JS7ConverterHelper.JSON_OM.writeValueAsString(workflow);
                        for (Map.Entry<String, Path> currentHelper : h.js7States.entrySet()) {
                            String js7SyncJobUniqueName = sinkJobs.get(h.js7States.get(currentHelper.getKey()));

                            // Generate 1 PostNotice for the current workflow state
                            String boardName = h.workflowName + NAME_CONCAT_CHARACTER + js7SyncJobUniqueName + NAME_CONCAT_CHARACTER + "s";
                            String regex = "(\"noticeBoardNames\"\\s*:\\s*\\[\")sospn-" + currentHelper.getKey() + "\"\\]";
                            String replacement = "$1" + boardName + "\"\\]";
                            w = w.replaceAll(regex, replacement);

                            createNoticeBoard(result, eo.getOriginalPath().getPath(), boardName, boardName);

                            // Generate n ExpectNotices of all workflows
                            List<String> al = new ArrayList<>();
                            for (BoardHelper hh : e.getValue()) {
                                boardName = hh.workflowName + NAME_CONCAT_CHARACTER + js7SyncJobUniqueName + NAME_CONCAT_CHARACTER + "s";
                                al.add("'" + boardName + "'");
                            }
                            regex = "(\"noticeBoardNames\"\\s*:\\s*\")sosen-" + currentHelper.getKey() + "\"";
                            replacement = "$1" + String.join(" && ", al) + "\"";
                            w = w.replaceAll(regex, replacement);
                        }
                        result.addOrReplace(eo.getOriginalPath().getPath(), JS7ConverterHelper.JSON_OM.readValue(w, Workflow.class));
                    } catch (Throwable ex) {
                        LOGGER.error(String.format("[postProcessing][boards][%s]%s", h.workflowPath, ex.toString()), ex);
                        ConverterReport.INSTANCE.addErrorRecord(h.workflowPath, "[postProcessing][boards]", ex);
                    }
                }
            }
        }
    }

    private Map<Path, AgentHelper> getJS1Agents() {
        Map<Path, AgentHelper> result = new HashMap<>();
        converterObjects.processClasses.unique.entrySet().stream().filter(e -> e.getValue().isAgent()).forEach(e -> {
            result.put(e.getValue().getPath(), new AgentHelper(e.getKey(), e.getValue()));
        });
        if (converterObjects.processClasses.duplicates.size() > 0) {
            for (Map.Entry<String, List<ProcessClass>> entry : converterObjects.processClasses.duplicates.entrySet()) {
                LOGGER.info("[convertStandalone][duplicate]" + entry.getKey());
                int counter = INITIAL_DUPLICATE_COUNTER;
                for (ProcessClass pc : entry.getValue()) {
                    if (!pc.isAgent()) {
                        continue;
                    }
                    result.put(pc.getPath(), new AgentHelper(entry.getKey() + DUPLICATE_PREFIX + counter, pc));
                    counter++;
                }
            }
        }
        return result;
    }

    private void addSchedulesBasedOnJS1Schedule(JS7ConverterResult result) {
        js1Schedules.entrySet().stream().sorted(Map.Entry.<String, ScheduleHelper> comparingByKey()).forEach(e -> {
            Schedule schedule = JS7RunTimeConverter.convert(e.getValue().js1Schedule, e.getValue().timeZone, e.getValue().workflows.stream().map(
                    w -> {
                        return w.name;
                    }).collect(Collectors.toList()));

            if (schedule != null) {
                if (e.getValue().startPosition != null) {
                    setSchedulePosition(schedule, e.getValue().startPosition);
                }
                if (e.getValue().orderParams != null && e.getValue().orderParams.size() > 0) {
                    schedule.setOrderParameterisations(e.getValue().orderParams);
                }
                result.add(getSchedulePathFromJS7Path(e.getValue().workflows.get(0).path, e.getKey(), ""), schedule);
            }
        });
    }

    private void parserSummaryReport() {
        long agentsStandalone = js1Agents.entrySet().stream().filter(a -> a.getValue().js1AgentIsStandalone).count();

        ParserReport.INSTANCE.addSummaryRecord("TOTAL FOLDERS", pr.getCountFolders());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL AGENTS", js1Agents.size() + ", STANDALONE=" + agentsStandalone + ", CLUSTER=" + (js1Agents
                .size() - agentsStandalone));
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB files", pr.getCountJobs() + ", STANDALONE=" + pr.getCountStandaloneJobs() + ", ORDER=" + pr
                .getCountOrderJobs());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOBS WITH MONITORS", js1JobsWithMonitors.size());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN files", pr.getCountJobChains());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN ORDER files", pr.getCountOrders());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN CONFIG files", pr.getCountJobChainConfigs());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL LOCK files", pr.getCountLocks());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL PROCESS CLASS files", pr.getCountProcessClasses());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL SCHEDULE files", pr.getCountSchedules());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL MONITOR files", pr.getCountMonitors());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL ANOTHER files", pr.getCountFiles());
        ParserReport.INSTANCE.addSummaryRecord("YADE MAIN CONFIGURATION file", pr.getYadeConfiguration() == null ? "" : pr.getYadeConfiguration()
                .toString());
    }

    private void analyzerReport() {

        // PARSER REPORT
        try {
            if (js1Agents.size() > 0) {
                ParserReport.INSTANCE.addAnalyzerRecord("AGENTS", "START");
                js1Agents.entrySet().stream().sorted(Map.Entry.<Path, AgentHelper> comparingByKey()).forEach(e -> {
                    ParserReport.INSTANCE.addAnalyzerRecord(e.getKey(), e.getValue().js7Agent.getJS7AgentName(), "standalone=" + e
                            .getValue().js1AgentIsStandalone);
                });
                ParserReport.INSTANCE.addAnalyzerRecord("AGENTS", "END");
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addWarningRecord("[analyzerReport]agents", e.toString());
        }

        try {
            if (orders.size() > 0) {
                List<JobChainOrder> empty = orders.stream().filter(o -> o.getRunTime() == null || o.getRunTime().isEmpty()).collect(Collectors
                        .toList());
                ParserReport.INSTANCE.addAnalyzerRecord("JOB CHAIN ORDER files", "TOTAL=" + orders.size() + "(empty run_time=" + empty.size() + ")");
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addWarningRecord("[analyzerReport]orders", e.toString());
        }

        try {
            if (js1JobsByLanguage.size() > 0) {
                ParserReport.INSTANCE.addAnalyzerRecord("JOBS BY LANGUAGE", "START");
                js1JobsByLanguage.entrySet().forEach(e -> {
                    ParserReport.INSTANCE.addAnalyzerRecord(e.getKey().toUpperCase(), "");
                    List<ACommonJob> sorted = e.getValue().stream().sorted((e1, e2) -> e1.getPath().compareTo(e2.getPath())).collect(Collectors
                            .toList());
                    for (ACommonJob job : sorted) {
                        String className = null;
                        if (job.getScript() != null && job.getScript().getJavaClass() != null) {
                            className = job.getScript().getJavaClass();
                        }
                        ParserReport.INSTANCE.addAnalyzerRecord(job.getPath(), job.getType().toString(), className);
                    }
                });
                ParserReport.INSTANCE.addAnalyzerRecord("JOBS BY LANGUAGE", "END");
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addWarningRecord("[analyzerReport]jobsByLanguage", e.toString());
        }

        try {
            if (js1Calendars.size() > 0) {
                ParserReport.INSTANCE.addAnalyzerRecord("CALENDARS", "START");
                js1Calendars.entrySet().stream().sorted(Map.Entry.<String, List<RunTime>> comparingByKey()).forEach(e -> {
                    ParserReport.INSTANCE.addAnalyzerRecord(e.getKey().toUpperCase(), "");
                    List<RunTime> sorted = e.getValue().stream().sorted((e1, e2) -> e1.getCurrentPath().compareTo(e2.getCurrentPath())).collect(
                            Collectors.toList());
                    for (RunTime r : sorted) {
                        ParserReport.INSTANCE.addAnalyzerRecord(r.getCurrentPath(), r.getCalendarsHelper().getText(), "");
                    }
                });
                ParserReport.INSTANCE.addAnalyzerRecord("CALENDARS", "END");
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addWarningRecord("[analyzerReport]calendars", e.toString());
        }

        try {
            if (js1JobsWithMonitors.size() > 0) {
                ParserReport.INSTANCE.addAnalyzerRecord("JOBS WITH MONITORS", "START");
                List<ACommonJob> sorted = js1JobsWithMonitors.stream().sorted((e1, e2) -> e1.getPath().compareTo(e2.getPath())).collect(Collectors
                        .toList());
                for (ACommonJob job : sorted) {
                    List<String> m = job.getMonitors().stream().map(e -> e.getNodeText()).collect(Collectors.toList());
                    ParserReport.INSTANCE.addAnalyzerRecord(job.getPath(), String.join(",", m), "");
                }
                ParserReport.INSTANCE.addAnalyzerRecord("JOBS WITH MONITORS", "END");
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addWarningRecord("[analyzerReport]monitors", e.toString());
        }

        // CONVERTER REPORT --------------------------
        try {
            if (js7Agents2js1ProcessClassResult.size() > 0) {
                ConverterReport.INSTANCE.addAnalyzerRecord("", "");
                ConverterReport.INSTANCE.addAnalyzerRecord("JS1 PROCESS_CLASS TO JS7 Agent", "START");
                List<ProcessClassFirstUsageHelper> sorted = js7Agents2js1ProcessClassResult.stream().sorted((e1, e2) -> e1.path.compareTo(e2.path))
                        .collect(Collectors.toList());
                for (ProcessClassFirstUsageHelper p : sorted) {
                    ConverterReport.INSTANCE.addAnalyzerRecord(p.path, p.js7AgentName, p.message);
                }
                ConverterReport.INSTANCE.addAnalyzerRecord("JS1 PROCESS_CLASS TO JS7 Agent", "END");
            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addWarningRecord("[analyzerReport]Agents", e.toString());
        }
        try {
            if (js1Agents.size() > 0) {
                Map<Path, AgentHelper> notUsed = new HashMap<>();
                for (Map.Entry<Path, AgentHelper> e : js1Agents.entrySet()) {
                    if (!js7Agents2js1ProcessClass.containsKey(e.getKey())) {
                        notUsed.put(e.getKey(), e.getValue());
                    }

                }
                if (notUsed.size() > 0) {
                    ConverterReport.INSTANCE.addAnalyzerRecord("", "");
                    ConverterReport.INSTANCE.addAnalyzerRecord("AGENTS NOT USED", "START");
                    notUsed.entrySet().stream().sorted(Map.Entry.<Path, AgentHelper> comparingByKey()).forEach(e -> {
                        ConverterReport.INSTANCE.addAnalyzerRecord(e.getKey(), e.getValue().js7Agent.getJS7AgentName(), "");
                    });
                    ConverterReport.INSTANCE.addAnalyzerRecord("AGENTS NOT USED", "END");
                }
                // ConverterReport.INSTANCE.addAnalyzerRecord("AGENTS NOT USED", js1Agents.size() + "=" + js7Agents2js1ProcessClass.size() + "="
                // + js7Agents.size());
            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addWarningRecord("[analyzerReport]Agents", e.toString());
        }
        try {
            if (js7Calendars.size() > 0) {
                ConverterReport.INSTANCE.addAnalyzerRecord("", "");
                ConverterReport.INSTANCE.addAnalyzerRecord("CALENDARS/JS7 Calendar", "START/JS1 Calendar");
                js7Calendars.entrySet().stream().sorted(Map.Entry.<String, String> comparingByKey()).forEach(e -> {
                    ConverterReport.INSTANCE.addAnalyzerRecord(e.getKey(), e.getValue());
                });
                ConverterReport.INSTANCE.addAnalyzerRecord("CALENDARS", "END");
            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addWarningRecord("[analyzerReport]js7Calendars", e.toString());
        }
    }

    private void addJobResources(JS7ConverterResult result) {
        String envVarPrefix = CONFIG.getJobConfig().isForcedV1Compatible() ? ENV_VAR_JS1_PREFIX : "";

        jobResources.entrySet().forEach(e -> {
            try {
                Params p = new Params(SOSXML.newXPath(), JS7ConverterHelper.getDocumentRoot(e.getKey()));
                Environment args = new Environment();
                Environment envs = new Environment();

                p.getParams().entrySet().forEach(pe -> {
                    args.setAdditionalProperty(pe.getKey(), JS7ConverterHelper.quoteValue4JS7(pe.getValue()));
                    envs.setAdditionalProperty(envVarPrefix + pe.getKey().toUpperCase(), "$" + pe.getKey());
                });
                JobResource jr = new JobResource(args, envs, null, null);
                result.add(Paths.get(e.getValue() + ".jobresource.json"), jr);
            } catch (Throwable e1) {
                ConverterReport.INSTANCE.addErrorRecord(e.getKey(), "jobResource=" + e.getValue(), e1);
            }
        });
    }

    private void convertYade(JS7ConverterResult result) {
        if (pr.getYadeConfiguration() != null) {
            try {
                Environment args = new Environment();
                Environment envs = new Environment();

                args.setAdditionalProperty(YADE_JOB_SETTINGS_ARG, "toFile('" + SOSPath.readFile(pr.getYadeConfiguration(), StandardCharsets.UTF_8)
                        + "')");
                envs.setAdditionalProperty(YADE_JOB_SETTINGS_ENV, "$" + YADE_JOB_SETTINGS_ARG);

                JobResource jr = new JobResource(args, envs, null, null);
                result.add(Paths.get(YADE_MAIN_CONFIGURATION_JOBRESOURCE + ".jobresource.json"), jr);
            } catch (IOException e) {
                ConverterReport.INSTANCE.addErrorRecord(pr.getYadeConfiguration(), "yade jobResource=yade", e);
                pr.setYadeConfiguration(null);
            }
        }
    }

    private void convertStandalone(JS7ConverterResult result) {
        ConverterObject<StandaloneJob> o = converterObjects.standalone;
        for (Map.Entry<String, StandaloneJob> entry : o.unique.entrySet()) {
            convertStandaloneWorkflow(result, entry.getKey(), entry.getValue(), null, null);
        }

        LOGGER.info("[convertStandalone]duplicates=" + o.duplicates.size());
        if (o.duplicates.size() > 0) {
            for (Map.Entry<String, List<StandaloneJob>> entry : o.duplicates.entrySet()) {
                LOGGER.info("[convertStandalone][duplicate]" + entry.getKey());
                int counter = INITIAL_DUPLICATE_COUNTER;
                for (StandaloneJob jn : entry.getValue()) {
                    String js7Name = entry.getKey() + DUPLICATE_PREFIX + counter;
                    LOGGER.info("[convertStandalone][duplicate][" + entry.getKey() + "][js7Name=" + js7Name + "][path]" + jn.getPath());
                    convertStandaloneWorkflow(result, js7Name, jn, null, null);
                    counter++;
                }
            }
        }
    }

    private String convertStandaloneWorkflow(JS7ConverterResult result, String js7Name, ACommonJob js1Job, Path mainWorkflowPath,
            String mainWorkflowName) {
        LOGGER.info("[convertStandaloneWorkflow]" + js1Job.getPath());

        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(js1Job.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());

        Jobs js = new Jobs();
        Job job = getJob(result, js1Job, null);
        if (job != null) {
            js.setAdditionalProperty(js7Name, job);
        }
        w.setJobs(js);

        List<Instruction> in = new ArrayList<>();
        in.add(getNamedJobInstruction(js1Job, js7Name, js7Name, null, null));
        in = getRetryInstructions(js1Job, in);
        in = getCyclicWorkflowInstructions(js1Job, in);
        w.setInstructions(in);

        Path workflowPath = mainWorkflowPath == null ? getWorkflowPathFromJS1Path(result, js1Job.getPath(), js7Name) : getWorkflowPathFromJS7Path(
                result, mainWorkflowPath, mainWorkflowName);
        String workflowName = getWorkflowName(workflowPath);

        RunTimeHelper rth = convertRunTimeForSchedule("STANDALONE", js1Job.getRunTime(), workflowPath, workflowName, "");
        if (rth != null) {
            if (rth.schedule != null) {
                result.add(rth.path, rth.schedule);
            }
        }
        if (mainWorkflowPath == null) {// js1 standalone jobs
            addJS1ScheduleFromScheduleOrRunTime(js1Job.getRunTime(), null, null, workflowPath, workflowName, null);
        } else {
            if (js1Job instanceof OrderJob) {
                job.setAdmissionTimeScheme(JS7RunTimeConverter.convert((OrderJob) js1Job));
            }
        }
        result.add(workflowPath, w);
        return workflowName;
    }

    // TODO quick tmp solution
    private void addJS1ScheduleFromScheduleOrRunTime(RunTime runTime, List<OrderParameterisation> orderParams, String startPosition,
            Path workflowPath, String workflowName, String add) {
        if (runTime == null) {
            return;
        }

        try {
            com.sos.js7.converter.js1.common.runtime.Schedule schedule = null;
            if (runTime.getSchedule() != null && runTime.getSchedule().getRunTime().getCalendarsHelper() == null) {
                schedule = runTime.getSchedule();
            } else {
                String scheduleName = workflowName;
                if (!SOSString.isEmpty(add)) {
                    scheduleName += add;
                }
                schedule = RunTime.newSchedule(runTime, scheduleName, workflowPath);
                if (!schedule.getRunTime().isConvertableWithoutCalendars()) {
                    schedule = null;
                }
            }

            if (schedule != null) {
                ScheduleHelper h = js1Schedules.get(schedule.getName());
                boolean addToSchedules = false;
                if (h == null) {
                    h = new ScheduleHelper(schedule, orderParams, runTime.getTimeZone(), startPosition);
                    addToSchedules = true;
                } else {
                    addToSchedules = h.workflows.stream().filter(w -> w.name.equals(workflowName)).findAny().orElse(null) == null;
                }
                if (addToSchedules) {
                    h.workflows.add(new WorkflowHelper(workflowName, workflowPath));
                    js1Schedules.put(schedule.getName(), h);
                }
            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addErrorRecord(workflowPath, "error on handle schedule", e);
        }
    }

    private RunTimeHelper convertRunTimeForSchedule(String range, RunTime runTime, Path workflowPath, String workflowName, String additionalName) {
        if (runTime != null && !runTime.isEmpty()) {
            if (runTime.getCalendarsHelper() != null || runTime.getSchedule() != null) {
                JS1Calendars calendars = null;
                if (runTime.getCalendarsHelper() != null) {
                    calendars = runTime.getCalendarsHelper().getCalendars();
                }
                if (calendars == null) {
                    if (runTime.getSchedule() != null && runTime.getSchedule().getRunTime() != null && runTime.getSchedule().getRunTime()
                            .getCalendarsHelper() != null) {
                        calendars = runTime.getSchedule().getRunTime().getCalendarsHelper().getCalendars();
                    }
                }
                if (calendars == null || calendars.getCalendars() == null) {
                    // ConverterReport.INSTANCE.addWarningRecord(runTime.getCurrentPath(), "[" + range
                    // + "][skip convert run-time][with calendars or schedule]" + runTime.getNodeText(), "calendars are null");
                } else if (calendars.getCalendars().size() == 0) {
                    ConverterReport.INSTANCE.addAnalyzerRecord(runTime.getCurrentPath(), "[" + range
                            + "][skip convert run-time][with calendars or schedule]" + runTime.getNodeText(), "calendars is empty");
                } else {
                    List<AssignedCalendars> working = new ArrayList<>();
                    List<AssignedNonWorkingDayCalendars> nonWorking = new ArrayList<>();
                    for (JS1Calendar js1 : calendars.getCalendars()) {
                        if (js1.getBasedOn() != null) {
                            List<RunTime> al = new ArrayList<>();
                            if (js1Calendars.containsKey(js1.getBasedOn())) {
                                al = js1Calendars.get(js1.getBasedOn());
                            }
                            al.add(runTime);
                            js1Calendars.put(js1.getBasedOn(), al);
                        }

                        Calendar cal = JS7CalendarConverter.convert(CONFIG, js1);
                        if (cal == null) {
                            ConverterReport.INSTANCE.addWarningRecord(runTime.getCurrentPath(), "[" + range
                                    + "][skip convert run-time][calendars is null]" + runTime.getNodeText(), "calendars is null");
                            continue;
                        }
                        js7Calendars.put(cal.getName(), js1.getBasedOn() == null ? "" : js1.getBasedOn());

                        List<Period> periods = JS7CalendarConverter.convertPeriods(js1.getPeriods());
                        switch (cal.getType()) {
                        case NONWORKINGDAYSCALENDAR:
                            AssignedNonWorkingDayCalendars nc = new AssignedNonWorkingDayCalendars();
                            nc.setCalendarName(cal.getName());
                            nonWorking.add(nc);
                            break;
                        case WORKINGDAYSCALENDAR:
                            AssignedCalendars c = new AssignedCalendars();
                            c.setCalendarName(cal.getName());
                            c.setTimeZone(runTime.getTimeZone());

                            c.setPeriods(periods);
                            c.setIncludes(cal.getIncludes());
                            c.setExcludes(cal.getExcludes());
                            working.add(c);
                            break;
                        }

                    }

                    Schedule s = new Schedule();
                    s.setWorkflowNames(Collections.singletonList(workflowName));
                    s.setCalendars(working.size() == 0 ? null : working);
                    s.setNonWorkingDayCalendars(nonWorking.size() == 0 ? null : nonWorking);
                    s.setPlanOrderAutomatically(CONFIG.getScheduleConfig().planOrders());
                    s.setSubmitOrderToControllerWhenPlanned(CONFIG.getScheduleConfig().submitOrders());

                    return new RunTimeHelper(getSchedulePathFromJS7Path(workflowPath, workflowName, additionalName), s);
                }

            } else {

            }
        }
        return null;
    }

    private Path getSchedulePathFromJS7Path(Path workflowPath, String workflowName, String additionalName) {
        Path parent = workflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return parent.resolve(workflowName + additionalName + ".schedule.json");
    }

    private Path getFileOrderSourcePathFromJS7Path(Path workflowPath, String workflowName) {
        Path parent = workflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return parent.resolve(workflowName + ".fileordersource.json");
    }

    private Path getNoticeBoardPathFromJS7Path(Path workflowPath, String boardName) {
        Path parent = workflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return parent.resolve(boardName + ".noticeboard.json");
    }

    private String getWorkflowName(Path workflowPath) {
        return workflowPath.getFileName().toString().replace(".workflow.json", "");
    }

    private Path getWorkflowPathFromJS1Path(JS7ConverterResult result, Path js1Path, String js7Name) {
        String relative = js1Path.getParent().toString().substring(inputDirPath.length());
        Path js7Path = null;
        if (SOSString.isEmpty(relative)) {
            js7Path = Paths.get("");
        } else {
            js7Path = getJS7ObjectPath(Paths.get(relative));
        }
        return js7Path.resolve(js7Name + ".workflow.json");
    }

    private Path getWorkflowPathFromJS7Path(JS7ConverterResult result, Path mainWorkflowPath, String name) {
        Path parent = mainWorkflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return parent.resolve(name + ".workflow.json");
    }

    private Path getJS7ObjectPath(Path path) {
        Path output = path.getRoot() == null ? Paths.get("") : path.getRoot();
        for (int i = 0; i < path.getNameCount(); i++) {
            output = output.resolve(getJS7ObjectName(path, path.getName(i).toString()));
        }
        return output;
    }

    private String getJS7ObjectName(Path js1Path, String js1Name) {
        String error = SOSCheckJavaVariableName.check(js1Name);
        if (error == null) {
            return js1Name;
        }
        String newName = SOSCheckJavaVariableName.makeStringRuleConform(js1Name);
        if (SOSString.isEmpty(newName)) {
            js7ConverterNameCounter++;
            newName = "js7_converter_name_" + js7ConverterNameCounter;
        }
        ConverterReport.INSTANCE.addAnalyzerRecord(js1Path, "MAKE STRING RULE CONFORM", "[changed][" + js1Name + "]" + newName);
        return newName;
    }

    private List<Instruction> getRetryInstructions(ACommonJob job, List<Instruction> in) {
        try {
            switch (job.getType()) {
            case ORDER:
                OrderJob oj = (OrderJob) job;
                if (oj.getDelayOrderAfterSetback() != null && oj.getDelayOrderAfterSetback().size() > 0) {
                    Optional<DelayOrderAfterSetback> maximum = oj.getDelayOrderAfterSetback().stream().filter(e -> e.getIsMaximum() != null && e
                            .getIsMaximum() && e.getSetbackCount() != null && e.getSetbackCount() > 0).findAny();
                    if (maximum.isPresent()) {
                        RetryCatch tryCatch = new RetryCatch();
                        tryCatch.setMaxTries(maximum.get().getSetbackCount());
                        tryCatch.setTry(new Instructions(in));

                        // List<DelayOrderAfterSetback> sorted = oj.getDelayOrderAfterSetback().stream().filter(e -> e.getSetbackCount() != null).sorted(
                        // (o1, o2) -> o1.getSetbackCount().compareTo(o2.getSetbackCount())).collect(Collectors.toList());

                        List<DelayOrderAfterSetback> sorted = oj.getDelayOrderAfterSetback().stream().filter(e -> e.getSetbackCount() != null && e
                                .getSetbackCount() != tryCatch.getMaxTries()).sorted((o1, o2) -> o1.getSetbackCount().compareTo(o2.getSetbackCount()))
                                .collect(Collectors.toList());

                        int lastDelay = 1;
                        int lastSetbackCount = 1;
                        List<Integer> setBackDelays = new ArrayList<>();
                        for (DelayOrderAfterSetback d : sorted) {
                            for (int i = lastSetbackCount + 1; i < d.getSetbackCount(); i++) {
                                setBackDelays.add(lastDelay);
                            }
                            if (d.getDelay() != null) {
                                int delaySeconds = new Long(SOSDate.getTimeAsSeconds(d.getDelay())).intValue();
                                setBackDelays.add(delaySeconds);
                                lastSetbackCount = d.getSetbackCount();
                                lastDelay = delaySeconds;
                            }
                        }
                        tryCatch.setRetryDelays(setBackDelays);

                        in = new ArrayList<>();
                        in.add(tryCatch);
                    } else {
                        LOGGER.error(String.format("[order][%s][delay_order_after_setback]is_maximum=true not found", job.getPath()));
                        ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "delay_order_after_setback", "is_maximum=true not found");
                    }
                }
                break;
            case STANDALONE:
                if (job.getDelayAfterError() != null && job.getDelayAfterError().size() > 0) {
                    Optional<DelayAfterError> stop = job.getDelayAfterError().stream().filter(e -> e.getDelay() != null && e.getDelay()
                            .equalsIgnoreCase("stop")).findAny();
                    if (stop.isPresent()) {
                        RetryCatch tryCatch = new RetryCatch();
                        tryCatch.setMaxTries(stop.get().getErrorCount());
                        tryCatch.setTry(new Instructions(in));

                        // List<DelayAfterError> sorted = job.getDelayAfterError().stream().filter(e -> e.getErrorCount() != null).sorted((o1, o2) -> o1
                        // .getErrorCount().compareTo(o2.getErrorCount())).collect(Collectors.toList());

                        List<DelayAfterError> sorted = job.getDelayAfterError().stream().filter(e -> e.getErrorCount() != null && e
                                .getErrorCount() != tryCatch.getMaxTries()).sorted((o1, o2) -> o1.getErrorCount().compareTo(o2.getErrorCount()))
                                .collect(Collectors.toList());

                        int lastDelay = 1;
                        int lastErrorCount = 1;
                        List<Integer> errorDelays = new ArrayList<>();
                        for (DelayAfterError d : sorted) {
                            for (int i = lastErrorCount + 1; i < d.getErrorCount(); i++) {
                                errorDelays.add(lastDelay);
                            }
                            if (d.getDelay() != null && !d.getDelay().equalsIgnoreCase("stop")) {
                                int delaySeconds = new Long(SOSDate.getTimeAsSeconds(d.getDelay())).intValue();
                                errorDelays.add(delaySeconds);
                                lastErrorCount = d.getErrorCount();
                                lastDelay = delaySeconds;
                            }
                        }
                        tryCatch.setRetryDelays(errorDelays);

                        in = new ArrayList<>();
                        in.add(tryCatch);
                    } else {
                        LOGGER.error(String.format("[standalone][%s][delay_after_error]STOP not found", job.getPath()));
                        ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "delay_after_error", "STOP not found");
                    }
                }
                break;
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][getRetryInstructions]%s", job.getPath(), e.toString()), e);
            ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "getRetryInstructions", e);
        }
        return in;
    }

    // TODO no extra instructions? is default by JS7
    @SuppressWarnings("unused")
    private List<Instruction> getSuspendInstructions(ACommonJob job, List<Instruction> in, JobChainStateHelper h) {
        try {
            TryCatch tryCatch = new TryCatch();
            tryCatch.setTry(new Instructions(in));

            Fail f = new Fail();
            f.setMessage("Failed because of Job(name=" + h.js7JobName + ",label=" + h.js7State + ") error.");
            f.setUncatchable(false);
            // Variables v = new Variables();
            // v.getAdditionalProperties().put("returnCode", 1);
            // f.setOutcome(v);
            tryCatch.getCatch().setInstructions(new ArrayList<>());
            tryCatch.getCatch().getInstructions().add(f);

            in = new ArrayList<>();
            in.add(tryCatch);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][getSuspendInstructions]%s", job.getPath(), e.toString()), e);
            ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "getSuspendInstructions", e);
        }
        return in;
    }

    private List<Instruction> getCyclicWorkflowInstructions(ACommonJob job, List<Instruction> in) {
        /** if (!CONFIG.getGenerateConfig().getCyclicOrders() && job.getRunTime().getStartMins().getValue() != null) { Periodic p = new Periodic();
         * p.setPeriod(3_600L); p.setOffsets(jilJob.getRunTime().getStartMins().getValue().stream().map(e -> new Long(e * 60)).collect(Collectors.toList()));
         * 
         * DailyPeriod dp = new DailyPeriod(); dp.setSecondOfDay(0L); dp.setDuration(86_400L);
         * 
         * CycleSchedule cs = new CycleSchedule(Collections.singletonList(new Scheme(p, new AdmissionTimeScheme(Collections.singletonList(dp))))); Instructions
         * ci = new Instructions(in);
         * 
         * in = new ArrayList<>(); in.add(new Cycle(ci, cs)); } */
        return in;
    }

    private Job getJob(JS7ConverterResult result, ACommonJob job, JS7Agent jobChainAgent) {
        if (job.getMonitors() != null && job.getMonitors().size() > 0) {
            if (!js1JobsWithMonitors.contains(job)) {
                js1JobsWithMonitors.add(job);
            }
        }

        JS7JobHelper jh = new JS7JobHelper(job);
        List<ACommonJob> jbt = js1JobsByLanguage.get(jh.getLanguage());
        if (jbt == null) {
            jbt = new ArrayList<>();
        }
        jbt.add(job);
        js1JobsByLanguage.put(jh.getLanguage(), jbt);

        if (!jh.createJS7Job()) {
            return null;
        }

        Job j = new Job();
        j.setTitle(job.getTitle());
        setFromConfig(j);

        JS7Agent js7Agent = getAgent(job.getProcessClass(), jobChainAgent, job, null);

        setAgent(j, js7Agent);
        setJobJobResources(j, job, jh);
        setExecutable(j, job, jh, js7Agent);
        setJobOptions(j, job);
        setJobNotification(j, job);

        return j;
    }

    private NamedJob getNamedJobInstruction(ACommonJob js1Job, String js7JobName, String js7JobLabel, Params jobChainConfigProcessParams,
            SOSParameterSubstitutor ps) {
        NamedJob nj = new NamedJob(js7JobName);
        nj.setLabel(js7JobLabel);
        if (jobChainConfigProcessParams != null && jobChainConfigProcessParams.hasParams()) {
            if (ps != null) {
                jobChainConfigProcessParams.getParams().entrySet().forEach(e -> {
                    ps.addKey(e.getKey(), e.getValue());
                });
            }

            Environment env = new Environment();

            // TODO see getJobArguments handling arguments
            JS7JobHelper jh = new JS7JobHelper(js1Job);
            JavaJITLJobHelper jitlJob = jh.getJavaJITLJob();
            ShellJobHelper shellJob = jh.getShellJob();

            Map<String, String> dynamic = null;
            if (jitlJob != null) {
                // Add Arguments
                for (Map.Entry<String, String> e : jitlJob.getParams().getToAdd().entrySet()) {
                    env.setAdditionalProperty(e.getKey(), JS7ConverterHelper.quoteValue4JS7(replaceJS1Values(e.getValue())));
                }

                // Prepare Dynamic Argument names to use
                if (jitlJob.getParams().getMappingDynamic() != null) {
                    String d = js1Job.getParams().getParams().get(jitlJob.getParams().getMappingDynamic().getParamName());
                    if (d != null) {
                        dynamic = jitlJob.getParams().getMappingDynamic().replace(d);
                    }
                }
            }

            for (Map.Entry<String, String> e : jobChainConfigProcessParams.getParams().entrySet()) {
                String name = e.getKey();
                String value = e.getValue();

                if (shellJob != null && shellJob.getYADE() != null) {
                    // Remove unused Arguments
                    if (shellJob.getYADE().getParams().getToRemove().contains(name)) {
                        ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Job " + nj.getJobName() + ",Label=" + nj.getLabel(), "Param["
                                + name + "=" + value + "]" + "not converted because not used in JS7");
                        continue;
                    }
                }
                if (jitlJob != null) {
                    // Remove unused Arguments
                    if (jitlJob.getParams().getToRemove().contains(name)) {
                        ConverterReport.INSTANCE.addWarningRecord(js1Job.getPath(), "Job " + nj.getJobName() + ",Label=" + nj.getLabel(), "Param["
                                + name + "=" + value + "]" + "not converted because not used in JS7");
                        continue;
                    }

                    // Map to new Argument name
                    String n = jitlJob.getParams().getMapping().get(name);
                    if (n != null) {
                        ConverterReport.INSTANCE.addAnalyzerRecord(js1Job.getPath(), "Job " + nj.getJobName() + ",Label=" + nj.getLabel(), "Param["
                                + name + "=" + value + "]" + "mapped to JS7 Argument[" + n + "=" + value + "]");
                        name = n;
                    }

                    // Map to new Argument name and new value only when Parameter value is true
                    if (n == null) {
                        n = jitlJob.getParams().getMappingWhenTrue().get(name);
                        if (n != null) {
                            if (JS7ConverterHelper.booleanValue(value, false)) {
                                String[] arr = n.split("=");

                                ConverterReport.INSTANCE.addAnalyzerRecord(js1Job.getPath(), "Job " + nj.getJobName() + ",Label=" + nj.getLabel(),
                                        "Param[" + name + "=" + value + "]" + "mapped to JS7 Argument[" + arr[0] + "=" + arr[1] + "]");

                                name = arr[0];
                                value = arr[1];
                            } else {
                                ConverterReport.INSTANCE.addAnalyzerRecord(js1Job.getPath(), "Job " + nj.getJobName() + ",Label=" + nj.getLabel(),
                                        "Param[" + name + "=" + value + "]" + "not mapped to JS7 Argument because has FALSE value");
                            }
                        }
                    }

                    // Convert JS1 boolean values(0,1, yes ...) to JS7 boolean
                    if (n == null) {
                        n = jitlJob.getParams().getMappingBoolean().get(name);
                        if (n != null) {
                            name = n;
                            value = String.valueOf(JS7ConverterHelper.booleanValue(value, false));
                        }
                    }

                    // Dynamic Arguments
                    if (n == null && dynamic != null) {
                        n = dynamic.get(name);
                        if (n != null) {
                            ConverterReport.INSTANCE.addAnalyzerRecord(js1Job.getPath(), "Job " + nj.getJobName() + ",Label=" + nj.getLabel(),
                                    "Param[" + name + "=" + value + "]" + "mapped to JS7 Argument[" + n + "=" + value + "]");
                            name = n;
                        }
                    }

                }

                String replaced = replaceJS1Values(value);
                String val = ps == null ? replaced : ps.replace(replaced);
                env.setAdditionalProperty(name, JS7ConverterHelper.quoteValue4JS7(val));
            }

            if (env.getAdditionalProperties().size() > 0) {
                nj.setDefaultArguments(env);
            }
        }
        return nj;
    }

    private String replaceJS1Values(String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }

        if (val.equals(VAR_CURRENT_FILE_JS1)) {
            return VAR_CURRENT_FILE_JS7;
        } else if (val.contains("${scheduler_data}")) {
            return val.replaceAll("\\$\\{scheduler_data\\}", "env('JS7_AGENT_DATA')");
        }

        return val;
    }

    private void setFromConfig(Job j) {
        if (CONFIG.getJobConfig().getForcedGraceTimeout() != null) {
            j.setGraceTimeout(CONFIG.getJobConfig().getForcedGraceTimeout());
        }
        if (CONFIG.getJobConfig().getForcedParallelism() != null) {
            j.setParallelism(CONFIG.getJobConfig().getForcedParallelism());
        }
        if (CONFIG.getJobConfig().getForcedFailOnErrWritten() != null) {
            j.setFailOnErrWritten(CONFIG.getJobConfig().getForcedFailOnErrWritten());
        }
    }

    private JS7Agent convertFrom(final JS7AgentConvertType type, final JS7Agent sourceConf, final JS7Agent defaultConf, final AgentHelper ah) {
        List<SubAgent> subagents = new ArrayList<>();
        JS7Agent source = new JS7Agent();
        com.sos.joc.model.agent.Agent agent = null;
        boolean isStandalone = false;

        if (sourceConf == null) {
            if (ah != null) {
                agent = new com.sos.joc.model.agent.Agent();
                isStandalone = ah.js1AgentIsStandalone;
            }
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
                        subagents = JS7ConverterHelper.copy(defaultConf.getAgentCluster().getSubagents());
                        isStandalone = false;
                    }
                } else {
                    isStandalone = true;
                }
            }
            if (agent == null) {
                agent = new com.sos.joc.model.agent.Agent();
                if (ah != null) {
                    isStandalone = ah.js1AgentIsStandalone;
                } else {
                    isStandalone = true;
                }
            }
        }
        // AGENT_NAME
        if (source.getJS7AgentName() == null) {
            if (ah == null) {
                switch (type) {
                case CONFIG_DEFAULT:
                    if (agent != null && !SOSString.isEmpty(agent.getAgentName())) {
                        source.setJS7AgentName(agent.getAgentName());
                    } else {
                        source.setJS7AgentName(DEFAULT_AGENT_NAME);
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
                        source.setJS7AgentName(DEFAULT_AGENT_NAME);
                    }
                    break;
                }
                if (defaultProcessClassPath == null && !SOSString.isEmpty(source.getJS7AgentName())) {
                    defaultProcessClassPath = Paths.get(source.getJS7AgentName());
                }
            } else {
                source.setJS7AgentName(ah.js7Agent.getJS7AgentName());
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
            if (ah != null && ah.processClass.getSpoolerId() != null) {
                agent.setControllerId(ah.processClass.getSpoolerId());
            }
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
                if (ah != null) {
                    agent.setUrl(getJS1StandaloneAgentURL(ah.processClass));
                } else {
                    if (defaultConf != null) {
                        if (defaultConf.getStandaloneAgent() != null) {
                            agent.setUrl(defaultConf.getStandaloneAgent().getUrl());
                        }
                    } else {
                        agent.setUrl(DEFAULT_AGENT_URL);
                    }
                }
            }
            source.setStandaloneAgent(agent);
        } else {
            if (subagents == null || subagents.size() == 0) {
                subagents = new ArrayList<>();
                if (ah != null && ah.processClass.getRemoteSchedulers() != null) {
                    subagents = JS7AgentConverter.convert(ah.processClass.getRemoteSchedulers(), agent.getAgentId());
                }
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
                id.setPriority(p);
                ids.add(id);
                p++;
            }
            subagentCluster.setSubagentIds(ids);

            if (source.getSubagentClusterId() == null) {
                source.setSubagentClusterId(subagentCluster.getSubagentClusterId());
            }
            source.setSubagentClusters(Collections.singletonList(subagentCluster));
        }
        return source;
    }

    private JS7Agent getAgent(ProcessClass js1ProcessClass, JS7Agent jobChainAgent, ACommonJob job, JobChain jobChain) {
        Path processClassPath = (js1ProcessClass != null && js1ProcessClass.getPath() != null) ? js1ProcessClass.getPath() : defaultProcessClassPath;
        if (processClassPath != null && js7Agents2js1ProcessClass.containsKey(processClassPath)) {
            return js7Agents2js1ProcessClass.get(processClassPath);
        }

        boolean isDebugEnabled = LOGGER.isDebugEnabled();

        AgentHelper ah = null;
        String js1AgentName = null;
        String js7AgentName = null;
        if (js1ProcessClass != null && js1ProcessClass.isAgent()) {
            ah = js1Agents.get(js1ProcessClass.getPath());
            if (ah != null) {
                js1AgentName = ah.js7Agent.getJS1AgentName();
                js7AgentName = ah.js7Agent.getJS7AgentName();
            }
        }
        JS7Agent agent = null;
        if (CONFIG.getAgentConfig().getForcedAgent() != null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getAgent][%s][js1AgentName=%s][js7AgentName=%s]", JS7AgentConvertType.CONFIG_FORCED.name(), js1AgentName,
                        js7AgentName));
            }
            agent = convertFrom(JS7AgentConvertType.CONFIG_FORCED, CONFIG.getAgentConfig().getForcedAgent(), CONFIG.getAgentConfig()
                    .getDefaultAgent(), ah);
            if (agent != null && !SOSString.isEmpty(agent.getJS7AgentName())) {
                defaultProcessClassPath = Paths.get(agent.getJS7AgentName());
            }
        } else {
            if (jobChainAgent != null && (job == null || job.getProcessClass() == null || !job.getProcessClass().isAgent())) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[getAgent][JOB_CHAIN]%s", jobChainAgent.getJS7AgentName()));
                }
                agent = jobChainAgent;
            } else if (js1AgentName != null && CONFIG.getAgentConfig().getMappings().containsKey(js1AgentName)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[getAgent][%s]js1AgentName=%s", JS7AgentConvertType.CONFIG_MAPPINGS.name(), js1AgentName));
                }
                agent = convertFrom(JS7AgentConvertType.CONFIG_MAPPINGS, CONFIG.getAgentConfig().getMappings().get(js1AgentName), CONFIG
                        .getAgentConfig().getDefaultAgent(), ah);
            } else if (js1ProcessClass != null && js1ProcessClass.isAgent()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[getAgent][%s][js1AgentName=%s][js7AgentName=%s]", JS7AgentConvertType.PROCESS_CLASS.name(),
                            js1AgentName, js7AgentName));
                }
                agent = convertFrom(JS7AgentConvertType.PROCESS_CLASS, null, CONFIG.getAgentConfig().getDefaultAgent(), ah);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[getAgent][%s][js1AgentName=%s][js7AgentName=%s]", JS7AgentConvertType.CONFIG_DEFAULT.name(),
                            js1AgentName, js7AgentName));
                }
                agent = convertFrom(JS7AgentConvertType.CONFIG_DEFAULT, CONFIG.getAgentConfig().getDefaultAgent(), null, ah);
            }
        }
        if (agent != null) {
            if (processClassPath == null) {
                if (defaultProcessClassPath == null) {
                    defaultProcessClassPath = Paths.get(DEFAULT_AGENT_NAME);
                }
                processClassPath = defaultProcessClassPath;
            }

            if (agent.getStandaloneAgent() != null && agent.getStandaloneAgent().getUrl() != null) {
                String agentUrl = agent.getStandaloneAgent().getUrl();
                if (js7StandaloneAgents.containsKey(agentUrl)) {
                    String n = js7StandaloneAgents.get(agentUrl);

                    if (!n.equals(agent.getJS7AgentName())) {
                        ConverterReport.INSTANCE.addAnalyzerRecord("[standalone agent][duplicate url]" + agentUrl, "[renamed][" + agent
                                .getJS7AgentName() + "]" + n);
                    }

                    agent.setJS7AgentName(n);
                    agent.getStandaloneAgent().setAgentName(n);
                    agent.getStandaloneAgent().setAgentId(n);
                } else {
                    js7StandaloneAgents.put(agentUrl, agent.getStandaloneAgent().getAgentName());
                }
                // LOGGER.info(agent.getJS7AgentName()+" = "+agent.getStandaloneAgent().getUrl());
            }

            StringBuilder sb = new StringBuilder();
            if (job != null) {
                sb.append("[first usage job=").append(job.getPath()).append("]");
            } else if (jobChain != null) {
                sb.append("[first usage job_chain=").append(jobChain.getPath()).append("]");
            } else {
                sb.append("[usage unknown]");
            }
            if (js1ProcessClass != null) {
                sb.append("[process_class]").append(js1ProcessClass.getPath());
            } else {
                sb.append("[process_class]without process_class");
            }
            js7Agents2js1ProcessClassResult.add(new ProcessClassFirstUsageHelper(processClassPath, agent.getJS7AgentName(), sb.toString()));

            if (js7Agents.containsKey(agent.getJS7AgentName())) {
                AgentHelper h = js7Agents.get(agent.getJS7AgentName());
                if (h.processClass != null) {
                    js7Agents2js1ProcessClassResult.add(new ProcessClassFirstUsageHelper(processClassPath, agent.getJS7AgentName(), "[already_used]"
                            + h.processClass.getPath()));
                } else {
                    js7Agents2js1ProcessClassResult.add(new ProcessClassFirstUsageHelper(processClassPath, agent.getJS7AgentName(), "[already_used]"
                            + sb));
                }
            }
            js7Agents.put(agent.getJS7AgentName(), new AgentHelper(agent, js1ProcessClass));
            js7Agents2js1ProcessClass.put(processClassPath, agent);
        } else {
            if (js1ProcessClass != null) {
                ConverterReport.INSTANCE.addWarningRecord(js1ProcessClass.getPath(), "Agent", "not used. Use a standalone " + DEFAULT_AGENT_NAME);
            }
        }
        return agent;
    }

    private void setExecutable(Job j, ACommonJob job, JS7JobHelper jh, JS7Agent js7Agent) {
        j.setExecutable(jh.getJavaJITLJob() == null ? getExecutableScript(j, job, js7Agent, jh) : getInternalExecutable(j, job, jh));
    }

    private ExecutableJava getInternalExecutable(Job j, ACommonJob job, JS7JobHelper jh) {
        ExecutableJava ej = new ExecutableJava();
        ej.setClassName(jh.getJavaJITLJob().getNewJavaClass());
        ej.setArguments(getJobArguments(job, jh));

        setLogLevel(ej, job);
        setMockLevel(ej);
        return ej;
    }

    private Environment getJobArguments(ACommonJob job, JS7JobHelper jh) {
        Environment env = null;
        if (job.getParams() != null && job.getParams().hasParams()) {
            // ARGUMENTS
            env = new Environment();

            JavaJITLJobHelper jitlJob = jh.getJavaJITLJob();
            ShellJobHelper shellJob = jh.getShellJob();
            Map<String, String> dynamic = null;
            if (jitlJob != null) {
                // Add Arguments
                for (Map.Entry<String, String> e : jitlJob.getParams().getToAdd().entrySet()) {
                    env.setAdditionalProperty(e.getKey(), JS7ConverterHelper.quoteValue4JS7(replaceJS1Values(e.getValue())));
                }

                // Prepare Dynamic Argument names to use
                if (jitlJob.getParams().getMappingDynamic() != null) {
                    String d = job.getParams().getParams().get(jitlJob.getParams().getMappingDynamic().getParamName());
                    if (d != null) {
                        dynamic = jitlJob.getParams().getMappingDynamic().replace(d);
                    }
                }
            }
            for (Map.Entry<String, String> e : job.getParams().getParams().entrySet()) {
                try {
                    String name = e.getKey();
                    String value = e.getValue();

                    if (shellJob != null && shellJob.getYADE() != null) {
                        // Remove unused Arguments
                        if (shellJob.getYADE().getParams().getToRemove().contains(name)) {
                            ConverterReport.INSTANCE.addWarningRecord(job.getPath(), "Job " + job.getName(), "Param[" + name + "=" + value + "]"
                                    + "not converted because not used in JS7");
                            continue;
                        }
                    }

                    if (jitlJob != null) {
                        // Remove unused Arguments
                        if (jitlJob.getParams().getToRemove().contains(name)) {
                            ConverterReport.INSTANCE.addWarningRecord(job.getPath(), "Job " + job.getName(), "Param[" + name + "=" + value + "]"
                                    + "not converted because not used in JS7");
                            continue;
                        }

                        // Map to new Argument name
                        String n = jitlJob.getParams().getMapping().get(name);
                        if (n != null) {
                            ConverterReport.INSTANCE.addAnalyzerRecord(job.getPath(), "Job " + job.getName(), "Param[" + name + "=" + value + "]"
                                    + "mapped to JS7 Argument[" + n + "=" + value + "]");
                            name = n;
                        }

                        // Map to new Argument name and new value only when Parameter value is true
                        if (n == null) {
                            n = jitlJob.getParams().getMappingWhenTrue().get(name);
                            if (n != null) {
                                if (JS7ConverterHelper.booleanValue(value, false)) {
                                    String[] arr = n.split("=");

                                    ConverterReport.INSTANCE.addAnalyzerRecord(job.getPath(), "Job " + job.getName(), "Param[" + name + "=" + value
                                            + "]" + "mapped to JS7 Argument[" + arr[0] + "=" + arr[1] + "]");

                                    name = arr[0];
                                    value = arr[1];
                                } else {
                                    ConverterReport.INSTANCE.addAnalyzerRecord(job.getPath(), "Job " + job.getName(), "Param[" + name + "=" + value
                                            + "]" + "not mapped to JS7 Argument because has FALSE value");
                                }
                            }
                        }

                        // Convert JS1 boolean values(0,1, yes ...) to JS7 boolean
                        if (n == null) {
                            n = jitlJob.getParams().getMappingBoolean().get(name);
                            if (n != null) {
                                name = n;
                                value = String.valueOf(JS7ConverterHelper.booleanValue(value, false));
                            }
                        }

                        // Dynamic Arguments
                        if (n == null && dynamic != null) {
                            n = dynamic.get(name);
                            if (n != null) {
                                ConverterReport.INSTANCE.addAnalyzerRecord(job.getPath(), "Job " + job.getName(), "Param[" + name + "=" + value + "]"
                                        + "mapped to JS7 Argument[" + n + "=" + value + "]");
                                name = n;
                            }
                        }
                    }
                    env.setAdditionalProperty(name, JS7ConverterHelper.quoteValue4JS7(replaceJS1Values(value)));
                } catch (Throwable ee) {
                    env.setAdditionalProperty(e.getKey(), e.getValue());
                    ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "getJobArguments: could not convert value=" + e.getValue(), ee);
                }
            }
            if (env.getAdditionalProperties().size() == 0) {
                env = null;
            }
        }
        return env;
    }

    private void setLogLevel(ExecutableJava ej, ACommonJob job) {
        String logLevel = null;

        if (CONFIG.getJobConfig().getForcedJitlLogLevel() != null) {
            logLevel = CONFIG.getJobConfig().getForcedJitlLogLevel();
        } else if (job != null && job.getSettings() != null && !SOSString.isEmpty(job.getSettings().getLogLevel())) {
            String lv = job.getSettings().getLogLevel().toLowerCase().trim();
            if (lv.startsWith("debug")) {
                logLevel = "debug";
            } else {
                logLevel = lv;
            }
        }

        if (!SOSString.isEmpty(logLevel)) {
            Environment env = ej.getArguments();
            if (env == null) {
                env = new Environment();
            }
            env.setAdditionalProperty("log_level", JS7ConverterHelper.quoteValue4JS7(logLevel.toUpperCase()));
            ej.setArguments(env);
        }
    }

    private void setMockLevel(ExecutableJava ej) {
        if (CONFIG.getMockConfig().getJitlJobsMockLevel() != null) {
            String mockLevel = CONFIG.getMockConfig().getJitlJobsMockLevel().toUpperCase();
            if (mockLevel.equals("INFO") || mockLevel.equals("ERROR")) {// see com.sos.jitl.jobs.common.JobArguments
                Environment env = ej.getArguments();
                if (env == null) {
                    env = new Environment();
                }
                env.setAdditionalProperty("mock_level", JS7ConverterHelper.quoteValue4JS7(mockLevel));
                ej.setArguments(env);
            }
        }
    }

    private ExecutableScript getExecutableScript(Job j, ACommonJob job, JS7Agent js7Agent, JS7JobHelper jh) {
        StringBuilder scriptHeader = new StringBuilder();
        StringBuilder scriptCommand = new StringBuilder();
        StringBuilder yadeCommand = new StringBuilder();
        String commentBegin = "#";
        boolean isMock = CONFIG.getMockConfig().hasScript();
        boolean isUnix = js7Agent.getPlatform().equals(Platform.UNIX);
        Environment args = getJobArguments(job, jh);

        ShellJobHelper shellJob = jh.getShellJob();
        if (isUnix) {
            commentBegin = "#";
            if (shellJob.getLanguage().equals("powershell")) {
                scriptHeader.append("#!/usr/bin/env pwsh");
                scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
                scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            } else {
                scriptHeader.append("#!/bin/bash");
                scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            }
            if (shellJob.getYADE() != null) {
                yadeCommand.append("$").append(shellJob.getYADE().getBin()).append(" ");
                if (args != null && args.getAdditionalProperties().size() > 0) {
                    boolean hasProfile = false;
                    boolean hasSettings = false;
                    for (Map.Entry<String, String> y : args.getAdditionalProperties().entrySet()) {
                        if (!hasProfile) {
                            hasProfile = y.getKey().toLowerCase().equals("profile");
                        }
                        if (!hasSettings) {
                            hasSettings = y.getKey().toLowerCase().equals("settings");
                        }
                        yadeCommand.append("-").append(y.getKey()).append("=");
                        yadeCommand.append("\"$").append(y.getKey().toUpperCase()).append("\"");
                        yadeCommand.append(" ");
                    }
                    if (hasProfile && !hasSettings && pr.getYadeConfiguration() != null) {
                        yadeCommand.append("-settings=\"$" + YADE_JOB_SETTINGS_ENV + "\" ");
                    }
                } else {
                    yadeCommand.append("-settings $" + YADE_JOB_SETTINGS_ENV + " -profile $" + YADE_JOB_PROFILE_ENV);
                }
            }
        } else {
            commentBegin = "REM";
            if (shellJob.getLanguage().equals("powershell")) {
                scriptHeader.append("@@findstr/v \"^@@f.*&\" \"%~f0\"|pwsh.exe -&goto:eof");
                scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
                scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            }
            if (shellJob.getYADE() != null) {
                yadeCommand.append("%").append(shellJob.getYADE().getBin()).append("% ");
                if (args != null && args.getAdditionalProperties().size() > 0) {
                    boolean hasProfile = false;
                    boolean hasSettings = false;
                    for (Map.Entry<String, String> y : args.getAdditionalProperties().entrySet()) {
                        if (!hasProfile) {
                            hasProfile = y.getKey().toLowerCase().equals("profile");
                        }
                        if (!hasSettings) {
                            hasSettings = y.getKey().toLowerCase().equals("settings");
                        }
                        yadeCommand.append("-").append(y.getKey()).append("=");
                        yadeCommand.append("\"%").append(y.getKey().toUpperCase()).append("%\"");
                        yadeCommand.append(" ");
                    }
                    if (hasProfile && !hasSettings && pr.getYadeConfiguration() != null) {
                        yadeCommand.append("-settings=\"%" + YADE_JOB_SETTINGS_ENV + "%\" ");
                    }
                } else {
                    yadeCommand.append("-settings %" + YADE_JOB_SETTINGS_ENV + "% -profile %" + YADE_JOB_PROFILE_ENV + "%");
                }
            }
        }
        if (!shellJob.getLanguage().equals("shell")) {// language always lower case
            if (shellJob.getYADE() == null) {
                scriptHeader.append(commentBegin).append(" language=").append(shellJob.getLanguage());
                if (shellJob.getClassName() != null) {
                    scriptHeader.append(",className=" + shellJob.getClassName());
                }
                scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            }
            if (isMock) {
                if (shellJob.getYADE() != null) {
                    scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
                    scriptHeader.append(commentBegin).append(" ").append(yadeCommand.toString().trim());
                    scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
                }
            }
        }
        if (shellJob.getYADE() == null) {
            if (job.getScript().getInclude() != null) {
                try {
                    Path p = findIncludeFile(pr, job.getPath(), job.getScript().getInclude().getIncludeFile());
                    if (p != null) {
                        scriptCommand.append(SOSPath.readFile(p, StandardCharsets.UTF_8));
                    } else {
                        scriptCommand.append(job.getScript().getInclude().getIncludeFile());
                        ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "[script][find include=" + job.getScript().getInclude().getNodeText()
                                + "]", "");
                    }
                } catch (Throwable e) {
                    scriptCommand.append(job.getScript().getInclude().getIncludeFile());
                    ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "[script][find include=" + job.getScript().getInclude().getNodeText()
                            + "]", e);
                }
                scriptCommand.append(CONFIG.getJobConfig().getScriptNewLine());
            }
            if (job.getScript().getScript() != null) {
                scriptCommand.append(job.getScript().getScript());
            }
        } else {
            scriptCommand.append(yadeCommand.toString().trim());
        }

        StringBuilder script = new StringBuilder(scriptHeader);
        if (isMock) {
            script.append(isUnix ? CONFIG.getMockConfig().getUnixScript() : CONFIG.getMockConfig().getWindowsScript());
            // script.append(" ");
        } else {
            script.append(scriptCommand);
        }

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());
        es.setV1Compatible(CONFIG.getJobConfig().getForcedV1Compatible());

        if (args != null) {
            String envVarPrefix = CONFIG.getJobConfig().isForcedV1Compatible() ? ENV_VAR_JS1_PREFIX : "";
            Map<String, String> upper = args.getAdditionalProperties().entrySet().stream().collect(Collectors.toMap(e -> envVarPrefix + e.getKey()
                    .toUpperCase(), e -> e.getValue()));
            args.getAdditionalProperties().clear();
            args.getAdditionalProperties().putAll(upper);

            es.setEnv(args);
        }

        return es;
    }

    private void setJobJobResources(Job j, ACommonJob job, JS7JobHelper jh) {
        if (job.getParams() != null && job.getParams().hasParams()) {
            // JOB RESOURCES
            List<String> names = new ArrayList<>();
            for (Include i : job.getParams().getIncludes()) {
                Path p = null;
                try {
                    p = findIncludeFile(pr, job.getPath(), i.getIncludeFile());
                    if (p == null) {
                        ConverterReport.INSTANCE.addWarningRecord(job.getPath(), "[params][find include=" + i.getNodeText() + "]", "not found");
                    } else {
                        String name = resolveJobResource(p);
                        if (name != null) {
                            names.add(name);
                        }
                    }
                } catch (Throwable e) {
                    ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "[params][find include=" + i.getNodeText() + "]", e);
                }
            }

            if (jh.getShellJob() != null && jh.getShellJob().getYADE() != null && job.getParams().getParams() != null) {
                boolean hasProfile = false;
                boolean hasSettings = false;
                for (Map.Entry<String, String> y : job.getParams().getParams().entrySet()) {
                    if (!hasProfile) {
                        hasProfile = y.getKey().toLowerCase().equals("profile");
                    }
                    if (!hasSettings) {
                        hasSettings = y.getKey().toLowerCase().equals("settings");
                    }
                }
                if (hasProfile && !hasSettings && pr.getYadeConfiguration() != null) {
                    names.add(YADE_MAIN_CONFIGURATION_JOBRESOURCE);
                }
            }

            if (names.size() > 0) {
                j.setJobResourceNames(names.stream().distinct().collect(Collectors.toList()));
            }
        }
    }

    private String resolveJobResource(Path p) {
        String name = null;
        if (jobResources.containsKey(p)) {
            name = jobResources.get(p);
        } else {
            String baseName = p.getFileName().toString().replace(".xml", "");
            long c = jobResources.entrySet().stream().filter(e -> e.getValue().equals(baseName)).count();
            if (c == 0) {
                name = baseName;
                jobResources.put(p, name);
            } else {
                Integer r = jobResourcesDuplicates.get(baseName);
                if (r == null) {
                    r = INITIAL_DUPLICATE_COUNTER;
                } else {
                    r++;
                }
                jobResourcesDuplicates.put(baseName, r);
                name = baseName + DUPLICATE_PREFIX + r;
                jobResources.put(p, name);
            }
        }
        return name;
    }

    public static Path findIncludeFile(DirectoryParserResult pr, Path currentPath, Path include) {
        String logPrefix = "[findIncludeFile][" + currentPath + "]";
        if (include.isAbsolute()) {
            LOGGER.debug(logPrefix + "[absolute]" + include);
            return Files.exists(include) ? include : null;
        }
        Path includePath = null;
        String ps = include.toString();
        LOGGER.debug(logPrefix + "include=" + include + "=" + pr);
        if (ps.startsWith("/") || ps.startsWith("\\")) {
            includePath = pr.getRoot().getPath().resolve(ps.substring(1)).normalize();
            LOGGER.debug(logPrefix + "[starts with / or \\]" + includePath);
        } else {
            includePath = currentPath.getParent().resolve(include).normalize();
            LOGGER.debug(logPrefix + "[relative]" + includePath);
        }
        return Files.exists(includePath) ? includePath : null;
    }

    private void setJobOptions(Job j, ACommonJob job) {
        if (job.getWarnIfLongerThan() != null) {
            j.setWarnIfLonger(job.getWarnIfLongerThan());
        }
        if (job.getWarnIfShorterThan() != null) {
            j.setWarnIfShorter(job.getWarnIfShorterThan());
        }
        if (job.getTasks() != null) {
            j.setParallelism(job.getTasks());
        }
        if (j.getParallelism() == null) {
            j.setParallelism(1);
        }
        if (job.getTimeout() != null) {
            j.setTimeout(Long.valueOf(SOSDate.getTimeAsSeconds(job.getTimeout())).intValue());
        }

        if (CONFIG.getJobConfig().getForcedFailOnErrWritten() == null) {
            if (job.getStderrLogLevel() != null && job.getStderrLogLevel().toLowerCase().equals("error")) {
                j.setFailOnErrWritten(true);
            }
        }
    }

    private void setJobNotification(Job j, ACommonJob job) {
        if (job.getSettings() != null && job.getSettings().hasMailSettings()) {
            List<JobNotificationType> types = new ArrayList<>();
            if (job.getSettings().isMailOnError()) {
                types.add(JobNotificationType.ERROR);
            }
            if (job.getSettings().isMailOnWarning()) {
                types.add(JobNotificationType.WARNING);
            }
            if (job.getSettings().isMailOnSuccess()) {
                types.add(JobNotificationType.SUCCESS);
            }

            String mailTo = SOSString.isEmpty(job.getSettings().getMailTo()) ? CONFIG.getJobConfig().getNotificationMailDefaultTo() : job
                    .getSettings().getMailTo();
            String mailCc = SOSString.isEmpty(job.getSettings().getMailCc()) ? CONFIG.getJobConfig().getNotificationMailDefaultCc() : job
                    .getSettings().getMailCc();
            String mailBcc = SOSString.isEmpty(job.getSettings().getMailBcc()) ? CONFIG.getJobConfig().getNotificationMailDefaultBcc() : job
                    .getSettings().getMailBcc();

            if (types.size() == 0) {
                if (!SOSString.isEmpty(mailTo) || !SOSString.isEmpty(mailCc) || !SOSString.isEmpty(mailBcc)) {
                    types.add(JobNotificationType.ERROR);
                    types.add(JobNotificationType.WARNING);
                }
            }

            if (types.size() > 0) {
                j.setNotification(new JobNotification(types, new JobNotificationMail(mailTo, mailCc, mailBcc)));
            }
        }
    }

    private void convertJobChains(JS7ConverterResult result) {
        ConverterObject<JobChain> o = converterObjects.jobChains;
        for (Map.Entry<String, JobChain> entry : o.unique.entrySet()) {
            convertJobChainWorkflow(result, entry.getKey(), entry.getValue());
        }

        LOGGER.info("[convertJobChains]duplicates=" + o.duplicates.size());
        if (o.duplicates.size() > 0) {
            for (Map.Entry<String, List<JobChain>> entry : o.duplicates.entrySet()) {
                LOGGER.info("[convertJobChains][duplicate]" + entry.getKey());
                int counter = INITIAL_DUPLICATE_COUNTER;
                for (JobChain jn : entry.getValue()) {
                    String js7Name = entry.getKey() + DUPLICATE_PREFIX + counter;
                    LOGGER.info("[convertJobChains][duplicate][" + entry.getKey() + "][js7Name=" + js7Name + "][path]" + jn.getPath());
                    convertJobChainWorkflow(result, js7Name, jn);
                    counter++;
                }
            }
        }
    }

    private Parameter getStringParameter(String defaultValue) {
        Parameter p = new Parameter();
        p.setType(ParameterType.String);
        if (defaultValue != null) {
            p.setDefault(JS7ConverterHelper.quoteValue4JS7(replaceJS1Values(defaultValue)));
        }
        return p;
    }

    private Workflow setWorkflowOrderPreparationOrResources(Workflow w, JobChain jobChain, Map<String, JobChainStateHelper> usedStates,
            List<JobChainNodeFileOrderSource> fileOrderSources) {
        Parameters parameters = null;
        if (fileOrderSources.size() > 0) {
            parameters = new Parameters();
            parameters.setAdditionalProperty("file", getStringParameter(null));
            parameters.setAdditionalProperty("source_file", getStringParameter(VAR_CURRENT_FILE_JS7));
            parameters.setAdditionalProperty("SCHEDULER_FILE_PATH", getStringParameter(VAR_CURRENT_FILE_JS7));
        }

        Map<String, String> params = new HashMap<>();
        if (jobChain.getConfig() != null && jobChain.getConfig().hasOrderParams()) {
            params.putAll(jobChain.getConfig().getOrderParams().getParams());
        }

        List<String> jobResources = new ArrayList<>();
        boolean hasOrders = false;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        for (JobChainOrder o : jobChain.getOrders()) {
            if (o.getState() != null) {
                if (!usedStates.containsKey(o.getState())) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[setWorkflowOrderPreparationOrResources][%s][state=%s][skip]because state not used", o.getPath(),
                                o.getState()));
                    }
                    continue;
                }
            }

            Params ps = o.getParams();
            if (ps != null && ps.hasParams()) {
                hasOrders = true;
                params.putAll(ps.getParams());

                for (Include i : ps.getIncludes()) {
                    Path path = null;
                    try {
                        path = findIncludeFile(pr, o.getPath(), i.getIncludeFile());
                        if (path == null) {
                            ConverterReport.INSTANCE.addWarningRecord(o.getPath(), "[order params][include=" + i.getNodeText() + "]", "not found");
                        } else {
                            String name = resolveJobResource(path);
                            if (name != null) {
                                jobResources.add(name);
                            }
                        }
                    } catch (Throwable e) {
                        ConverterReport.INSTANCE.addErrorRecord(o.getPath(), "[order params][include=" + i.getNodeText() + "]" + e.toString(), e);
                    }
                }
            }
        }

        if (params.size() > 0) {
            if (parameters == null) {
                parameters = new Parameters();
            }
            for (Map.Entry<String, String> e : params.entrySet()) {
                parameters.setAdditionalProperty(getParamNameFromJS1Order(usedStates, e.getKey()), getStringParameter(hasOrders ? null : e
                        .getValue()));
            }
        }

        if (parameters != null) {
            w.setOrderPreparation(new Requirements(parameters, false));
        }
        if (jobResources.size() > 0) {
            w.setJobResourceNames(jobResources.stream().distinct().collect(Collectors.toList()));
        }
        return w;
    }

    private String getParamNameFromJS1Order(Map<String, JobChainStateHelper> usedStates, String paramName) {
        String name = paramName;
        int inx = name.indexOf("/");// order_state/param_name
        if (inx > -1) {
            String state = name.substring(0, inx);
            if (usedStates.containsKey(state)) {
                name = paramName.substring(inx + 1);
            }
        }
        return name;
    }

    private void convertJobChainWorkflow(JS7ConverterResult result, String js7Name, JobChain jobChain) {
        String method = "convertJobChainWorkflow";
        LOGGER.info("[" + method + "]" + jobChain.getPath());

        Workflow w = new Workflow();
        w.setTitle(jobChain.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());

        Map<String, JobChainJobHelper> uniqueJobs = new LinkedHashMap<>();
        Map<String, JobChainStateHelper> allStates = new LinkedHashMap<>();
        Map<String, JobChainNodeFileOrderSink> fileOrderSinkStates = new HashMap<>();
        List<JobChainNodeFileOrderSource> fileOrderSources = new ArrayList<>();
        int duplicateJobCounter = 0;
        for (AJobChainNode n : jobChain.getNodes()) {
            switch (n.getType()) {
            case NODE:
                JobChainNode jcn = (JobChainNode) n;
                if (jcn.getJob() == null) {
                    if (jcn.getState() != null) {
                        allStates.put(jcn.getState(), new JobChainStateHelper(jcn, null, null));
                    }
                } else {
                    Path job = null;
                    try {
                        job = findIncludeFile(pr, jobChain.getPath(), Paths.get(jcn.getJob() + EConfigFileExtensions.JOB.extension()));
                        if (job == null) {
                            ConverterReport.INSTANCE.addWarningRecord(job, "[find job file][jobChain " + jobChain.getName() + "/node=" + SOSString
                                    .toString(n) + "]", "not found");
                        } else {
                            try {
                                OrderJob oj = orderJobs.get(job);
                                if (oj == null) {
                                    throw new Exception("[job " + job + "]not found");
                                }
                                String js1JobName = oj.getName();
                                String js7JobName = getJS7ObjectName(oj.getPath(), js1JobName);
                                if (uniqueJobs.containsKey(js1JobName)) {
                                    OrderJob uoj = uniqueJobs.get(js1JobName).job;
                                    // same name but another location
                                    if (!uoj.getPath().equals(oj.getPath())) {
                                        duplicateJobCounter++;
                                        js1JobName = js1JobName + NAME_CONCAT_CHARACTER + duplicateJobCounter;
                                        js7JobName = js7JobName + NAME_CONCAT_CHARACTER + duplicateJobCounter;
                                        uniqueJobs.put(js1JobName, new JobChainJobHelper(oj, js7JobName));
                                    }
                                } else {
                                    uniqueJobs.put(js1JobName, new JobChainJobHelper(oj, js7JobName));
                                }

                                allStates.put(jcn.getState(), new JobChainStateHelper(jcn, js1JobName, js7JobName));
                            } catch (Throwable e) {
                                LOGGER.warn("[jobChain " + jobChain.getPath() + "/node=" + SOSString.toString(n) + "]" + e.getMessage());
                                ConverterReport.INSTANCE.addWarningRecord(job, "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
                                        + "]", e.toString());
                            }
                        }
                    } catch (Throwable e) {
                        ConverterReport.INSTANCE.addErrorRecord(job, "[find job file][jobChain " + jobChain.getName() + "/node=" + SOSString.toString(
                                n) + "]", e);
                    }
                }
                break;
            case ORDER_SINK:
                JobChainNodeFileOrderSink fos = (JobChainNodeFileOrderSink) n;
                if (!SOSString.isEmpty(fos.getState())) {
                    fileOrderSinkStates.put(fos.getState(), fos);
                } else {
                    ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
                            + "]", "state not found");
                }
                break;
            case ORDER_SOURCE:
                fileOrderSources.add((JobChainNodeFileOrderSource) n);
                break;
            case JOB_CHAIN:
                ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
                        + "]", "not implemented yet");
                break;
            case END:
                ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
                        + "]", "not implemented yet");
                break;
            }
        }

        List<Instruction> in = new ArrayList<>();
        Map<String, List<Instruction>> workflowInstructions = new LinkedHashMap<>();
        String startState = getNodesStartState(allStates);
        Map<String, String> fileOrderSinkJobs = new HashMap<>();

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            allStates.entrySet().forEach(e -> {
                LOGGER.debug(String.format("[convertJobChainWorkflow][allStates]state=%s,helper=%s", e.getKey(), SOSString.toString(e.getValue())));
            });
        }

        Map<String, JobChainStateHelper> usedStates = new LinkedHashMap<>();
        BoardHelper boardHelper = new BoardHelper();
        if (startState != null) {
            in.addAll(getNodesInstructions(workflowInstructions, jobChain, uniqueJobs, startState, allStates, usedStates, fileOrderSinkStates,
                    fileOrderSinkJobs, boardHelper, false));
        } else {
            ConverterReport.INSTANCE.addErrorRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "]", "startState not found");
        }

        if (isDebugEnabled) {
            usedStates.entrySet().forEach(e -> {
                LOGGER.debug(String.format("[convertJobChainWorkflow][usedStates]state=%s,helper=%s", e.getKey(), SOSString.toString(e.getValue())));
            });
        }

        Map<String, JobChainStateHelper> notUsedStates = new LinkedHashMap<>();
        for (Map.Entry<String, JobChainStateHelper> e : allStates.entrySet()) {
            if (e.getValue().js1JobName == null) {
                continue;
            }
            if (!usedStates.containsKey(e.getKey())) {
                notUsedStates.put(e.getKey(), e.getValue());
            }
        }
        w = setWorkflowOrderPreparationOrResources(w, jobChain, usedStates, fileOrderSources);

        JS7Agent jobChainAgent = getJobChainAgent(jobChain, false);
        Jobs js = new Jobs();
        Set<String> usedJobNames = usedStates.entrySet().stream().filter(e -> e.getValue().js1JobName != null).map(e -> e.getValue().js1JobName)
                .distinct().collect(Collectors.toSet());
        uniqueJobs.entrySet().forEach(e -> {
            OrderJob js1Job = e.getValue().job;
            if (usedJobNames.contains(e.getKey())) {
                Job job = getJob(result, js1Job, jobChainAgent);
                if (job == null) {
                    if (js1Job.isJavaJITLSplitterJob() || js1Job.isJavaJITLJoinJob()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][Job %s=%s][not converted]splitter/join job", method, jobChain.getPath(), e.getKey(),
                                    js1Job.getPath()));
                        }
                    } else if (js1Job.isJavaJITLSynchronizerJob()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][Job %s=%s][not converted]synchronizer job", method, jobChain.getPath(), e.getKey(),
                                    js1Job.getPath()));
                        }
                    } else {
                        try {
                            ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "]", "Job " + js1Job
                                    .getPath() + " cannot be converted");
                        } catch (Throwable ee) {
                        }
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][Job %s=%s]cannot be converted", method, jobChain.getPath(), e.getKey(), js1Job
                                    .getPath()));
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s]Job %s=%s", method, jobChain.getPath(), e.getKey(), js1Job.getPath()));
                    }
                    job.setAdmissionTimeScheme(JS7RunTimeConverter.convert(js1Job));
                    js.setAdditionalProperty(e.getValue().js7JobName, job);
                }
            } else {
                // try {
                // ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "]", "Job " + e.getValue()
                // .getPath() + " not used");
                // } catch (Throwable ee) {
                // }
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][Job %s=%s]not used in the current workflow. handle separately", method, jobChain.getPath(), e
                            .getKey(), js1Job.getPath()));
                }
            }
        });
        w.setJobs(js);

        // FileOrderSources and FileOrderSink
        JS7Agent jobChainFileWatchingAgent = getJobChainAgent(jobChain, true);
        if (jobChainFileWatchingAgent == null) {
            jobChainFileWatchingAgent = getAgent(null, null, null, null);
        }
        for (Map.Entry<String, String> e : fileOrderSinkJobs.entrySet()) {
            w.getJobs().getAdditionalProperties().put(e.getKey(), getFileOrderSinkJob(e.getKey(), jobChainFileWatchingAgent));
        }

        w.setInstructions(in);

        Path workflowPath = null;

        // when only 1 job in the workflow and for all another jobs the new workflows creates - rename the main workflow (add state)
        if (w.getJobs().getAdditionalProperties().size() == 1 && usedStates.size() == 1 && notUsedStates.size() > 0) {
            Map.Entry<String, JobChainStateHelper> firstStateHelper = usedStates.entrySet().stream().findFirst().orElse(null);
            if (firstStateHelper != null) {
                Map.Entry<String, Job> firstJob = w.getJobs().getAdditionalProperties().entrySet().stream().findFirst().orElse(null);
                if (firstJob != null) {
                    if (firstJob.getKey().equals(firstStateHelper.getValue().js7JobName)) {
                        workflowPath = getWorkflowPathFromJS1Path(result, jobChain.getPath(), (js7Name + NAME_CONCAT_CHARACTER + firstStateHelper
                                .getValue().js7State));
                    }
                }
            }
        }

        if (workflowPath == null) {
            workflowPath = getWorkflowPathFromJS1Path(result, jobChain.getPath(), js7Name);
        }
        result.add(workflowPath, w);

        String workflowName = getWorkflowName(workflowPath);
        handleBoardHelpers(boardHelper, workflowPath, workflowName);
        convertJobChainOrders2Schedules(result, jobChain, usedStates, workflowPath, workflowName);
        convertJobChainFileOrderSources(result, jobChain, fileOrderSources, workflowPath, workflowName, jobChainFileWatchingAgent);

        if (notUsedStates.size() > 0) {
            Path mainWorkflowPath = getWorkflowPathFromJS1Path(result, jobChain.getPath(), js7Name);
            ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "Splitting into several workflows", "START");
            ConverterReport.INSTANCE.addAnalyzerRecord(null, workflowName);
            convertJobChainWorkflowNotUsedStates(result, jobChain, uniqueJobs, notUsedStates, workflowPath, getWorkflowName(mainWorkflowPath));
            ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "Splitting into several workflows", "END");
        }
    }

    private void handleBoardHelpers(BoardHelper workflowBoardHelper, Path workflowPath, String workflowName) {
        if (workflowBoardHelper.js7States.size() > 0) {
            Map<Path, List<String>> map = new HashMap<>();
            for (Map.Entry<String, Path> e : workflowBoardHelper.js7States.entrySet()) {
                List<String> al;
                if (map.containsKey(e.getValue())) {
                    al = map.get(e.getValue());
                } else {
                    al = new ArrayList<>();
                }
                al.add(e.getKey());
                map.put(e.getValue(), al);
            }

            for (Map.Entry<Path, List<String>> e : map.entrySet()) {
                List<BoardHelper> al;
                if (js7BoardHelpers.containsKey(e.getKey())) {
                    al = js7BoardHelpers.get(e.getKey());
                } else {
                    al = new ArrayList<>();
                }
                BoardHelper b = new BoardHelper();
                b.workflowPath = workflowPath;
                b.workflowName = workflowName;
                for (String js7State : e.getValue()) {
                    b.js7States.put(js7State, e.getKey());
                }
                al.add(b);
                js7BoardHelpers.put(e.getKey(), al);
            }
        }
    }

    private void convertJobChainWorkflowNotUsedStates(JS7ConverterResult result, JobChain jobChain, Map<String, JobChainJobHelper> uniqueJobs,
            Map<String, JobChainStateHelper> notUsedStates, Path workflowPath, String workflowName) {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        String method = "convertJobChainWorkflowNotUsedStates";
        notUsedStates.entrySet().forEach(e -> {
            JobChainJobHelper jh = uniqueJobs.get(e.getValue().js1JobName);
            if (jh == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][main workflow][%s][state=%s]job not found", method, workflowPath, e.getKey()));
                }
            } else {
                OrderJob job = uniqueJobs.get(e.getValue().js1JobName).job;
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][main workflow][%s]state=%s", method, workflowPath, e.getKey()));
                }
                String js7Name = workflowName + NAME_CONCAT_CHARACTER + e.getValue().js7State;
                String newWorkflowName = convertStandaloneWorkflow(result, js7Name, job, workflowPath, js7Name);
                ConverterReport.INSTANCE.addAnalyzerRecord(null, newWorkflowName);

                if (jobChain.getOrders() != null) {

                    List<JobChainOrder> orders = jobChain.getOrders().stream().filter(o -> o.getState() != null && o.getState().equals(e
                            .getValue().js1State) && o.getRunTime() != null && !o.getRunTime().isEmpty()).collect(Collectors.toList());

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][main workflow][%s][state=%s]order files", method, workflowPath, e.getKey(), orders.size()));
                    }
                    boolean useAdd = orders.size() > 1;
                    boolean hasConfigProcess = jobChain.getConfig() != null && jobChain.getConfig().hasProcess();
                    for (JobChainOrder o : orders) {
                        String js7OrderName = getJS7ObjectName(o.getPath(), o.getName());
                        String add = "";
                        if (useAdd) {
                            add = NAME_CONCAT_CHARACTER + js7OrderName;
                        }

                        //
                        List<OrderParameterisation> l = new ArrayList<>();
                        boolean hasOrderParams = o.getParams() != null && o.getParams().hasParams();
                        if (hasConfigProcess || hasOrderParams) {
                            OrderParameterisation set = new OrderParameterisation();
                            set.setOrderName(js7OrderName);

                            Variables vs = new Variables();
                            if (hasConfigProcess) {
                                Params processParams = jobChain.getConfig().getProcess().get(e.getValue().js1State);
                                if (processParams != null && processParams.hasParams()) {
                                    processParams.getParams().entrySet().forEach(p -> {
                                        vs.setAdditionalProperty(p.getKey(), p.getValue());
                                    });
                                }
                            }
                            if (hasOrderParams) {
                                o.getParams().getParams().entrySet().forEach(p -> {
                                    vs.setAdditionalProperty(getParamNameFromJS1Order(notUsedStates, p.getKey()), p.getValue());
                                });
                            }
                            set.setVariables(vs);
                            l.add(set);
                        }
                        //
                        RunTimeHelper rth = convertRunTimeForSchedule("ORDER", o.getRunTime(), workflowPath, newWorkflowName, add);
                        if (rth != null && rth.schedule != null) {
                            Schedule s = rth.schedule;
                            s.setTitle(o.getTitle());

                            if (l.size() > 0) {
                                s.setOrderParameterisations(l);
                            }
                            result.add(rth.path, s);
                        } else {
                            addJS1ScheduleFromScheduleOrRunTime(o.getRunTime(), l, null, workflowPath, newWorkflowName, add);
                        }
                    }
                }
            }
        });
    }

    private JS7Agent getJobChainAgent(JobChain jobChain, boolean checkFileWatching) {
        if (checkFileWatching) {
            if (jobChain.getFileWatchingProcessClass() != null) {
                return getAgent(jobChain.getFileWatchingProcessClass(), null, null, jobChain);
            }
        }
        return jobChain.getProcessClass() != null ? getAgent(jobChain.getProcessClass(), null, null, jobChain) : null;
    }

    private void convertJobChainFileOrderSources(JS7ConverterResult result, JobChain jobChain, List<JobChainNodeFileOrderSource> fileOrderSources,
            Path workflowPath, String workflowName, JS7Agent js7Agent) {
        if (fileOrderSources.size() > 0) {
            boolean useNextState = fileOrderSources.size() > 1;
            for (JobChainNodeFileOrderSource n : fileOrderSources) {
                String name = workflowName;
                if (useNextState) {
                    name = name + NAME_CONCAT_CHARACTER + n.getNextState();
                }
                FileOrderSource fos = new FileOrderSource();
                fos.setWorkflowName(workflowName);
                fos.setAgentName(js7Agent.getJS7AgentName());
                fos.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());
                fos.setDirectoryExpr(JS7ConverterHelper.quoteValue4JS7(n.getDirectory()));
                fos.setPattern(n.getRegex());
                Long delay = null;
                if (n.getRepeat() != null && !n.getRepeat().toLowerCase().equals("no")) {
                    try {
                        delay = Long.parseLong(n.getRepeat());
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s][fileOrderSource nextState=%s][repeat=%s]%s", workflowPath, n.getNextState(), n.getRepeat(), e
                                .toString()));
                        ConverterReport.INSTANCE.addErrorRecord(workflowPath, "[fileOrderSource nextState=" + n.getNextState() + "]repeat=" + n
                                .getRepeat(), e);
                    }
                }
                fos.setDelay(delay);
                result.add(getFileOrderSourcePathFromJS7Path(workflowPath, name), fos);
            }
        }
    }

    private void convertJobChainOrders2Schedules(JS7ConverterResult result, JobChain jobChain, Map<String, JobChainStateHelper> usedStates,
            Path workflowPath, String workflowName) {
        if (jobChain.getOrders().size() > 0) {
            List<JobChainOrder> orders = jobChain.getOrders().stream().filter(o -> o.getRunTime() != null && !o.getRunTime().isEmpty()).collect(
                    Collectors.toList());
            if (orders.size() > 0) {
                boolean hasJobChainConfigOrderParams = jobChain.getConfig() != null && jobChain.getConfig().hasOrderParams();
                boolean isDebugEnabled = LOGGER.isDebugEnabled();

                List<JobChainOrder> checkedOrders = new ArrayList<>();
                for (JobChainOrder o : orders) {
                    if (o.getState() != null) {
                        if (!usedStates.containsKey(o.getState())) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format(
                                        "[convertJobChainOrders2Schedules][%s][state=%s][skip]because state not used in the current workflow. handle later..",
                                        o.getPath(), o.getState()));
                            }

                            // ConverterReport.INSTANCE.addWarningRecord(o.getPath(), "skip convert to schedule", "state=" + o.getState()
                            // + " becase state not used in the workflow");
                            continue;
                        }
                    }
                    checkedOrders.add(o);
                }

                boolean useAdd = checkedOrders.size() > 1;
                for (JobChainOrder o : checkedOrders) {
                    String startPosition = null;
                    if (o.getState() != null) {
                        // TODO
                        startPosition = "0";
                    }
                    String js7OrderName = getJS7ObjectName(o.getPath(), o.getName());
                    String add = "";
                    if (useAdd) {
                        add = NAME_CONCAT_CHARACTER + js7OrderName;
                    }
                    //
                    List<OrderParameterisation> l = new ArrayList<>();
                    boolean hasOrderParams = o.getParams() != null && o.getParams().hasParams();
                    if (hasJobChainConfigOrderParams || hasOrderParams) {
                        OrderParameterisation set = new OrderParameterisation();
                        set.setOrderName(js7OrderName);

                        Variables vs = new Variables();
                        if (hasJobChainConfigOrderParams) {
                            jobChain.getConfig().getOrderParams().getParams().entrySet().forEach(e -> {
                                vs.setAdditionalProperty(e.getKey(), e.getValue());
                            });
                        }
                        if (hasOrderParams) {
                            o.getParams().getParams().entrySet().forEach(e -> {
                                vs.setAdditionalProperty(getParamNameFromJS1Order(usedStates, e.getKey()), e.getValue());
                            });
                        }
                        set.setVariables(vs);
                        l.add(set);
                    }
                    //
                    RunTimeHelper rth = convertRunTimeForSchedule("ORDER", o.getRunTime(), workflowPath, workflowName, add);
                    if (rth != null && rth.schedule != null) {
                        Schedule s = rth.schedule;
                        s.setTitle(o.getTitle());

                        if (l.size() > 0) {
                            s.setOrderParameterisations(l);
                        }

                        if (startPosition != null) {
                            setSchedulePosition(s, startPosition);
                        }
                        result.add(rth.path, s);
                    } else {
                        addJS1ScheduleFromScheduleOrRunTime(o.getRunTime(), l, startPosition, workflowPath, workflowName, add);
                    }
                }
            }
        }
    }

    private void setSchedulePosition(Schedule schedule, String startPosition) {
        if (startPosition == null || startPosition.equals("0")) {
            return;
        }

        List<OrderParameterisation> l = schedule.getOrderParameterisations();
        if (l == null || l.size() == 0) {
            l = new ArrayList<>();
            l.add(new OrderParameterisation());
        }
        OrderParameterisation op = l.get(0);
        OrderPositions p = new OrderPositions();
        p.setStartPosition(Collections.singletonList(startPosition));
        op.setPositions(p);
        schedule.setOrderParameterisations(l);
    }

    private String getNodesStartState(Map<String, JobChainStateHelper> states) {
        for (Map.Entry<String, JobChainStateHelper> entry : states.entrySet()) {
            long c = states.entrySet().stream().filter(e -> e.getValue().js1NextState.equals(entry.getKey()) || e.getValue().js1ErrorState.equals(
                    entry.getKey())).count();
            if (c == 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    private List<Instruction> getNodesInstructions(Map<String, List<Instruction>> workflowInstructions, JobChain jobChain,
            Map<String, JobChainJobHelper> uniqueJobs, String js1StartState, Map<String, JobChainStateHelper> allStates,
            Map<String, JobChainStateHelper> usedStates, Map<String, JobChainNodeFileOrderSink> fileOrderSinkStates,
            Map<String, String> fileOrderSinkJobs, BoardHelper boardHelper, boolean isFork) {
        List<Instruction> result = new ArrayList<>();

        JobChainStateHelper h = allStates.get(js1StartState);
        boolean hasConfigOrderParams = jobChain.getConfig() != null && jobChain.getConfig().hasOrderParams();
        boolean hasConfigProcess = jobChain.getConfig() != null && jobChain.getConfig().hasProcess();
        while (h != null) {
            JobChainJobHelper jh = uniqueJobs.get(h.js1JobName);
            if (jh == null) {
                return new ArrayList<>();
            }

            OrderJob job = uniqueJobs.get(h.js1JobName).job;
            usedStates.put(h.js1State, h);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getNodesInstructions]state=%s,isFork=%s,isJoinJob=%s,isSynchronizerJob=%s", h.js1State, isFork, job
                        .isJavaJITLJoinJob(), job.isJavaJITLSynchronizerJob()));
            }

            if (isFork && job.isJavaJITLJoinJob()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[getNodesInstructions][skip][state=%s]because isFork and isJoinJob", h.js1State));
                }
                h = null;
                continue;
            }

            List<Instruction> in = new ArrayList<>();

            if (job.isJavaJITLSplitterJob()) {
                ForkJoin forkJoin = new ForkJoin();
                List<Branch> branches = new ArrayList<>();
                int b = 1;

                List<String> splitterStates = getSplitterStates(job, jobChain, h.js1State);
                for (String splitterState : splitterStates) {
                    JobChainStateHelper sh = allStates.get(splitterState);
                    if (sh == null) {
                        ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "Splitter Job(state=" + h.js1State + ")", "State "
                                + splitterState + " not found");
                    }
                    BranchWorkflow bw = new BranchWorkflow(getNodesInstructions(workflowInstructions, jobChain, uniqueJobs, splitterState, allStates,
                            usedStates, fileOrderSinkStates, fileOrderSinkJobs, boardHelper, true), null);
                    branches.add(new Branch("branch_" + b, bw));
                    b++;
                }
                forkJoin.setBranches(branches);
                in.add(forkJoin);
            } else if (job.isJavaJITLJoinJob()) {

            } else if (job.isJavaJITLSynchronizerJob()) {
                in.add(new PostNotices(Collections.singletonList("sospn-" + h.js7State)));

                ExpectNotices en = new ExpectNotices();
                en.setNoticeBoardNames("sosen-" + h.js7State);
                in.add(en);

                boardHelper.js7States.put(h.js7State, job.getPath());
                if (!sinkJobs.containsKey(job.getPath())) {
                    sinkJobs.put(job.getPath(), getUniqueSinkJobName(h.js7JobName, null));
                }

            } else {
                SOSParameterSubstitutor ps = new SOSParameterSubstitutor(true, "${", "}");
                if (hasConfigOrderParams) {
                    jobChain.getConfig().getOrderParams().getParams().entrySet().forEach(e -> {
                        ps.addKey(e.getKey(), e.getValue());
                    });
                }
                in.add(getNamedJobInstruction(job, h.js7JobName, h.js7State, hasConfigProcess ? jobChain.getConfig().getProcess().get(h.js1State)
                        : null, ps));
            }

            String onError = h.onError.toLowerCase();
            switch (onError) {
            case "setback":
                in = getRetryInstructions(job, in);
                break;
            case "suspend":
                // nothing to do - is default JS7 behaviour
                // in = getSuspendInstructions(job, in, h);
                break;
            }

            workflowInstructions.put(h.js1State, in);

            boolean hasErrorStateJob = allStates.get(h.js1ErrorState) != null && allStates.get(h.js1ErrorState).js1JobName != null;
            if (h.js1ErrorState.length() > 0 && (hasErrorStateJob || fileOrderSinkStates.containsKey(h.js1ErrorState))) {
                TryCatch tryCatch = new TryCatch();
                tryCatch.setTry(new Instructions(workflowInstructions.get(h.js1State)));

                boolean add = false;
                if (hasErrorStateJob) {
                    tryCatch.setCatch(new Instructions(getNodesInstructions(workflowInstructions, jobChain, uniqueJobs, h.js1ErrorState, allStates,
                            usedStates, fileOrderSinkStates, fileOrderSinkJobs, boardHelper, false)));
                    add = true;
                } else {
                    NamedJob nj = getFileOrderSinkNamedJob(fileOrderSinkStates.get(h.js1ErrorState), h.js1ErrorState, fileOrderSinkJobs);
                    if (nj != null) {
                        List<Instruction> al = new ArrayList<>();
                        al.add(nj);
                        tryCatch.setCatch(new Instructions(al));
                        add = true;
                    }
                }
                if (add) {
                    // TODO Finish ??? error ???
                    if (tryCatch.getCatch().getInstructions() == null) {
                        tryCatch.getCatch().setInstructions(new ArrayList<>());
                    }
                    tryCatch.getCatch().getInstructions().add(new Finish());

                    result.add(tryCatch);
                }
            } else if (workflowInstructions.get(h.js1State) != null) {
                result.addAll(workflowInstructions.get(h.js1State));
            }

            if (h.js1NextState.length() > 0 && !h.js1NextState.equals(h.js1ErrorState)) {
                String nextState = h.js1NextState;
                h = allStates.get(nextState);
                if (h == null) {
                    //
                } else if (h.js1JobName == null) {
                    h = null;
                }
                if (h == null && fileOrderSinkStates.containsKey(nextState)) {
                    NamedJob nj = getFileOrderSinkNamedJob(fileOrderSinkStates.get(nextState), nextState, fileOrderSinkJobs);
                    if (nj != null) {
                        result.add(nj);
                    }
                }
            } else {
                h = null;
            }
        }
        return result;
    }

    private String getUniqueSinkJobName(String mainName, String newName) {
        String name = newName == null ? mainName : newName;
        Map.Entry<Path, String> s = sinkJobs.entrySet().stream().filter(e -> e.getValue().equals(name)).findFirst().orElse(null);
        if (s == null) {
            return name;
        }
        if (sinkJobsDuplicateCounter > 100) {
            return name;
        }
        sinkJobsDuplicateCounter++;
        return getUniqueSinkJobName(mainName, mainName + NAME_CONCAT_CHARACTER + sinkJobsDuplicateCounter);
    }

    private List<String> getSplitterStates(OrderJob js1Job, JobChain js1JobChain, String state) {
        String paramName = "state_names";

        String splitterStates = null;
        // CONFIG
        if (js1JobChain.getConfig() != null) {
            if (js1JobChain.getConfig().hasProcess()) {
                Params p = js1JobChain.getConfig().getProcess().get(state);
                if (p != null && p.getParams() != null) {
                    String s = p.getParams().get(paramName);
                    if (!SOSString.isEmpty(s)) {
                        splitterStates = s;
                    }
                }
            }
            if (SOSString.isEmpty(splitterStates) && js1JobChain.getConfig().getOrderParams() != null && js1JobChain.getConfig().getOrderParams()
                    .getParams() != null) {
                String s = js1JobChain.getConfig().getOrderParams().getParams().get(paramName);
                if (!SOSString.isEmpty(s)) {
                    splitterStates = s;
                }
            }
        }

        // ORDER
        // TODO multiple files ....
        if (SOSString.isEmpty(splitterStates) && js1JobChain.getOrders() != null && js1JobChain.getOrders().size() > 0) {
            for (JobChainOrder o : js1JobChain.getOrders()) {
                if (o.getParams() != null) {
                    if (o.getParams().getParams() != null) {
                        String s = o.getParams().getParams().get(paramName);
                        if (!SOSString.isEmpty(s)) {
                            splitterStates = s;
                        }
                    }

                    if (SOSString.isEmpty(splitterStates) && o.getParams().getIncludes() != null) {
                        for (Include in : o.getParams().getIncludes()) {
                            Params p = include2params(o.getPath(), in);
                            if (p != null && p.hasParams()) {
                                String s = p.getParams().get(paramName);
                                if (!SOSString.isEmpty(s)) {
                                    splitterStates = s;
                                }
                            }
                        }
                    }
                }
            }
        }

        // JOB
        if (SOSString.isEmpty(splitterStates) && js1Job.getParams() != null) {
            if (js1Job.getParams().getParams() != null) {
                splitterStates = js1Job.getParams().getParams().get(paramName);
            }
            if (SOSString.isEmpty(splitterStates) && js1Job.getParams().getIncludes() != null) {
                for (Include in : js1Job.getParams().getIncludes()) {
                    Params p = include2params(js1Job.getPath(), in);
                    if (p != null && p.hasParams()) {
                        String s = p.getParams().get(paramName);
                        if (!SOSString.isEmpty(s)) {
                            splitterStates = s;
                        }
                    }
                }
            }
        }

        if (SOSString.isEmpty(splitterStates)) {
            ConverterReport.INSTANCE.addWarningRecord(js1JobChain.getPath(), "Splitter Job(state=" + state + ")", "Parameter " + paramName
                    + " not found or is empty");

            return new ArrayList<>();
        }
        String delimiter = splitterStates.indexOf(";") > -1 ? ";" : ",";
        return Stream.of(splitterStates.split(delimiter)).map(e -> e.trim()).collect(Collectors.toList());
    }

    private Params include2params(Path currentPath, Include i) {
        Path path = null;
        try {
            path = findIncludeFile(pr, currentPath, i.getIncludeFile());
            if (path == null) {
                ConverterReport.INSTANCE.addWarningRecord(currentPath, "[include2params][include=" + i.getNodeText() + "]", "not found");
            } else {
                try {
                    return new Params(SOSXML.newXPath(), JS7ConverterHelper.getDocumentRoot(path));
                } catch (Exception e) {
                    ConverterReport.INSTANCE.addErrorRecord(currentPath, "[include2params][include=" + i.getNodeText() + "]" + e.toString(), e);
                }

            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addErrorRecord(currentPath, "[include2params][include=" + i.getNodeText() + "]" + e.toString(), e);
        }

        return null;
    }

    private NamedJob getFileOrderSinkNamedJob(JobChainNodeFileOrderSink fos, String label, Map<String, String> fileOrderSinkJobs) {
        if (SOSString.isEmpty(fos.getMoveTo()) && fos.getRemove() == null) {
            return null;
        }
        boolean isRemove = SOSString.isEmpty(fos.getMoveTo()) && fos.getRemove() != null;

        NamedJob job = new NamedJob(isRemove ? REMOVE_JOB_NAME : MOVE_JOB_NAME);
        fileOrderSinkJobs.put(job.getJobName(), job.getJobName());
        job.setLabel(label);

        Environment env = new Environment();
        env.setAdditionalProperty("gracious", "true");
        if (!isRemove) {
            env.setAdditionalProperty("target_file", JS7ConverterHelper.quoteValue4JS7(fos.getMoveTo()));
        }
        job.setDefaultArguments(env);
        return job;
    }

    private Job getFileOrderSinkJob(String jobName, JS7Agent jobChainAgent) {
        Job job = new Job();
        setAgent(job, jobChainAgent);
        setFromConfig(job);

        ExecutableJava ex = new ExecutableJava();
        switch (jobName) {
        case MOVE_JOB_NAME:
            ex.setClassName("com.sos.jitl.jobs.file.RenameFileJob");
            break;
        case REMOVE_JOB_NAME:
            ex.setClassName("com.sos.jitl.jobs.file.RemoveFileJob");
            break;
        }
        setLogLevel(ex, null);
        setMockLevel(ex);
        job.setExecutable(ex);
        return job;
    }

    private void setAgent(Job job, JS7Agent js7Agent) {
        if (job != null && js7Agent != null) {
            job.setAgentName(js7Agent.getJS7AgentName());
            job.setSubagentClusterId(js7Agent.getSubagentClusterId());
        }
    }

    private void createNoticeBoard(JS7ConverterResult result, Path workflowPath, String boardName, String boardTitle) {
        Board b = new Board();
        b.setTitle(boardTitle);
        b.setEndOfLife("$js7EpochMilli + 1 * 24 * 60 * 60 * 1000");
        b.setExpectOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");
        b.setPostOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");

        result.add(getNoticeBoardPathFromJS7Path(workflowPath, boardName), b);
    }

    private ConverterObjects getConverterObjects(Folder root) {
        ConverterObjects co = new ConverterObjects();
        walk(root, co);
        return co;
    }

    private void walk(Folder f, ConverterObjects co) {
        for (StandaloneJob o : f.getStandaloneJobs()) {
            String name = getJS7ObjectName(o.getPath(), o.getName());
            if (co.standalone.unique.containsKey(name)) {
                List<StandaloneJob> l = new ArrayList<>();
                if (co.standalone.duplicates.containsKey(name)) {
                    l = co.standalone.duplicates.get(name);
                }
                l.add(o);
                co.standalone.duplicates.put(name, l);
            } else {
                co.standalone.unique.put(name, o);
            }
        }
        for (JobChain o : f.getJobChains()) {
            String name = getJS7ObjectName(o.getPath(), o.getName());
            if (co.jobChains.unique.containsKey(name)) {
                List<JobChain> l = new ArrayList<>();
                if (co.jobChains.duplicates.containsKey(name)) {
                    l = co.jobChains.duplicates.get(name);
                }
                l.add(o);
                co.jobChains.duplicates.put(name, l);
            } else {
                co.jobChains.unique.put(name, o);
            }

            if (o.getOrders() != null) {
                for (JobChainOrder jo : o.getOrders()) {
                    orders.add(jo);
                }
            }

        }
        for (ProcessClass o : f.getProcessClasses()) {
            String name = getJS7ObjectName(o.getPath(), o.getName());
            if (co.processClasses.unique.containsKey(name)) {
                List<ProcessClass> l = new ArrayList<>();
                if (co.processClasses.duplicates.containsKey(name)) {
                    l = co.processClasses.duplicates.get(name);
                }
                l.add(o);
                co.processClasses.duplicates.put(name, l);
            } else {
                co.processClasses.unique.put(name, o);
            }
        }
        for (Path o : f.getFiles()) {
            String n = o.getFileName().toString();
            if (co.files.unique.containsKey(n)) {
                List<Path> l = new ArrayList<>();
                if (co.files.duplicates.containsKey(n)) {
                    l = co.files.duplicates.get(n);
                }
                l.add(o);
                co.files.duplicates.put(n, l);
            } else {
                co.files.unique.put(n, o);
            }
        }

        for (OrderJob o : f.getOrderJobs()) {
            orderJobs.put(o.getPath(), o);
        }

        for (Folder ff : f.getFolders()) {
            walk(ff, co);
        }
    }

    private class ConverterObjects {

        private ConverterObject<StandaloneJob> standalone = new ConverterObject<>();
        private ConverterObject<JobChain> jobChains = new ConverterObject<>();
        private ConverterObject<ProcessClass> processClasses = new ConverterObject<>();
        private ConverterObject<Path> files = new ConverterObject<>();

    }

    private class ConverterObject<T> {

        private Map<String, T> unique = new HashMap<>();
        private Map<String, List<T>> duplicates = new HashMap<>();
    }

    private class JobChainJobHelper {

        private OrderJob job;
        private String js7JobName;

        private JobChainJobHelper(OrderJob job, String js7JobName) {
            this.job = job;
            this.js7JobName = js7JobName;
        }
    }

    private class JobChainStateHelper {

        private String js1State;
        private String js1NextState;
        private String js1ErrorState;
        private String js1JobName;

        private String js7State;
        private String js7JobName;

        private String onError;

        private JobChainStateHelper(JobChainNode node, String js1JobName, String js7JobName) {
            this.js1State = node.getState();
            this.js1NextState = node.getNextState() == null ? "" : node.getNextState();
            this.js1ErrorState = node.getErrorState() == null ? "" : node.getErrorState();
            this.js1JobName = js1JobName;
            this.js7JobName = js7JobName;

            this.js7State = getJS7Name(node, this.js1State);

            this.onError = node.getOnError() == null ? "" : node.getOnError();
        }

        private String getJS7Name(JobChainNode node, String val) {
            if (SOSString.isEmpty(val)) {
                return null;
            }
            return getJS7ObjectName(node.getPath(), val);
        }
    }

    private class RunTimeHelper {

        private Path path;
        private Schedule schedule;

        private RunTimeHelper(Path path, Schedule schedule) {
            this.path = path;
            this.schedule = schedule;
        }
    }

    private class ScheduleHelper {

        private final com.sos.js7.converter.js1.common.runtime.Schedule js1Schedule;
        private final List<OrderParameterisation> orderParams;
        private final String timeZone;
        private final String startPosition;

        private List<WorkflowHelper> workflows = new ArrayList<>();

        private ScheduleHelper(com.sos.js7.converter.js1.common.runtime.Schedule schedule, List<OrderParameterisation> orderParams, String timeZone,
                String startPosition) {
            this.js1Schedule = schedule;
            this.orderParams = orderParams;
            this.timeZone = timeZone;
            this.startPosition = startPosition;
        }
    }

    private class WorkflowHelper {

        private String name;
        private Path path;

        private WorkflowHelper(String name, Path path) {
            this.name = name;
            this.path = path;
        }
    }

    private class AgentHelper {

        private JS7Agent js7Agent;
        private ProcessClass processClass;
        private boolean js1AgentIsStandalone;

        private AgentHelper(String js7AgentName, ProcessClass processClass) {
            this((JS7Agent) null, processClass);
            this.js7Agent = new JS7Agent();
            this.js7Agent.setJS7AgentName(js7AgentName);
            if (processClass != null) {
                this.js7Agent.setJS1AgentName(processClass.getName());
            }
        }

        private AgentHelper(JS7Agent js7Agent, ProcessClass processClass) {
            this.js7Agent = js7Agent;
            this.processClass = processClass;
            this.js1AgentIsStandalone = processClass == null || processClass.getRemoteSchedulers() == null || processClass.getRemoteSchedulers()
                    .getRemoteScheduler().size() == 1;

        }
    }

    private class ProcessClassFirstUsageHelper {

        private final Path path;
        private final String js7AgentName;
        private final String message;

        private ProcessClassFirstUsageHelper(Path path, String js7AgentName, String message) {
            this.path = path;
            this.js7AgentName = js7AgentName;
            this.message = message;
        }
    }

    private class BoardHelper {

        private Map<String, Path> js7States = new HashMap<>();
        private Path workflowPath;
        private String workflowName;

    }

}
