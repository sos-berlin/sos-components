package com.sos.js7.converter.autosys.output;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.board.Board;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.calendar.Frequencies;
import com.sos.inventory.model.calendar.Period;
import com.sos.inventory.model.calendar.WeekDays;
import com.sos.inventory.model.calendar.WhenHolidayType;
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ExpectNotice;
import com.sos.inventory.model.instruction.Fail;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.instruction.schedule.CycleSchedule;
import com.sos.inventory.model.instruction.schedule.Periodic;
import com.sos.inventory.model.instruction.schedule.Scheme;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.DailyPeriod;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions;
import com.sos.js7.converter.autosys.input.AFileParser;
import com.sos.js7.converter.autosys.input.DirectoryParser;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.commons.JS7ConverterConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.JobConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.MockConfig;
import com.sos.js7.converter.commons.JS7ConverterConfig.SubFolderConfig;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.JS7ExportObjects.JS7ExportObject;
import com.sos.js7.converter.commons.output.OutputWriter;
import com.sos.js7.converter.commons.report.CommonReport.ErrorType;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.commons.report.ParserReport;
import com.sos.js7.converter.commons.report.ReportWriter;

/** <br/>
 * TODO Locks<br/>
 * TODO Conditions with operators AND/OR and condition groups<br/>
 * TODO Conditions with lookBack<br/>
 * TODO Box Jobs<br/>
 * TODO Report<br/>
 */
