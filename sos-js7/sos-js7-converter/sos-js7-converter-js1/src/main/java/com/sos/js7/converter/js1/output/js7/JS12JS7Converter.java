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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.controller.model.workflow.Workflow;
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
import com.sos.inventory.model.instruction.LockDemand;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.RetryCatch;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.JobTemplateRef;
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
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.JS7ExportObjects;
import com.sos.js7.converter.commons.JS7ExportObjects.JS7ExportObject;
import com.sos.js7.converter.commons.agent.JS7AgentConverter;
import com.sos.js7.converter.commons.agent.JS7AgentConverter.JS7AgentConvertType;
import com.sos.js7.converter.commons.agent.JS7AgentHelper;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.config.items.GenerateConfig;
import com.sos.js7.converter.commons.config.json.JS7Agent;
import com.sos.js7.converter.commons.output.OutputWriter;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.commons.report.ConverterReportWriter;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.js1.common.Folder;
import com.sos.js7.converter.js1.common.Include;
import com.sos.js7.converter.js1.common.Params;
import com.sos.js7.converter.js1.common.job.ACommonJob;
import com.sos.js7.converter.js1.common.job.ACommonJob.DelayAfterError;
import com.sos.js7.converter.js1.common.job.ACommonJob.Type;
import com.sos.js7.converter.js1.common.job.OrderJob;
import com.sos.js7.converter.js1.common.job.OrderJob.DelayOrderAfterSetback;
import com.sos.js7.converter.js1.common.job.StandaloneJob;
import com.sos.js7.converter.js1.common.jobchain.JobChain;
import com.sos.js7.converter.js1.common.jobchain.JobChainOrder;
import com.sos.js7.converter.js1.common.jobchain.node.AJobChainNode;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNode;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNodeFileOrderSink;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNodeFileOrderSource;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNodeOnReturnCode;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendar;
import com.sos.js7.converter.js1.common.json.calendars.JS1Calendars;
import com.sos.js7.converter.js1.common.lock.LockUse;
import com.sos.js7.converter.js1.common.processclass.ProcessClass;
import com.sos.js7.converter.js1.common.runtime.RunTime;
import com.sos.js7.converter.js1.config.JS1ConverterConfig;
import com.sos.js7.converter.js1.input.DirectoryParser;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.js1.output.js7.helper.AgentHelper;
import com.sos.js7.converter.js1.output.js7.helper.BoardHelper;
import com.sos.js7.converter.js1.output.js7.helper.ConverterObjects;
import com.sos.js7.converter.js1.output.js7.helper.ConverterObjects.ConverterObject;
import com.sos.js7.converter.js1.output.js7.helper.JobChainJobHelper;
import com.sos.js7.converter.js1.output.js7.helper.JobChainStateHelper;
import com.sos.js7.converter.js1.output.js7.helper.JobHelper;
import com.sos.js7.converter.js1.output.js7.helper.JobHelper.JavaJITLJobHelper;
import com.sos.js7.converter.js1.output.js7.helper.JobHelper.ShellJobHelper;
import com.sos.js7.converter.js1.output.js7.helper.JobTemplateHelper;
import com.sos.js7.converter.js1.output.js7.helper.LockHelper;
import com.sos.js7.converter.js1.output.js7.helper.NamedJobHelper;
import com.sos.js7.converter.js1.output.js7.helper.ProcessClassFirstUsageHelper;
import com.sos.js7.converter.js1.output.js7.helper.RunTimeHelper;
import com.sos.js7.converter.js1.output.js7.helper.ScheduleHelper;
import com.sos.js7.converter.js1.output.js7.helper.TryCatchHelper;
import com.sos.js7.converter.js1.output.js7.helper.TryCatchHelper.TryCatchPartHelper;
import com.sos.js7.converter.js1.output.js7.helper.WorkflowHelper;

/** <br/>
 * TODO Locks<br/>
 * --- JS7 - Lock definition<br/>
 * ------------ capacity: <br />
 * -------------- exclusive = ??? 1 <br/>
 * -------------- shared = ??? <lock max_non_exclusive = "integer" <br/>
 * ------- - Lock assigning<br/>
 * ------------- exclusive = ??? weight=null <br/>
 * ------------- shared = ??? weight=1 <br/>
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
 * TODO Order state - Schedule start position<br/>
 * TODO YADE without settings - generate jobresource ... ? -<br/>
 */
