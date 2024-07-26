package com.sos.js7.converter.autosys.output.js7;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.Fail;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.instruction.schedule.CycleSchedule;
import com.sos.inventory.model.instruction.schedule.Periodic;
import com.sos.inventory.model.instruction.schedule.Scheme;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.DailyPeriod;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
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
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.JobFW;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.input.AFileParser;
import com.sos.js7.converter.autosys.input.DirectoryParser;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.input.JILJobParser;
import com.sos.js7.converter.autosys.input.XMLJobParser;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.report.AutosysReport;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.JS7ExportObjects;
import com.sos.js7.converter.commons.JS7ExportObjects.JS7ExportObject;
import com.sos.js7.converter.commons.agent.JS7AgentConverter;
import com.sos.js7.converter.commons.agent.JS7AgentConverter.JS7AgentConvertType;
import com.sos.js7.converter.commons.agent.JS7AgentHelper;
import com.sos.js7.converter.commons.config.JS7ConverterConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.config.items.SubFolderConfig;
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

    public static final String REPORT_FILE_NAME_BOX_CHILDREN_JOBS_RECURSION = "Report-BOX[children_jobs]recursion.txt";
    public static final String REPORT_FILE_NAME_BOX_CONDITION_REFERS_TO_CHILDREN_JOBS = "Report-BOX[condition]refers_to_children_jobs.txt";
    public static final String REPORT_FILE_NAME_BOX_CONDITION_REFERS_TO_BOX_ITSELF = "Report-BOX[condition]refers_to_box_itself.txt";
    public static final String REPORT_FILE_NAME_BOX_CONDITIONS_SUCCESS_FAILURE = "Report-BOX[conditions]box_success,box_failure.txt";

    public static final String REPORT_FILE_NAME_JOBS_WITH_OR_CONDITIONS = "Report-Conditions[OR].txt";
    public static final String REPORT_FILE_NAME_JOBS_WITH_GROUP_CONDITIONS = "Report-Conditions[Groups].txt";
    public static final String REPORT_FILE_NAME_CONDITIONS_BY_TYPE = "Report-Conditions[by_type].txt";
    public static final String REPORT_FILE_NAME_JOBS_DUPLICATES = "Report-Jobs[duplicates].txt";
    public static final String REPORT_FILE_NAME_JOBS_BY_TYPE = "Report-Jobs[by_type].txt";
    public static final String REPORT_FILE_NAME_JOBS_BY_APPLICATION_GROUP = "Report-Jobs[by_application,group].txt";

    public static final String REPORT_DELIMETER_LINE =
            "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
    public static final String REPORT_DETAILS_LINE = "---- DETAILS " + REPORT_DELIMETER_LINE;

    public static AutosysConverterConfig CONFIG = new AutosysConverterConfig();

    private Map<String, JS7Agent> machine2js7Agent = new HashMap<>();
    private Map<String, String> defaultAutosysJS7Calendars = new HashMap<>();

    public static DirectoryParserResult parseInput(Path input, Path reportDir, boolean isXMLParser) {
        AFileParser parser = isXMLParser ? new XMLJobParser(Autosys2JS7Converter.CONFIG, reportDir) : new JILJobParser(Autosys2JS7Converter.CONFIG,
                reportDir);
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

        ConverterResult cr = convert(reportDir, pr);
        JS7ConverterResult result = cr.getResult();
        LOGGER.info(String.format("[%s][JS7][convert][end]%s", method, SOSDate.getDuration(start, Instant.now())));
        // 5.1 - Converter Reports
        AutosysReport.analyze(cr.getStandaloneJobs(), cr.getBoxJobs());

        ConverterReportWriter.writeConverterReport(reportDir.resolve("converter_errors.csv"), reportDir.resolve("converter_warnings.csv"), reportDir
                .resolve("converter_analyzer.csv"));

        // 6 - Write JS7 files
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

        LOGGER.info(String.format("[%s][JS7][write][boards]...", method));
        OutputWriter.write(outputDir, result.getBoards());
        ConverterReport.INSTANCE.addSummaryRecord("Boards", result.getBoards().getItems().size());

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

    private static ConverterResult convert(Path reportDir, DirectoryParserResult pr) {
        String method = "convert";

        Autosys2JS7Converter c = new Autosys2JS7Converter();
        JS7ConverterResult result = new JS7ConverterResult();
        result.getApplications().addAll(pr.getJobs().stream().map(e -> e.getFolder().getApplication().getValue()).filter(Objects::nonNull).distinct()
                .collect(Collectors.toSet()));

        List<ACommonJob> standaloneJobs = new ArrayList<>();
        List<ACommonJob> boxJobs = new ArrayList<>();
        Map<ConverterJobType, List<ACommonJob>> jobsPerType = pr.getJobs().stream().collect(Collectors.groupingBy(ACommonJob::getConverterJobType,
                Collectors.toList()));
        int size = 0;
        for (Map.Entry<ConverterJobType, List<ACommonJob>> entry : jobsPerType.entrySet()) {
            ConverterJobType key = entry.getKey();
            List<ACommonJob> value = entry.getValue();
            size = value.size();

            switch (key) {
            case CMD:
                standaloneJobs.addAll(value);
                LOGGER.info(String.format("[%s][standalone][CMD jobs=%s][start]...", method, size));
                for (ACommonJob j : value) {
                    c.convertStandalone(result, (JobCMD) j);
                }
                LOGGER.info(String.format("[%s][standalone][CMD jobs=%s][end]", method, size));
                break;
            case BOX:
                boxJobs.addAll(value);
                break;
            default:
                LOGGER.info(String.format("[%s][%s jobs=%s]not implemented yet", method, key, size));
                for (ACommonJob j : value) {
                    ConverterReport.INSTANCE.addAnalyzerRecord(j.getSource(), j.getName(), j.getJobType().getValue() + ":not implemented yet");
                }
                break;
            }
        }

        size = boxJobs.size();
        if (size > 0) {
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s][start]...", method, size));
            for (ACommonJob j : boxJobs) {
                c.convertBoxWorkflow(result, (JobBOX) j);
            }
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s][end]", method, size));
        } else {
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s]skip", method, size));
        }

        postProcessing(result);

        c.convertAgents(result);
        c.convertCalendars(result);

        // AutosysReport.analyze(standaloneJobs, boxJobs);
        // return result;
        return new ConverterResult(result, standaloneJobs, boxJobs);
    }

    private void convertAgents(JS7ConverterResult result) {
        if (CONFIG.getGenerateConfig().getAgents()) {
            result = JS7ConverterHelper.convertAgents(result, machine2js7Agent.entrySet().stream().map(e -> e.getValue()).collect(Collectors
                    .toList()));
        }
    }

    private void convertCalendars(JS7ConverterResult result) {
        if (CONFIG.getGenerateConfig().getCalendars()) {
            Path rootPath = CONFIG.getCalendarConfig().getForcedFolder() == null ? Paths.get("") : CONFIG.getCalendarConfig().getForcedFolder();

            for (Map.Entry<String, String> e : defaultAutosysJS7Calendars.entrySet()) {
                result.add(JS7ConverterHelper.getCalendarPath(rootPath, e.getValue()), JS7ConverterHelper.createDefaultWorkingDaysCalendar());
            }
        }
    }

    private static void postProcessing(JS7ConverterResult result) {
        String method = "postProcessing";
        if (result.getPostNotices().isEmpty()) {
            LOGGER.info(String.format("[%s][skip]no postNotices found", method));
            return;
        }
        // SUCCESS
        for (String fullJobName : result.getPostNotices().getSuccess()) {
            String jobName = normalizeName(result, fullJobName, "[postProcessing][postNotice][success]");
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflowByPath(jobName);

            result.getWorkflows().getItems().forEach(i -> {
                LOGGER.trace("[postProcessing][postNotice][success]workflow=" + i.getOriginalPath().getPath());
            });
            // TODO for all types - failed, done etc. check create instructions ...
            if (eo == null) {
                eo = result.getExportObjectWorkflowByJobName(jobName);
            }
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]workflow not found", method, jobName));
                // ConverterReport.INSTANCE.addErrorRecord("[postProcessing][postNotice][success][workflow not found]" + jobName);
            } else {
                Workflow w = (Workflow) eo.getObject();
                w.getInstructions().add(new PostNotices(Collections.singletonList(jobName + "-success")));
                if (result.getPostNotices().getFailed().contains(fullJobName) || result.getPostNotices().getDone().contains(fullJobName)) {
                    TryCatch tryCatch = new TryCatch();
                    tryCatch.setTry(new Instructions(w.getInstructions()));

                    List<Instruction> catchIn = new ArrayList<>();
                    if (result.getPostNotices().getFailed().contains(fullJobName)) {
                        catchIn.add(new PostNotices(Collections.singletonList(jobName + "-failed")));
                    }
                    if (result.getPostNotices().getDone().contains(fullJobName)) {
                        catchIn.add(new PostNotices(Collections.singletonList(jobName + "-done")));
                    }
                    catchIn.add(new Fail("'job terminates with return code: ' ++ $returnCode", null, null));
                    tryCatch.setCatch(new Instructions(catchIn));

                    w.setInstructions(Collections.singletonList(tryCatch));
                }
                result.addOrReplace(eo.getOriginalPath().getPath(), w);
            }
        }

        // FAILED
        for (String fullJobName : result.getPostNotices().getFailed()) {
            if (result.getPostNotices().getSuccess().contains(fullJobName)) {
                continue;
            }

            String jobName = normalizeName(result, fullJobName, "[postProcessing][postNotice][failed]");
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflowByPath(jobName);
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]not found", method, jobName));
                ConverterReport.INSTANCE.addErrorRecord("[postProcessing][postNotice][failed][workflow not found]" + jobName);
            } else {
                Workflow w = (Workflow) eo.getObject();
                w.getInstructions().add(new PostNotices(Collections.singletonList(jobName + "-failed")));

                TryCatch tryCatch = new TryCatch();
                tryCatch.setTry(new Instructions(w.getInstructions()));

                List<Instruction> catchIn = new ArrayList<>();
                if (result.getPostNotices().getDone().contains(fullJobName)) {
                    catchIn.add(new PostNotices(Collections.singletonList(jobName + "-done")));
                }
                catchIn.add(new Fail("'job terminates with return code: ' ++ $returnCode", null, null));
                tryCatch.setCatch(new Instructions(catchIn));

                w.setInstructions(Collections.singletonList(tryCatch));
                result.addOrReplace(eo.getOriginalPath().getPath(), w);
            }
        }

        // DONE
        for (String fullJobName : result.getPostNotices().getDone()) {
            if (result.getPostNotices().getSuccess().contains(fullJobName)) {
                continue;
            }
            if (result.getPostNotices().getFailed().contains(fullJobName)) {
                continue;
            }

            String jobName = normalizeName(result, fullJobName, "[postProcessing][postNotice][done]");
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflowByPath(jobName);
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]not found", method, jobName));
                ConverterReport.INSTANCE.addErrorRecord("[postProcessing][postNotice][done][workflow not found]" + jobName);
            } else {
                Workflow w = (Workflow) eo.getObject();
                w.getInstructions().add(new PostNotices(Collections.singletonList(jobName + "-done")));

                TryCatch tryCatch = new TryCatch();
                tryCatch.setTry(new Instructions(w.getInstructions()));

                List<Instruction> catchIn = new ArrayList<>();
                catchIn.add(new PostNotices(Collections.singletonList(jobName + "-done")));
                catchIn.add(new Fail("'job terminates with return code: ' ++ $returnCode", null, null));
                tryCatch.setCatch(new Instructions(catchIn));

                w.setInstructions(Collections.singletonList(tryCatch));
                result.addOrReplace(eo.getOriginalPath().getPath(), w);
            }
        }

    }

    private void convertBoxWorkflow(JS7ConverterResult result, JobBOX jilJob) {
        if (jilJob.getJobs() == null) {
            return;
        }
        int size = jilJob.getJobs().size();
        if (size == 0) {
            return;
        }
        List<ACommonJob> fileWatchers = jilJob.getJobs().stream().filter(j -> j instanceof JobFW).collect(Collectors.toList());

        if (fileWatchers.size() > 0) {
            jilJob.getJobs().removeAll(fileWatchers);
        }

        // WORKFLOW
        WorkflowResult wr = new WorkflowResult();
        wr.setName(normalizeName(result, jilJob, jilJob.getName()));
        Workflow w = new Workflow();
        w.setTitle(jilJob.getDescription().getValue());
        w.setTimeZone(jilJob.getRunTime().getTimezone().getValue() == null ? CONFIG.getWorkflowConfig().getDefaultTimeZone() : jilJob.getRunTime()
                .getTimezone().getValue());

        Jobs jobs = new Jobs();
        for (ACommonJob j : jilJob.getJobs()) {
            String jn = normalizeName(result, j, j.getName());
            if (j instanceof JobCMD) {
                jobs.setAdditionalProperty(jn, getJob(result, (JobCMD) j));
            } else {
                ConverterReport.INSTANCE.addErrorRecord("[convertBoxWorkflow][box=" + wr.getName() + "][job=" + jn + "][not impemented yet]type=" + j
                        .getConverterJobType());
            }
            // TODO FW etc jobs
        }
        w.setJobs(jobs);

        List<Instruction> in = getExpectNoticeInstructions(result, wr.getName(), jilJob);
        // in.add(getNamedJobInstruction(jobName));
        // in = getCyclicWorkflowInstructions(jilJob, in);

        if (size == 1) {
            ACommonJob jJob = jilJob.getJobs().get(0);
            in.add(getNamedJobInstruction(normalizeName(result, jJob, jJob.getName())));
        } else {
            List<ACommonJob> children = removeBoxJobMainConditionsFromChildren(jilJob);
            List<ACommonJob> childrenCopy = new ArrayList<>(children);
            List<ACommonJob> firstFork = getFirstForkChildren(jilJob, childrenCopy);
            List<ACommonJob> added = new ArrayList<>();
            // childrenCopy after getFirstForkChildren is without firstFork jobs and contains job dependent of the firstFork jobs
            if (firstFork.size() < 2) {
                ACommonJob child = firstFork.get(0);
                String cn = normalizeName(result, child, child.getName());
                in.add(getNamedJobInstruction(cn));
                added.add(child);

                while (child != null) {
                    List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child, childrenCopy, added);
                    if (js.size() == 1) {
                        cn = normalizeName(result, js.get(0), js.get(0).getName());
                        in.add(getNamedJobInstruction(cn));

                        child = js.get(0);
                        added.add(child);
                    } else {
                        if (js.size() > 1) {
                            in.add(createForkJoin(result, jilJob, js, childrenCopy, added));
                        } else {
                            child = null;
                        }
                    }
                }
            } else {
                in.add(createForkJoin(result, jilJob, firstFork, childrenCopy, added));
            }

            // TODO
            if (childrenCopy.size() > 0) {
                ACommonJob child = childrenCopy.get(0);
                String cn = normalizeName(result, child, child.getName());
                in.add(getNamedJobInstruction(cn));
                added.add(child);
                childrenCopy.remove(child);

                while (child != null) {
                    List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child, childrenCopy, added);
                    if (js.size() == 1) {
                        cn = normalizeName(result, js.get(0), js.get(0).getName());
                        in.add(getNamedJobInstruction(cn));

                        child = js.get(0);
                        added.add(child);
                    } else {
                        if (js.size() > 1) {
                            in.add(createForkJoin(result, jilJob, js, childrenCopy, added));
                        } else {
                            child = null;
                        }
                    }
                }
            }
            LOGGER.debug("[convertBoxWorkflow]childrenCopy=" + childrenCopy);
            if (childrenCopy.size() > 0) {
                ConverterReport.INSTANCE.addErrorRecord("[convertBoxWorkflow][box=" + wr.getName() + "][not converted jobs]" + childrenCopy);
            }

        }
        in = getCyclicWorkflowInstructions(jilJob, in);
        w.setInstructions(in);

        if (fileWatchers.size() > 0) {
            Parameter p = new Parameter();
            p.setType(ParameterType.String);
            p.setDefault("${file}");

            Parameters ps = new Parameters();
            ps.getAdditionalProperties().put("file", p);

            w.setOrderPreparation(new Requirements(ps, false));
        }
        wr.setPath(getWorkflowPath(jilJob, wr.getName()));

        result.add(wr.getPath(), w);

        if (fileWatchers.size() > 0) {
            try {
                String agentName = w.getJobs().getAdditionalProperties().entrySet().iterator().next().getValue().getAgentName();
                JS7Agent a = new JS7Agent();
                a.setJS7AgentName(agentName);
                convertFileOrderSources(result, fileWatchers, wr, a);
            } catch (Throwable e) {
                LOGGER.error("[convertBoxWorkflow][box=" + wr.getName() + "][convertFileOrderSources]" + e.toString(), e);
                ConverterReport.INSTANCE.addErrorRecord(wr.getPath(), "[convertBoxWorkflow][box=" + wr.getName() + "]convertFileOrderSources", e);
            }
        }
        convertSchedule(result, jilJob, wr);
    }

    private void convertFileOrderSources(JS7ConverterResult result, List<ACommonJob> fileOrderSources, WorkflowResult wr, JS7Agent js7Agent) {
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
                fos.setAgentName(js7Agent.getJS7AgentName());
                fos.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());
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

    private static ForkJoin createForkJoin(JS7ConverterResult result, JobBOX jilJob, List<ACommonJob> children, List<ACommonJob> childrenCopy,
            List<ACommonJob> added) {
        List<Branch> branches = new ArrayList<>();
        Boolean joinIfFailed = false;
        int i = 1;
        added.addAll(children);
        for (ACommonJob child : children) {
            List<Instruction> bwIn = new ArrayList<>();
            String cn = normalizeName(result, child, child.getName());
            bwIn.add(getNamedJobInstruction(cn));

            ACommonJob child2 = child;
            while (child2 != null) {
                List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child2, childrenCopy, added);
                if (js.size() == 1) {
                    cn = normalizeName(result, js.get(0), js.get(0).getName());
                    bwIn.add(getNamedJobInstruction(cn));

                    child2 = js.get(0);
                    added.add(child2);
                } else {
                    if (js.size() > 1) {
                        bwIn.add(createForkJoin(result, jilJob, js, childrenCopy, added));
                    } else {
                        child2 = null;// js.get(0);
                    }
                }
            }

            BranchWorkflow bw = new BranchWorkflow(bwIn, null);

            Branch branch = new Branch("branch_" + i, bw);
            branches.add(branch);
            i++;

            // LOGGER.info("child.getCondition()=" + child.getCondition().getCondition().getValue() + "=" + child.getCondition().getOriginalCondition());
        }
        return new ForkJoin(branches, joinIfFailed);
    }

    private static List<ACommonJob> findBoxJobChildSuccessor(JobBOX boxJob, ACommonJob currentChild, List<ACommonJob> children,
            List<ACommonJob> added) {
        if (currentChild == null) {
            return new ArrayList<>();
        }
        // List<Condition> currentChildConditions = getOnlyBoxJobsConditions(boxJob, currentChild);

        List<ACommonJob> result = new ArrayList<>();

        LOGGER.info("[findBoxJobChildSuccessor]currentChild=" + currentChild + ", children=" + children);
        LOGGER.info("    added=" + added);
        for (ACommonJob j : children) {
            List<Condition> jConditions = getOnlyBoxJobsConditions(boxJob, j);
            LOGGER.info("[findBoxJobChildSuccessor][child " + j.getName() + "]child conditions=" + jConditions + "=" + Conditions.getConditions(j
                    .getCondition().getCondition().getValue()));
            boolean found = jConditions.stream().filter(c -> currentChild.isNameEquals(c)).count() > 0;
            if (found) {
                if (jConditions.size() == 1) {// only parent job
                    // TODO remove only box jobs conditions
                    j.getCondition().getCondition().getValue().clear();
                    result.add(j);
                } else {

                    long count = jConditions.stream().filter(c -> children.stream().filter(jj -> jj.isNameEquals(c)).count() > 0).count();
                    if (count == 0) {
                        count = jConditions.stream().filter(c -> added.stream().filter(jj -> jj.isNameEquals(c)).count() > 0).count();
                        if (count == 0) {
                            // TODO remove only box jobs conditions
                            j.getCondition().getCondition().getValue().clear();
                            result.add(j);
                        }
                    }
                }
            }
        }
        LOGGER.info("[findBoxJobChildSuccessor]result=" + result);
        LOGGER.info("[findBoxJobChildSuccessor]--------------------------------------------------------------------------");
        children.removeAll(result);
        return result;
    }

    private static List<Condition> getOnlyBoxJobsConditions(JobBOX boxJob, ACommonJob currentChild) {
        if (currentChild == null) {
            return new ArrayList<>();
        }
        List<ConditionType> excludedTypes = Arrays.asList(ConditionType.VARIABLE, ConditionType.NOTRUNNING, ConditionType.TERMINATED,
                ConditionType.EXITCODE);
        List<Condition> conditions = Conditions.getConditions(currentChild.getCondition().getCondition().getValue());
        return conditions.stream().filter(c -> !excludedTypes.contains(c.getType())).filter(x -> {
            return boxJob.getJobs().stream().filter(cj -> cj.isNameEquals(x)).count() > 0;
        }).collect(Collectors.toList());
    }

    public static List<ACommonJob> removeBoxJobMainConditionsFromChildren(JobBOX jilJob) {
        List<Object> mainConditions = jilJob.getCondition().getCondition().getValue();
        if (mainConditions != null && mainConditions.size() > 0) {
            List<ACommonJob> l = new ArrayList<ACommonJob>();
            for (ACommonJob j : jilJob.getJobs()) {
                List<Object> jConditions = j.getCondition().getCondition().getValue();
                if (jConditions != null && jConditions.size() > 0) {
                    for (Object o : mainConditions) {
                        if (o instanceof Condition) {
                            Conditions.remove(jConditions, (Condition) o);
                        }
                    }
                }
                l.add(j);
            }
            return l;
        }
        return jilJob.getJobs();
    }

    @SuppressWarnings("unused")
    private static Map<String, Map<ConditionType, List<ACommonJob>>> getBoxJobChildrenConditions(JobBOX boxJob, List<ACommonJob> children) {
        Map<String, Map<ConditionType, List<ACommonJob>>> result = new HashMap<>();

        List<ACommonJob> withoutConditions = children.stream().filter(e -> e.getCondition().getCondition().getValue() == null || e.getCondition()
                .getCondition().getValue().size() == 0).collect(Collectors.toList());
        // List<ACommonJob> result = new ArrayList<>(withoutConditions);
        List<ACommonJob> withNotThisBoxConditions = new ArrayList<>();

        children.removeAll(withoutConditions);
        LOGGER.info("getBoxJobChildrenConditions children adter remove=" + children);
        for (ACommonJob j : children) {
            List<Condition> conditions = Conditions.getConditions(j.getCondition().getCondition().getValue());
            long count = 0;
            for (Condition c : conditions) {
                if (c.getType().equals(ConditionType.VARIABLE)) {
                    continue;
                }
                // TODO - remove if supported
                if (c.getType().equals(ConditionType.NOTRUNNING) || c.getType().equals(ConditionType.TERMINATED) || c.getType().equals(
                        ConditionType.EXITCODE)) {
                    continue;
                }
                count += boxJob.getJobs().stream().filter(cj -> cj.isNameEquals(c)).count();
            }
            if (count == 0) {
                LOGGER.info("getBoxJobChildrenConditions ADD=" + j.getName());
                withNotThisBoxConditions.add(j);
            }
        }
        // result.addAll(withNotThisBoxConditions);
        children.removeAll(withNotThisBoxConditions);
        return result;
    }

    private static List<ACommonJob> getFirstForkChildren(JobBOX boxJob, List<ACommonJob> children) {
        List<ACommonJob> withoutConditions = children.stream().filter(e -> e.getCondition().getCondition().getValue() == null || e.getCondition()
                .getCondition().getValue().size() == 0).collect(Collectors.toList());
        List<ACommonJob> result = new ArrayList<>(withoutConditions);
        List<ACommonJob> withNotThisBoxConditions = new ArrayList<>();

        children.removeAll(withoutConditions);
        LOGGER.info("[getFirstForkChildren][afterRemoveWithoutConditions]" + children);
        for (ACommonJob j : children) {
            List<Condition> conditions = Conditions.getConditions(j.getCondition().getCondition().getValue());
            long count = 0;
            for (Condition c : conditions) {
                if (c.getType().equals(ConditionType.VARIABLE)) {
                    continue;
                }
                // TODO - remove if supported
                if (c.getType().equals(ConditionType.NOTRUNNING) || c.getType().equals(ConditionType.TERMINATED) || c.getType().equals(
                        ConditionType.EXITCODE)) {
                    continue;
                }
                count += boxJob.getJobs().stream().filter(cj -> cj.isNameEquals(c)).count();
            }
            if (count == 0) {
                LOGGER.info("[getFirstForkChildren][add][notThisBoxCondition]" + j.getName());
                withNotThisBoxConditions.add(j);
            }
        }
        result.addAll(withNotThisBoxConditions);
        children.removeAll(withNotThisBoxConditions);
        LOGGER.info("[getFirstForkChildren][afterRemoveNotThisBoxConditions]" + children);
        return result;
    }

    private void convertStandalone(JS7ConverterResult result, JobCMD jilJob) {

        LOGGER.info("[convertStandalone]baseName=" + jilJob.getBaseName() + ",fullPath=" + jilJob.getJobFullPathFromJILDefinition());

        WorkflowResult wr = convertStandaloneWorkflow(result, jilJob);
        convertSchedule(result, jilJob, wr);
    }

    private WorkflowResult convertStandaloneWorkflow(JS7ConverterResult result, JobCMD jilJob) {
        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(jilJob.getDescription().getValue());
        w.setTimeZone(jilJob.getRunTime().getTimezone().getValue() == null ? CONFIG.getWorkflowConfig().getDefaultTimeZone() : jilJob.getRunTime()
                .getTimezone().getValue());

        WorkflowResult wr = new WorkflowResult();
        wr.setName(normalizeName(result, jilJob, jilJob.getName()));
        wr.setPath(getWorkflowPath(jilJob, wr.getName()));

        Jobs js = new Jobs();
        js.setAdditionalProperty(wr.getName(), getJob(result, jilJob));
        w.setJobs(js);

        List<Instruction> in = getExpectNoticeInstructions(result, wr.getName(), jilJob);
        in.add(getNamedJobInstruction(wr.getName()));
        in = getCyclicWorkflowInstructions(jilJob, in);
        w.setInstructions(in);
        result.add(wr.getPath(), w);
        return wr;
    }

    private Job getJob(JS7ConverterResult result, JobCMD jilJob) {
        Job j = new Job();
        j.setTitle(jilJob.getDescription().getValue());
        j = setFromConfig(j);

        JS7Agent js7Agent = getAgent(result, j, jilJob);
        j = JS7AgentHelper.setAgent(j, js7Agent);
        j = setExecutable(j, jilJob, js7Agent.getPlatform());
        j = setJobOptions(j, jilJob);
        return j;
    }

    private void convertSchedule(JS7ConverterResult result, ACommonJob jilJob, WorkflowResult wr) {
        String calendarName = getCalendarName(result, CONFIG, jilJob);
        if (calendarName == null) {
            ConverterReport.INSTANCE.addWarningRecord(null, jilJob.getName(), "[convertSchedule][job without " + jilJob.getRunTime().getRunCalendar()
                    .getName() + "][missing callendar]scheduleConfig.forced- or defaultWorkingDayCalendarName is not configured");
            return;
        }

        AssignedCalendars calendar = new AssignedCalendars();
        calendar.setCalendarName(calendarName);
        calendar.setTimeZone(jilJob.getRunTime().getTimezone().getValue() == null ? CONFIG.getScheduleConfig().getDefaultTimeZone() : jilJob
                .getRunTime().getTimezone().getValue());
        Frequencies includes = new Frequencies();
        if (jilJob.getRunTime().getDaysOfWeek().getValue() != null) {
            WeekDays weekDays = new WeekDays();
            weekDays.setDays(JS7ConverterHelper.getDays(jilJob.getRunTime().getDaysOfWeek().getValue().getDays()));
            includes.setWeekdays(Collections.singletonList(weekDays));
        }
        calendar.setIncludes(includes);

        List<Period> periods = new ArrayList<>();
        if (jilJob.getRunTime().getStartTimes().getValue() != null) {
            for (String time : jilJob.getRunTime().getStartTimes().getValue()) {
                Period p = new Period();
                p.setSingleStart(time);
                p.setWhenHoliday(WhenHolidayType.SUPPRESS);
                periods.add(p);
            }
        } else if (jilJob.getRunTime().getStartMins().getValue() != null && jilJob.getRunTime().getStartMins().getValue().size() > 0) {
            Period p = new Period();
            if (CONFIG.getGenerateConfig().getCyclicOrders()) {
                p.setBegin(JS7ConverterHelper.toMins(jilJob.getRunTime().getStartMins().getValue().get(0)));
                p.setEnd("24:00");
                p.setRepeat(JS7ConverterHelper.toRepeat(jilJob.getRunTime().getStartMins().getValue()));

            } else {
                p.setSingleStart("00:00:00");
            }
            p.setWhenHoliday(WhenHolidayType.SUPPRESS);
            periods.add(p);
        }
        // re 2024-02-05
        if (periods.size() == 0) {
            return;
        }

        calendar.setPeriods(periods);

        Schedule s = new Schedule();
        s.setWorkflowNames(Collections.singletonList(wr.getName()));
        s.setPlanOrderAutomatically(CONFIG.getScheduleConfig().planOrders());
        s.setSubmitOrderToControllerWhenPlanned(CONFIG.getScheduleConfig().submitOrders());
        s.setCalendars(Collections.singletonList(calendar));
        result.add(JS7ConverterHelper.getSchedulePathFromJS7Path(wr.getPath(), wr.getName(), ""), s);
    }

    private String getCalendarName(JS7ConverterResult result, JS7ConverterConfig config, ACommonJob jilJob) {
        String name = null;
        if (config.getScheduleConfig().getForcedWorkingDayCalendarName() != null) {
            name = config.getScheduleConfig().getForcedWorkingDayCalendarName();
        } else if (jilJob.getRunTime().getRunCalendar().getValue() != null) {
            name = normalizeName(result, jilJob, jilJob.getRunTime().getRunCalendar().getValue());
        } else if (config.getScheduleConfig().getDefaultWorkingDayCalendarName() != null) {
            name = config.getScheduleConfig().getDefaultWorkingDayCalendarName();
        }
        if (!defaultAutosysJS7Calendars.containsKey(name)) {
            defaultAutosysJS7Calendars.put(name, name);
        }
        return name;
    }

    private static Job setFromConfig(Job j) {
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

    private JS7Agent getAgent(JS7ConverterResult result, Job j, JobCMD jilJob) {
        String machine = normalizeName(result, jilJob, jilJob.getMachine().getValue());
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
                LOGGER.debug(String.format("[getAgent][%s][autosys.machine=%s]", JS7AgentConvertType.CONFIG_DEFAULT.name(), machine));
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

        // source.setJS7AgentName("agent_name");
        return source;
    }

    private Job setExecutable(Job j, JobCMD jilJob, String platform) {
        boolean isMock = CONFIG.getMockConfig().hasScript();
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
            script.append(jilJob.getProfile().getValue()).append(JS7ConverterHelper.JS7_NEW_LINE);
        }
        if (isMock) {
            script.append(commentBegin);
        }
        script.append(command);

        if (isMock) {
            script.append(JS7ConverterHelper.JS7_NEW_LINE);
            script.append(isUnix ? CONFIG.getMockConfig().getUnixScript() : CONFIG.getMockConfig().getWindowsScript());
            script.append(JS7ConverterHelper.JS7_NEW_LINE);
        }

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());
        es.setV1Compatible(CONFIG.getJobConfig().getForcedV1Compatible());
        j.setExecutable(es);
        return j;
    }

    private String getScriptBegin(String command, boolean isUnix) {
        if (isUnix) {
            if (command != null && !command.toString().startsWith("#!/")) {
                StringBuilder sb = new StringBuilder();
                if (!SOSString.isEmpty(CONFIG.getJobConfig().getUnixDefaultShebang())) {
                    sb.append(CONFIG.getJobConfig().getUnixDefaultShebang());
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

    private static List<Instruction> getExpectNoticeInstructions(JS7ConverterResult result, String jobName, ACommonJob jilJob) {
        List<Instruction> wis = new ArrayList<>();

        Map<ConditionType, List<Condition>> map = Conditions.getByType(jilJob.getCondition().getCondition().getValue());
        for (Map.Entry<ConditionType, List<Condition>> e : map.entrySet()) {
            switch (e.getKey()) {
            case SUCCESS:
                for (Condition c : e.getValue()) {
                    String nn = normalizeName(result, c);
                    String boardName = nn + "-success";
                    String title = "Expect notice for successful job: " + c.getName();

                    wis.add(createExpectNotice(result, jilJob, boardName, title));

                    if (!result.getPostNotices().getSuccess().contains(c.getName())) {
                        result.getPostNotices().getSuccess().add(c.getName());
                    }
                }
                break;
            case DONE:
                for (Condition c : e.getValue()) {
                    String nn = normalizeName(result, c);
                    String boardName = nn + "-done";
                    String title = "Expect notice for done job: " + c.getName();

                    wis.add(createExpectNotice(result, jilJob, boardName, title));

                    if (!result.getPostNotices().getDone().contains(c.getName())) {
                        result.getPostNotices().getDone().add(c.getName());
                    }
                }
                break;
            case FAILURE:
                for (Condition c : e.getValue()) {
                    String nn = normalizeName(result, c);
                    String boardName = nn + "-failed";
                    String title = "Expect notice for failed job: " + c.getName();

                    wis.add(createExpectNotice(result, jilJob, boardName, title));

                    if (!result.getPostNotices().getFailed().contains(c.getName())) {
                        result.getPostNotices().getFailed().add(c.getName());
                    }
                }
                break;
            case VARIABLE:
                for (Condition c : e.getValue()) {
                    String nn = normalizeName(result, c);
                    String boardName = nn;
                    String title = "Expect notice for variable: " + c.getName();

                    wis.add(createExpectNotice(result, jilJob, boardName, title));
                }

                break;
            default:
                LOGGER.warn(String.format("[%s][%s][not used]size=%s", jilJob.getCondition().getOriginalCondition(), e.getKey(), e.getValue()
                        .size()));
                ConverterReport.INSTANCE.addWarningRecord(null, jobName, "[conditions][not used][original]" + jilJob.getCondition()
                        .getOriginalCondition());
                for (Condition c : e.getValue()) {
                    ConverterReport.INSTANCE.addWarningRecord(null, jobName, "    [condition][not used][detail][" + c.getType() + "]" + c.toString());
                }
                break;
            }
        }
        return wis;
    }

    private static NamedJob getNamedJobInstruction(String jobName) {
        NamedJob nj = new NamedJob(jobName);
        nj.setLabel(nj.getJobName());
        return nj;
    }

    private static List<Instruction> getCyclicWorkflowInstructions(ACommonJob jilJob, List<Instruction> in) {
        if (!CONFIG.getGenerateConfig().getCyclicOrders() && jilJob.getRunTime().getStartMins().getValue() != null) {
            Periodic p = new Periodic();
            p.setPeriod(3_600L);
            // TODO
            if (jilJob.getRunTime().getStartMins().getValue().size() == 60) {
                p.setOffsets(Collections.singletonList(60L));
            } else {
                p.setOffsets(jilJob.getRunTime().getStartMins().getValue().stream().map(e -> Long.valueOf(e * 60L)).collect(Collectors.toList()));
            }

            DailyPeriod dp = new DailyPeriod();
            dp.setSecondOfDay(0L);
            dp.setDuration(86_400L);

            CycleSchedule cs = new CycleSchedule(Collections.singletonList(new Scheme(p, new AdmissionTimeScheme(Collections.singletonList(dp)))));
            Instructions ci = new Instructions(in);

            in = new ArrayList<>();
            in.add(new Cycle(ci, cs));
        }
        return in;
    }

    private static ExpectNotices createExpectNotice(JS7ConverterResult result, ACommonJob jilJob, String boardName, String boardTitle) {
        ExpectNotices en = new ExpectNotices();
        en.setNoticeBoardNames("'" + boardName + "'");

        Board b = new Board();
        b.setTitle(boardTitle);
        b.setEndOfLife("$js7EpochMilli + 1 * 24 * 60 * 60 * 1000");
        b.setExpectOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");
        b.setPostOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");

        result.add(getNoticeBoardPath(jilJob, boardName), b);
        return en;
    }

    private static String normalizeName(JS7ConverterResult result, Condition c) {
        return normalizeName(result, c.getName(), String.format("[condition][%s][%s]", c.getType(), c));
    }

    private static String normalizeName(JS7ConverterResult result, String name, String msg) {
        int i = name.indexOf(".");
        if (i > -1) {
            if (result.getApplications().contains(name.substring(0, i))) {
                return name.substring(i + 1);
            }
            StringBuilder sb = new StringBuilder();
            if (msg != null) {
                sb.append(msg);
            }
            sb.append("[job application can't be extracted because unknown]").append(name);
            LOGGER.warn(sb.toString());
            // ConverterReport.INSTANCE.addErrorRecord(sb.toString());
        }
        return name;
    }

    private static String normalizeName(JS7ConverterResult result, ACommonJob jilJob, String name) {
        if (jilJob.getFolder().getApplication().getValue() != null) {
            if (name.startsWith(jilJob.getFolder().getApplication().getValue() + ".")) {
                name = name.substring((jilJob.getFolder().getApplication().getValue() + ".").length());
            }

            if (!result.getApplications().contains(jilJob.getFolder().getApplication().getValue())) {
                result.getApplications().add(jilJob.getFolder().getApplication().getValue());
            }
        }
        if (name != null) {
            name = JS7ConverterHelper.getJS7ObjectName(name);
        }
        return name;
    }

    // TODO sub folder
    private static Path getWorkflowPath(ACommonJob job, String normalizedJobName) {
        Path p = Paths.get(getApplication(job));
        Path subFolders = getSubFolders(getApplication(job), normalizedJobName);
        if (subFolders != null) {
            p = p.resolve(subFolders);
        }
        return p.resolve(normalizedJobName + ".workflow.json");
    }

    private static String getApplication(ACommonJob job) {
        if (job.getFolder() == null || job.getFolder().getApplication() == null || job.getFolder().getApplication().getValue() == null) {
            return "";
        }
        return job.getFolder().getApplication().getValue();
    }

    private static Path getNoticeBoardPath(ACommonJob job, String normalizedName) {
        Path p = Paths.get(getApplication(job));
        return p.resolve(normalizedName + ".noticeboard.json");
    }

    private static Path getSubFolders(String application, String normalizedName) {
        SubFolderConfig c = CONFIG.getSubFolderConfig();
        if (c.getMappings().size() > 0 && c.getSeparator() != null && application != null) {
            Integer position = c.getMappings().get(application);
            if (position != null && position > 0) {
                String[] arr = normalizedName.split(c.getSeparator());
                if (arr.length >= position) {
                    return Paths.get(arr[position]);
                } else {
                    // TODO use last or return null?
                    return Paths.get(arr[arr.length - 1]);
                }
            }
        }
        return null;
    }
}
