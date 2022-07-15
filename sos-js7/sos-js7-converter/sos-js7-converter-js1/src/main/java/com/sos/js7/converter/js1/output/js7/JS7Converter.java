package com.sos.js7.converter.js1.output.js7;

import java.io.IOException;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.Fail;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.RetryCatch;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.AdmissionTimePeriod;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.inventory.model.job.notification.JobNotificationMail;
import com.sos.inventory.model.job.notification.JobNotificationType;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.BranchWorkflow;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Parameters;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.js7.converter.commons.JS7ConverterConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.JS7Agent;
import com.sos.js7.converter.commons.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
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
import com.sos.js7.converter.js1.common.json.calendar.JS1Calendar;
import com.sos.js7.converter.js1.common.json.calendar.JS1Calendars;
import com.sos.js7.converter.js1.common.processclass.ProcessClass;
import com.sos.js7.converter.js1.common.runtime.RunTime;
import com.sos.js7.converter.js1.input.DirectoryParser;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;
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
 */
public class JS7Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7Converter.class);

    public static JS7ConverterConfig CONFIG = new JS7ConverterConfig();

    private static final int INITIAL_DUPLICATE_COUNTER = 1;
    private static final String MOVE_JOB_NAME = "fileOrderSinkMove";
    private static final String REMOVE_JOB_NAME = "fileOrderSinkRemove";
    private static final String VAR_CURRENT_FILE_JS7 = "${file}";
    private static final String VAR_CURRENT_FILE_JS1 = "${scheduler_file_path}";
    private static final String ENV_VAR_JS1_PREFIX = "SCHEDULER_PARAM_";

    private ConverterObjects converterObjects;
    private DirectoryParserResult pr;
    private String inputDirPath;
    private Map<Path, String> jobResources = new HashMap<>();
    private Map<String, Integer> jobResourcesDuplicates = new HashMap<>();
    private Map<Path, OrderJob> orderJobs = new HashMap<>();
    private Map<Path, AgentHelper> agents = new HashMap<>();
    private List<JobChainOrder> orders = new ArrayList<>();

    private Map<String, List<ACommonJob>> js1JobsByLanguage = new HashMap<>();
    private Map<String, List<RunTime>> js1Calendars = new HashMap<>();
    private Map<String, ScheduleHelper> js1Schedules = new HashMap<>();
    private List<ACommonJob> js1JobsWithMonitors = new ArrayList<>();

    public static void convert(Path input, Path outputDir, Path reportDir) throws IOException {

        String method = "convert";

        // APP start
        Instant appStart = Instant.now();
        LOGGER.info(String.format("[%s][start]...", method));

        OutputWriter.prepareDirectory(reportDir);
        OutputWriter.prepareDirectory(outputDir);

        // 1 - Parse JS1 files
        LOGGER.info(String.format("[%s][JIL][parse][start]...", method));
        DirectoryParserResult pr = DirectoryParser.parse(CONFIG.getParserConfig(), input, outputDir);
        LOGGER.info(String.format("[%s][JIL][parse][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));

        // 2 - Convert to JS7
        Instant start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][convert][start]...", method));
        JS7ConverterResult result = convert(pr);
        LOGGER.info(String.format("[%s][JS7][convert][end]%s", method, SOSDate.getDuration(start, Instant.now())));

        // 2.1 - Parser Reports
        ConverterReportWriter.writeParserReport(reportDir.resolve("parser_summary.csv"), reportDir.resolve("parser_errors.csv"), reportDir.resolve(
                "parser_warnings.csv"), reportDir.resolve("parser_analyzer.csv"));
        // 2.2 - Converter Reports
        ConverterReportWriter.writeConverterReport(reportDir.resolve("converter_errors.csv"), reportDir.resolve("converter_warnings.csv"), reportDir
                .resolve("converter_analyzer.csv"));

        // 3 - Write JS7 files
        start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][write][start]...", method));
        if (CONFIG.getGenerateConfig().getWorkflows()) {
            LOGGER.info(String.format("[%s][JS7][write][workflows]...", method));
            OutputWriter.write(outputDir, result.getWorkflows());
            ConverterReport.INSTANCE.addSummaryRecord("Workflows", result.getWorkflows().getItems().size());
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

        LOGGER.info(String.format("[%s][JS7][write][jobResources]...", method));
        OutputWriter.write(outputDir, result.getJobResources());
        ConverterReport.INSTANCE.addSummaryRecord("JobResources", result.getJobResources().getItems().size());

        LOGGER.info(String.format("[%s][JS7][write][fileOrderSources]...", method));
        OutputWriter.write(outputDir, result.getFileOrderSources());
        ConverterReport.INSTANCE.addSummaryRecord("FileOrderSources", result.getFileOrderSources().getItems().size());

        LOGGER.info(String.format("[%s][JS7][write][boards]...", method));
        OutputWriter.write(outputDir, result.getBoards());
        ConverterReport.INSTANCE.addSummaryRecord("Boards", result.getBoards().getItems().size());

        // 3.1 - Summary Report
        ConverterReportWriter.writeSummaryReport(reportDir.resolve("converter_summary.csv"));

        LOGGER.info(String.format("[%s][[JS7]write][end]%s", method, SOSDate.getDuration(start, Instant.now())));

        // APP end
        LOGGER.info(String.format("[%s][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));
    }

    private static JS7ConverterResult convert(DirectoryParserResult pr) {
        JS7ConverterResult result = new JS7ConverterResult();

        JS7Converter c = new JS7Converter();
        c.pr = pr;
        c.inputDirPath = pr.getRoot().getPath().toString();
        c.converterObjects = c.getConverterObjects(pr.getRoot());
        c.agents = c.getAgents();

        c.convertStandalone(result);
        c.convertJobChains(result);
        c.addJobResources(result);
        c.addSchedulesBasedOnJS1Schedule(result);

        c.analyzerReport();

        return result;
    }

    private Map<Path, AgentHelper> getAgents() {
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
                    result.put(pc.getPath(), new AgentHelper(entry.getKey() + "_copy" + counter, pc));
                    counter++;
                }
            }
        }
        return result;
    }

    private void addSchedulesBasedOnJS1Schedule(JS7ConverterResult result) {
        js1Schedules.entrySet().forEach(e -> {
            Schedule schedule = JS7RunTimeConverter.convert(e.getValue().js1Schedule, e.getValue().timeZone, e.getValue().workflows.stream().map(
                    w -> {
                        return w.name;
                    }).collect(Collectors.toList()));

            if (schedule != null) {
                result.add(getSchedulePath(e.getValue().workflows.get(0).path, e.getKey(), ""), schedule);
            }
        });
    }

    private void analyzerReport() {
        try {
            if (agents.size() > 0) {
                ParserReport.INSTANCE.addAnalyzerRecord("AGENTS", "START");

                agents.entrySet().stream().sorted(Map.Entry.<Path, AgentHelper> comparingByKey()).forEach(e -> {
                    ParserReport.INSTANCE.addAnalyzerRecord(e.getKey(), e.getValue().name, "standalone=" + e.getValue().standalone);
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
                        ParserReport.INSTANCE.addAnalyzerRecord(r.getCurrentPath(), r.getNodeText(), "");
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
    }

    private void addJobResources(JS7ConverterResult result) {
        String envVarPrefix = CONFIG.getJobConfig().isForcedV1Compatible() ? ENV_VAR_JS1_PREFIX : "";

        jobResources.entrySet().forEach(e -> {
            try {
                Params p = new Params(SOSXML.newXPath(), JS7ConverterHelper.getDocumentRoot(e.getKey()));
                Environment args = new Environment();
                Environment envs = new Environment();

                p.getParams().entrySet().forEach(pe -> {
                    args.setAdditionalProperty(pe.getKey(), JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(pe.getValue()));
                    envs.setAdditionalProperty(envVarPrefix + pe.getKey().toUpperCase(), "$" + pe.getKey());
                });
                JobResource jr = new JobResource(args, envs, null, null);
                result.add(Paths.get(e.getValue() + ".jobresource.json"), jr);
            } catch (Throwable e1) {
                ConverterReport.INSTANCE.addErrorRecord(e.getKey(), "jobResource=" + e.getValue(), e1);
            }
        });
    }

    private void convertStandalone(JS7ConverterResult result) {
        ConverterObject<StandaloneJob> o = converterObjects.standalone;
        for (Map.Entry<String, StandaloneJob> entry : o.unique.entrySet()) {
            convertStandaloneWorkflow(result, entry.getValue(), 0);
        }

        LOGGER.info("[convertStandalone]duplicates=" + o.duplicates.size());
        if (o.duplicates.size() > 0) {
            for (Map.Entry<String, List<StandaloneJob>> entry : o.duplicates.entrySet()) {
                LOGGER.info("[convertStandalone][duplicate]" + entry.getKey());
                int counter = INITIAL_DUPLICATE_COUNTER;
                for (StandaloneJob jn : entry.getValue()) {
                    LOGGER.info("[convertStandalone][duplicate][" + entry.getKey() + "][" + counter + "][path]" + jn.getPath());
                    convertStandaloneWorkflow(result, jn, counter);
                    counter++;
                }
            }
        }
    }

    private void convertStandaloneWorkflow(JS7ConverterResult result, StandaloneJob js1Job, int counter) {
        LOGGER.info("[convertStandaloneWorkflow]" + js1Job.getPath());

        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(js1Job.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());

        Jobs js = new Jobs();
        Job job = getJob(result, js1Job, null);
        if (job != null) {
            js.setAdditionalProperty(js1Job.getName(), job);
        }
        w.setJobs(js);

        List<Instruction> in = new ArrayList<>();
        in.add(getNamedJobInstruction(js1Job.getName(), js1Job.getName(), null, null));
        in = getRetryInstructions(js1Job, in);
        in = getCyclicWorkflowInstructions(js1Job, in);
        w.setInstructions(in);

        Path workflowPath = getWorkflowPath(result, js1Job, counter);
        String workflowName = getWorkflowName(workflowPath);

        RunTimeHelper rth = convertRunTime("STANDALONE", js1Job.getRunTime(), workflowPath, workflowName, "");
        if (rth != null) {
            if (rth.schedule != null) {
                result.add(rth.path, rth.schedule);
            }
        }
        addJS1ScheduleFromScheduleOrRunTime(js1Job.getRunTime(), workflowName, workflowPath);
        result.add(workflowPath, w);
    }

    // TODO quick tmp solution
    private void addJS1ScheduleFromScheduleOrRunTime(RunTime runTime, String workflowName, Path workflowPath) {
        if (runTime == null) {
            return;
        }

        try {
            com.sos.js7.converter.js1.common.runtime.Schedule schedule = null;
            if (runTime.getSchedule() != null && runTime.getSchedule().getRunTime().getCalendars() == null) {
                schedule = runTime.getSchedule();
            } else {
                schedule = RunTime.newSchedule(runTime, workflowName, workflowPath);
                if (!schedule.getRunTime().isConvertableWithoutCalendars()) {
                    schedule = null;
                }
            }

            if (schedule != null) {
                ScheduleHelper h = js1Schedules.get(schedule.getName());
                boolean add = false;
                if (h == null) {
                    h = new ScheduleHelper(schedule, runTime.getTimeZone());
                    add = true;
                } else {
                    add = h.workflows.stream().filter(w -> w.name.equals(workflowName)).findAny().orElse(null) == null;
                }
                if (add) {
                    h.workflows.add(new WorkflowHelper(workflowName, workflowPath));
                    js1Schedules.put(schedule.getName(), h);
                }
            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addErrorRecord(workflowPath, "error on handle schedule", e);
        }
    }

    private RunTimeHelper convertRunTime(String range, RunTime runTime, Path workflowPath, String workflowName, String additionalName) {
        if (runTime != null && !runTime.isEmpty()) {
            if (runTime.hasCalendars() || runTime.getSchedule() != null) {
                JS1Calendars calendars = runTime.getCalendars();
                if (calendars == null) {
                    calendars = runTime.getSchedule().getRunTime().getCalendars();
                }
                if (calendars == null || calendars.getCalendars() == null) {
                    ConverterReport.INSTANCE.addWarningRecord(workflowPath, "[" + range + "][not empty run time][with calendars or schedule]"
                            + runTime.getNodeText(), "calendars are null");
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
                            ConverterReport.INSTANCE.addWarningRecord(workflowPath, "[" + range + "][run time][calendars is null]" + runTime
                                    .getNodeText(), "calendars is null");
                            continue;
                        }

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

                    return new RunTimeHelper(getSchedulePath(workflowPath, workflowName, additionalName), s);
                }

            } else {
                AdmissionTimeScheme ats = runTimeToAdmissionTime(runTime);
                if (ats == null) {
                    ConverterReport.INSTANCE.addWarningRecord(workflowPath, "[" + range + "][not empty run time][without calendars or schedule]"
                            + runTime.getNodeText(), "not implemented yet");
                }
            }
        }
        return null;
    }

    private AdmissionTimeScheme runTimeToAdmissionTime(RunTime runTime) {
        List<AdmissionTimePeriod> periods = new ArrayList<>();
        if (runTime.getWeekDays() != null && runTime.getWeekDays().size() > 0) {
            for (com.sos.js7.converter.js1.common.runtime.WeekDays wd : runTime.getWeekDays()) {
                if (wd.getDays() != null) {
                    for (com.sos.js7.converter.js1.common.runtime.Day d : wd.getDays()) {
                        List<Integer> days = d.getDays();
                        if (days != null && days.size() > 0) {
                            // WeekdayPeriod wdp = new WeekdayPeriod(null);
                            // wdp.setSecondOfWeek(null);
                            // wdp.setDuration(null);

                            if (d.getPeriods() != null) {
                                for (com.sos.js7.converter.js1.common.runtime.Period p : d.getPeriods()) {
                                    // p.
                                }
                            }
                        }
                    }
                }
            }
        }
        return periods.size() > 0 ? new AdmissionTimeScheme(periods) : null;
    }

    private Path getSchedulePath(Path workflowPath, String workflowName, String additionalName) {
        Path parent = workflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return parent.resolve(workflowName + additionalName + ".schedule.json");
    }

    private Path getFileOrderSourcePath(Path workflowPath, String workflowName) {
        Path parent = workflowPath.getParent();
        if (parent == null) {
            parent = Paths.get("");
        }
        return parent.resolve(workflowName + ".fileordersource.json");
    }

    private String getWorkflowName(Path workflowPath) {
        return workflowPath.getFileName().toString().replace(".workflow.json", "");
    }

    private Path getWorkflowPath(JS7ConverterResult result, StandaloneJob job, int counter) {
        Path p = Paths.get(job.getPath().getParent().toString().substring(inputDirPath.length()));
        String add = counter == 0 ? "" : ("_copy" + counter);
        return p.resolve(job.getName() + add + ".workflow.json");
    }

    private Path getWorkflowPath(JS7ConverterResult result, JobChain jobChain, int counter) {
        Path p = Paths.get(jobChain.getPath().getParent().toString().substring(inputDirPath.length()));
        String add = counter == 0 ? "" : ("_copy" + counter);
        return p.resolve(jobChain.getName() + add + ".workflow.json");
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

                        List<DelayOrderAfterSetback> sorted = oj.getDelayOrderAfterSetback().stream().filter(e -> e.getSetbackCount() != null).sorted(
                                (o1, o2) -> o1.getSetbackCount().compareTo(o2.getSetbackCount())).collect(Collectors.toList());

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

                        List<DelayAfterError> sorted = job.getDelayAfterError().stream().filter(e -> e.getErrorCount() != null).sorted((o1, o2) -> o1
                                .getErrorCount().compareTo(o2.getErrorCount())).collect(Collectors.toList());

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
    private List<Instruction> getSuspendInstructions(ACommonJob job, List<Instruction> in, JobChainStateHelper h) {
        try {
            TryCatch tryCatch = new TryCatch();
            tryCatch.setTry(new Instructions(in));

            Fail f = new Fail();
            f.setMessage("Failed because of Job(name=" + h.jobName + ",label=" + h.state + ") error.");
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

    private List<Instruction> getCyclicWorkflowInstructions(StandaloneJob job, List<Instruction> in) {
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

    private Job getJob(JS7ConverterResult result, ACommonJob job, String jobChainAgentName) {
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

        JS7Agent js7Agent = setAgent(j, job, jobChainAgentName);
        setJobJobResources(j, job);
        setExecutable(j, job, jh, js7Agent);
        setJobOptions(j, job);
        setJobNotification(j, job);

        return j;
    }

    private NamedJob getNamedJobInstruction(String jobName, String jobLabel, Params params, SOSParameterSubstitutor ps) {
        NamedJob nj = new NamedJob(jobName);
        nj.setLabel(jobLabel);
        if (params != null && params.hasParams()) {
            if (ps != null) {
                params.getParams().entrySet().forEach(e -> {
                    ps.addKey(e.getKey(), e.getValue());
                });
            }

            Environment env = new Environment();
            params.getParams().entrySet().forEach(e -> {
                String replaced = replaceJS1Values(e.getValue());
                String val = ps == null ? replaced : ps.replace(replaced);
                env.setAdditionalProperty(e.getKey(), JS7ConverterHelper.quoteJS7StringValueWithSingleQuotes(val));
            });

            nj.setDefaultArguments(env);
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

    private JS7Agent setAgent(Job j, ACommonJob js1Job, String jobChainAgentName) {
        String name = null;
        String subagentClusterId = null;
        Platform platform = null;
        boolean isForcedPlatform = false;
        if (CONFIG.getAgentConfig().getForcedAgent() != null) {
            name = CONFIG.getAgentConfig().getForcedAgent().getName();
            platform = CONFIG.getAgentConfig().getForcedAgent().getPlatform();
            if (platform != null) {
                isForcedPlatform = true;
            }
        } else {
            if (jobChainAgentName != null) {
                name = jobChainAgentName;
            } else if (js1Job != null) {
                if (js1Job.getProcessClass() != null && js1Job.getProcessClass().isAgent()) {
                    // name = job.getProcessClass().getName();
                    AgentHelper ah = agents.get(js1Job.getProcessClass().getPath());
                    if (ah != null) {
                        name = ah.name;
                    }
                }
            }

        }
        if (platform == null) {
            platform = CONFIG.getAgentConfig().getForcedPlatform();
            if (platform != null) {
                isForcedPlatform = true;
            }
        }

        if (name == null && CONFIG.getAgentConfig().getDefaultAgent() != null) {
            name = CONFIG.getAgentConfig().getDefaultAgent().getName();
            if (platform == null) {
                platform = CONFIG.getAgentConfig().getDefaultAgent().getPlatform();
            }
        }
        if (name != null && CONFIG.getAgentConfig().getForcedAgent() == null && CONFIG.getAgentConfig().getMappings().containsKey(name)) {
            JS7Agent a = CONFIG.getAgentConfig().getMappings().get(name);
            name = a.getName();

            if (!isForcedPlatform) {
                platform = a.getPlatform();
            }
        }

        if (platform == null) {
            platform = Platform.UNIX;
        }
        j.setAgentName(name);
        j.setSubagentClusterId(subagentClusterId);
        return CONFIG.newJS7Agent(name, platform);
    }

    private void setExecutable(Job j, ACommonJob job, JS7JobHelper jh, JS7Agent js7Agent) {
        j.setExecutable(jh.getJavaJITLJob() == null ? getExecutableScript(j, job, js7Agent, jh.getShellJob()) : getInternalExecutable(j, job, jh
                .getJavaJITLJob()));
    }

    private ExecutableJava getInternalExecutable(Job j, ACommonJob job, JavaJITLJobHelper jitlJob) {
        ExecutableJava ej = new ExecutableJava();
        ej.setClassName(jitlJob.getNewJavaClass());
        ej.setArguments(getJobArguments(job, jitlJob));

        setLogLevel(ej, job);
        setMockLevel(ej);
        return ej;
    }

    private Environment getJobArguments(ACommonJob job, JavaJITLJobHelper jitlJob) {
        Environment env = null;
        if (job.getParams() != null && job.getParams().hasParams()) {
            // ARGUMENTS
            env = new Environment();
            if (jitlJob != null) {
                for (Map.Entry<String, String> e : jitlJob.getParams().getToAdd().entrySet()) {
                    env.setAdditionalProperty(e.getKey(), JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(replaceJS1Values(e.getValue())));
                }
            }
            for (Map.Entry<String, String> e : job.getParams().getParams().entrySet()) {
                try {
                    String name = e.getKey();
                    if (jitlJob != null) {
                        if (jitlJob.getParams().getToRemove().contains(name)) {
                            ConverterReport.INSTANCE.addWarningRecord(job.getPath(), "Job " + job.getName(), "Param[" + name + "=" + e.getValue()
                                    + "]" + " not converted because not used in JS7");
                            continue;
                        }
                        String n = jitlJob.getParams().getMapping().get(name);
                        if (n != null) {
                            ConverterReport.INSTANCE.addAnalyzerRecord(job.getPath(), "Job " + job.getName(), "Param[" + name + "=" + e.getValue()
                                    + "]" + " mapped to JS7 Argument[" + n + "]");
                            name = n;
                        }
                    }
                    env.setAdditionalProperty(name, JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(replaceJS1Values(e.getValue())));
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
            env.setAdditionalProperty("log_level", JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(logLevel.toUpperCase()));
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
                env.setAdditionalProperty("mock_level", JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(mockLevel));
                ej.setArguments(env);
            }
        }
    }

    private ExecutableScript getExecutableScript(Job j, ACommonJob job, JS7Agent js7Agent, ShellJobHelper shellJob) {
        StringBuilder scriptHeader = new StringBuilder();
        StringBuilder scriptCommand = new StringBuilder();
        StringBuilder yadeCommand = new StringBuilder();
        String commentBegin = "#";
        boolean isMock = CONFIG.getMockConfig().hasScript();

        boolean isUnix = js7Agent.getPlatform().equals(Platform.UNIX);
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
                yadeCommand.append("$").append(shellJob.getYADE().getBin()).append(" -settings $SETTINGS -profile $PROFILE");
            }
        } else {
            commentBegin = "REM";
            if (shellJob.getLanguage().equals("powershell")) {
                scriptHeader.append("@@findstr/v \"^@@f.*&\" \"%~f0\"|pwsh.exe -&goto:eof");
                scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
                scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            }
            if (shellJob.getYADE() != null) {
                yadeCommand.append("%").append(shellJob.getYADE().getBin()).append("% -settings %SETTINGS% -profile %PROFILE%");
            }
        }
        if (!shellJob.getLanguage().equals("shell")) {// language always lower case
            scriptHeader.append(commentBegin).append(" language=").append(shellJob.getLanguage());
            if (shellJob.getClassName() != null) {
                scriptHeader.append(",className=" + shellJob.getClassName());
            }
            scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            if (isMock) {
                if (shellJob.getYADE() != null) {
                    scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
                    scriptHeader.append(commentBegin).append(" ").append(yadeCommand);
                    scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
                }
            }
        }
        if (shellJob.getYADE() == null) {
            if (job.getScript().getInclude() != null) {
                // TODO resolve include
                scriptCommand.append(job.getScript().getInclude().getIncludeFile());
                scriptCommand.append(CONFIG.getJobConfig().getScriptNewLine());
            }
            if (job.getScript().getScript() != null) {
                scriptCommand.append(job.getScript().getScript());
            }
        } else {
            scriptCommand.append(yadeCommand);
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

        Environment args = getJobArguments(job, null);
        if (args != null) {
            if (es.getV1Compatible() != null && es.getV1Compatible()) {
                j.setDefaultArguments(args);// will be automatically set by JS7 as env var with the prefix SCHEDULER_PARAM_<arr name upper case>
            } else {
                Map<String, String> upper = args.getAdditionalProperties().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toUpperCase(),
                        e -> e.getValue()));

                args.getAdditionalProperties().clear();
                args.getAdditionalProperties().putAll(upper);
                es.setEnv(args);
            }
        }

        return es;
    }

    private void setJobJobResources(Job j, ACommonJob job) {
        if (job.getParams() != null && job.getParams().hasParams()) {
            // JOB RESOURCES
            List<String> names = new ArrayList<>();
            for (Include i : job.getParams().getIncludes()) {
                Path p = null;
                try {
                    p = findIncludeFile(pr, job.getPath(), i.getIncludeFile());
                } catch (Throwable e) {
                    ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "[params][find include=" + i.getNodeText() + "]", e);
                }
                if (p == null) {
                    continue;
                }
                String name = resolveJobResource(p);
                if (name != null) {
                    names.add(name);
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
                name = baseName + "_copy" + r;
                jobResources.put(p, name);
            }
        }
        return name;
    }

    public static Path findIncludeFile(DirectoryParserResult pr, Path currentPath, Path include) {
        String logPrefix = "[findIncludeFile][" + currentPath + "]";
        if (include.isAbsolute()) {
            LOGGER.debug(logPrefix + "[absolute]" + include);
            return include;
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
        return includePath;
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
            convertJobChainWorkflow(result, entry.getValue(), 0);
        }

        LOGGER.info("[convertJobChains]duplicates=" + o.duplicates.size());
        if (o.duplicates.size() > 0) {
            for (Map.Entry<String, List<JobChain>> entry : o.duplicates.entrySet()) {
                LOGGER.info("[convertJobChains][duplicate]" + entry.getKey());
                int counter = INITIAL_DUPLICATE_COUNTER;
                for (JobChain jn : entry.getValue()) {
                    LOGGER.info("[convertJobChains][duplicate][" + entry.getKey() + "][" + counter + "][path]" + jn.getPath());
                    convertJobChainWorkflow(result, jn, counter);
                    counter++;
                }
            }
        }
    }

    private Parameter getStringParameter(String defaultValue) {
        Parameter p = new Parameter();
        p.setType(ParameterType.String);
        if (defaultValue != null) {
            p.setDefault(JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(replaceJS1Values(defaultValue)));
        }
        return p;
    }

    private Workflow setWorkflowOrderPreparationOrResources(Workflow w, JobChain jobChain, List<JobChainNodeFileOrderSource> fileOrderSources) {
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
        for (JobChainOrder o : jobChain.getOrders()) {
            Params ps = o.getParams();
            if (ps != null && ps.hasParams()) {
                hasOrders = true;
                params.putAll(ps.getParams());

                for (Include i : ps.getIncludes()) {
                    Path path = null;
                    try {
                        path = findIncludeFile(pr, o.getPath(), i.getIncludeFile());
                    } catch (Throwable e) {
                        ConverterReport.INSTANCE.addErrorRecord(o.getPath(), "[order params][include=" + i.getNodeText() + "]" + e.toString(), e);
                    }
                    if (path != null) {
                        String name = resolveJobResource(path);
                        if (name != null) {
                            jobResources.add(name);
                        }
                    }
                }
            }
        }

        if (params.size() > 0) {
            if (parameters == null) {
                parameters = new Parameters();
            }
            for (Map.Entry<String, String> e : params.entrySet()) {
                parameters.setAdditionalProperty(e.getKey(), getStringParameter(hasOrders ? null : e.getValue()));
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

    private void convertJobChainWorkflow(JS7ConverterResult result, JobChain jobChain, int counter) {
        LOGGER.info("[convertJobChainWorkflow]" + jobChain.getPath());

        Workflow w = new Workflow();
        w.setTitle(jobChain.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());

        Map<String, OrderJob> uniqueJobs = new LinkedHashMap<>();
        Map<String, JobChainStateHelper> states = new LinkedHashMap<>();
        Map<String, JobChainNodeFileOrderSink> fileOrderSinkStates = new HashMap<>();
        List<JobChainNodeFileOrderSource> fileOrderSources = new ArrayList<>();
        int duplicateJobCounter = 0;
        for (AJobChainNode n : jobChain.getNodes()) {
            switch (n.getType()) {
            case NODE:
                JobChainNode jcn = (JobChainNode) n;
                if (jcn.getJob() == null) {
                    if (jcn.getState() != null) {
                        states.put(jcn.getState(), new JobChainStateHelper(jcn, null));
                    }
                } else {
                    Path job = null;
                    try {
                        job = findIncludeFile(pr, jobChain.getPath(), Paths.get(jcn.getJob() + EConfigFileExtensions.JOB.extension()));
                    } catch (Throwable e) {
                        ConverterReport.INSTANCE.addErrorRecord(job, "[find job file][jobChain " + jobChain.getName() + "/node=" + SOSString.toString(
                                n) + "]", e);
                    }
                    if (job != null) {
                        try {
                            OrderJob oj = orderJobs.get(job);
                            if (oj == null) {
                                throw new Exception("[job " + job + "]not found");
                            }
                            String jobName = EConfigFileExtensions.getJobName(job);
                            if (uniqueJobs.containsKey(jobName)) {
                                OrderJob uoj = uniqueJobs.get(jobName);
                                // same name but another location
                                if (!uoj.getPath().equals(oj.getPath())) {
                                    duplicateJobCounter++;
                                    jobName = jobName + "_" + duplicateJobCounter;
                                    uniqueJobs.put(jobName, oj);
                                }
                            } else {
                                uniqueJobs.put(jobName, oj);
                            }

                            states.put(jcn.getState(), new JobChainStateHelper(jcn, jobName));
                        } catch (Throwable e) {
                            LOGGER.warn("[jobChain " + jobChain.getPath() + "/node=" + SOSString.toString(n) + "]" + e.getMessage());
                            ConverterReport.INSTANCE.addWarningRecord(job, "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n) + "]",
                                    e.toString());
                        }
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

        w = setWorkflowOrderPreparationOrResources(w, jobChain, fileOrderSources);

        String agentName = getJobChainAgentName(jobChain, false);
        Jobs js = new Jobs();
        uniqueJobs.entrySet().forEach(e -> {
            Job job = getJob(result, e.getValue(), agentName);
            if (job != null) {
                js.setAdditionalProperty(e.getKey(), job);
            }
        });
        w.setJobs(js);

        List<Instruction> in = new ArrayList<>();
        Map<String, List<Instruction>> workflowInstructions = new LinkedHashMap<>();
        String startState = getNodesStartState(states);
        Map<String, String> fileOrderSinkJobs = new HashMap<>();

        if (LOGGER.isDebugEnabled()) {
            states.entrySet().forEach(e -> {
                LOGGER.debug(String.format("[convertJobChainWorkflow]state=%s,helper=%s", e.getKey(), SOSString.toString(e.getValue())));
            });
        }

        if (startState != null) {
            in.addAll(getNodesInstructions(workflowInstructions, jobChain, uniqueJobs, startState, states, fileOrderSinkStates, fileOrderSinkJobs,
                    false));
        } else {
            ConverterReport.INSTANCE.addErrorRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "]", "startState not found");
        }

        // FileOrderSources and FileOrderSink
        String agentNameWithFileWatching = getJobChainAgentName(jobChain, true);
        if (CONFIG.getAgentConfig().getForcedAgent() != null) {
            agentNameWithFileWatching = CONFIG.getAgentConfig().getForcedAgent().getName();
        }
        if (agentNameWithFileWatching == null && CONFIG.getAgentConfig().getDefaultAgent() != null) {
            agentNameWithFileWatching = CONFIG.getAgentConfig().getDefaultAgent().getName();
        }
        if (w.getJobs() != null) {
            for (Map.Entry<String, String> e : fileOrderSinkJobs.entrySet()) {
                w.getJobs().getAdditionalProperties().put(e.getKey(), getFileOrderSinkJob(e.getKey(), agentNameWithFileWatching));
            }
        }

        w.setInstructions(in);
        Path workflowPath = getWorkflowPath(result, jobChain, counter);
        result.add(workflowPath, w);

        String workflowName = getWorkflowName(workflowPath);
        convertJobChainOrders2Schedules(result, jobChain, workflowPath, workflowName);
        convertJobChainFileOrderSources(result, jobChain, fileOrderSources, workflowPath, workflowName, agentNameWithFileWatching);

    }

    private String getJobChainAgentName(JobChain jobChain, boolean checkFileWatching) {
        String agentName = null;
        if (checkFileWatching) {
            if (jobChain.getFileWatchingProcessClass() != null) {
                agentName = jobChain.getFileWatchingProcessClass().getName();
            }
        } else {
            if (jobChain.getProcessClass() != null) {
                agentName = jobChain.getProcessClass().getName();
            }
        }
        if (agentName != null && CONFIG.getAgentConfig().getForcedAgent() == null) {
            JS7Agent a = CONFIG.getAgentConfig().getMappings().get(agentName);
            if (a != null) {
                agentName = a.getName();
            }
        }
        return agentName;
    }

    private void convertJobChainFileOrderSources(JS7ConverterResult result, JobChain jobChain, List<JobChainNodeFileOrderSource> fileOrderSources,
            Path workflowPath, String workflowName, String agentName) {
        if (fileOrderSources.size() > 0) {
            boolean useNextState = fileOrderSources.size() > 1;
            for (JobChainNodeFileOrderSource n : fileOrderSources) {
                String name = workflowName;
                if (useNextState) {
                    name = name + "_" + n.getNextState();
                }
                FileOrderSource fos = new FileOrderSource();
                fos.setWorkflowName(workflowName);
                fos.setAgentName(agentName);
                fos.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());
                fos.setDirectoryExpr(JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(n.getDirectory()));
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
                result.add(getFileOrderSourcePath(workflowPath, name), fos);
            }
        }
    }

    private void convertJobChainOrders2Schedules(JS7ConverterResult result, JobChain jobChain, Path workflowPath, String workflowName) {
        if (jobChain.getOrders().size() > 0) {
            List<JobChainOrder> orders = jobChain.getOrders().stream().filter(o -> o.getRunTime() != null && !o.getRunTime().isEmpty()).collect(
                    Collectors.toList());
            if (orders.size() > 0) {
                boolean hasJobChainConfigOrderParams = jobChain.getConfig() != null && jobChain.getConfig().hasOrderParams();

                for (JobChainOrder o : orders) {
                    RunTimeHelper rth = convertRunTime("ORDER", o.getRunTime(), workflowPath, workflowName, "_" + o.getName());
                    if (rth != null && rth.schedule != null) {
                        Schedule s = rth.schedule;
                        s.setTitle(o.getTitle());

                        List<OrderParameterisation> l = new ArrayList<>();
                        boolean hasOrderParams = o.getParams() != null && o.getParams().hasParams();
                        if (hasJobChainConfigOrderParams || hasOrderParams) {
                            OrderParameterisation set = new OrderParameterisation();
                            set.setOrderName(o.getName());

                            Variables vs = new Variables();
                            if (hasJobChainConfigOrderParams) {
                                jobChain.getConfig().getOrderParams().getParams().entrySet().forEach(e -> {
                                    vs.setAdditionalProperty(e.getKey(), e.getValue());
                                });
                            }
                            if (hasOrderParams) {
                                o.getParams().getParams().entrySet().forEach(e -> {
                                    vs.setAdditionalProperty(e.getKey(), e.getValue());
                                });
                            }
                            set.setVariables(vs);
                            l.add(set);
                        }
                        if (l.size() > 0) {
                            s.setOrderParameterisations(l);
                        }
                        result.add(rth.path, s);
                    } else {
                        addJS1ScheduleFromScheduleOrRunTime(o.getRunTime(), workflowName, workflowPath);
                    }
                }
            }
        }
    }

    private String getNodesStartState(Map<String, JobChainStateHelper> states) {
        for (Map.Entry<String, JobChainStateHelper> entry : states.entrySet()) {
            long c = states.entrySet().stream().filter(e -> e.getValue().nextState.equals(entry.getKey()) || e.getValue().errorState.equals(entry
                    .getKey())).count();
            if (c == 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    private List<Instruction> getNodesInstructions(Map<String, List<Instruction>> workflowInstructions, JobChain jobChain,
            Map<String, OrderJob> uniqueJobs, String startState, Map<String, JobChainStateHelper> states,
            Map<String, JobChainNodeFileOrderSink> fileOrderSinkStates, Map<String, String> fileOrderSinkJobs, boolean isFork) {
        List<Instruction> result = new ArrayList<>();

        JobChainStateHelper h = states.get(startState);
        boolean hasConfigOrderParams = jobChain.getConfig() != null && jobChain.getConfig().hasOrderParams();
        boolean hasConfigProcess = jobChain.getConfig() != null && jobChain.getConfig().hasProcess();
        while (h != null) {
            OrderJob job = uniqueJobs.get(h.jobName);
            if (job == null) {
                return new ArrayList<>();
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[getNodesInstructions]state=%s,isFork=%s,isJoinJob=%s", h.state, isFork, job.isJavaJITLJoinJob()));
            }

            if (isFork && job.isJavaJITLJoinJob()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[getNodesInstructions][skip][state=%s]because isFork and isJoinJob", h.state));
                }
                h = null;
                continue;
            }

            List<Instruction> in = new ArrayList<>();

            if (job.isJavaJITLSplitterJob()) {
                ForkJoin forkJoin = new ForkJoin();
                List<Branch> branches = new ArrayList<>();
                int b = 1;

                List<String> splitterStates = getSplitterStates(job, jobChain, h.state);
                for (String splitterState : splitterStates) {
                    JobChainStateHelper sh = states.get(splitterState);
                    if (sh == null) {
                        ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "Splitter Job(state=" + h.state + ")", "State " + splitterState
                                + " not found");
                    }
                    BranchWorkflow bw = new BranchWorkflow(getNodesInstructions(workflowInstructions, jobChain, uniqueJobs, splitterState, states,
                            fileOrderSinkStates, fileOrderSinkJobs, true), null);
                    branches.add(new Branch("branch_" + b, bw));
                    b++;
                }
                forkJoin.setBranches(branches);
                in.add(forkJoin);
            } else if (job.isJavaJITLJoinJob()) {

            } else {
                SOSParameterSubstitutor ps = new SOSParameterSubstitutor(true, "${", "}");
                if (hasConfigOrderParams) {
                    jobChain.getConfig().getOrderParams().getParams().entrySet().forEach(e -> {
                        ps.addKey(e.getKey(), e.getValue());
                    });
                }
                in.add(getNamedJobInstruction(job.getName(), h.state, hasConfigProcess ? jobChain.getConfig().getProcess().get(h.state) : null, ps));
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

            workflowInstructions.put(h.state, in);

            boolean hasErrorStateJob = states.get(h.errorState) != null && states.get(h.errorState).jobName != null;
            if (h.errorState.length() > 0 && (hasErrorStateJob || fileOrderSinkStates.containsKey(h.errorState))) {
                TryCatch tryCatch = new TryCatch();
                tryCatch.setTry(new Instructions(workflowInstructions.get(h.state)));

                boolean add = false;
                if (hasErrorStateJob) {
                    tryCatch.setCatch(new Instructions(getNodesInstructions(workflowInstructions, jobChain, uniqueJobs, h.errorState, states,
                            fileOrderSinkStates, fileOrderSinkJobs, false)));
                    add = true;
                } else {
                    NamedJob nj = getFileOrderSinkNamedJob(fileOrderSinkStates.get(h.errorState), h.errorState, fileOrderSinkJobs);
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
            } else if (workflowInstructions.get(h.state) != null) {
                result.addAll(workflowInstructions.get(h.state));
            }

            if (h.nextState.length() > 0 && !h.nextState.equals(h.errorState)) {
                String nextState = h.nextState;
                h = states.get(nextState);
                if (h == null) {
                    //
                } else if (h.jobName == null) {
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
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addErrorRecord(currentPath, "[include2params][include=" + i.getNodeText() + "]" + e.toString(), e);
        }
        if (path != null) {
            try {
                return new Params(SOSXML.newXPath(), JS7ConverterHelper.getDocumentRoot(path));
            } catch (Exception e) {
                ConverterReport.INSTANCE.addErrorRecord(currentPath, "[include2params][include=" + i.getNodeText() + "]" + e.toString(), e);
            }

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
            env.setAdditionalProperty("target_file", JS7ConverterHelper.quoteJS7StringValueWithSingleQuotes(fos.getMoveTo()));
        }
        job.setDefaultArguments(env);
        return job;
    }

    private Job getFileOrderSinkJob(String jobName, String jobChainAgentName) {
        Job job = new Job();
        setAgent(job, null, jobChainAgentName);
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

    private ConverterObjects getConverterObjects(Folder root) {
        ConverterObjects co = new ConverterObjects();
        walk(root, co);
        return co;
    }

    private void walk(Folder f, ConverterObjects co) {
        for (StandaloneJob o : f.getStandaloneJobs()) {
            if (co.standalone.unique.containsKey(o.getName())) {
                List<StandaloneJob> l = new ArrayList<>();
                if (co.standalone.duplicates.containsKey(o.getName())) {
                    l = co.standalone.duplicates.get(o.getName());
                }
                l.add(o);
                co.standalone.duplicates.put(o.getName(), l);
            } else {
                co.standalone.unique.put(o.getName(), o);
            }
        }
        for (JobChain o : f.getJobChains()) {
            if (co.jobChains.unique.containsKey(o.getName())) {
                List<JobChain> l = new ArrayList<>();
                if (co.jobChains.duplicates.containsKey(o.getName())) {
                    l = co.jobChains.duplicates.get(o.getName());
                }
                l.add(o);
                co.jobChains.duplicates.put(o.getName(), l);
            } else {
                co.jobChains.unique.put(o.getName(), o);
            }

            if (o.getOrders() != null) {
                for (JobChainOrder jo : o.getOrders()) {
                    orders.add(jo);
                }
            }

        }
        for (ProcessClass o : f.getProcessClasses()) {
            if (co.processClasses.unique.containsKey(o.getName())) {
                List<ProcessClass> l = new ArrayList<>();
                if (co.processClasses.duplicates.containsKey(o.getName())) {
                    l = co.processClasses.duplicates.get(o.getName());
                }
                l.add(o);
                co.processClasses.duplicates.put(o.getName(), l);
            } else {
                co.processClasses.unique.put(o.getName(), o);
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

    private class JobChainStateHelper {

        private String state;
        private String nextState;
        private String errorState;
        private String onError;
        private String jobName;

        private JobChainStateHelper(JobChainNode node, String jobName) {
            this.state = node.getState();
            this.nextState = node.getNextState() == null ? "" : node.getNextState();
            this.errorState = node.getErrorState() == null ? "" : node.getErrorState();
            this.onError = node.getOnError() == null ? "" : node.getOnError();
            this.jobName = jobName;
        }
    }

    private class RunTimeHelper {

        private Path path;
        private Schedule schedule;
        private AdmissionTimeScheme admissionTimeScheme;

        private RunTimeHelper(Path path, Schedule schedule) {
            this.path = path;
            this.schedule = schedule;
        }

        private RunTimeHelper(AdmissionTimeScheme admissionTimeScheme) {
            this.admissionTimeScheme = admissionTimeScheme;
        }

    }

    private class ScheduleHelper {

        private com.sos.js7.converter.js1.common.runtime.Schedule js1Schedule;
        private String timeZone;
        private List<WorkflowHelper> workflows = new ArrayList<>();

        private ScheduleHelper(com.sos.js7.converter.js1.common.runtime.Schedule schedule, String timeZone) {
            this.js1Schedule = schedule;
            this.timeZone = timeZone;
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

        private String name;
        private ProcessClass processClass;
        private boolean standalone;

        private AgentHelper(String name, ProcessClass processClass) {
            this.name = name;
            this.processClass = processClass;
            this.standalone = processClass.getRemoteSchedulers() == null || !(processClass.getRemoteSchedulers().getRemoteScheduler().size() > 1);

        }
    }
}