public class JS12JS7Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS12JS7Converter.class);

    public static JS1ConverterConfig CONFIG = new JS1ConverterConfig();

    public static final String NAME_CONCAT_CHARACTER = "-";
    private static final String DUPLICATE_PREFIX = NAME_CONCAT_CHARACTER + "dup";
    private static final Pattern DUPLICATE_PATTERN = Pattern.compile("(.*)(" + DUPLICATE_PREFIX + ")([0-9]+)$");
    private static final int DUPLICATE_INITIAL_COUNTER = 1;

    private static final String MOVE_JOB_NAME = "fileOrderSinkMove";
    private static final String REMOVE_JOB_NAME = "fileOrderSinkRemove";
    private static final String VAR_CURRENT_FILE_JS7 = "${file}";
    private static final String VAR_CURRENT_FILE_JS1 = "${scheduler_file_path}";
    private static final String ENV_VAR_JS1_PREFIX = "SCHEDULER_PARAM_";

    private static final String YADE_MAIN_CONFIGURATION_JOBRESOURCE = "yade";
    private static final String YADE_JOBRESOURCE_SETTINGS_ARG = "yadeXml";
    private static final String YADE_JOBRESOURCE_SETTINGS_ENV = "YADE_XML";
    private static final String YADE_JOB_SETTINGS_ENV = "YADE_SETTINGS";
    private static final String YADE_JOB_PROFILE_ENV = "YADE_PROFILE";

    private static final String LOG_DELIMITER_LINE = "--------------------------------------------------";

    private ConverterObjects converterObjects;
    private DirectoryParserResult pr;
    private String inputDirPath;

    private Map<String, List<ACommonJob>> js1JobsByLanguage = new HashMap<>();
    private Map<String, List<RunTime>> js1Calendars = new HashMap<>();
    private Map<Path, JS7Agent> js1ProcessClass2js7Agent = new HashMap<>();
    private Map<Path, OrderJob> js1OrderJobs = new HashMap<>();
    private Map<Path, StandaloneJob> js1StandaloneShellJobs = new HashMap<>();
    private Map<Path, AgentHelper> js1Agents = new HashMap<>();
    private Map<String, ScheduleHelper> js1Schedules = new HashMap<>();
    private List<ProcessClassFirstUsageHelper> js1ProcessClass2js7AgentResult = new ArrayList<>();
    private List<JobChainOrder> js1Orders = new ArrayList<>();
    private List<ACommonJob> js1JobsWithMonitors = new ArrayList<>();
    private Set<Path> js1JobStreamJobs = new HashSet<>();

    private Map<Path, List<BoardHelper>> js7BoardHelpers = new HashMap<>();
    private Map<String, AgentHelper> js7Agents = new HashMap<>();
    private Map<Path, JobTemplateHelper> js7JobTemplateHelpers = new HashMap<>();
    private Map<Path, LockHelper> js7LockHelpers = new HashMap<>();
    private Map<Path, String> js7SinkJobs = new HashMap<>();
    private Map<Path, String> js7JobResources = new HashMap<>();
    private Map<String, String> js7Calendars = new HashMap<>();
    private Map<String, String> js7StandaloneAgents = new HashMap<>();
    private Map<String, Integer> js7JobResourcesDuplicates = new HashMap<>();
    private Set<String> js7WorkflowNames = new HashSet<>();
    private int js7SinkJobsDuplicateCounter = 0;

    private Path defaultProcessClassPath = null;

    public static void convert(Path input, Path outputDir, Path reportDir) throws Exception {

        GenerateConfig gc = CONFIG.getGenerateConfig();
        if (gc.getCyclicOrders()) {
            LOGGER.info(LOG_DELIMITER_LINE);
            LOGGER.info(String.format("[config][%s=true]not implemented yet", gc.getFullPropertyNameCyclicOrders()));
            LOGGER.info(LOG_DELIMITER_LINE);
        }

        String method = "convert";
        // APP start
        Instant appStart = Instant.now();
        LOGGER.info(String.format("[%s][start]...", method));

        try {
            OutputWriter.prepareDirectory(reportDir);
            OutputWriter.prepareDirectory(outputDir);

            // 1 - Config Report
            ConverterReportWriter.writeConfigReport(reportDir.resolve("config_errors.csv"), reportDir.resolve("config_warnings.csv"), reportDir
                    .resolve("config_analyzer.csv"));

            // 2 - Parse JS1 files
            LOGGER.info(LOG_DELIMITER_LINE);
            LOGGER.info(String.format("[%s][JS1][parse][start]%s", method, input));
            DirectoryParserResult pr = DirectoryParser.parse(CONFIG.getParserConfig(), input, outputDir);

            LOGGER.info(String.format("[%s][JS1][parse][result][total]folders=%s", method, pr.getCountFolders()));
            LOGGER.info(String.format("[%s][JS1][parse][result][total]process_class=%s (agent standalone=%s, cluster=%s)", method, pr
                    .getCountProcessClasses(), pr.getCountProcessClassesAgentStandalone(), pr.getCountProcessClassesAgentCluster()));
            LOGGER.info(String.format("[%s][JS1][parse][result][total]job_chain=%s (order=%s, config=%s)", method, pr.getCountJobChains(), pr
                    .getCountOrders(), pr.getCountJobChainConfigs()));
            LOGGER.info(String.format("[%s][JS1][parse][result][total]job=%s (order=%s, standalone=%s), monitor=%s", method, pr.getCountJobs(), pr
                    .getCountOrderJobs(), pr.getCountStandaloneJobs(), pr.getCountMonitors()));
            LOGGER.info(String.format("[%s][JS1][parse][result][total]lock=%s, schedule=%s, other files=%s (json=%s)", method, pr.getCountLocks(), pr
                    .getCountSchedules(), pr.getCountOtherFiles(), pr.getJsonFiles().size()));

            LOGGER.info(String.format("[%s][JS1][parse][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));

            // 3 - Convert to JS7
            LOGGER.info(LOG_DELIMITER_LINE);
            Instant start = Instant.now();
            LOGGER.info(String.format("[%s][JS7][convert][start]...", method));
            JS7ConverterResult result = convert(pr);
            LOGGER.info(String.format("[%s][JS7][convert][end]%s", method, SOSDate.getDuration(start, Instant.now())));

            // 3.1 - Parser Reports
            LOGGER.info(LOG_DELIMITER_LINE);
            ConverterReportWriter.writeParserReport("JS1", reportDir.resolve("parser_summary.csv"), reportDir.resolve("parser_errors.csv"), reportDir
                    .resolve("parser_warnings.csv"), reportDir.resolve("parser_analyzer.csv"));
            // 3.2 - Converter Reports
            ConverterReportWriter.writeConverterReport(reportDir.resolve("converter_errors.csv"), reportDir.resolve("converter_warnings.csv"),
                    reportDir.resolve("converter_analyzer.csv"));

            // 4 - Write JS7 files
            LOGGER.info(LOG_DELIMITER_LINE);
            start = Instant.now();
            LOGGER.info(String.format("[%s][JS7][write][start]...", method));

            if (CONFIG.getGenerateConfig().getAgents()) {
                long total = result.getAgents().getItems().size();
                if (total > 0) {
                    LOGGER.info(String.format("[%s][JS7][write][Agents]...", method));
                    OutputWriter.write(outputDir, result.getAgents());
                } else {
                    LOGGER.info(String.format("[%s][JS7][write][Agents][skip]0 items", method));
                }
                long standalone = result.getAgents().getItems().stream().filter(a -> a.getObject().getStandaloneAgent() != null).count();
                ConverterReport.INSTANCE.addSummaryRecord("Agents", total + ", STANDALONE=" + standalone + ", CLUSTER=" + (total - standalone));
            } else {
                LOGGER.info(String.format("[%s][JS7][write][Agents][skip]%s=false", method, CONFIG.getGenerateConfig().getFullPropertyNameAgents()));
            }

            write(outputDir, "Workflows", result.getWorkflows(), gc.getWorkflows(), gc.getFullPropertyNameWorkflows());
            write(outputDir, "Calendars", result.getCalendars(), gc.getCalendars(), gc.getFullPropertyNameCalendars());
            write(outputDir, "Schedules", result.getSchedules(), gc.getSchedules(), gc.getFullPropertyNameSchedules());
            write(outputDir, "Boards", result.getBoards(), true, null);
            write(outputDir, "FileOrderSources", result.getFileOrderSources(), true, null);
            write(outputDir, "Locks", result.getLocks(), gc.getLocks(), gc.getFullPropertyNameLocks());
            write(outputDir, "JobResources", result.getJobResources(), true, null);
            write(outputDir, "JobTemplates", result.getJobTemplates(), gc.getJobTemplates(), gc.getFullPropertyNameJobTemplates());
            LOGGER.info(String.format("[%s][JS7][write][end]%s", method, SOSDate.getDuration(start, Instant.now())));

            // 4.1 - Summary Report
            LOGGER.info(LOG_DELIMITER_LINE);
            LOGGER.info("[converterReport][JS7][summary][start]...");
            ConverterReportWriter.writeSummaryReport(reportDir.resolve("converter_summary.csv"));
            LOGGER.info("[converterReport][JS7][summary][end]");
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            throw e;
        } finally {
            // APP end
            LOGGER.info(LOG_DELIMITER_LINE);
        }
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

    private static JS7ConverterResult convert(DirectoryParserResult pr) {
        JS7ConverterResult result = new JS7ConverterResult();

        JS12JS7Converter c = new JS12JS7Converter();
        c.pr = pr;

        c.inputDirPath = pr.getRoot().getPath().toString();
        c.converterObjects = c.getConverterObjects(pr.getRoot());
        c.js1Agents = c.getJS1Agents();

        try {
            JS7JsonFilesConverter.convert(c, result);

            c.convertYade(result);
            c.convertStandalone(result);
            c.convertJobChains(result);
            c.addJobResources(result);
            c.addLocks(result);
            c.addSchedulesBasedOnJS1Schedule(result);
            c.convertAgents(result);
            c.convertCalendars(result);
            c.postProcessing(result);
        } catch (Throwable e) {
            LOGGER.error(String.format("[convert]%s", e.toString()), e);
            ConverterReport.INSTANCE.addErrorRecord(null, "[convert]", e);
        }

        c.parserSummaryReport();
        c.analyzerReport();

        return result;
    }

    private void convertCalendars(JS7ConverterResult result) {
        if (result.getSchedules() != null && result.getSchedules().getItems().size() > 0) {
            Path rootPath = CONFIG.getCalendarConfig().getForcedFolder() == null ? Paths.get("") : CONFIG.getCalendarConfig().getForcedFolder();
            Set<String> names = new HashSet<>();
            for (JS7ExportObjects<Schedule>.JS7ExportObject item : result.getSchedules().getItems()) {
                Schedule s = item.getObject();
                if (s.getCalendars() != null && s.getCalendars().size() > 0) {
                    for (AssignedCalendars ac : s.getCalendars()) {
                        if (!SOSString.isEmpty(ac.getCalendarName())) {
                            if (!names.contains(ac.getCalendarName())) {
                                result.add(JS7ConverterHelper.getCalendarPath(rootPath, ac.getCalendarName()), JS7ConverterHelper
                                        .createDefaultWorkingDaysCalendar());
                                names.add(ac.getCalendarName());
                            }
                        }
                    }
                }
            }
        }
    }

    public StandaloneJob findStandaloneJobByPath(Path path) {
        StandaloneJob job = converterObjects.getStandalone().getUnique().entrySet().stream().map(e -> e.getValue()).filter(j -> j.getPath().equals(
                path)).findAny().orElse(null);
        if (job == null) {
            for (Map.Entry<String, List<StandaloneJob>> e : converterObjects.getStandalone().getDuplicates().entrySet()) {
                job = e.getValue().stream().filter(j -> j.getPath().equals(path)).findAny().orElse(null);
                if (job != null) {
                    return job;
                }
            }
        }
        return job;
    }

    private String getJS1StandaloneAgentURL(ProcessClass p) {
        if (p == null || !p.isAgent()) {
            return null;
        }
        return p.getRemoteScheduler() == null ? p.getRemoteSchedulers().getRemoteScheduler().get(0).getRemoteScheduler() : p.getRemoteScheduler();
    }

    private void convertAgents(JS7ConverterResult result) {
        if (CONFIG.getGenerateConfig().getAgents()) {
            result = JS7ConverterHelper.convertAgents(result, js7Agents.entrySet().stream().map(e -> e.getValue().getJS7Agent()).collect(Collectors
                    .toList()));
        }
    }

    private void postProcessing(JS7ConverterResult result) {
        for (Map.Entry<Path, List<BoardHelper>> e : js7BoardHelpers.entrySet()) {
            for (BoardHelper h : e.getValue()) {
                @SuppressWarnings("rawtypes")
                JS7ExportObject eo = result.getExportObjectWorkflowByPath(h.getWorkflowPath());
                if (eo == null) {
                    LOGGER.error(String.format("[postProcessing][boards][%s]workflow not found", h.getWorkflowPath()));
                    ConverterReport.INSTANCE.addErrorRecord("[postProcessing][boards][workflow not found]" + h.getWorkflowPath());
                } else {
                    Workflow workflow = (Workflow) eo.getObject();
                    try {
                        String w = JS7ConverterHelper.JSON_OM.writeValueAsString(workflow);
                        for (Map.Entry<String, Path> currentHelper : h.getJS7States().entrySet()) {
                            String js7SyncJobUniqueName = js7SinkJobs.get(h.getJS7States().get(currentHelper.getKey()));

                            // Generate 1 PostNotice for the current workflow state
                            String boardName = h.getWorkflowName() + NAME_CONCAT_CHARACTER + js7SyncJobUniqueName + NAME_CONCAT_CHARACTER + "s";
                            String regex = "(\"noticeBoardNames\"\\s*:\\s*\\[\")sospn-" + currentHelper.getKey() + "\"\\]";
                            String replacement = "$1" + boardName + "\"\\]";
                            w = w.replaceAll(regex, replacement);

                            JS7ConverterHelper.createNoticeBoardFromWorkflowPath(result, eo.getOriginalPath().getPath(), boardName, boardName);

                            // Generate n ExpectNotices of all workflows
                            List<String> al = new ArrayList<>();
                            for (BoardHelper hh : e.getValue()) {
                                boardName = hh.getWorkflowName() + NAME_CONCAT_CHARACTER + js7SyncJobUniqueName + NAME_CONCAT_CHARACTER + "s";
                                al.add("'" + boardName + "'");
                            }
                            regex = "(\"noticeBoardNames\"\\s*:\\s*\")sosen-" + currentHelper.getKey() + "\"";
                            replacement = "$1" + String.join(" && ", al) + "\"";
                            w = w.replaceAll(regex, replacement);
                        }
                        result.addOrReplace(eo.getOriginalPath().getPath(), JS7ConverterHelper.JSON_OM.readValue(w, Workflow.class));
                    } catch (Throwable ex) {
                        LOGGER.error(String.format("[postProcessing][boards][%s]%s", h.getWorkflowPath(), ex.toString()), ex);
                        ConverterReport.INSTANCE.addErrorRecord(h.getWorkflowPath(), "[postProcessing][boards]", ex);
                    }
                }
            }
        }
    }

    private Map<Path, AgentHelper> getJS1Agents() {
        Map<Path, AgentHelper> result = new HashMap<>();
        converterObjects.getProcessClasses().getUnique().entrySet().stream().filter(e -> e.getValue().isAgent()).forEach(e -> {
            try {
                result.put(e.getValue().getPath(), new AgentHelper(e.getKey(), e.getValue()));
            } catch (Throwable ex) {
                LOGGER.error(String.format("[getJS1Agents][processClasses][unique][%s]%s", e.getValue(), ex.toString()), ex);
                Path p = e.getValue() == null ? null : e.getValue().getPath();
                ConverterReport.INSTANCE.addErrorRecord(p, "[getJS1Agents][processClasses][unique]", ex);
            }
        });
        if (converterObjects.getProcessClasses().getDuplicates().size() > 0) {
            for (Map.Entry<String, List<ProcessClass>> entry : converterObjects.getProcessClasses().getDuplicates().entrySet()) {
                LOGGER.info("[getJS1Agents][processClasses][duplicate]" + entry.getKey());
                int counter = DUPLICATE_INITIAL_COUNTER;
                for (ProcessClass pc : entry.getValue()) {
                    if (!pc.isAgent()) {
                        continue;
                    }
                    try {
                        result.put(pc.getPath(), new AgentHelper(getDuplicateName(entry.getKey(), counter), pc));
                        counter++;
                    } catch (Throwable ex) {
                        LOGGER.error(String.format("[getJS1Agents][processClasses][duplicate][%s]%s", pc, ex.toString()), ex);
                        ConverterReport.INSTANCE.addErrorRecord(pc.getPath(), "[getJS1Agents][processClasses][duplicate]", ex);
                    }
                }
            }
        }
        return result;
    }

    private void addSchedulesBasedOnJS1Schedule(JS7ConverterResult result) {
        js1Schedules.entrySet().stream().sorted(Map.Entry.<String, ScheduleHelper> comparingByKey()).forEach(e -> {
            String js7ScheduleName = JS7ConverterHelper.getJS7ObjectName(e.getKey());
            Path schedulePath = JS7ConverterHelper.getSchedulePathFromJS7Path(e.getValue().getWorkflows().get(0).getPath(), js7ScheduleName, "");

            Schedule schedule = JS7RunTimeConverter.convert(schedulePath, e.getValue().getJS1Schedule(), e.getValue().getTimeZone(), e.getValue()
                    .getWorkflows().stream().map(w -> {
                        return w.getName();
                    }).collect(Collectors.toList()));

            if (schedule != null) {
                if (e.getValue().getStartPosition() != null) {
                    setSchedulePosition(schedule, e.getValue().getStartPosition());
                }
                if (e.getValue().getOrderParams() != null && e.getValue().getOrderParams().size() > 0) {
                    schedule.setOrderParameterisations(e.getValue().getOrderParams());
                }
                // result.add(getSchedulePathFromJS7Path(e.getValue().getWorkflows().get(0).getPath(), e.getKey(), ""), schedule);
                result.add(schedulePath, schedule);
            }
        });
    }

    private void parserSummaryReport() {
        long agentsStandalone = js1Agents.entrySet().stream().filter(a -> a.getValue().isJS1AgentIsStandalone()).count();

        ParserReport.INSTANCE.addSummaryRecord("TOTAL FOLDERS", pr.getCountFolders());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL PROCESS CLASS files", pr.getCountProcessClasses());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL AGENTS", js1Agents.size() + " (STANDALONE=" + agentsStandalone + ", CLUSTER=" + (js1Agents
                .size() - agentsStandalone) + ")");
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB files", pr.getCountJobs() + " (STANDALONE=" + pr.getCountStandaloneJobs() + ", ORDER=" + pr
                .getCountOrderJobs() + ")");
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOBS WITH MONITORS", js1JobsWithMonitors.size());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN files", pr.getCountJobChains());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN ORDER files", pr.getCountOrders());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL JOB CHAIN CONFIG files", pr.getCountJobChainConfigs());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL LOCK files", pr.getCountLocks());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL SCHEDULE files", pr.getCountSchedules());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL MONITOR files", pr.getCountMonitors());
        ParserReport.INSTANCE.addSummaryRecord("TOTAL OTHER files", pr.getCountOtherFiles() + " (JSON files=" + pr.getJsonFiles().size() + ")");
        ParserReport.INSTANCE.addSummaryRecord("YADE MAIN CONFIGURATION file", pr.getYadeConfiguration() == null ? "" : pr.getYadeConfiguration()
                .toString());
    }

    private void analyzerReport() {

        // PARSER REPORT
        try {
            if (js1Agents.size() > 0) {
                ParserReport.INSTANCE.addAnalyzerRecord("AGENTS", "START");
                js1Agents.entrySet().stream().sorted(Map.Entry.<Path, AgentHelper> comparingByKey()).forEach(e -> {
                    ParserReport.INSTANCE.addAnalyzerRecord(e.getKey(), e.getValue().getJS7Agent().getJS7AgentName(), "standalone=" + e.getValue()
                            .isJS1AgentIsStandalone());
                });
                ParserReport.INSTANCE.addAnalyzerRecord("AGENTS", "END");
            }
        } catch (Throwable e) {
            ParserReport.INSTANCE.addWarningRecord("[analyzerReport]agents", e.toString());
        }

        try {
            if (js1Orders.size() > 0) {
                List<JobChainOrder> empty = js1Orders.stream().filter(o -> o.getRunTime() == null || o.getRunTime().isEmpty()).collect(Collectors
                        .toList());
                ParserReport.INSTANCE.addAnalyzerRecord("JOB CHAIN ORDER files", "TOTAL=" + js1Orders.size() + "(empty run_time=" + empty.size()
                        + ")");
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
                        String add = "";
                        if (Type.STANDALONE.equals(job.getType()) && js1JobStreamJobs.contains(job.getPath())) {
                            add = " (JOBSTREAM)";
                        }
                        ParserReport.INSTANCE.addAnalyzerRecord(job.getPath(), job.getType().toString() + add, className);
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
                        ParserReport.INSTANCE.addAnalyzerRecord(r.getCurrentPath(), r.getCalendarsHelper() == null ? "" : r.getCalendarsHelper()
                                .getText(), "");
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
            // if (js1ProcessClass2js7AgentResult.size() > 0) {
            // ConverterReport.INSTANCE.addAnalyzerRecord("", "");
            // ConverterReport.INSTANCE.addAnalyzerRecord("JS7 Agent TO JS1 PROCESS_CLASS", "START");
            // List<ProcessClassFirstUsageHelper> sorted = js1ProcessClass2js7AgentResult.stream().sorted((e1, e2) -> e1.getPath().compareTo(e2
            // .getPath())).collect(Collectors.toList());
            // for (ProcessClassFirstUsageHelper p : sorted) {
            // ConverterReport.INSTANCE.addAnalyzerRecord(p.getPath(), p.getJs7AgentName(), p.getMessage());
            // }
            // ConverterReport.INSTANCE.addAnalyzerRecord("JS7 Agent TO JS1 PROCESS_CLASS", "END");
            // }

            if (js1ProcessClass2js7Agent.size() > 0) {
                ConverterReport.INSTANCE.addAnalyzerRecord("", "");
                ConverterReport.INSTANCE.addAnalyzerRecord("JS7 Agent TO  JS1 Agent", "START");
                js1ProcessClass2js7Agent.entrySet().forEach(p -> {
                    ConverterReport.INSTANCE.addAnalyzerRecord(null, p.getValue().getJS7AgentName(), p.getValue().getOriginalAgentName());
                });
                ConverterReport.INSTANCE.addAnalyzerRecord("JS7 Agent TO  JS1 Agent", "END");
            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addWarningRecord("[analyzerReport]Agents", e.toString());
        }
        try {
            if (js1Agents.size() > 0) {
                Map<Path, AgentHelper> notUsed = new HashMap<>();
                for (Map.Entry<Path, AgentHelper> e : js1Agents.entrySet()) {
                    if (!js1ProcessClass2js7Agent.containsKey(e.getKey())) {
                        notUsed.put(e.getKey(), e.getValue());
                    }

                }
                if (notUsed.size() > 0) {
                    ConverterReport.INSTANCE.addAnalyzerRecord("", "");
                    ConverterReport.INSTANCE.addAnalyzerRecord("AGENTS NOT USED", "START");
                    notUsed.entrySet().stream().sorted(Map.Entry.<Path, AgentHelper> comparingByKey()).forEach(e -> {
                        ConverterReport.INSTANCE.addAnalyzerRecord(e.getKey(), e.getValue().getJS7Agent().getJS7AgentName(), "");
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

        js7JobResources.entrySet().forEach(e -> {
            try {
                Path js7Path = JS7ConverterHelper.getJobResourcePath(getJS7PathFromJS1PathParent(e.getKey()), e.getValue());

                Params p = new Params(SOSXML.newXPath(), JS7ConverterHelper.getDocumentRoot(e.getKey()));
                Environment args = new Environment();
                Environment envs = new Environment();
                p.getParams().entrySet().forEach(pe -> {
                    args.setAdditionalProperty(pe.getKey(), JS7ConverterHelper.quoteValue4JS7(pe.getValue()));
                    envs.setAdditionalProperty(envVarPrefix + pe.getKey().toUpperCase(), "$" + pe.getKey());
                });
                JobResource jr = new JobResource(args, envs, null, null);

                result.add(js7Path, jr);
            } catch (Throwable e1) {
                ConverterReport.INSTANCE.addErrorRecord(e.getKey(), "jobResource=" + e.getValue(), e1);
            }
        });
    }

    private void addLocks(JS7ConverterResult result) {
        if (!CONFIG.getGenerateConfig().getLocks()) {
            return;
        }

        js7LockHelpers.entrySet().forEach(e -> {
            try {
                result.add(e.getValue().getJS7Path(), e.getValue().getJS7Lock());
            } catch (Throwable e1) {
                ConverterReport.INSTANCE.addErrorRecord(e.getKey(), "lock=" + e.getValue(), e1);
            }
        });
    }

    private void convertYade(JS7ConverterResult result) {
        if (pr.getYadeConfiguration() != null) {
            try {
                Environment args = new Environment();
                Environment envs = new Environment();

                args.setAdditionalProperty(YADE_JOBRESOURCE_SETTINGS_ARG, "toFile('" + SOSPath.readFile(pr.getYadeConfiguration(),
                        StandardCharsets.UTF_8) + "','*.xml')");
                envs.setAdditionalProperty(YADE_JOBRESOURCE_SETTINGS_ENV, "$" + YADE_JOBRESOURCE_SETTINGS_ARG);

                JobResource jr = new JobResource(args, envs, null, null);
                // result.add(Paths.get(YADE_MAIN_CONFIGURATION_JOBRESOURCE + ".jobresource.json"), jr);
                result.add(JS7ConverterHelper.getJobResourcePath(Paths.get(""), YADE_MAIN_CONFIGURATION_JOBRESOURCE), jr);
            } catch (IOException e) {
                ConverterReport.INSTANCE.addErrorRecord(pr.getYadeConfiguration(), "yade jobResource=yade", e);
                pr.setYadeConfiguration(null);
            }
        }
    }

    private void convertStandalone(JS7ConverterResult result) {
        ConverterObject<StandaloneJob> o = converterObjects.getStandalone();
        String workflowName;
        for (Map.Entry<String, StandaloneJob> entry : o.getUnique().entrySet()) {
            workflowName = convertStandaloneWorkflow(result, entry.getKey(), entry.getValue(), null, null, null, null);
        }

        LOGGER.info("[convertStandalone]duplicates=" + o.getDuplicates().size());
        if (o.getDuplicates().size() > 0) {
            for (Map.Entry<String, List<StandaloneJob>> entry : o.getDuplicates().entrySet()) {
                LOGGER.info("[convertStandalone][duplicate]" + entry.getKey());
                int counter = DUPLICATE_INITIAL_COUNTER;
                for (StandaloneJob jn : entry.getValue()) {
                    String js7Name = getDuplicateName(entry.getKey(), counter);
                    LOGGER.info("[convertStandalone][duplicate][" + entry.getKey() + "][js7Name=" + js7Name + "][path]" + jn.getPath());
                    workflowName = convertStandaloneWorkflow(result, js7Name, jn, null, null, null, null);
                    if (workflowName != null) {

                        String fo = "";
                        try {
                            fo = " (first occurrence=" + o.getUnique().get(entry.getKey()).getPath().toString() + ")";
                        } catch (Throwable e) {

                        }
                        ConverterReport.INSTANCE.addAnalyzerRecord(jn.getPath(), "[DUPLICATE JS1]StandaloneJob=" + entry.getKey(), "js7Name="
                                + js7Name + fo);

                        counter++;
                    }
                }
            }
        }
    }

    private String convertStandaloneWorkflow(JS7ConverterResult result, String js7Name, ACommonJob js1Job, Path mainWorkflowPath,
            String mainWorkflowName, JobChainStateHelper h, JS7Agent jobChainAgent) {

        if (js1JobStreamJobs.contains(js1Job.getPath())) {
            LOGGER.info("[convertStandaloneWorkflow][skip][JobStream job]" + js1Job.getPath());

            return null;
        } else {
            LOGGER.info("[convertStandaloneWorkflow]" + js1Job.getPath());
        }

        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(js1Job.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());

        Jobs js = new Jobs();
        JobHelper jh = getJob(result, js1Job, jobChainAgent, null, null);
        Job job = null;
        if (jh != null) {
            job = jh.getJS7Job();

            // set JobResource on workflow instead of job
            w.setJobResourceNames(job.getJobResourceNames());
            job.setJobResourceNames(null);

            js.setAdditionalProperty(js7Name, job);
        }
        w.setJobs(js);

        List<Instruction> in = new ArrayList<>();
        in.add(getNamedJobInstruction(js1Job, js7Name, js7Name, null, null, null).getInstruction());
        if (h == null) {
            in = getRetryInstructions(js1Job, in);
        } else {
            if (h.getOnError() != null) {
                String onError = h.getOnError().toLowerCase();
                switch (onError) {
                case "setback":
                    in = getRetryInstructions(js1Job, in);
                    break;
                }
            }
        }

        in = getCyclicWorkflowInstructions(js1Job, in);
        w.setInstructions(in);

        Path workflowPath = mainWorkflowPath == null ? getWorkflowPathFromJS1Path(js1Job.getPath(), js7Name) : getWorkflowPathFromJS7Path(result,
                mainWorkflowPath, mainWorkflowName);
        String workflowName = JS7ConverterHelper.getWorkflowName(workflowPath);

        RunTimeHelper rth = convertRunTimeForSchedule("STANDALONE", js1Job.getRunTime(), workflowPath, workflowName, "");
        // boolean hasSchedule = false;
        if (rth != null) {
            if (rth.getSchedule() != null) {
                result.add(rth.getPath(), rth.getSchedule());
                // hasSchedule = true;
            }
        }
        if (mainWorkflowPath == null) {// js1 standalone jobs
            // hasSchedule =
            // addJS1ScheduleFromScheduleOrRunTime(js1Job.getRunTime(), getStandaloneOrderParameterisation(jh), null, workflowPath, workflowName, null);
            addJS1ScheduleFromScheduleOrRunTime(js1Job.getRunTime(), null, null, workflowPath, workflowName, null);
        } else {
            // ??? why OrderJob???
            if (js1Job instanceof OrderJob) {
                job.setAdmissionTimeScheme(JS7RunTimeConverter.convert((OrderJob) js1Job));
            }
        }

        if (jh.isStandalone()) {// ?why check standalone?
            if (jh.getJS7JobArgumentsNotQuotedValues() != null && jh.getJS7JobArgumentsNotQuotedValues().getAdditionalProperties().size() > 0) {
                Parameters parameters = new Parameters();
                for (Map.Entry<String, String> e : jh.getJS7JobArgumentsNotQuotedValues().getAdditionalProperties().entrySet()) {
                    // jh.getJS7JobArgumentsNotQuotedValues().getAdditionalProperties().entrySet().forEach(e -> {
                    // parameters.setAdditionalProperty(e.getKey(), getOrderPreparationStringParameter(e.getValue()));
                    // parameters.setAdditionalProperty(e.getKey(), getOrderPreparationStringParameter(hasSchedule ? null : e.getValue()));
                    parameters.setAdditionalProperty(e.getKey(), getOrderPreparationStringParameter(e.getValue()));
                    // });
                }
                w.setOrderPreparation(new Requirements(parameters, false));
            }
        }

        result.add(workflowPath, w);
        return workflowName;
    }

    @SuppressWarnings("unused")
    private List<OrderParameterisation> getStandaloneOrderParameterisation(JobHelper jh) {
        if (!jh.isStandalone()) {
            return null;
        }
        if (jh.getJS7JobArgumentsNotQuotedValues() == null || jh.getJS7JobArgumentsNotQuotedValues().getAdditionalProperties().size() == 0) {
            return null;
        }
        List<OrderParameterisation> l = new ArrayList<>();
        OrderParameterisation set = new OrderParameterisation();
        set.setOrderName(null);
        Variables vs = new Variables();
        for (Map.Entry<String, String> e : jh.getJS7JobArgumentsNotQuotedValues().getAdditionalProperties().entrySet()) {
            vs.setAdditionalProperty(e.getKey(), e.getValue());
        }
        set.setVariables(vs);
        l.add(set);
        return l;
    }

    // TODO quick tmp solution
    public boolean addJS1ScheduleFromScheduleOrRunTime(RunTime runTime, List<OrderParameterisation> orderParams, String startPosition,
            Path workflowPath, String workflowName, String add) {
        if (runTime == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[addJS1ScheduleFromScheduleOrRunTime][skip][runtTime null]workflow=%s", workflowName));
            }
            return false;
        }

        boolean addToSchedules = false;
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
            if (schedule == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[addJS1ScheduleFromScheduleOrRunTime][workflow=%s][skip]schedule null", workflowName));
                }
            } else {
                ScheduleHelper h = js1Schedules.get(schedule.getName());
                if (h == null) {
                    h = new ScheduleHelper(schedule, orderParams, runTime.getTimeZone(), startPosition);
                    addToSchedules = true;
                } else {
                    addToSchedules = h.getWorkflows().stream().filter(w -> w.getName().equals(workflowName)).findAny().orElse(null) == null;
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[addJS1ScheduleFromScheduleOrRunTime][workflow=%s][addToSchedules=%s]schedule=%s", workflowName,
                            addToSchedules, schedule.getName()));
                }
                if (addToSchedules) {
                    h.getWorkflows().add(new WorkflowHelper(workflowName, workflowPath));
                    js1Schedules.put(schedule.getName(), h);
                }
            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addErrorRecord(workflowPath, "error on handle schedule", e);
        }
        return addToSchedules;
    }

    public RunTimeHelper convertRunTimeForSchedule(String range, RunTime runTime, Path workflowPath, String workflowName, String additionalName) {
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
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[convertRunTimeForSchedule][range=%s][workflow=%s][additionalName=%s]use JS1 calendars", range,
                                workflowName, additionalName));
                    }

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

                    return new RunTimeHelper(JS7ConverterHelper.getSchedulePathFromJS7Path(workflowPath, workflowName, additionalName), s);
                }
            } else {
            }
        }
        return null;
    }

    private Path getWorkflowPathFromJS1Path(Path js1Path, String js7Name) {
        // return js7Path.resolve(js7Name + ".workflow.json");
        return JS7ConverterHelper.resolvePath(getJS7PathFromJS1PathParent(js1Path), js7Name + ".workflow.json");
    }

    public Path getJS7PathFromJS1PathParent(Path js1Path) {
        String relative = js1Path.getParent().toString().substring(inputDirPath.length());
        Path js7Path = null;
        if (SOSString.isEmpty(relative)) {
            js7Path = Paths.get("");
        } else {
            js7Path = JS7ConverterHelper.getJS7ObjectPath(Paths.get(relative));
        }
        return js7Path;
    }

    public String getUniqueWorkflowName(String name) {
        if (js7WorkflowNames.contains(name)) {
            String newName = name;
            boolean run = true;
            int counter = 0;
            while (run) {
                counter++;
                Matcher m = DUPLICATE_PATTERN.matcher(newName);
                if (m.find()) {
                    if (m.groupCount() == 3) {
                        newName = m.group(1) + m.group(2) + (Integer.parseInt(m.group(3)) + 1);
                    } else {
                        newName = getDuplicateName(name, DUPLICATE_INITIAL_COUNTER);
                    }
                } else {
                    newName = getDuplicateName(name, DUPLICATE_INITIAL_COUNTER);
                }
                if (!js7WorkflowNames.contains(newName)) {
                    run = false;
                }
                if (counter >= 1_000) {
                    newName = getDuplicateName(name, 2_000);
                    run = false;
                }
            }
            js7WorkflowNames.add(newName);
            return newName;
        } else {
            js7WorkflowNames.add(name);
            return name;
        }
    }

    private Path getWorkflowPathFromJS7Path(JS7ConverterResult result, Path mainWorkflowPath, String name) {
        Path parent = mainWorkflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        // return parent.resolve(name + ".workflow.json");
        return JS7ConverterHelper.resolvePath(parent, name + ".workflow.json");
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
            f.setMessage("Failed because of Job(name=" + h.getJS7JobName() + ",label=" + h.getJS7State() + ") error.");
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

    public com.sos.inventory.model.instruction.Lock getLockInstruction(ACommonJob job, NamedJob nj) {
        if (!CONFIG.getGenerateConfig().getLocks() || !job.hasLockUses()) {
            return null;
        }
        if (job.getLockUses() == null || job.getLockUses().size() == 0) {
            return null;
        }

        com.sos.inventory.model.instruction.Lock l = new com.sos.inventory.model.instruction.Lock();
        List<LockDemand> demands = new ArrayList<>();

        long exclusive = job.getLockUses().stream().filter(e -> e.getExclusive()).count();
        boolean onlyExclusive = exclusive == job.getLockUses().size();
        boolean onlyShared = exclusive == 0;

        for (LockUse lu : job.getLockUses()) {
            LockHelper lh = js7LockHelpers.get(lu.getLock().getFile());
            if (lh == null) {
                lh = new LockHelper(this, lu);
                js7LockHelpers.put(lu.getLock().getFile(), lh);
            }
            if (onlyExclusive) {
                demands.add(new LockDemand(lh.getJS7Name(), null));
            } else if (onlyShared) {
                demands.add(new LockDemand(lh.getJS7Name(), 1));
            } else {
                // mix of exclusive,shared - set to shared
                demands.add(new LockDemand(lh.getJS7Name(), 1));
                if (lu.getExclusive()) {
                    ConverterReport.INSTANCE.addWarningRecord(job.getPath(), "[multiple lock type detected]exclusive changed to shared", "[lock.use]"
                            + lu);
                }
            }
        }
        if (demands.size() > 0) {
            ArrayList<Instruction> al = new ArrayList<>();
            al.add(nj);
            l.setDemands(demands);
            l.setLockedWorkflow(new Instructions(al));

            return l;
        }
        return null;
    }

    // TODO
    private List<Instruction> getCyclicWorkflowInstructions(ACommonJob job, List<Instruction> in) {
        if (!CONFIG.getGenerateConfig().getCyclicOrders() && job.getRunTime() != null) {
            /** CycleSchedule cs = job.getRunTime().convertForCyclicWorkflow(); if (cs != null) { Instructions ci = new Instructions(in);
             * 
             * in = new ArrayList<>(); in.add(new Cycle(ci, cs)); } */
        }
        return in;
    }

    public JobHelper getJob(JS7ConverterResult result, ACommonJob job, JS7Agent jobChainAgent, JobChain jobChain,
            JobChainJobHelper jobChainJobHelper) {
        if (job.getMonitors() != null && job.getMonitors().size() > 0) {
            if (!js1JobsWithMonitors.contains(job)) {
                js1JobsWithMonitors.add(job);
            }
        }

        JobHelper jh = new JobHelper(job, jobChain);
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
        j = JS7ConverterHelper.setFromConfig(CONFIG, j);

        jh.setJS7Agent(getAgent(job.getProcessClass(), jobChainAgent, job, null));

        j = JS7AgentHelper.setAgent(j, jh.getJS7Agent());
        setJobJobResources(j, jh);
        setExecutable(j, jh, jobChainJobHelper);
        setJobOptions(j, job);
        setJobNotification(j, job);
        setJobTemplate(result, j, job);
        jh.setJS7Job(j);

        return jh;
    }

    private void setJobTemplate(JS7ConverterResult result, Job j, ACommonJob job) {
        if (!CONFIG.getGenerateConfig().getJobTemplates()) {
            return;
        }
        if (job.getPath() != null) {
            Path p = job.getPath().toAbsolutePath();
            if (pr.getIncludedOrderJobs().contains(p)) {
                JobTemplateHelper th = js7JobTemplateHelpers.get(p);
                if (th == null) {
                    th = new JobTemplateHelper(this, p, j);
                    js7JobTemplateHelpers.put(p, th);
                    result.add(th.getJS7Path(), th.getJobTemplate());
                }
                JobTemplateRef r = new JobTemplateRef();
                r.setName(th.getJS7Name());
                r.setHash(th.getJobTemplate().getHash());
                j.setJobTemplate(r);
            }
        }
    }

    public NamedJobHelper getNamedJobInstruction(ACommonJob js1Job, String js7JobName, String js7JobLabel, Params jobChainConfigProcessParams,
            SOSParameterSubstitutor ps, JobChain jobChain) {
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
            JobHelper jh = new JobHelper(js1Job, jobChain);
            JavaJITLJobHelper jitlJob = jh.getJavaJITLJob();
            ShellJobHelper shellJob = jh.getShellJob();

            Map<String, String> dynamic = null;
            if (jitlJob != null) {
                // Add Arguments
                for (Map.Entry<String, String> e : jitlJob.getParams().getToAdd().entrySet()) {
                    env.setAdditionalProperty(e.getKey(), getQuotedAndReplacedParamValue(e.getValue()));
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

                String replaced = getReplacedParamValue(value);
                String val = ps == null ? replaced : ps.replace(replaced);
                env.setAdditionalProperty(name, JS7ConverterHelper.quoteValue4JS7(val));
            }

            if (env.getAdditionalProperties().size() > 0) {
                nj.setDefaultArguments(env);
            }
        }
        return new NamedJobHelper(nj, getLockInstruction(js1Job, nj));
    }

    private JS7Agent convertAgentFrom(final JS7AgentConvertType type, final JS7Agent sourceConf, final JS7Agent defaultConf, final AgentHelper ah) {
        List<SubAgent> subagents = new ArrayList<>();
        JS7Agent source = new JS7Agent();
        com.sos.joc.model.agent.Agent agent = null;
        boolean isStandalone = false;

        if (sourceConf == null) {
            if (ah != null) {
                agent = new com.sos.joc.model.agent.Agent();
                isStandalone = ah.isJS1AgentIsStandalone();
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
                        subagents = JS7AgentHelper.copySubagents(defaultConf.getAgentCluster().getSubagents());
                        isStandalone = false;
                    }
                } else {
                    isStandalone = true;
                }
                if (agent != null) {
                    if (ah != null) {
                        agent.setUrl(getJS1StandaloneAgentURL(ah.getProcessClass()));
                    }
                }
            }
            if (agent == null) {
                agent = new com.sos.joc.model.agent.Agent();
                if (ah != null) {
                    isStandalone = ah.isJS1AgentIsStandalone();
                    agent.setUrl(getJS1StandaloneAgentURL(ah.getProcessClass()));
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
                if (defaultProcessClassPath == null && !SOSString.isEmpty(source.getJS7AgentName())) {
                    defaultProcessClassPath = Paths.get(source.getJS7AgentName());
                }
            } else {
                source.setJS7AgentName(ah.getJS7Agent().getJS7AgentName());
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
            if (ah != null && ah.getProcessClass().getSpoolerId() != null) {
                agent.setControllerId(ah.getProcessClass().getSpoolerId());
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
                    agent.setUrl(getJS1StandaloneAgentURL(ah.getProcessClass()));
                } else {
                    if (defaultConf != null) {
                        if (defaultConf.getStandaloneAgent() != null) {
                            agent.setUrl(defaultConf.getStandaloneAgent().getUrl());
                        }
                    } else {
                        agent.setUrl(JS7AgentConverter.DEFAULT_AGENT_URL);
                    }
                }
            }
            source.setStandaloneAgent(agent);
        } else {
            if (subagents == null || subagents.size() == 0) {
                subagents = new ArrayList<>();
                if (ah != null && ah.getProcessClass().getRemoteSchedulers() != null) {
                    subagents = JS1JS7AgentConverter.convert(ah.getProcessClass().getRemoteSchedulers(), agent.getAgentId());
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
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isTraceEnabled = LOGGER.isTraceEnabled();
        Path processClassPath = (js1ProcessClass != null && js1ProcessClass.getPath() != null) ? js1ProcessClass.getPath() : null;// defaultProcessClassPath;
        if (processClassPath != null && js1ProcessClass2js7Agent.containsKey(processClassPath)) {
            JS7Agent agent = js1ProcessClass2js7Agent.get(processClassPath);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getAgent][return][js1ProcessClass2js7Agent][%s][%s][details][processClassPath=%s]agent=%s",
                        processClassPath.getFileName(), agent.getJS7AgentName(), processClassPath, SOSString.toString(agent)));
            }
            return agent;
        }

        AgentHelper ah = null;
        String js1AgentName = null;
        String js7AgentName = null;
        if (js1ProcessClass != null && js1ProcessClass.isAgent()) {
            ah = js1Agents.get(js1ProcessClass.getPath());
            if (ah != null) {
                js1AgentName = ah.getJS7Agent().getOriginalAgentName();
                js7AgentName = ah.getJS7Agent().getJS7AgentName();
            }
        }
        JS7Agent agent = null;
        if (CONFIG.getAgentConfig().getForcedAgent() != null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getAgent][%s][js1AgentName=%s][js7AgentName=%s]", JS7AgentConvertType.CONFIG_FORCED.name(), js1AgentName,
                        js7AgentName));
            }
            agent = convertAgentFrom(JS7AgentConvertType.CONFIG_FORCED, CONFIG.getAgentConfig().getForcedAgent(), CONFIG.getAgentConfig()
                    .getDefaultAgent(), ah);
            if (agent != null && !SOSString.isEmpty(agent.getJS7AgentName())) {
                defaultProcessClassPath = Paths.get(agent.getJS7AgentName());
            }
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format(
                        "[getAgent][notForced][start][jobChainAgent=%s][js1AgentName=%s][js7AgentName=%s][js1ProcessClass=%s][ah=%s][ah processClass=%s]",
                        jobChainAgent, js1AgentName, js7AgentName, SOSString.toString(js1ProcessClass), SOSString.toString(ah), (ah == null ? "null"
                                : SOSString.toString(ah.getProcessClass()))));
            }
            String range = "";
            if (jobChainAgent != null && (job == null || job.getProcessClass() == null || !job.getProcessClass().isAgent())) {
                range = "JOB_CHAIN";

                agent = jobChainAgent;
            } else if (js1AgentName != null && CONFIG.getAgentConfig().getMappings().containsKey(js1AgentName)) {
                range = JS7AgentConvertType.CONFIG_MAPPINGS.name();

                agent = convertAgentFrom(JS7AgentConvertType.CONFIG_MAPPINGS, CONFIG.getAgentConfig().getMappings().get(js1AgentName), CONFIG
                        .getAgentConfig().getDefaultAgent(), ah);
            } else if (js1ProcessClass != null && js1ProcessClass.isAgent()) {
                range = JS7AgentConvertType.PROCESS_CLASS.name();

                agent = convertAgentFrom(JS7AgentConvertType.PROCESS_CLASS, null, CONFIG.getAgentConfig().getDefaultAgent(), ah);
            } else {
                range = JS7AgentConvertType.CONFIG_DEFAULT.name();

                agent = convertAgentFrom(JS7AgentConvertType.CONFIG_DEFAULT, CONFIG.getAgentConfig().getDefaultAgent(), null, ah);
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getAgent][notForced][end][%s][js1AgentName=%s]agent=%s", range, js1AgentName, SOSString.toString(
                        agent)));
            }
        }
        if (agent != null) {
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[getAgent][1]agent=%s", SOSString.toString(agent)));
            }
            if (processClassPath == null) {
                if (defaultProcessClassPath == null) {
                    defaultProcessClassPath = Paths.get(JS7AgentConverter.DEFAULT_AGENT_NAME);
                }
                processClassPath = defaultProcessClassPath;
            }

            if (isTraceEnabled) {
                LOGGER.trace(String.format("[getAgent][2]agent=%s", SOSString.toString(agent)));
            }

            if (agent.getStandaloneAgent() != null && agent.getStandaloneAgent().getUrl() != null) {
                String agentUrl = agent.getStandaloneAgent().getUrl();
                if (js7StandaloneAgents.containsKey(agentUrl)) {
                    String n = js7StandaloneAgents.get(agentUrl);

                    if (!n.equals(agent.getJS7AgentName())) {
                        ConverterReport.INSTANCE.addWarningRecord("[standalone agent][duplicate url]" + agentUrl, "[renamed][" + agent
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

            if (isTraceEnabled) {
                LOGGER.trace(String.format("[getAgent][3]agent=%s", SOSString.toString(agent)));
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
            js1ProcessClass2js7AgentResult.add(new ProcessClassFirstUsageHelper(processClassPath, agent.getJS7AgentName(), sb.toString()));

            if (isTraceEnabled) {
                LOGGER.trace(String.format("[getAgent][4]agent=%s", SOSString.toString(agent)));
            }

            if (js7Agents.containsKey(agent.getJS7AgentName())) {
                AgentHelper h = js7Agents.get(agent.getJS7AgentName());
                if (h.getProcessClass() != null) {
                    js1ProcessClass2js7AgentResult.add(new ProcessClassFirstUsageHelper(processClassPath, agent.getJS7AgentName(), "[already_used]"
                            + h.getProcessClass().getPath()));
                } else {
                    js1ProcessClass2js7AgentResult.add(new ProcessClassFirstUsageHelper(processClassPath, agent.getJS7AgentName(), "[already_used]"
                            + sb));
                }
            }

            if (isTraceEnabled) {
                LOGGER.trace(String.format("[getAgent][5]agent=%s", SOSString.toString(agent)));
            }

            js7Agents.put(agent.getJS7AgentName(), new AgentHelper(agent, js1ProcessClass));
            js1ProcessClass2js7Agent.put(processClassPath, agent);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getAgent][put][js1ProcessClass2js7Agent][%s][%s][details][processClassPath=%s]agent=%s", processClassPath
                        .getFileName(), agent.getJS7AgentName(), processClassPath, SOSString.toString(agent)));
            }
        } else {
            if (js1ProcessClass != null) {
                ConverterReport.INSTANCE.addWarningRecord(js1ProcessClass.getPath(), "Agent", "not used. Use a standalone "
                        + JS7AgentConverter.DEFAULT_AGENT_NAME);
            }
        }
        return agent;
    }

    private void setExecutable(Job j, JobHelper jh, JobChainJobHelper jcjh) {
        j.setExecutable(jh.getJavaJITLJob() == null ? getExecutableScript(jh, jcjh) : getInternalExecutable(jh));
    }

    private ExecutableJava getInternalExecutable(JobHelper jh) {
        ExecutableJava ej = new ExecutableJava();
        ej.setClassName(jh.getJavaJITLJob().getNewJavaClass());
        setJobArguments(jh, null);
        ej.setArguments(jh.getJS7JobEnvironment());

        setLogLevel(ej, jh.getJS1Job());
        setMockLevel(ej);
        return ej;
    }

    // TODO use this function
    public String getQuotedAndReplacedParamValue(String val) {
        return JS7ConverterHelper.quoteValue4JS7(getReplacedParamValue(val));
    }

    public String getQuotedParamValue(String val) {
        return JS7ConverterHelper.quoteValue4JS7(val);
    }

    public String getReplacedParamValue(String val) {
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

    private Parameter getOrderPreparationStringParameter(String defaultValue) {
        Parameter p = new Parameter();
        p.setType(ParameterType.String);
        if (defaultValue != null) {
            // p.setDefault("\"" + getReplacedParamValue(defaultValue) + "\"");
            p.setDefault(JS7ConverterHelper.doubleQuoteStringValue4JS7(getReplacedParamValue(defaultValue)));
        }
        return p;
    }

    private void setJobArguments(JobHelper jh, JobChainJobHelper jobChainJobHelper) {
        ACommonJob job = jh.getJS1Job();

        Map<String, String> params = new HashMap<>();
        if (job.getParams() != null && job.getParams().hasParams()) {
            params.putAll(job.getParams().getParams());
        }

        if (jh.getJS7OrderVariables() != null && jh.getJS7OrderVariables().getAdditionalProperties().size() > 0) {
            jh.getJS7OrderVariables().getAdditionalProperties().entrySet().forEach(ov -> {
                if (ov.getValue() != null) {
                    params.put(ov.getKey(), ov.getValue().toString());
                }
            });
        }

        if (params.size() > 0) {
            // ARGUMENTS
            JavaJITLJobHelper jitlJob = jh.getJavaJITLJob();
            ShellJobHelper shellJob = jh.getShellJob();
            Map<String, String> dynamic = null;
            if (jitlJob != null) {
                // Add Arguments
                for (Map.Entry<String, String> e : jitlJob.getParams().getToAdd().entrySet()) {
                    setJobHelperParam(jh, e.getKey(), e.getValue());
                }

                // Prepare Dynamic Argument names to use
                if (jitlJob.getParams().getMappingDynamic() != null) {
                    String d = params.get(jitlJob.getParams().getMappingDynamic().getParamName());
                    if (d != null) {
                        dynamic = jitlJob.getParams().getMappingDynamic().replace(d);
                    }
                }
            }
            for (Map.Entry<String, String> e : params.entrySet()) {
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
                    setJobHelperParam(jh, name, value);
                } catch (Throwable ee) {
                    setJobHelperParam(jh, e.getKey(), e.getValue());
                    ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "getJobArguments: could not convert value=" + e.getValue(), ee);
                }
            }
        }

        // set nodes default arguments as ENV vars
        if (jobChainJobHelper != null && jobChainJobHelper.getJS7JobNodesDefaultArguments() != null) {
            Set<String> defaultArguments = jobChainJobHelper.getJS7JobNodesDefaultArguments();
            if (defaultArguments.size() > 0) {
                for (String da : defaultArguments) {
                    // if (!env.getAdditionalProperties().containsKey(da)) {
                    // overwrite job parameters
                    jh.addJS7JobEnvironment(da, "$" + da);
                }
            }
        }
    }

    private void setJobHelperParam(JobHelper jh, String name, String val) {
        String v = getReplacedParamValue(val);
        jh.addJS7JobArgumentNotQuotedValue(name, v);
        jh.addJS7JobEnvironment(name, getQuotedParamValue(v));
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

    private ExecutableScript getExecutableScript(JobHelper jh, JobChainJobHelper jcjh) {
        StringBuilder scriptHeader = new StringBuilder();
        StringBuilder scriptCommand = new StringBuilder();
        String commentBegin = "#";
        boolean isMock = CONFIG.getMockConfig().hasScript();
        boolean isUnix = jh.getJS7Agent().getPlatform().equalsIgnoreCase(Platform.UNIX.name());
        ShellJobHelper shellJob = jh.getShellJob();
        boolean isPowershell = false;
        if (shellJob.getLanguage().equals("powershell")) {
            isPowershell = true;
            isUnix = false; // JS1 script=powershell only for Windows
        }

        String newLine = isUnix ? CONFIG.getJobConfig().getUnixNewLine() : CONFIG.getJobConfig().getWindowsNewLine();
        setJobArguments(jh, jcjh);

        boolean checkUnixFirstLine = false;
        if (isUnix) {
            commentBegin = "#";
            if (isPowershell) {// not reachable because only for windows ...
                if (!SOSString.isEmpty(CONFIG.getJobConfig().getUnixPowershellShebang())) {
                    scriptHeader.append(CONFIG.getJobConfig().getUnixPowershellShebang());
                    scriptHeader.append(newLine);
                    scriptHeader.append(newLine);
                }
            } else {
                checkUnixFirstLine = true;
                // scriptHeader.append("#!/bin/bash");
                // scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            }
        } else {
            commentBegin = "REM";
            if (isPowershell) {
                commentBegin = "#";
                if (!SOSString.isEmpty(CONFIG.getJobConfig().getWindowsPowershellShebang())) {
                    scriptHeader.append(CONFIG.getJobConfig().getWindowsPowershellShebang());
                    scriptHeader.append(newLine);
                    scriptHeader.append(newLine);
                }
            }
        }

        boolean isYADE = shellJob.getYADE() != null;
        if (!shellJob.getLanguage().equals("shell")) {// language always lower case
            if (!isYADE && !isPowershell) {
                scriptHeader.append(commentBegin).append(" language=").append(shellJob.getLanguage());
                if (shellJob.getClassName() != null) {
                    scriptHeader.append(",className=" + shellJob.getClassName());
                }
                scriptHeader.append(newLine);
            }
        }
        if (isYADE) {
            StringBuilder yadeCommand = getYADECommand(shellJob, isUnix, jh.getJS7JobEnvironment());
            if (isMock) {
                scriptHeader.append(newLine);
                scriptHeader.append(commentBegin).append(" ").append(yadeCommand.toString().trim());
                scriptHeader.append(newLine);
            } else {
                scriptCommand.append(yadeCommand.toString().trim());
            }
        } else {
            ACommonJob job = jh.getJS1Job();
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
                scriptCommand.append(newLine);
            }
            if (job.getScript().getScript() != null) {
                scriptCommand.append(job.getScript().getScript());
            }
        }

        String command = null;
        if (isMock) {
            command = isUnix ? CONFIG.getMockConfig().getUnixScript() : CONFIG.getMockConfig().getWindowsScript();
        } else {
            command = scriptCommand.toString();
        }
        command = StringUtils.stripStart(command, null);
        if (checkUnixFirstLine) {
            // // scriptHeader.append("#!/bin/bash");
            // scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            if (!command.startsWith("#!/")) {
                StringBuilder sb = new StringBuilder();
                if (!SOSString.isEmpty(CONFIG.getJobConfig().getUnixDefaultShebang())) {
                    sb.append(CONFIG.getJobConfig().getUnixDefaultShebang());
                    sb.append(newLine);
                }
                sb.append(scriptHeader);
                scriptHeader = sb;
            }
        }

        StringBuilder script = new StringBuilder(scriptHeader);
        script.append(command);

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());
        es.setV1Compatible(CONFIG.getJobConfig().getForcedV1Compatible());

        if (jh.getJS7JobEnvironment() != null && jh.getJS7JobEnvironment().getAdditionalProperties().size() > 0) {
            if (jh.isStandalone()) {
                if (!CONFIG.getJobConfig().isForcedV1Compatible()) {
                    Map<String, String> orderPreparation = jh.getJS7JobEnvironment().getAdditionalProperties().entrySet().stream().collect(Collectors
                            .toMap(e -> e.getKey().toUpperCase(), e -> "$" + e.getKey()));

                    es.setEnv(new Environment());
                    es.getEnv().getAdditionalProperties().putAll(orderPreparation);
                }
            } else {
                String envVarPrefix = CONFIG.getJobConfig().isForcedV1Compatible() && !isYADE ? ENV_VAR_JS1_PREFIX : "";
                Map<String, String> upper = jh.getJS7JobEnvironment().getAdditionalProperties().entrySet().stream().collect(Collectors.toMap(
                        e -> envVarPrefix + e.getKey().toUpperCase(), e -> e.getValue()));

                es.setEnv(new Environment());
                es.getEnv().getAdditionalProperties().putAll(upper);
            }
        }

        return es;
    }

    private StringBuilder getYADECommand(ShellJobHelper shellJob, boolean isUnix, Environment args) {
        StringBuilder sb = new StringBuilder();
        if (shellJob.getYADE() != null) {
            sb.append("\"").append(getEnvVar(shellJob.getYADE().getBin(), isUnix)).append("\" ");
            if (args != null && args.getAdditionalProperties().size() > 0) {
                boolean hasProfile = false;
                boolean hasSettings = false;
                Map<String, String> toReplace = new HashMap<>();
                for (Map.Entry<String, String> y : args.getAdditionalProperties().entrySet()) {
                    String var = y.getKey();
                    String envVar = var.toUpperCase();
                    if (!hasProfile) {
                        hasProfile = var.equalsIgnoreCase("profile");
                        if (hasProfile) {
                            envVar = YADE_JOB_PROFILE_ENV;
                            toReplace.put(var, y.getValue());
                        }
                    }
                    if (!hasSettings) {
                        hasSettings = var.equalsIgnoreCase("settings");
                        if (hasSettings) {
                            envVar = YADE_JOB_SETTINGS_ENV;
                            toReplace.put(var, y.getValue());
                        }
                    }
                    sb.append("-").append(var.toLowerCase()).append("=");
                    sb.append("\"").append(getEnvVar(envVar, isUnix)).append("\"");
                    sb.append(" ");
                }
                toReplace.entrySet().forEach(e -> {
                    if (e.getKey().equalsIgnoreCase("profile")) {
                        args.getAdditionalProperties().remove(e.getKey());
                        args.getAdditionalProperties().put(YADE_JOB_PROFILE_ENV, e.getValue());
                    } else if (e.getKey().equalsIgnoreCase("settings")) {
                        args.getAdditionalProperties().remove(e.getKey());
                        args.getAdditionalProperties().put(YADE_JOB_SETTINGS_ENV, e.getValue());
                    }
                });

                if (hasProfile && !hasSettings && pr.getYadeConfiguration() != null) {
                    sb.append("-settings=\"" + getEnvVar(YADE_JOBRESOURCE_SETTINGS_ENV, isUnix) + "\" ");
                }
            } else {
                sb.append("-settings=\"" + getEnvVar(YADE_JOB_SETTINGS_ENV, isUnix) + "\" -profile=\"" + getEnvVar(YADE_JOB_PROFILE_ENV, isUnix)
                        + "\"");
            }
        }
        return sb;
    }

    private String getEnvVar(String name, boolean isUnix) {
        return isUnix ? "$" + name : "%" + name + "%";
    }

    private void setJobJobResources(Job j, JobHelper jh) {
        ACommonJob job = jh.getJS1Job();
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
                        hasProfile = y.getKey().equalsIgnoreCase("profile");
                    }
                    if (!hasSettings) {
                        hasSettings = y.getKey().equalsIgnoreCase("settings");
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

    private String resolveJobResource(Path js1IncludeFile) {
        String name = null;
        if (js7JobResources.containsKey(js1IncludeFile)) {
            name = js7JobResources.get(js1IncludeFile);
        } else {
            String baseName = js1IncludeFile.getFileName().toString().replace(".xml", "");
            long c = js7JobResources.entrySet().stream().filter(e -> e.getValue().equals(baseName)).count();
            if (c == 0) {
                name = baseName;
                js7JobResources.put(js1IncludeFile, name);
            } else {
                Integer r = js7JobResourcesDuplicates.get(baseName);
                if (r == null) {
                    r = DUPLICATE_INITIAL_COUNTER;
                } else {
                    r++;
                }
                js7JobResourcesDuplicates.put(baseName, r);
                name = getDuplicateName(baseName, r);
                js7JobResources.put(js1IncludeFile, name);
            }
        }
        return name;
    }

    public static String getDuplicateName(String name, int counter) {
        return name + DUPLICATE_PREFIX + counter;
    }

    public static Path findIncludeFile(DirectoryParserResult pr, Path currentPath, Path include) {
        Path liveRoot = pr.getRoot().getPath();
        StringBuilder msg = new StringBuilder();
        msg.append("[findIncludeFile]");
        msg.append("[liveRoot=").append(liveRoot).append("]");
        msg.append("[currentPath=").append(currentPath).append("]");
        msg.append("[include=" + include + "]");

        if (SOSShell.IS_WINDOWS && include.isAbsolute()) {
            msg.append("[absolute]");
            if (Files.exists(include)) {
                msg.append(include);
                LOGGER.debug(msg.toString());
                return include;
            } else {
                msg.append("[not found]");
                msg.append(include.toAbsolutePath());
                LOGGER.error(msg.toString());
                return null;
            }
        }

        Path includePath = null;
        String is = include.toString();
        if (is.startsWith("/") || is.startsWith("\\")) {
            if (!SOSShell.IS_WINDOWS) {
                if (Files.exists(include)) {
                    msg.append("[absolute]");
                    includePath = include;
                }
            }
            if (includePath == null) {
                includePath = liveRoot.resolve(is.substring(1)).normalize();
                msg.append("[liveRoot.resolve]");
            }
        } else {
            includePath = currentPath.getParent().resolve(include).normalize();
            msg.append("[currentPath.getParent().resolve]");
        }
        if (Files.exists(includePath)) {
            msg.append(includePath);
            LOGGER.debug(msg.toString());
            return includePath;
        } else {
            msg.append("[not found]");
            msg.append(includePath.toAbsolutePath());
            LOGGER.error(msg.toString());
            return null;
        }
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
        ConverterObject<JobChain> o = converterObjects.getJobChains();
        for (Map.Entry<String, JobChain> entry : o.getUnique().entrySet()) {
            convertJobChainWorkflow(result, entry.getKey(), entry.getValue());
        }

        LOGGER.info("[convertJobChains]duplicates=" + o.getDuplicates().size());
        if (o.getDuplicates().size() > 0) {
            for (Map.Entry<String, List<JobChain>> entry : o.getDuplicates().entrySet()) {
                LOGGER.info("[convertJobChains][duplicate]" + entry.getKey());
                int counter = DUPLICATE_INITIAL_COUNTER;
                for (JobChain jn : entry.getValue()) {
                    String js7Name = getDuplicateName(entry.getKey(), counter);
                    LOGGER.info("[convertJobChains][duplicate][" + entry.getKey() + "][js7Name=" + js7Name + "][path]" + jn.getPath());
                    String fo = "";
                    try {
                        fo = " (first occurrence=" + o.getUnique().get(entry.getKey()).getPath().toString() + ")";
                    } catch (Throwable e) {

                    }
                    ConverterReport.INSTANCE.addAnalyzerRecord(jn.getPath(), "[DUPLICATE JS1]JobChain=" + entry.getKey(), "js7Name=" + js7Name + fo);
                    convertJobChainWorkflow(result, js7Name, jn);
                    counter++;
                }
            }
        }
    }

    private Workflow setWorkflowOrderPreparationOrResources(Workflow w, JobChain jobChain, Map<String, JobChainStateHelper> usedStates,
            List<JobChainNodeFileOrderSource> fileOrderSources) {
        Parameters parameters = null;
        if (fileOrderSources.size() > 0) {
            parameters = new Parameters();
            parameters.setAdditionalProperty("file", getOrderPreparationStringParameter(null));
            parameters.setAdditionalProperty("source_file", getOrderPreparationStringParameter(VAR_CURRENT_FILE_JS7));
            parameters.setAdditionalProperty("SCHEDULER_FILE_PATH", getOrderPreparationStringParameter(VAR_CURRENT_FILE_JS7));
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
                parameters.setAdditionalProperty(getParamNameFromJS1Order(usedStates, e.getKey()), getOrderPreparationStringParameter(hasOrders ? null
                        : e.getValue()));
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
        LOGGER.info("[" + method + "][" + jobChain.getName() + "]" + jobChain.getPath());

        Map<String, JobChainJobHelper> uniqueJobs = new LinkedHashMap<>();
        Map<String, JobChainStateHelper> allStates = new LinkedHashMap<>();
        Map<String, JobChainNodeFileOrderSink> fileOrderSinkStates = new HashMap<>();
        List<JobChainNodeFileOrderSource> fileOrderSources = new ArrayList<>();
        int duplicateJobCounter = 0;
        for (AJobChainNode n : jobChain.getNodes()) {
            switch (n.getType()) {
            case NODE:
                JobChainNode jcn = (JobChainNode) n;
                if (jcn.getOnReturnCodes() != null && jcn.getOnReturnCodes().size() > 0) {
                    for (JobChainNodeOnReturnCode rc : jcn.getOnReturnCodes()) {
                        ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "ON_RETURN_CODE not implemented yet", String.format(
                                "[state=%s][ON_RETURN_CODE]returnCode=%s,toState=%s", jcn.getState(), rc.getReturnCode(), rc.getToState()));
                    }
                }
                if (jcn.getJob() == null) {
                    if (jcn.getState() != null) {
                        allStates.put(jcn.getState(), new JobChainStateHelper(jcn, null, null));
                    }
                } else {
                    Path job = null;
                    try {
                        // job = findIncludeFile(pr, jobChain.getPath(), Paths.get(jcn.getJob() + EConfigFileExtensions.JOB.extension()));
                        job = jcn.getJobPath();
                        if (job == null) {
                            ConverterReport.INSTANCE.addWarningRecord(job, "[find job file][jobChain " + jobChain.getName() + "/node=" + SOSString
                                    .toString(n) + "]", "not found");
                        } else {
                            try {
                                OrderJob oj = js1OrderJobs.get(job);
                                if (oj == null) {
                                    LOGGER.info("[jobChain " + jobChain.getPath() + "/node=" + SOSString.toString(n)
                                            + "]no order job found. try to find a standalone shell job " + job);
                                    StandaloneJob sj = js1StandaloneShellJobs.get(job);
                                    if (sj == null) {
                                        throw new Exception("[job " + job + "]neither order job nor standalone shell job found");
                                    }
                                    oj = ACommonJob.convert(sj);
                                    js1OrderJobs.put(job, oj);
                                }
                                String js1JobName = oj.getName();
                                String js7JobName = JS7ConverterHelper.getJS7ObjectName(oj.getPath(), js1JobName);
                                if (uniqueJobs.containsKey(js1JobName)) {
                                    OrderJob uoj = uniqueJobs.get(js1JobName).getJob();
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

        String startState = getNodesStartState(allStates);
        Map<String, JobChainStateHelper> notUsedStates = new LinkedHashMap<>();
        convertJobChainWorkflow(result, js7Name, jobChain, startState, allStates, notUsedStates, uniqueJobs, fileOrderSinkStates, fileOrderSources,
                true, null);
    }

    private void convertJobChainWorkflow(JS7ConverterResult result, String js7Name, JobChain jobChain, String startState,
            Map<String, JobChainStateHelper> allStates, Map<String, JobChainStateHelper> notUsedStates, Map<String, JobChainJobHelper> uniqueJobs,
            Map<String, JobChainNodeFileOrderSink> fileOrderSinkStates, List<JobChainNodeFileOrderSource> fileOrderSources, boolean isMailJobChain,
            JobChainStateHelper currentNotUsedState) {
        String method = "convertJobChainWorkflow";
        List<Instruction> in = new ArrayList<>();
        Map<String, List<Instruction>> workflowInstructions = new LinkedHashMap<>();
        Map<String, String> fileOrderSinkJobs = new HashMap<>();

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            allStates.entrySet().forEach(e -> {
                LOGGER.debug(String.format("[convertJobChainWorkflow][%s][allStates]state=%s,helper=%s", jobChain.getName(), e.getKey(), SOSString
                        .toString(e.getValue())));
            });
        }

        Map<String, JobChainStateHelper> usedStates = new LinkedHashMap<>();
        BoardHelper boardHelper = new BoardHelper();
        TryCatchHelper tryCatchHelper = new TryCatchHelper(allStates);
        if (startState != null) {
            in.addAll(getNodesInstructions(workflowInstructions, jobChain, uniqueJobs, startState, allStates, usedStates, tryCatchHelper,
                    fileOrderSinkStates, fileOrderSinkJobs, boardHelper, false));
        } else {
            ConverterReport.INSTANCE.addErrorRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "]", "startState not found");
        }

        if (isDebugEnabled) {
            usedStates.entrySet().forEach(e -> {
                LOGGER.debug(String.format("[convertJobChainWorkflow][%s][usedStates]state=%s,helper=%s", jobChain.getName(), e.getKey(), SOSString
                        .toString(e.getValue())));
            });
        }

        if (isMailJobChain) {
            for (Map.Entry<String, JobChainStateHelper> e : allStates.entrySet()) {
                if (e.getValue().getJS1JobName() == null) {
                    continue;
                }
                if (!usedStates.containsKey(e.getKey())) {
                    notUsedStates.put(e.getKey(), e.getValue());
                }
            }
        } else {
            for (Map.Entry<String, JobChainStateHelper> e : usedStates.entrySet()) {
                if (notUsedStates.containsKey(e.getKey())) {
                    notUsedStates.remove(e.getKey());
                }
            }
        }

        Workflow w = new Workflow();
        w.setTitle(jobChain.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());
        w = setWorkflowOrderPreparationOrResources(w, jobChain, usedStates, fileOrderSources);

        JS7Agent jobChainAgent = getJobChainAgent(jobChain, false);
        Jobs js = new Jobs();
        Set<String> usedJobNames = usedStates.entrySet().stream().filter(e -> e.getValue().getJS1JobName() != null).map(e -> e.getValue()
                .getJS1JobName()).distinct().collect(Collectors.toSet());

        Map<String, JobHelper> yadeJobs = new HashMap<>();
        uniqueJobs.entrySet().forEach(e -> {
            OrderJob js1Job = e.getValue().getJob();
            if (usedJobNames.contains(e.getKey())) {
                JobHelper jh = getJob(result, js1Job, jobChainAgent, jobChain, e.getValue());
                if (jh == null) {
                    if (js1Job.isJavaJITLSplitterJob() || js1Job.isJavaJITLJoinJob()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][Job %s=%s][not converted]splitter/join job", method, jobChain.getName(), jobChain
                                    .getPath(), e.getKey(), js1Job.getPath()));
                        }
                    } else if (js1Job.isJavaJITLSynchronizerJob()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][Job %s=%s][not converted]synchronizer job", method, jobChain.getName(), jobChain
                                    .getPath(), e.getKey(), js1Job.getPath()));
                        }
                    } else {
                        try {
                            ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "]", "Job " + js1Job
                                    .getPath() + " cannot be converted");
                        } catch (Throwable ee) {
                        }
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][%s][Job %s=%s]cannot be converted", method, jobChain.getName(), jobChain.getPath(), e
                                    .getKey(), js1Job.getPath()));
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s]Job %s=%s", method, jobChain.getName(), jobChain.getPath(), e.getKey(), js1Job
                                .getPath()));
                    }
                    Job job = jh.getJS7Job();
                    job.setAdmissionTimeScheme(JS7RunTimeConverter.convert(js1Job));
                    js.setAdditionalProperty(e.getValue().getJS7JobName(), job);

                    if (jh.getShellJob() != null && jh.getShellJob().getYADE() != null) {
                        yadeJobs.put(e.getValue().getJS7JobName(), jh);
                    }
                }
            } else {
                // try {
                // ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "]", "Job " + e.getValue()
                // .getPath() + " not used");
                // } catch (Throwable ee) {
                // }
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][Job %s=%s]not used in the current workflow. handle separately", method, jobChain
                            .getName(), jobChain.getPath(), e.getKey(), js1Job.getPath()));
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

        in = cleanup(in);

        w.setInstructions(in);

        Path workflowPath = null;

        // when only 1 job in the workflow and for all another jobs the new workflows creates - rename the main workflow (add state)
        if (w.getJobs().getAdditionalProperties().size() == 1 && usedStates.size() == 1 && notUsedStates.size() > 0) {
            Map.Entry<String, JobChainStateHelper> firstStateHelper = usedStates.entrySet().stream().findFirst().orElse(null);
            if (firstStateHelper != null) {
                Map.Entry<String, Job> firstJob = w.getJobs().getAdditionalProperties().entrySet().stream().findFirst().orElse(null);
                if (firstJob != null) {
                    if (firstJob.getKey().equals(firstStateHelper.getValue().getJS7JobName())) {
                        workflowPath = getWorkflowPathFromJS1Path(jobChain.getPath(), (js7Name + NAME_CONCAT_CHARACTER + firstStateHelper.getValue()
                                .getJS7State()));
                    }
                }
            }
        }

        if (workflowPath == null) {
            if (isMailJobChain) {
                workflowPath = getWorkflowPathFromJS1Path(jobChain.getPath(), js7Name);
            } else {
                workflowPath = getWorkflowPathFromJS1Path(jobChain.getPath(), js7Name + NAME_CONCAT_CHARACTER + currentNotUsedState.getJS7State());
            }
        }
        // result.add(workflowPath, w);

        String workflowName = JS7ConverterHelper.getWorkflowName(workflowPath);
        handleBoardHelpers(boardHelper, workflowPath, workflowName);
        convertJobChainOrders2Schedules(result, jobChain, usedStates, workflowPath, workflowName, w, yadeJobs);
        convertJobChainFileOrderSources(result, jobChain, fileOrderSources, workflowPath, workflowName, jobChainFileWatchingAgent);

        result.add(workflowPath, w);

        if (notUsedStates.size() > 0) {
            if (isMailJobChain) {
                ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "Splitting into several workflows", "START");

            }
            ConverterReport.INSTANCE.addAnalyzerRecord(null, workflowName);

            String nus = getNodesStartState(notUsedStates);
            if (nus == null) {
                nus = notUsedStates.entrySet().iterator().next().getKey();
            }
            JobChainStateHelper h = notUsedStates.get(nus);
            notUsedStates.remove(nus);

            LOGGER.info("----------------------------------NotUsedStates=" + nus);

            convertJobChainWorkflow(result, js7Name, jobChain, nus, allStates, notUsedStates, uniqueJobs, fileOrderSinkStates, fileOrderSources,
                    false, h);

            /*
             * Path mainWorkflowPath = getWorkflowPathFromJS1Path(result, jobChain.getPath(), js7Name);
             * ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "Splitting into several workflows", "START");
             * ConverterReport.INSTANCE.addAnalyzerRecord(null, workflowName); convertJobChainWorkflowNotUsedStates(result, jobChain, uniqueJobs, notUsedStates,
             * workflowPath, getWorkflowName(mainWorkflowPath), jobChainAgent); ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(),
             * "Splitting into several workflows", "END");
             */
        } else {
            if (!isMailJobChain) {
                ConverterReport.INSTANCE.addAnalyzerRecord(null, workflowName);
                ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "Splitting into several workflows", "END");
            }
        }
    }

    private List<Instruction> cleanup(List<Instruction> in) {
        // Workaround - remove Finish from the last Try/Catch instruction
        if (in != null) {
            try {
                if (in.size() > 0) {
                    Instruction li = in.get(in.size() - 1);
                    if (li instanceof TryCatch) {
                        TryCatch ltc = (TryCatch) li;
                        if (ltc.getCatch() != null && ltc.getCatch().getInstructions() != null) {
                            ltc.getCatch().getInstructions().removeIf(lii -> lii instanceof Finish);
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
            }
        }
        return in;
    }

    private void handleBoardHelpers(BoardHelper workflowBoardHelper, Path workflowPath, String workflowName) {
        if (workflowBoardHelper.getJS7States().size() > 0) {
            Map<Path, List<String>> map = new HashMap<>();
            for (Map.Entry<String, Path> e : workflowBoardHelper.getJS7States().entrySet()) {
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
                b.setWorkflowPath(workflowPath);
                b.setWorkflowName(workflowName);
                for (String js7State : e.getValue()) {
                    b.getJS7States().put(js7State, e.getKey());
                }
                al.add(b);
                js7BoardHelpers.put(e.getKey(), al);
            }
        }
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
                name = JS7ConverterHelper.getJS7ObjectName(n.getJobChainPath(), name);
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
                result.add(JS7ConverterHelper.getFileOrderSourcePathFromJS7Path(workflowPath, name), fos);
            }
        }
    }

    private void convertJobChainOrders2Schedules(JS7ConverterResult result, JobChain jobChain, Map<String, JobChainStateHelper> usedStates,
            Path workflowPath, String workflowName, Workflow w, Map<String, JobHelper> yadeJobs) {
        if (jobChain.getOrders().size() > 0) {
            List<JobChainOrder> orders = jobChain.getOrders().stream().filter(o -> o.getRunTime() != null && !o.getRunTime().isEmpty()).collect(
                    Collectors.toList());
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            if (orders.size() > 0) {
                boolean hasJobChainConfigOrderParams = jobChain.getConfig() != null && jobChain.getConfig().hasOrderParams();

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
                    String js7OrderName = JS7ConverterHelper.getJS7ObjectName(o.getPath(), o.getName());
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

                        if (yadeJobs.size() > 0 && vs.getAdditionalProperties().size() > 0) {
                            boolean hasProfile = vs.getAdditionalProperties().get("profile") != null || vs.getAdditionalProperties().get(
                                    "PROFILE") != null;
                            boolean hasSettings = vs.getAdditionalProperties().get("settings") != null || vs.getAdditionalProperties().get(
                                    "SETTINGS") != null;

                            for (Map.Entry<String, JobHelper> ye : yadeJobs.entrySet()) {
                                Job wj = w.getJobs().getAdditionalProperties().get(ye.getKey());
                                if (wj != null) {
                                    if (hasProfile && !hasSettings && pr.getYadeConfiguration() != null) {
                                        if (wj.getJobResourceNames() == null || !wj.getJobResourceNames().contains(
                                                YADE_MAIN_CONFIGURATION_JOBRESOURCE)) {
                                            if (wj.getJobResourceNames() == null) {
                                                wj.setJobResourceNames(new ArrayList<>());
                                            }
                                            wj.getJobResourceNames().add(YADE_MAIN_CONFIGURATION_JOBRESOURCE);
                                        }
                                    }
                                    JobHelper yejh = ye.getValue();
                                    yejh.setJS7OrderVariables(vs);
                                    wj.setExecutable(getExecutableScript(yejh, null));
                                }
                            }
                        }
                    }
                    //
                    RunTimeHelper rth = convertRunTimeForSchedule("ORDER", o.getRunTime(), workflowPath, workflowName, add);
                    if (rth != null && rth.getSchedule() != null) {
                        Schedule s = rth.getSchedule();
                        s.setTitle(o.getTitle());

                        if (l.size() > 0) {
                            s.setOrderParameterisations(l);
                        }

                        if (startPosition != null) {
                            setSchedulePosition(s, startPosition);
                        }
                        result.add(rth.getPath(), s);
                    } else {
                        addJS1ScheduleFromScheduleOrRunTime(o.getRunTime(), l, startPosition, workflowPath, workflowName, add);
                    }
                }
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[convertJobChainOrders2Schedules][%s][skip]%s orders found but without Run-Time", jobChain.getPath(),
                            jobChain.getOrders().size()));
                }
            }
        }
    }

    public void setSchedulePosition(Schedule schedule, String startPosition) {
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
            long c = states.entrySet().stream().filter(e -> e.getValue().getJS1NextState().equals(entry.getKey()) || e.getValue().getJS1ErrorState()
                    .equals(entry.getKey())).count();
            if (c == 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    private List<Instruction> getNodesInstructions(Map<String, List<Instruction>> workflowInstructions, JobChain jobChain,
            Map<String, JobChainJobHelper> uniqueJobs, String js1StartState, Map<String, JobChainStateHelper> allStates,
            Map<String, JobChainStateHelper> usedStates, TryCatchHelper tryCatchHelper, Map<String, JobChainNodeFileOrderSink> fileOrderSinkStates,
            Map<String, String> fileOrderSinkJobs, BoardHelper boardHelper, boolean isFork) {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        List<Instruction> result = new ArrayList<>();

        JobChainStateHelper h = allStates.get(js1StartState);
        boolean hasConfigOrderParams = jobChain.getConfig() != null && jobChain.getConfig().hasOrderParams();
        boolean hasConfigProcess = jobChain.getConfig() != null && jobChain.getConfig().hasProcess();
        while (h != null) {
            JobChainJobHelper jh = uniqueJobs.get(h.getJS1JobName());
            if (jh == null) {
                return new ArrayList<>();
            }

            OrderJob job = uniqueJobs.get(h.getJS1JobName()).getJob();
            // NEW 11.10.2022
            if (usedStates.containsKey(h.getJS1State()) && !job.isJavaJITLJoinJob() && h.getJS1NextState().length() > 0 && !h
                    .isJS1NextStateEqualsErrorState()) {
                ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "BREAK NEXT_STATE HANDLING", "Recursion detected. Node(state=" + h
                        .getJS1State() + ",next_state=" + h.getJS1NextState() + ") already used");
                h = null;
                continue;
            }

            usedStates.put(h.getJS1State(), h);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getNodesInstructions]state=%s,isFork=%s,isSplitterJob=%s,isJoinJob=%s,isSynchronizerJob=%s", h
                        .getJS1State(), isFork, job.isJavaJITLSplitterJob(), job.isJavaJITLJoinJob(), job.isJavaJITLSynchronizerJob()));
            }

            if (isFork && job.isJavaJITLJoinJob()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[getNodesInstructions][skip][state=%s]because isFork and isJoinJob", h.getJS1State()));
                }
                h = null;
                continue;
            }

            List<Instruction> in = new ArrayList<>();

            if (job.isJavaJITLSplitterJob()) {
                ForkJoin forkJoin = new ForkJoin();
                List<Branch> branches = new ArrayList<>();
                int b = 1;

                List<String> splitterStates = getSplitterStates(job, jobChain, h.getJS1State());
                for (String splitterState : splitterStates) {
                    JobChainStateHelper sh = allStates.get(splitterState);
                    if (sh == null) {
                        ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "Splitter Job(state=" + h.getJS1State() + ")", "State "
                                + splitterState + " not found");
                    }
                    BranchWorkflow bw = new BranchWorkflow(getNodesInstructions(workflowInstructions, jobChain, uniqueJobs, splitterState, allStates,
                            usedStates, tryCatchHelper, fileOrderSinkStates, fileOrderSinkJobs, boardHelper, true), null);
                    branches.add(new Branch("branch_" + b, bw));
                    b++;
                }
                forkJoin.setBranches(branches);
                in.add(forkJoin);
            } else if (job.isJavaJITLJoinJob()) {

            } else if (job.isJavaJITLSynchronizerJob()) {
                in.add(new PostNotices(Collections.singletonList("sospn-" + h.getJS7State())));

                ExpectNotices en = new ExpectNotices();
                en.setNoticeBoardNames("sosen-" + h.getJS7State());
                in.add(en);

                boardHelper.getJS7States().put(h.getJS7State(), job.getPath());
                if (!js7SinkJobs.containsKey(job.getPath())) {
                    js7SinkJobs.put(job.getPath(), getUniqueSinkJobName(h.getJS7JobName(), null));
                }

            } else {
                SOSParameterSubstitutor ps = new SOSParameterSubstitutor(true, "${", "}");
                if (hasConfigOrderParams) {
                    jobChain.getConfig().getOrderParams().getParams().entrySet().forEach(e -> {
                        ps.addKey(e.getKey(), e.getValue());
                    });
                }
                NamedJobHelper njh = getNamedJobInstruction(job, h.getJS7JobName(), h.getJS7State(), hasConfigProcess ? jobChain.getConfig()
                        .getProcess().get(h.getJS1State()) : null, ps, jobChain);
                NamedJob nji = njh.getNamedJob();
                if (nji.getDefaultArguments() != null) {
                    jh.setJS7JobNodesDefaultArguments(nji.getDefaultArguments());
                }
                in.add(njh.getInstruction());
            }

            String onError = h.getOnError().toLowerCase();
            switch (onError) {
            case "setback":
                in = getRetryInstructions(job, in);
                break;
            case "suspend":
                // nothing to do - is default JS7 behaviour
                // in = getSuspendInstructions(job, in, h);
                break;
            }

            workflowInstructions.put(h.getJS1State(), in);
            JobChainStateHelper errorStateJob = allStates.get(h.getJS1ErrorState());
            if (errorStateJob != null && SOSString.isEmpty(errorStateJob.getJS1JobName())) {
                errorStateJob = null;
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]errorStateJob=%s", h.getJS1State(), errorStateJob));
            }

            if (h.getJS1ErrorState().length() > 0 && (errorStateJob != null || fileOrderSinkStates.containsKey(h.getJS1ErrorState()))) {
                if (tryCatchHelper.contains(h.getJS1ErrorState())) {
                    TryCatchPartHelper ph = tryCatchHelper.getTryCatchPart(h.getJS1ErrorState());
                    if (ph.getTryStates().contains(h.getJS1State())) {
                        ph.getLastTryCatch().getTry().getInstructions().addAll(workflowInstructions.get(h.getJS1State()));
                    } else if (ph.getCatchStates().contains(h.getJS1State())) {
                        ph.getLastTryCatch().getCatch().getInstructions().addAll(workflowInstructions.get(h.getJS1State()));
                    }

                    if (tryCatchHelper.contains(h.getJS1State())) {
                        result.add(tryCatchHelper.getTryCatchPart(h.getJS1State()).getTryCatch());

                        tryCatchHelper.remove(h.getJS1State());
                    }

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][TCH][CONTAINS errorState=%s]%s", h.getJS1State(), h.getJS1ErrorState(), ph));
                    }
                } else {
                    TryCatchPartHelper ph = null;
                    if (tryCatchHelper.contains(h.getJS1NextState())) {
                        ph = tryCatchHelper.getTryCatchPart(h.getJS1NextState());
                    } else if (tryCatchHelper.contains(h.getJS1State())) {
                        result.add(tryCatchHelper.getTryCatchPart(h.getJS1State()).getTryCatch());
                        // result.addAll(workflowInstructions.get(h.getJS1State()));
                        tryCatchHelper.remove(h.getJS1State());
                    }

                    NamedJob nj = null;
                    if (fileOrderSinkStates.containsKey(h.getJS1ErrorState())) {
                        nj = getFileOrderSinkNamedJob(fileOrderSinkStates.get(h.getJS1ErrorState()), h.getJS1ErrorState(), fileOrderSinkJobs);
                    }
                    tryCatchHelper.add(h, workflowInstructions.get(h.getJS1State()), ph, nj);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][TCH][ADD]%s", h.getJS1State(), tryCatchHelper));
                    }
                }

            } else if (workflowInstructions.get(h.getJS1State()) != null) {
                if (tryCatchHelper.contains(h.getJS1State())) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][WI][TCH]%s", h.getJS1State(), tryCatchHelper));
                    }

                    TryCatchPartHelper ph = tryCatchHelper.getTryCatchPart(h.getJS1State());
                    if (ph.getPreviousPartHelper() == null) {
                        if (ph.getCatchStates().contains(h.getJS1State())) {
                            ph.getTryCatch().getCatch().getInstructions().addAll(workflowInstructions.get(h.getJS1State()));
                            result.add(ph.getTryCatch());
                        } else if (ph.getTryStates().contains(h.getJS1State())) {
                            ph.getTryCatch().getTry().getInstructions().addAll(workflowInstructions.get(h.getJS1State()));
                            result.add(ph.getTryCatch());
                        } else {
                            result.add(ph.getTryCatch());
                            result.addAll(workflowInstructions.get(h.getJS1State()));

                        }

                        tryCatchHelper.remove(h.getJS1State());
                    } else {
                        if (ph.getPreviousPartHelper().isCatch()) {
                            ph.getPreviousPartHelper().getTryCatch().getCatch().getInstructions().addAll(workflowInstructions.get(h.getJS1State()));
                        } else {
                            ph.getPreviousPartHelper().getTryCatch().getTry().getInstructions().addAll(workflowInstructions.get(h.getJS1State()));
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][WI]%s", h.getJS1State(), tryCatchHelper));
                    }
                    List<Instruction> ins = workflowInstructions.get(h.getJS1State());
                    if (!tryCatchHelper.addByState(h.getJS1State(), ins)) {
                        result.addAll(ins);
                    }
                }
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]nextState=%s,errorState=%s", h.getJS1State(), h.getJS1NextState(), h.getJS1ErrorState()));
                LOGGER.debug(String.format("[BeforeEnd][%s]result=%s", h.getJS1State(), result));
            }

            // if (h.getJS1NextState().length() > 0 && !h.isJS1NextStateEqualsErrorState()) {

            String currentState = h.getJS1State();
            String nextState = h.getJS1NextState();
            String errorState = h.getJS1ErrorState();
            if (nextState.length() > 0) {
                h = allStates.get(nextState);
                if (h == null) {
                    //
                } else if (h.getJS1JobName() == null) {
                    h = null;
                }

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][nextState=%s]h=%s", currentState, nextState, h));
                }

                if (h == null) {
                    boolean cns = fileOrderSinkStates.containsKey(nextState);
                    boolean ces = fileOrderSinkStates.containsKey(errorState);
                    if (cns || ces) {
                        if (isDebugEnabled) {
                            LOGGER.debug("----------------[fileOrderSink]containsNextState=" + nextState + "=" + cns + ",containsErrorState="
                                    + errorState + "=" + ces);
                        }
                        boolean nextStateAdded = false;
                        if (tryCatchHelper.contains(nextState) && cns) {
                            TryCatchPartHelper ph = tryCatchHelper.getTryCatchPart(nextState);
                            if (ph.addFileOrderSink(null)) {
                                result.add(ph.getTryCatch());
                                tryCatchHelper.remove(nextState);
                                nextStateAdded = true;
                            }
                        }
                        if (tryCatchHelper.contains(errorState) && ces) {
                            NamedJob nextFileOrderSink = null;
                            if (!nextStateAdded && cns) {
                                nextFileOrderSink = getFileOrderSinkNamedJob(fileOrderSinkStates.get(nextState), nextState, fileOrderSinkJobs);
                            }

                            TryCatchPartHelper ph = tryCatchHelper.getTryCatchPart(errorState);
                            ph.addFileOrderSink(nextFileOrderSink);
                            result.add(ph.getTryCatch());

                            tryCatchHelper.remove(errorState);
                        }
                    }
                }
            } else {
                h = null;
            }

            if (h == null && errorState.length() > 0) {
                h = allStates.get(errorState);
                if (h == null) {
                    //
                } else {
                    if (h.getJS1JobName() == null) {
                        h = null;
                    }
                }
            }

            if (isDebugEnabled) {
                if (h == null) {
                    LOGGER.debug(String.format("[atEnd][null]result=%s", result));
                } else {
                    LOGGER.debug(String.format("[atEnd][%s]result=%s", h.getJS1State(), result));
                }
            }
        }
        return result;
    }

    private String getUniqueSinkJobName(String mainName, String newName) {
        String name = newName == null ? mainName : newName;
        Map.Entry<Path, String> s = js7SinkJobs.entrySet().stream().filter(e -> e.getValue().equals(name)).findFirst().orElse(null);
        if (s == null) {
            return name;
        }
        if (js7SinkJobsDuplicateCounter > 100) {
            return name;
        }
        js7SinkJobsDuplicateCounter++;
        return getUniqueSinkJobName(mainName, getDuplicateName(mainName, js7SinkJobsDuplicateCounter));
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
        job = JS7AgentHelper.setAgent(job, jobChainAgent);
        job = JS7ConverterHelper.setFromConfig(CONFIG, job);

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

    private ConverterObjects getConverterObjects(Folder root) {
        ConverterObjects co = new ConverterObjects();
        walk(root, co);
        return co;
    }

    private void walk(Folder f, ConverterObjects co) {
        for (StandaloneJob o : f.getStandaloneJobs()) {
            String name = JS7ConverterHelper.getJS7ObjectName(o.getPath(), o.getName());
            if (co.getStandalone().getUnique().containsKey(name)) {
                List<StandaloneJob> l = new ArrayList<>();
                if (co.getStandalone().getDuplicates().containsKey(name)) {
                    l = co.getStandalone().getDuplicates().get(name);
                }
                l.add(o);
                co.getStandalone().getDuplicates().put(name, l);
            } else {
                co.getStandalone().getUnique().put(name, o);
            }
        }
        for (JobChain o : f.getJobChains()) {
            String name = JS7ConverterHelper.getJS7ObjectName(o.getPath(), o.getName());
            if (co.getJobChains().getUnique().containsKey(name)) {
                List<JobChain> l = new ArrayList<>();
                if (co.getJobChains().getDuplicates().containsKey(name)) {
                    l = co.getJobChains().getDuplicates().get(name);
                }
                l.add(o);
                co.getJobChains().getDuplicates().put(name, l);
            } else {
                co.getJobChains().getUnique().put(name, o);
            }

            if (o.getOrders() != null) {
                for (JobChainOrder jo : o.getOrders()) {
                    js1Orders.add(jo);
                }
            }
        }
        for (ProcessClass o : f.getProcessClasses()) {
            String name = JS7ConverterHelper.getJS7ObjectName(o.getPath(), o.getName());
            if (co.getProcessClasses().getUnique().containsKey(name)) {
                List<ProcessClass> l = new ArrayList<>();
                if (co.getProcessClasses().getDuplicates().containsKey(name)) {
                    l = co.getProcessClasses().getDuplicates().get(name);
                }
                l.add(o);
                co.getProcessClasses().getDuplicates().put(name, l);
            } else {
                co.getProcessClasses().getUnique().put(name, o);
            }
        }
        for (Path o : f.getFiles()) {
            String n = o.getFileName().toString();
            if (co.getFiles().getUnique().containsKey(n)) {
                List<Path> l = new ArrayList<>();
                if (co.getFiles().getDuplicates().containsKey(n)) {
                    l = co.getFiles().getDuplicates().get(n);
                }
                l.add(o);
                co.getFiles().getDuplicates().put(n, l);
            } else {
                co.getFiles().getUnique().put(n, o);
            }
        }

        for (OrderJob o : f.getOrderJobs()) {
            js1OrderJobs.put(o.getPath(), o);
        }

        for (StandaloneJob sj : f.getStandaloneJobs()) {
            if (sj.isShellJob()) {
                js1StandaloneShellJobs.put(sj.getPath(), sj);
            }
        }

        for (Folder ff : f.getFolders()) {
            walk(ff, co);
        }
    }

    public DirectoryParserResult getPr() {
        return pr;
    }

    public Set<Path> getJS1JobStreamJobs() {
        return js1JobStreamJobs;
    }

}
