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
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
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
import com.sos.js7.converter.autosys.output.js7.helper.BoardExpectConsumHelper;
import com.sos.js7.converter.autosys.output.js7.helper.BoardHelper;
import com.sos.js7.converter.autosys.output.js7.helper.BoardTryCatchHelper;
import com.sos.js7.converter.autosys.output.js7.helper.PathResolver;
import com.sos.js7.converter.autosys.output.js7.helper.Report;
import com.sos.js7.converter.autosys.output.js7.helper.RunTimeHelper;
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

    public static AutosysConverterConfig CONFIG = new AutosysConverterConfig();

    private AutosysAnalyzer analyzer;

    private Map<String, JS7Agent> machine2js7Agent = new HashMap<>();

    public static DirectoryParserResult parseInput(Path input, Path reportDir, boolean isXMLParser) {
        AFileParser parser = isXMLParser ? new XMLJobParser(Autosys2JS7Converter.CONFIG, reportDir) : new JILJobParser(Autosys2JS7Converter.CONFIG,
                reportDir);
        LOGGER.info("[" + input + "][isXMLParser=" + isXMLParser + "]" + parser);
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

    private static ConverterResult convert(Path reportDir, DirectoryParserResult pr, AutosysAnalyzer analyzer) {
        // ----------------------------
        BoardHelper.clear();
        RunTimeHelper.clear();
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
                        analyzer.getConditionAnalyzer().handleJobBoxConditions(entry.getValue());
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
                LOGGER.info(String.format("[%s][standalone][CMD jobs=%s][start]...", method, size));
                for (ACommonJob j : value) {
                    c.convertStandalone(result, (JobCMD) j);
                }
                LOGGER.info(String.format("[%s][standalone][CMD jobs=%s][end]", method, size));
                break;
            case FW:
                LOGGER.info(String.format("[%s][%s jobs=%s]not implemented yet", method, key, size));
                for (ACommonJob j : value) {
                    ConverterReport.INSTANCE.addAnalyzerRecord(j.getSource(), j.getName(), j.getJobType().getValue() + ":not implemented yet");
                }
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

        // postProcessing(result);
        c.convertBoards(result);
        c.convertAgents(result);
        c.convertCalendars(result);

        Report.writeJS7Reports(pr, reportDir, analyzer);

        // AutosysReport.analyze(standaloneJobs, boxJobs);
        // return result;
        return new ConverterResult(result, standaloneJobs, boxJobs);
    }

    private void convertBoards(JS7ConverterResult result) {
        for (Map.Entry<Condition, Path> entry : BoardHelper.JS7_BOARDS.entrySet()) {
            Path p = entry.getValue().getParent();
            if (p == null) {
                p = Paths.get("");
            }
            if (SOSString.isEmpty(entry.getValue().getFileName().toString())) {
                LOGGER.info("AAAAAAAAAAAAA=" + entry.getValue() + "=" + p + "=" + entry.getKey());
            }

            JS7ConverterHelper.createNoticeBoardByParentPath(result, p, entry.getValue().getFileName().toString(), BoardHelper.getBoardTitle(entry
                    .getKey()));
        }
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
            for (String name : RunTimeHelper.JS7_CALENDARS) {
                result.add(JS7ConverterHelper.getCalendarPath(rootPath, name), JS7ConverterHelper.createDefaultWorkingDaysCalendar());
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
        // wr.setName(normalizeName(result, jilJob, jilJob.getName()));
        wr.setName(JS7ConverterHelper.getJS7ObjectName(jilJob.getName()));
        Workflow w = new Workflow();
        w.setTitle(jilJob.getDescription().getValue());
        w.setTimeZone(jilJob.getRunTime().getTimezone().getValue() == null ? CONFIG.getWorkflowConfig().getDefaultTimeZone() : jilJob.getRunTime()
                .getTimezone().getValue());

        Jobs jobs = new Jobs();
        for (ACommonJob j : jilJob.getJobs()) {
            // String jn = normalizeName(result, j, j.getName());
            String jn = JS7ConverterHelper.getJS7ObjectName(j.getName());
            if (j instanceof JobCMD) {
                jobs.setAdditionalProperty(jn, getJob(result, (JobCMD) j));
            } else {
                ConverterReport.INSTANCE.addErrorRecord("[convertBoxWorkflow][box=" + wr.getName() + "][job=" + jn + "][not impemented yet]type=" + j
                        .getConverterJobType());
            }
            // TODO FW etc jobs
        }
        w.setJobs(jobs);

        List<Instruction> in = new ArrayList<>(); // getExpectNoticeInstructions(result, wr.getName(), jilJob);
        // in.add(getNamedJobInstruction(jobName));
        // in = getCyclicWorkflowInstructions(jilJob, in);

        if (size == 1) {
            ACommonJob jJob = jilJob.getJobs().get(0);
            in.add(getNamedJobInstruction(JS7ConverterHelper.getJS7ObjectName(jJob.getName())));
        } else {
            List<ACommonJob> children = removeBoxJobMainConditionsFromChildren(jilJob);
            List<ACommonJob> childrenCopy = new ArrayList<>(children);
            List<ACommonJob> firstFork = getFirstForkChildren(jilJob, childrenCopy);
            List<ACommonJob> added = new ArrayList<>();
            // childrenCopy after getFirstForkChildren is without firstFork jobs and contains job dependent of the firstFork jobs
            if (firstFork.size() < 2) {
                ACommonJob child = firstFork.get(0);
                String cn = JS7ConverterHelper.getJS7ObjectName(child.getName());
                in.add(getNamedJobInstruction(cn));
                added.add(child);

                while (child != null) {
                    List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child, childrenCopy, added);
                    if (js.size() == 1) {
                        cn = JS7ConverterHelper.getJS7ObjectName(js.get(0).getName());
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
                String cn = JS7ConverterHelper.getJS7ObjectName(child.getName());
                in.add(getNamedJobInstruction(cn));
                added.add(child);
                childrenCopy.remove(child);

                while (child != null) {
                    List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child, childrenCopy, added);
                    if (js.size() == 1) {
                        cn = JS7ConverterHelper.getJS7ObjectName(js.get(0).getName());
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
            // LOGGER.debug("[convertBoxWorkflow]childrenCopy=" + childrenCopy);
            if (childrenCopy.size() > 0) {
                ConverterReport.INSTANCE.addErrorRecord("[convertBoxWorkflow][box=" + wr.getName() + "][not converted jobs]" + childrenCopy);
            }

        }
        in = getCyclicWorkflowInstructions(jilJob, in, null);
        w.setInstructions(in);

        if (fileWatchers.size() > 0) {
            Parameter p = new Parameter();
            p.setType(ParameterType.String);
            p.setDefault("${file}");

            Parameters ps = new Parameters();
            ps.getAdditionalProperties().put("file", p);

            w.setOrderPreparation(new Requirements(ps, false));
        }
        wr.setPath(PathResolver.getJS7WorkflowPath(jilJob, wr.getName()));
        wr.setTimezone(w.getTimeZone());

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
        convertSchedule(result, wr, jilJob);
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
            String cn = JS7ConverterHelper.getJS7ObjectName(child.getName());
            bwIn.add(getNamedJobInstruction(cn));

            ACommonJob child2 = child;
            while (child2 != null) {
                List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child2, childrenCopy, added);
                if (js.size() == 1) {
                    cn = JS7ConverterHelper.getJS7ObjectName(js.get(0).getName());
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

        // LOGGER.info("[findBoxJobChildSuccessor]currentChild=" + currentChild + ", children=" + children);
        // LOGGER.info(" added=" + added);
        for (ACommonJob j : children) {
            List<Condition> jConditions = getOnlyBoxJobsConditions(boxJob, j);
            // LOGGER.info("[findBoxJobChildSuccessor][child " + j.getName() + "]child conditions=" + jConditions + "=" + Conditions.getConditions(j
            // .getCondition().getCondition().getValue()));
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
        // LOGGER.info("[findBoxJobChildSuccessor]result=" + result);
        // LOGGER.info("[findBoxJobChildSuccessor]--------------------------------------------------------------------------");
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
        // LOGGER.info("[getFirstForkChildren][afterRemoveWithoutConditions]" + children);
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
                // LOGGER.info("[getFirstForkChildren][add][notThisBoxCondition]" + j.getName());
                withNotThisBoxConditions.add(j);
            }
        }
        result.addAll(withNotThisBoxConditions);
        children.removeAll(withNotThisBoxConditions);
        // LOGGER.info("[getFirstForkChildren][afterRemoveNotThisBoxConditions]" + children);
        return result;
    }

    private void convertStandalone(JS7ConverterResult result, JobCMD jilJob) {
        WorkflowResult wr = convertStandaloneWorkflow(result, jilJob);

        if (!jilJob.hasRunTime()) {
            jilJob.getRunTime().setTimezone(wr.getTimezone());
            jilJob.getRunTime().setStartTimes("00:00");
        }
        convertSchedule(result, wr, jilJob);
    }

    private WorkflowResult convertStandaloneWorkflow(JS7ConverterResult result, JobCMD jilJob) {
        // WORKFLOW

        String runTimeTimezone = jilJob.getRunTime().getTimezone().getValue();

        Workflow w = new Workflow();
        w.setTitle(jilJob.getDescription().getValue());
        w.setTimeZone(runTimeTimezone == null ? CONFIG.getWorkflowConfig().getDefaultTimeZone() : runTimeTimezone);

        WorkflowResult wr = new WorkflowResult();
        wr.setName(JS7ConverterHelper.getJS7ObjectName(jilJob.getName()));
        wr.setPath(PathResolver.getJS7WorkflowPath(jilJob, wr.getName()));
        wr.setTimezone(w.getTimeZone());

        // LOGGER.info("[convertStandalone]" + wr.getPath());

        Jobs js = new Jobs();
        js.setAdditionalProperty(wr.getName(), getJob(result, jilJob));
        w.setJobs(js);

        List<Instruction> in = new ArrayList<>(); // getExpectNoticeInstructions(result, wr.getName(), jilJob);
        BoardExpectConsumHelper nh = BoardHelper.expectNotices(analyzer, jilJob);
        ConsumeNotices cn = null;
        if (nh != null) {
            ExpectNotices en = nh.toExpectNotices();
            if (en != null) {
                in.add(en);
            }

            cn = nh.toConsumeNotices();
            if (cn != null) {
                // in.add(cn);
            }
        }

        // always try catch
        TryCatch tryCatch = new TryCatch();

        // Try
        List<Instruction> tryInstructions = new ArrayList<>();
        tryInstructions.add(getNamedJobInstruction(wr.getName()));

        BoardTryCatchHelper btch = new BoardTryCatchHelper(jilJob, analyzer);
        tryInstructions = getCyclicWorkflowInstructions(jilJob, tryInstructions, btch);

        if (btch.getTryPostNotices() != null) {
            tryInstructions.add(btch.getTryPostNotices());
        }

        Instructions inst;
        if (cn != null) {
            cn.setSubworkflow(new Instructions(tryInstructions));
            inst = new Instructions(Collections.singletonList(cn));
        } else {
            inst = new Instructions(tryInstructions);
        }

        tryCatch.setTry(inst);

        // Catch
        List<Instruction> catchInstructions = new ArrayList<>();
        if (btch.getCatchPostNotices() != null) {
            catchInstructions.add(btch.getCatchPostNotices());
        }
        catchInstructions.add(new Finish("'job terminates with return code: ' ++ $returnCode", true));
        tryCatch.setCatch(new Instructions(catchInstructions));

        // add TryCatch
        in.add(tryCatch);
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

    private void convertSchedule(JS7ConverterResult result, WorkflowResult wr, ACommonJob j) {
        Schedule s = RunTimeHelper.toSchedule(CONFIG, wr, j);
        if (s != null) {
            result.add(JS7ConverterHelper.getSchedulePathFromJS7Path(wr.getPath(), wr.getName(), ""), s);
        }
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

    private static NamedJob getNamedJobInstruction(String jobName) {
        NamedJob nj = new NamedJob(jobName);
        nj.setLabel(nj.getJobName());
        return nj;
    }

    private static List<Instruction> getCyclicWorkflowInstructions(ACommonJob jilJob, List<Instruction> in, BoardTryCatchHelper btch) {
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

            if (btch != null && btch.getTryPostNotices() != null) {
                in.add(btch.getTryPostNotices());
                btch.resetTryPostNotices();
            }

            Instructions ci = new Instructions(in);

            in = new ArrayList<>();
            in.add(new Cycle(ci, cs));

        }
        return in;
    }

}