public class AutosysConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutosysConverter.class);

    public static JS7ConverterConfig CONFIG = new JS7ConverterConfig();

    public static void convert(AFileParser parser, Path inputDir, Path outputDir, Path reportDir) throws IOException {

        String method = "convert";

        // APP start
        Instant appStart = Instant.now();
        LOGGER.info(String.format("[%s][start]...", method));

        // 1- Parse Autosys files
        LOGGER.info(String.format("[%s][JIL][parse][start]...", method));
        DirectoryParserResult pr = DirectoryParser.parse(parser, inputDir);
        LOGGER.info(String.format("[%s][JIL][parse][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));

        OutputWriter.prepareDirectory(reportDir);
        if (ParserReport.INSTANCE.getError().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JIL][parse][write error report][start]...", method));
            ReportWriter.write(reportDir.resolve("parse_errors.csv"), ParserReport.INSTANCE.getError());
            LOGGER.info(String.format("[%s][JIL][parse][write error report][end]", method));
        }

        // 2- Convert to JS7
        Instant start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][convert][start]...", method));
        JS7ConverterResult result = convert(pr);
        LOGGER.info(String.format("[%s][JS7][convert][end]%s", method, SOSDate.getDuration(start, Instant.now())));

        if (ConverterReport.INSTANCE.getError().getRecords().size() > 0) {
            LOGGER.info(String.format("[%s][JS7][convert][write error report][start]...", method));
            ReportWriter.write(reportDir.resolve("converter_errors.csv"), ConverterReport.INSTANCE.getError());
            LOGGER.info(String.format("[%s][JS7][convert][write error report][end]", method));
        }

        // 3- Write JS7 files
        start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][write][start]...", method));
        OutputWriter.prepareDirectory(outputDir);
        if (CONFIG.getGenerateConfig().getWorkflows()) {
            LOGGER.info(String.format("[%s][JS7][write][workflows]...", method));
            OutputWriter.write(outputDir, result.getWorkflows());
            ConverterReport.INSTANCE.addSuccessRecord("Workflows", result.getWorkflows().getItems().size());

        }
        if (CONFIG.getGenerateConfig().getCalendars()) {
            LOGGER.info(String.format("[%s][JS7][write][calendars]...", method));
            OutputWriter.write(outputDir, result.getCalendars());
            ConverterReport.INSTANCE.addSuccessRecord("Calendars", result.getCalendars().getItems().size());
        }
        if (CONFIG.getGenerateConfig().getSchedules()) {
            LOGGER.info(String.format("[%s][JS7][write][schedules]...", method));
            OutputWriter.write(outputDir, result.getSchedules());
            ConverterReport.INSTANCE.addSuccessRecord("Schedules", result.getSchedules().getItems().size());
        }

        LOGGER.info(String.format("[%s][JS7][write][boards]...", method));
        OutputWriter.write(outputDir, result.getBoards());
        ConverterReport.INSTANCE.addSuccessRecord("Boards", result.getBoards().getItems().size());
        ReportWriter.write(reportDir.resolve("converter.csv"), ConverterReport.INSTANCE.getSuccess());

        LOGGER.info(String.format("[%s][[JS7]write][end]%s", method, SOSDate.getDuration(start, Instant.now())));

        // APP end
        LOGGER.info(String.format("[%s][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));
    }

    private static JS7ConverterResult convert(DirectoryParserResult pr) {
        String method = "convert";

        JS7ConverterResult result = new JS7ConverterResult();
        result.getApplications().addAll(pr.getJobs().stream().map(e -> e.getFolder().getApplication().getValue()).filter(Objects::nonNull).distinct()
                .collect(Collectors.toSet()));

        List<ACommonJob> boxsJobs = new ArrayList<>();
        Map<ConverterJobType, List<ACommonJob>> jobsPerType = pr.getJobs().stream().collect(Collectors.groupingBy(ACommonJob::getConverterJobType,
                Collectors.toList()));
        int size = 0;
        for (Map.Entry<ConverterJobType, List<ACommonJob>> entry : jobsPerType.entrySet()) {
            ConverterJobType key = entry.getKey();
            List<ACommonJob> value = entry.getValue();
            size = value.size();

            switch (key) {
            case CMD:
                LOGGER.info(String.format("[%s][standalone][CMD jobs=%s]start ...", method, size));
                for (ACommonJob j : value) {
                    convertStandalone(result, (JobCMD) j);
                }
                LOGGER.info(String.format("[%s][standalone][CMD jobs=%s]end", method, size));
                break;
            case BOX:
                boxsJobs.addAll(value);
                break;
            default:
                LOGGER.info(String.format("[%s][%s jobs=%]not implemented yet", method, key, size));
                for (ACommonJob j : value) {
                    ConverterReport.INSTANCE.addErrorRecord(j.getSource(), j.getInsertJob().getValue(), ErrorType.WARNING, j.getJobType().getValue()
                            + ":not implemented yet");
                }
                break;
            }
        }
        size = boxsJobs.size();
        if (size > 0) {
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s]start ...", method, size));
            // TODO analyze boxJobs and result
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s]end", method, size));
        } else {
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s]skip", method, size));
        }

        postProcessing(result);

        return result;
    }

    private static void postProcessing(JS7ConverterResult result) {
        String method = "postProcessing";
        if (result.getPostNotices().isEmpty()) {
            LOGGER.info(String.format("[%s][skip]no postNotices found", method));
            return;
        }
        // SUCCESS
        for (String fullJobName : result.getPostNotices().getSuccess()) {
            String jobName = normalizeName(result, fullJobName);
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflow(jobName);
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]not found", method, jobName));
                ConverterReport.INSTANCE.addErrorRecord("[postProcessing][success postNotice][job not found]" + jobName);
            } else {
                Workflow w = (Workflow) eo.getObject();
                w.getInstructions().add(new PostNotice(jobName + "-success"));
                if (result.getPostNotices().getFailed().contains(fullJobName) || result.getPostNotices().getDone().contains(fullJobName)) {
                    TryCatch tryCatch = new TryCatch();
                    tryCatch.setTry(new Instructions(w.getInstructions()));

                    List<Instruction> catchIn = new ArrayList<>();
                    if (result.getPostNotices().getFailed().contains(fullJobName)) {
                        catchIn.add(new PostNotice(jobName + "-failed"));
                    }
                    if (result.getPostNotices().getDone().contains(fullJobName)) {
                        catchIn.add(new PostNotice(jobName + "-done"));
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

            String jobName = normalizeName(result, fullJobName);
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflow(jobName);
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]not found", method, jobName));
                ConverterReport.INSTANCE.addErrorRecord("[postProcessing][failed postNotice][job not found]" + jobName);
            } else {
                Workflow w = (Workflow) eo.getObject();
                w.getInstructions().add(new PostNotice(jobName + "-failed"));

                TryCatch tryCatch = new TryCatch();
                tryCatch.setTry(new Instructions(w.getInstructions()));

                List<Instruction> catchIn = new ArrayList<>();
                if (result.getPostNotices().getDone().contains(fullJobName)) {
                    catchIn.add(new PostNotice(jobName + "-done"));
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

            String jobName = normalizeName(result, fullJobName);
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflow(jobName);
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]not found", method, jobName));
                ConverterReport.INSTANCE.addErrorRecord("[postProcessing][done postNotice][job not found]" + jobName);
            } else {
                Workflow w = (Workflow) eo.getObject();
                w.getInstructions().add(new PostNotice(jobName + "-done"));

                TryCatch tryCatch = new TryCatch();
                tryCatch.setTry(new Instructions(w.getInstructions()));

                List<Instruction> catchIn = new ArrayList<>();
                catchIn.add(new PostNotice(jobName + "-done"));
                catchIn.add(new Fail("'job terminates with return code: ' ++ $returnCode", null, null));
                tryCatch.setCatch(new Instructions(catchIn));

                w.setInstructions(Collections.singletonList(tryCatch));
                result.addOrReplace(eo.getOriginalPath().getPath(), w);
            }
        }

    }

    private static void convertStandalone(JS7ConverterResult result, JobCMD jilJob) {
        convertStandaloneWorkflow(result, jilJob);
        convertSchedule(result, jilJob);
    }

    private static void convertStandaloneWorkflow(JS7ConverterResult result, JobCMD jilJob) {
        if (!CONFIG.getGenerateConfig().getWorkflows()) {
            return;
        }

        // JOB Definition
        Job j = new Job();
        j.setTitle(jilJob.getDescription().getValue());
        j = setFromConfig(j);
        j = setAgent(result, j, jilJob);
        j = setExecutable(j, jilJob);
        j = setOthers(j, jilJob);

        // WORKFLOW
        String jobName = normalizeName(result, jilJob, jilJob.getInsertJob().getValue());
        Workflow w = new Workflow();
        w.setTitle(jilJob.getDescription().getValue());
        w.setTimeZone(jilJob.getRunTime().getTimezone().getValue());
        w = addJob(w, jobName, j);
        w = setInstructions(result, w, j, jobName, jilJob);

        result.add(getWorkflowPath(jilJob, jobName), w);
    }

    private static void convertSchedule(JS7ConverterResult result, ACommonJob jilJob) {
        if (!CONFIG.getGenerateConfig().getSchedules()) {
            return;
        }

        String calendarName = getCalendarName(result, jilJob);
        if (calendarName == null) {
            calendarName = getCalendarName(CONFIG);
        }
        if (calendarName == null) {
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
                p.setBegin(JS7ConverterHelper.toTimePart(jilJob.getRunTime().getStartMins().getValue().get(0)));
                p.setEnd("24:00");
                p.setRepeat(JS7ConverterHelper.getRepeat(jilJob.getRunTime().getStartMins().getValue()));

            } else {
                p.setSingleStart("00:00:00");
            }
            p.setWhenHoliday(WhenHolidayType.SUPPRESS);
            periods.add(p);
        }
        calendar.setPeriods(periods);

        String jobName = normalizeName(result, jilJob, jilJob.getInsertJob().getValue());
        Schedule s = new Schedule();
        s.setWorkflowNames(Collections.singletonList(jobName));
        s.setPlanOrderAutomatically(CONFIG.getScheduleConfig().planOrders());
        s.setSubmitOrderToControllerWhenPlanned(CONFIG.getScheduleConfig().submitOrders());
        s.setCalendars(Collections.singletonList(calendar));
        result.add(getSchedulePath(jilJob, jobName), s);
    }

    private static String getCalendarName(JS7ConverterResult result, ACommonJob jilJob) {
        String name = null;
        if (jilJob.getRunTime().getRunCalendar().getValue() != null) {
            name = normalizeName(result, jilJob, jilJob.getRunTime().getRunCalendar().getValue());
        }
        return name;
    }

    private static String getCalendarName(JS7ConverterConfig config) {
        String name = null;
        if (config.getScheduleConfig() != null && config.getScheduleConfig().getDefaultCalendarName() != null) {
            name = config.getScheduleConfig().getDefaultCalendarName();
        }
        return name;
    }

    private static Job setFromConfig(Job j) {
        JobConfig c = CONFIG.getJobConfig();
        if (c != null) {
            if (c.getGraceTimeout() != null) {
                j.setGraceTimeout(c.getGraceTimeout());
            }
            if (c.getParallelism() != null) {
                j.setParallelism(c.getParallelism());
            }
            if (c.getFailOnErrWritten() != null) {
                j.setFailOnErrWritten(c.getFailOnErrWritten());
            }
        }
        return j;
    }

    private static Job setAgent(JS7ConverterResult result, Job j, JobCMD jilJob) {
        MockConfig c = CONFIG.getMockConfig();
        if (c != null && c.getAgentName() != null) {
            j.setAgentName(c.getAgentName());
        } else {
            // TODO agent cluster etc
            j.setAgentName(normalizeName(result, jilJob, jilJob.getMachine().getValue()));
        }
        return j;
    }

    private static Job setExecutable(Job j, JobCMD jilJob) {
        ExecutableScript e = new ExecutableScript();
        // TODO
        StringBuilder script = new StringBuilder("#!/bin/sh");
        script.append(CONFIG.getJobConfig().getScriptNewLine());
        if (jilJob.getProfile().getValue() != null) {
            script.append(jilJob.getProfile().getValue()).append(CONFIG.getJobConfig().getScriptNewLine());
        }
        if (CONFIG.getMockConfig() != null && CONFIG.getMockConfig().getScript() != null) {
            script.append(CONFIG.getMockConfig().getScript()).append(" ");
        }
        script.append(jilJob.getCommand().getValue());
        e.setScript(script.toString());

        j.setExecutable(e);
        return j;
    }

    private static Job setOthers(Job j, JobCMD jilJob) {
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

    private static Workflow addJob(Workflow w, String jobName, Job j) {
        Jobs js = new Jobs();
        js.setAdditionalProperty(jobName, j);
        w.setJobs(js);
        return w;
    }

    private static Workflow setInstructions(JS7ConverterResult result, Workflow w, Job j, String jobName, JobCMD jilJob) {
        List<Instruction> wis = new ArrayList<>();

        Map<ConditionType, List<Condition>> map = Conditions.getByType(jilJob.getCondition().getCondition().getValue());
        for (Map.Entry<ConditionType, List<Condition>> e : map.entrySet()) {
            switch (e.getKey()) {
            case SUCCESS:
                for (Condition c : e.getValue()) {
                    String nn = normalizeName(result, c.getName());
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
                    String nn = normalizeName(result, c.getName());
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
                    String nn = normalizeName(result, c.getName());
                    String boardName = nn + "-failed";
                    String title = "Expect notice for failed job: " + c.getName();

                    wis.add(createExpectNotice(result, jilJob, boardName, title));

                    if (!result.getPostNotices().getFailed().contains(c.getName())) {
                        result.getPostNotices().getFailed().add(c.getName());
                    }
                }
                break;
            case GLOBAL_VARIABLE:
                for (Condition c : e.getValue()) {
                    String nn = normalizeName(result, c.getName());
                    String boardName = nn;
                    String title = "Expect notice for variable: " + c.getName();

                    wis.add(createExpectNotice(result, jilJob, boardName, title));
                }

                break;
            default:
                LOGGER.warn(String.format("[%s][%s][not used]size=%s", jilJob.getCondition().getOriginalCondition(), e.getKey(), e.getValue()
                        .size()));
                ConverterReport.INSTANCE.addErrorRecord(null, jobName, ErrorType.WARNING, "[condition][not used]original=" + jilJob.getCondition()
                        .getOriginalCondition());
                for (Condition c : e.getValue()) {
                    ConverterReport.INSTANCE.addErrorRecord(null, jobName, ErrorType.WARNING, "[condition][not used]%s" + c.getName());
                }
                break;
            }
        }

        NamedJob nj = new NamedJob(jobName);
        nj.setLabel(nj.getJobName());
        wis.add(nj);
        if (!CONFIG.getGenerateConfig().getCyclicOrders() && jilJob.getRunTime().getStartMins().getValue() != null) {
            Periodic p = new Periodic();
            p.setPeriod(3_600L);
            p.setOffsets(jilJob.getRunTime().getStartMins().getValue().stream().map(e -> new Long(e * 60)).collect(Collectors.toList()));

            DailyPeriod dp = new DailyPeriod();
            dp.setSecondOfDay(0L);
            dp.setDuration(86_400L);

            CycleSchedule cs = new CycleSchedule(Collections.singletonList(new Scheme(p, new AdmissionTimeScheme(Collections.singletonList(dp)))));
            Instructions ci = new Instructions(wis);

            wis = new ArrayList<>();
            wis.add(new Cycle(ci, cs));
        }
        w.setInstructions(wis);
        return w;
    }

    private static ExpectNotice createExpectNotice(JS7ConverterResult result, JobCMD jilJob, String boardName, String boardTitle) {
        ExpectNotice en = new ExpectNotice(boardName);

        Board b = new Board();
        b.setTitle(boardTitle);
        b.setEndOfLife("$js7EpochMilli + 1 * 24 * 60 * 60 * 1000");
        b.setExpectOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");
        b.setPostOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");

        result.add(getNoticeBoardPath(jilJob, en.getNoticeBoardName()), b);
        return en;
    }

    private static String normalizeName(JS7ConverterResult result, String name) {
        int i = name.indexOf(".");
        if (i > -1) {
            if (result.getApplications().contains(name.substring(0, i))) {
                return name.substring(i + 1);
            }
            String msg = String.format("[%s]application can't be extracted because unknown", name);
            LOGGER.warn(msg);
            ConverterReport.INSTANCE.addErrorRecord(msg);
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
        return name;
    }

    // TODO sub folder
    private static Path getWorkflowPath(ACommonJob job, String normalizedJobName) {
        Path p = Paths.get(job.getFolder().getApplication().getValue());

        Path subFolders = getSubFolders(job.getFolder().getApplication().getValue(), normalizedJobName);
        if (subFolders != null) {
            p = p.resolve(subFolders);
        }
        return p.resolve(normalizedJobName + ".workflow.json");
    }

    private static Path getSchedulePath(ACommonJob job, String normalizedName) {
        Path p = Paths.get(job.getFolder().getApplication().getValue());

        Path subFolders = getSubFolders(job.getFolder().getApplication().getValue(), normalizedName);
        if (subFolders != null) {
            p = p.resolve(subFolders);
        }
        return p.resolve(normalizedName + ".schedule.json");
    }

    private static Path getNoticeBoardPath(ACommonJob job, String normalizedName) {
        Path p = Paths.get(job.getFolder().getApplication().getValue());
        return p.resolve(normalizedName + ".noticeboard.json");
    }

    private static Path getSubFolders(String application, String normalizedName) {
        SubFolderConfig c = CONFIG.getSubFolderConfig();
        if (c != null && c.getMap() != null && c.getSeparator() != null && application != null) {
            Integer position = c.getMap().get(application);
            if (position != null) {
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
