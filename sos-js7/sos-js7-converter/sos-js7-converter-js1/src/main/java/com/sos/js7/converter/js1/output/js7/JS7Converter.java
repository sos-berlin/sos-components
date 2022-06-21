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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.AssignedNonWorkingDayCalendars;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.fileordersource.FileOrderSource;
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
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Parameters;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.js7.converter.commons.JS7ConverterConfig;
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
import com.sos.js7.converter.js1.common.RunTime;
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
import com.sos.js7.converter.js1.input.DirectoryParser;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

/** <br/>
 * TODO Locks<br/>
 * --- current JS7 state - 1 Lock support - generate 1 Lock, ignore/report following<br/>
 * TODO JobChain .config.xml - <br/>
 * --- params as node instructions<br/>
 * --- ... substitute variables <br/>
 * TODO JobChainNodes:<br/>
 * ---- job_chain_node.job_chain<br/>
 * ---- file_order_sink - generate a fileOrderSing JITL Job(Move,Remove)<br/>
 * -------- use $FILE<br/>
 * -------- WARN if the file not exists(grace argument?)<br/>
 * -------- otherwise exception (can't be moved/removed: permission denied etc)<br/>
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
 * TODO Java: Split/Join<br/>
 */
public class JS7Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7Converter.class);

    public static JS7ConverterConfig CONFIG = new JS7ConverterConfig();

    private static final int INITIAL_DUPLICATE_COUNTER = 1;

    private ConverterObjects converterObjects;
    private DirectoryParserResult pr;
    private String inputDirPath;
    private Map<Path, String> jobResources = new HashMap<>();
    private Map<String, Integer> jobResourcesDuplicates = new HashMap<>();
    private Map<Path, OrderJob> orderJobs = new HashMap<>();

    private Map<String, List<ACommonJob>> js1JobsByLanguage = new HashMap<>();
    private Map<String, List<RunTime>> js1Calendars = new HashMap<>();
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
        DirectoryParserResult pr = DirectoryParser.parse(CONFIG.getParserConfig(), input);
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

        c.convertStandalone(result);
        c.convertJobChains(result);
        c.addJobResources(result);

        c.analyzerReport();

        return result;
    }

    private void analyzerReport() {
        if (js1JobsByLanguage.size() > 0) {
            ParserReport.INSTANCE.addAnalyzerRecord("JOBS BY LANGUAGE", "START");
            js1JobsByLanguage.entrySet().forEach(e -> {
                ParserReport.INSTANCE.addAnalyzerRecord(e.getKey().toUpperCase(), "");
                for (ACommonJob job : e.getValue()) {
                    String className = null;
                    if (job.getScript() != null && job.getScript().getJavaClass() != null) {
                        className = job.getScript().getJavaClass();
                    }
                    ParserReport.INSTANCE.addAnalyzerRecord(job.getPath(), job.getType().toString(), className);
                }
            });
            ParserReport.INSTANCE.addAnalyzerRecord("JOBS BY LANGUAGE", "END");
        }
        if (js1Calendars.size() > 0) {
            ParserReport.INSTANCE.addAnalyzerRecord("CALENDARS", "START");
            js1Calendars.entrySet().forEach(e -> {
                ParserReport.INSTANCE.addAnalyzerRecord(e.getKey().toUpperCase(), "");
                for (RunTime r : e.getValue()) {
                    ParserReport.INSTANCE.addAnalyzerRecord(r.getCurrentPath(), r.getNodeText(), "");
                }
            });
            ParserReport.INSTANCE.addAnalyzerRecord("CALENDARS", "END");
        }
        if (js1JobsWithMonitors.size() > 0) {
            ParserReport.INSTANCE.addAnalyzerRecord("JOBS WITH MONITORS", "START");
            for (ACommonJob job : js1JobsWithMonitors) {
                List<String> m = job.getMonitors().stream().map(e -> e.getNodeText()).collect(Collectors.toList());
                ParserReport.INSTANCE.addAnalyzerRecord(job.getPath(), String.join(",", m), "");
            }
            ParserReport.INSTANCE.addAnalyzerRecord("JOBS WITH MONITORS", "END");
        }
    }

    private void addJobResources(JS7ConverterResult result) {
        jobResources.entrySet().forEach(e -> {
            try {
                Params p = new Params(SOSXML.newXPath(), JS7ConverterHelper.getDocumentRoot(e.getKey()));
                Environment args = new Environment();

                p.getParams().entrySet().forEach(pe -> {
                    args.setAdditionalProperty(pe.getKey(), pe.getValue());
                });
                JobResource jr = new JobResource(args, null, null, null);
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
    }

    private void convertStandaloneWorkflow(JS7ConverterResult result, StandaloneJob js1Job, int counter) {
        LOGGER.info("[convertStandaloneWorkflow]" + js1Job.getPath());

        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(js1Job.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());

        Jobs js = new Jobs();
        js.setAdditionalProperty(js1Job.getName(), getJob(result, js1Job));
        w.setJobs(js);

        List<Instruction> in = new ArrayList<>();
        in.add(getNamedJobInstruction(js1Job.getName(), js1Job.getName()));
        in = getRetryInstructions(js1Job, in);
        in = getCyclicWorkflowInstructions(js1Job, in);
        w.setInstructions(in);

        Path workflowPath = getWorkflowPath(result, js1Job, counter);
        String workflowName = getWorkflowName(workflowPath);

        ScheduleHelper sh = convertRunTime2Schedule("STANDALONE", js1Job.getRunTime(), workflowPath, workflowName, "");
        if (sh != null) {
            result.add(sh.path, sh.schedule);
        }
        result.add(workflowPath, w);
    }

    private ScheduleHelper convertRunTime2Schedule(String range, RunTime runTime, Path workflowPath, String workflowName, String additionalName) {
        if (runTime != null && !runTime.isEmpty()) {
            if (runTime.hasCalendars() || runTime.getSchedule() != null) {
                JS1Calendars calendars = runTime.getCalendars();
                if (calendars == null) {
                    calendars = runTime.getSchedule().getCalendars();
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

                    return new ScheduleHelper(getSchedulePath(workflowPath, workflowName, additionalName), s);
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
            for (com.sos.js7.converter.js1.common.RunTime.WeekDays wd : runTime.getWeekDays()) {
                if (wd.getDays() != null) {
                    for (com.sos.js7.converter.js1.common.RunTime.Day d : wd.getDays()) {
                        List<Integer> days = d.getDays();
                        if (days != null && days.size() > 0) {
                            // WeekdayPeriod wdp = new WeekdayPeriod(null);
                            // wdp.setSecondOfWeek(null);
                            // wdp.setDuration(null);

                            if (d.getPeriods() != null) {
                                for (com.sos.js7.converter.js1.common.RunTime.Period p : d.getPeriods()) {

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

    private List<Instruction> getRetryInstructionsXXX(ACommonJob job, List<Instruction> in) {
        try {
            switch (job.getType()) {
            case ORDER:
                OrderJob oj = (OrderJob) job;
                if (oj.getDelayOrderAfterSetback() != null && oj.getDelayOrderAfterSetback().size() > 0) {
                    Optional<DelayOrderAfterSetback> maximum = oj.getDelayOrderAfterSetback().stream().filter(e -> e.getIsMaximum() != null && e
                            .getIsMaximum() && e.getSetbackCount() != null && e.getSetbackCount() > 0).findAny();
                    if (maximum.isPresent()) {
                        TryCatch tryCatch = new TryCatch();
                        tryCatch.setMaxTries(maximum.get().getSetbackCount());
                        tryCatch.setTry(new Instructions(in));
                        tryCatch.setCatch(new Instructions(in));

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
                        TryCatch tryCatch = new TryCatch();
                        tryCatch.setMaxTries(stop.get().getErrorCount());
                        tryCatch.setTry(new Instructions(in));
                        tryCatch.setCatch(new Instructions(in));

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

    private Job getJob(JS7ConverterResult result, ACommonJob job) {
        if (job.getMonitors() != null && job.getMonitors().size() > 0) {
            if (!js1JobsWithMonitors.contains(job)) {
                js1JobsWithMonitors.add(job);
            }
        }

        Job j = new Job();
        j.setTitle(job.getTitle());
        j = setFromConfig(j);
        j = setAgent(result, j, job);
        j = setJobJobResources(j, job);
        j = setExecutable(j, job);
        j = setJobOptions(j, job);
        j = setJobNotification(j, job);
        return j;
    }

    private NamedJob getNamedJobInstruction(String jobName, String jobLabel) {
        NamedJob nj = new NamedJob(jobName);
        nj.setLabel(jobLabel);
        return nj;
    }

    private Job setFromConfig(Job j) {
        if (CONFIG.getJobConfig().getForcedGraceTimeout() != null) {
            j.setGraceTimeout(CONFIG.getJobConfig().getForcedGraceTimeout());
        }
        if (CONFIG.getJobConfig().getForcedParallelism() != null) {
            j.setParallelism(CONFIG.getJobConfig().getForcedParallelism());
        }
        if (CONFIG.getJobConfig().getForcedFailOnErrWritten() != null) {
            j.setFailOnErrWritten(CONFIG.getJobConfig().getForcedFailOnErrWritten());
        }
        return j;
    }

    private Job setAgent(JS7ConverterResult result, Job j, ACommonJob job) {
        String name = null;
        if (CONFIG.getAgentConfig().getForcedName() != null) {
            name = CONFIG.getAgentConfig().getForcedName();
        } else {
            if (job.getProcessClass() != null && job.getProcessClass().isAgent()) {
                name = job.getProcessClass().getName();
            }
        }
        if (name == null) {
            name = CONFIG.getAgentConfig().getDefaultName();
        }
        if (name != null && CONFIG.getAgentConfig().getMapping().containsKey(name)) {
            name = CONFIG.getAgentConfig().getMapping().get(name);
        }

        j.setAgentName(name);
        // j.setSubagentClusterId(name);
        return j;
    }

    private String getFileName(String p) {
        if (p.endsWith("/")) {
            return "";
        }
        int i = p.lastIndexOf("/");
        return i > -1 ? p.substring(i + 1) : p;
    }

    private Job setExecutable(Job j, ACommonJob job) {
        JS7ScriptLanguageConverter c = new JS7ScriptLanguageConverter(job);

        List<ACommonJob> jbt = js1JobsByLanguage.get(c.getLanguage());
        if (jbt == null) {
            jbt = new ArrayList<>();
        }
        jbt.add(job);
        js1JobsByLanguage.put(c.getLanguage(), jbt);

        c.process();

        j.setExecutable(c.getJavaClassName() == null ? getExecutableScript(j, job, c.getLanguage(), c.getClassName(), c.isYADE())
                : getInternalExecutable(j, job, c.getJavaClassName()));
        return j;
    }

    private ExecutableJava getInternalExecutable(Job j, ACommonJob job, String javaClassName) {
        ExecutableJava ej = new ExecutableJava();
        ej.setClassName(javaClassName);

        if (job.getParams() != null && job.getParams().hasParams()) {
            // ARGUMENTS
            Environment env = new Environment();
            job.getParams().getParams().entrySet().forEach(e -> {
                try {
                    env.setAdditionalProperty(e.getKey(), JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(e.getValue()));
                } catch (Throwable ee) {
                    env.setAdditionalProperty(e.getKey().toUpperCase(), e.getValue());
                    ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "could not convert env value=" + e.getValue(), ee);
                }
            });
            ej.setArguments(env);
        }

        return ej;
    }

    private ExecutableScript getExecutableScript(Job j, ACommonJob job, String language, String className, boolean isYADE) {
        Platform platform = CONFIG.getAgentConfig().getForcedPlatform() == null ? Platform.UNIX : CONFIG.getAgentConfig().getForcedPlatform();

        StringBuilder scriptHeader = new StringBuilder();
        StringBuilder scriptCommand = new StringBuilder();
        if (platform.equals(Platform.UNIX)) {
            scriptHeader.append("#!/bin/bash");
            scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            if (isYADE) {
                scriptCommand.append("$YADE_BIN -settings $SETTINGS -profile $PROFILE");
            }
        } else {
            if (isYADE) {
                scriptCommand.append("%YADE_BIN% -settings %SETTINGS% -profile %PROFILE%");
            }
        }
        if (!language.equals("shell")) {
            scriptHeader.append((platform.equals(Platform.UNIX) ? "#" : "REM")).append(" language=").append(language);
            if (className != null) {
                scriptHeader.append(",className=" + className);
            }
            scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
        }

        if (!isYADE) {
            if (job.getScript().getInclude() != null) {
                // TODO resolve include
                scriptCommand.append(job.getScript().getInclude().getIncludeFile());
                scriptCommand.append(CONFIG.getJobConfig().getScriptNewLine());
            }
            if (job.getScript().getScript() != null) {
                scriptCommand.append(job.getScript().getScript());
            }
        }

        StringBuilder script = new StringBuilder(scriptHeader);
        if (CONFIG.getMockConfig().getScript() != null) {
            script.append(CONFIG.getMockConfig().getScript()).append(" ");
        }
        script.append(scriptCommand);

        ExecutableScript es = new ExecutableScript();
        es.setScript(script.toString());
        es.setV1Compatible(CONFIG.getJobConfig().getForcedV1Compatible());

        if (job.getParams() != null && job.getParams().hasParams()) {
            // ARGUMENTS
            Environment env = new Environment();
            job.getParams().getParams().entrySet().forEach(e -> {
                try {
                    env.setAdditionalProperty(e.getKey().toUpperCase(), JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(e.getValue()));
                } catch (Throwable ee) {
                    env.setAdditionalProperty(e.getKey().toUpperCase(), e.getValue());
                    ConverterReport.INSTANCE.addErrorRecord(job.getPath(), "could not convert env value=" + e.getValue(), ee);
                }
            });
            es.setEnv(env);
        }

        return es;
    }

    private Job setJobJobResources(Job j, ACommonJob job) {
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
        return j;
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

    private Job setJobOptions(Job j, ACommonJob job) {
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
        return j;
    }

    private Job setJobNotification(Job j, ACommonJob job) {
        if (job.getSettings() != null) {
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
            if (types.size() > 0) {
                j.setNotification(new JobNotification(types, new JobNotificationMail(job.getSettings().getMailTo(), job.getSettings().getMailCC(), job
                        .getSettings().getMailBCC())));
            }
        }
        return j;
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

    private Workflow setWorkflowOrderPreparationOrResources(Workflow w, JobChainOrder o) {
        if (o.getParams() != null && o.getParams().hasParams()) {
            // ARGUMENTS
            Parameters parameters = new Parameters();
            o.getParams().getParams().entrySet().forEach(e -> {
                Parameter p = new Parameter();
                p.setType(ParameterType.String);
                // try {
                // p.setDefault(JS7ConverterHelper.asJS7OrderPreparationStringValue(e.getValue()));
                // } catch (Throwable ee) {
                // p.setDefault(e.getValue());
                // ConverterReport.INSTANCE.addErrorRecord(o.getPath(), "can't convert value=" + e.getValue(), ee);
                // }
                parameters.setAdditionalProperty(e.getKey(), p);
            });
            w.setOrderPreparation(new Requirements(parameters, false));

            // JOB RESOURCES
            List<String> names = new ArrayList<>();
            for (Include i : o.getParams().getIncludes()) {
                Path p = null;
                try {
                    p = findIncludeFile(pr, o.getPath(), i.getIncludeFile());
                } catch (Throwable e) {
                    ConverterReport.INSTANCE.addErrorRecord(o.getPath(), "[order params][include=" + i.getNodeText() + "]" + e.toString(), e);
                }
                if (p != null) {
                    String name = resolveJobResource(p);
                    if (name != null) {
                        names.add(name);
                    }
                }
            }
            if (names.size() > 0) {
                w.setJobResourceNames(names.stream().distinct().collect(Collectors.toList()));
            }
        }
        return w;
    }

    private void convertJobChainWorkflow(JS7ConverterResult result, JobChain jobChain, int counter) {
        LOGGER.info("[convertJobChainWorkflow]" + jobChain.getPath());

        Workflow w = new Workflow();
        w.setTitle(jobChain.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());

        if (jobChain.getOrders().size() > 0) {
            for (JobChainOrder o : jobChain.getOrders()) {
                // see convertJobChainOrders
                // if (o.getRunTime() == null || o.getRunTime().isEmpty()) {
                w = setWorkflowOrderPreparationOrResources(w, o);
                // }
            }
        }

        Map<String, OrderJob> uniqueJobs = new LinkedHashMap<>();
        Map<String, JobChainStateHelper> states = new LinkedHashMap<>();
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
                JobChainNodeFileOrderSink os = (JobChainNodeFileOrderSink) n;

                ConverterReport.INSTANCE.addWarningRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
                        + "]", "not implemented yet");
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

        Jobs js = new Jobs();
        uniqueJobs.entrySet().forEach(e -> {
            js.setAdditionalProperty(e.getKey(), getJob(result, e.getValue()));
        });
        w.setJobs(js);

        List<Instruction> in = new ArrayList<>();
        Map<String, List<Instruction>> workflowInstructions = new LinkedHashMap<>();
        String startState = getNodesStartState(states);
        if (startState != null) {
            in.addAll(getNodesInstructions(startState, states, uniqueJobs, workflowInstructions));
        } else {
            ConverterReport.INSTANCE.addErrorRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "]", "startState not found");
        }

        w.setInstructions(in);
        Path workflowPath = getWorkflowPath(result, jobChain, counter);
        result.add(workflowPath, w);

        String workflowName = getWorkflowName(workflowPath);
        convertJobChainOrders(result, jobChain, workflowPath, workflowName);
        convertJobChainFileOrderSources(result, jobChain, fileOrderSources, workflowPath, workflowName);

    }

    private void convertJobChainFileOrderSources(JS7ConverterResult result, JobChain jobChain, List<JobChainNodeFileOrderSource> fileOrderSources,
            Path workflowPath, String workflowName) {
        if (fileOrderSources.size() > 0) {
            boolean useNextState = fileOrderSources.size() > 1;
            for (JobChainNodeFileOrderSource n : fileOrderSources) {
                String name = workflowName;
                if (useNextState) {
                    name = name + "_" + n.getNextState();
                }
                FileOrderSource fos = new FileOrderSource();
                fos.setWorkflowName(workflowName);
                fos.setAgentName(CONFIG.getAgentConfig().getForcedName() == null ? CONFIG.getAgentConfig().getDefaultName() : CONFIG.getAgentConfig()
                        .getForcedName());
                fos.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());
                fos.setDirectoryExpr(JS7ConverterHelper.quoteJS7StringValueWithDoubleQuotes(n.getDirectory()));
                fos.setPattern(n.getRegex());
                Long delay = null;
                if (n.getRepeat() != null && !n.getRepeat().toLowerCase().equals("no")) {
                    try {
                        delay = Long.parseLong(n.getRepeat());
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s][fileOrderSource nextState=%s][repeat=%s]%s", workflowPath, n.getNextState(), n.getRepeat(), e
                                .toString()), e);
                        ConverterReport.INSTANCE.addErrorRecord(workflowPath, "[fileOrderSource nextState=" + n.getNextState() + "]repeat=" + n
                                .getRepeat(), e);
                    }
                }
                fos.setDelay(delay);
                result.add(getFileOrderSourcePath(workflowPath, workflowName), fos);
            }
        }
    }

    private void convertJobChainOrders(JS7ConverterResult result, JobChain jobChain, Path workflowPath, String workflowName) {
        if (jobChain.getOrders().size() > 0) {
            List<JobChainOrder> orders = jobChain.getOrders().stream().filter(o -> o.getRunTime() != null && !o.getRunTime().isEmpty()).collect(
                    Collectors.toList());
            if (orders.size() > 0) {
                for (JobChainOrder o : orders) {
                    ScheduleHelper sh = convertRunTime2Schedule("ORDER", o.getRunTime(), workflowPath, workflowName, "_" + o.getName());
                    if (sh != null) {
                        Schedule s = sh.schedule;
                        s.setTitle(o.getTitle());

                        List<OrderParameterisation> l = new ArrayList<>();
                        if (o.getParams() != null && o.getParams().hasParams()) {
                            OrderParameterisation set = new OrderParameterisation();
                            set.setOrderName(o.getName());

                            Variables vs = new Variables();
                            o.getParams().getParams().entrySet().forEach(e -> {
                                vs.setAdditionalProperty(e.getKey(), e.getValue());
                            });
                            set.setVariables(vs);
                            l.add(set);
                        }
                        if (l.size() > 0) {
                            s.setOrderParameterisations(l);
                        }
                        result.add(sh.path, s);
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

    private List<Instruction> getNodesInstructions(String startState, Map<String, JobChainStateHelper> states, Map<String, OrderJob> uniqueJobs,
            Map<String, List<Instruction>> workflowInstructions) {
        List<Instruction> result = new ArrayList<>();

        JobChainStateHelper h = states.get(startState);
        while (h != null) {
            OrderJob job = uniqueJobs.get(h.jobName);
            if (job == null) {
                return new ArrayList<>();
            }

            List<Instruction> in = new ArrayList<>();
            in.add(getNamedJobInstruction(job.getName(), h.state));

            if (h.onError.toLowerCase().equals("setback")) {
                in = getRetryInstructions(job, in);
            }
            workflowInstructions.put(h.state, in);

            if (h.errorState.length() > 0 && states.get(h.errorState) != null && states.get(h.errorState).jobName != null) {
                TryCatch tryCatch = new TryCatch();
                tryCatch.setTry(new Instructions(workflowInstructions.get(h.state)));
                tryCatch.setCatch(new Instructions(getNodesInstructions(h.errorState, states, uniqueJobs, workflowInstructions)));
                result.add(tryCatch);
            } else if (workflowInstructions.get(h.state) != null) {
                result.addAll(workflowInstructions.get(h.state));
            }

            if (h.nextState.length() > 0 && !h.nextState.equals(h.errorState)) {
                h = states.get(h.nextState);
                if (h == null) {
                    //
                } else if (h.jobName == null) {
                    h = null;
                }
            } else {
                h = null;
            }
        }
        return result;
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

    private class ScheduleHelper {

        private final Path path;
        private final Schedule schedule;

        private ScheduleHelper(Path path, Schedule schedule) {
            this.path = path;
            this.schedule = schedule;
        }
    }

}
