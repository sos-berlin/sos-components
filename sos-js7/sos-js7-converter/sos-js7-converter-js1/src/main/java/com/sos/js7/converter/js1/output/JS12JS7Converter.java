package com.sos.js7.converter.js1.output;

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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.calendar.AssignedCalendars;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.inventory.model.job.notification.JobNotificationMail;
import com.sos.inventory.model.job.notification.JobNotificationType;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.schedule.VariableSet;
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
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.Folder;
import com.sos.js7.converter.js1.common.Include;
import com.sos.js7.converter.js1.common.Params;
import com.sos.js7.converter.js1.common.job.ACommonJob;
import com.sos.js7.converter.js1.common.job.ACommonJob.DelayAfterError;
import com.sos.js7.converter.js1.common.job.OrderJob;
import com.sos.js7.converter.js1.common.job.StandaloneJob;
import com.sos.js7.converter.js1.common.jobchain.JobChain;
import com.sos.js7.converter.js1.common.jobchain.JobChainOrder;
import com.sos.js7.converter.js1.common.jobchain.node.AJobChainNode;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNode;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNodeFileOrderSink;
import com.sos.js7.converter.js1.common.jobchain.node.JobChainNodeFileOrderSource;
import com.sos.js7.converter.js1.common.processclass.ProcessClass;
import com.sos.js7.converter.js1.input.DirectoryParser;
import com.sos.js7.converter.js1.input.DirectoryParser.DirectoryParserResult;

/** <br/>
 * TODO<br/>
 * Locks<br/>
 * Schedule - convert from files and not from a jobscheduler answer ...<br/>
 */
public class JS12JS7Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS12JS7Converter.class);

    public static JS7ConverterConfig CONFIG = new JS7ConverterConfig();

    private static final int INITIAL_DUPLICATE_COUNTER = 1;

    private ConverterObjects converterObjects;
    private DirectoryParserResult pr;
    private String inputDirPath;
    private Map<Path, String> jobResources = new HashMap<>();
    private Map<String, Integer> jobResourcesDuplicates = new HashMap<>();
    private Map<Path, OrderJob> orderJobs = new HashMap<>();

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
        // 1.1 - Parser Reports
        ConverterReportWriter.writeParserReport(reportDir.resolve("parser_summary.csv"), reportDir.resolve("parser_errors.csv"), reportDir.resolve(
                "parser_warnings.csv"), reportDir.resolve("parser_analyzer.csv"));

        // 2 - Convert to JS7
        Instant start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][convert][start]...", method));
        JS7ConverterResult result = convert(pr);
        LOGGER.info(String.format("[%s][JS7][convert][end]%s", method, SOSDate.getDuration(start, Instant.now())));
        // 2.1 - Converter Reports
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

        JS12JS7Converter c = new JS12JS7Converter();
        c.inputDirPath = pr.getRoot().getPath().toString();
        c.converterObjects = c.getConverterObjects(pr.getRoot());

        c.convertStandalone(result);
        c.convertJobChains(result);
        c.addJobResources(result);

        return result;
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

    private void convertStandaloneWorkflow(JS7ConverterResult result, StandaloneJob job, int counter) {
        LOGGER.info("[convertStandaloneWorkflow]" + job.getPath());

        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(job.getTitle());
        w.setTimeZone(CONFIG.getWorkflowConfig().getDefaultTimeZone());

        Jobs js = new Jobs();
        js.setAdditionalProperty(job.getName(), getJob(result, job));
        w.setJobs(js);

        List<Instruction> in = new ArrayList<>();
        in.add(getNamedJobInstruction(job.getName()));
        in = getRetryInstructions(job, in);
        in = getCyclicWorkflowInstructions(job, in);
        w.setInstructions(in);
        Path wp = getWorkflowPath(result, job, counter);
        result.add(wp, w);

        convertSchedule(result, job, wp);
    }

    private void convertSchedule(JS7ConverterResult result, StandaloneJob job, Path workflowPath) {
        if (job.getRunTime() != null && !job.getRunTime().isEmpty()) {
            LOGGER.info("RunTime=" + job.getPath());
        }
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
        if (job.getDelayAfterError() != null && job.getDelayAfterError().size() > 0) {
            TryCatch tryCatch = new TryCatch();
            tryCatch.setTry(new Instructions(in));
            tryCatch.setCatch(new Instructions(in));

            List<Integer> retryDelays = new ArrayList<>();
            for (DelayAfterError d : job.getDelayAfterError()) {
                if (d.getDelay() != null) {
                    if (d.getDelay().equalsIgnoreCase("stop")) {
                        tryCatch.setMaxTries(d.getErrorCount());
                    } else {
                        for (int i = 0; i < d.getErrorCount(); i++) {
                            retryDelays.add(new Long(SOSDate.getTimeAsSeconds(d.getDelay())).intValue());
                        }
                    }
                }

            }
            tryCatch.setRetryDelays(retryDelays);

            in = new ArrayList<>();
            in.add(tryCatch);
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
        Job j = new Job();
        j.setTitle(job.getTitle());
        j = setFromConfig(j);
        j = setAgent(result, j, job);
        j = setArgumentsOrResources(j, job);
        j = setExecutable(j, job);
        j = setJobOptions(j, job);
        j = setJobNotification(j, job);
        return j;
    }

    private NamedJob getNamedJobInstruction(String jobName) {
        NamedJob nj = new NamedJob(jobName);
        nj.setLabel(nj.getJobName());
        return nj;
    }

    private Job setFromConfig(Job j) {
        if (CONFIG.getJobConfig().getGraceTimeout() != null) {
            j.setGraceTimeout(CONFIG.getJobConfig().getGraceTimeout());
        }
        if (CONFIG.getJobConfig().getParallelism() != null) {
            j.setParallelism(CONFIG.getJobConfig().getParallelism());
        }
        if (CONFIG.getJobConfig().getFailOnErrWritten() != null) {
            j.setFailOnErrWritten(CONFIG.getJobConfig().getFailOnErrWritten());
        }
        return j;
    }

    private Job setAgent(JS7ConverterResult result, Job j, ACommonJob job) {
        String name = null;
        if (CONFIG.getAgentConfig().getForcedName() != null) {
            name = CONFIG.getAgentConfig().getForcedName();
        } else {
            if (job.getProcessClass() != null) {
                name = getFileName(job.getProcessClass());
                if (converterObjects.processClasses.unique.containsKey(name)) {
                    // TODO parse file to check if remote scheduler
                }
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
        boolean isYADE = false;
        String javaClassName = null;

        String language = job.getScript().getLanguage() == null ? "shell" : job.getScript().getLanguage();
        String className = null;
        switch (language) {
        case "java":
            String jc = job.getScript().getJavaClass();
            switch (jc) {
            // DB
            case "com.sos.jitl.extract.job.ResultSet2CSVJobJSAdapterClass":
            case "com.sos.jitl.managed.job.ManagedDatabaseJob":
                javaClassName = "com.sos.jitl.jobs.db.SQLExecutorJob";
                break;
            case "sos.scheduler.db.JobSchedulerPLSQLJobJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.db.oracle.PLSQLJob";
                break;
            case "sos.scheduler.db.SOSSQLPlusJobJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.db.oracle.SQLPLUSJob";
                break;
            // CHECKHISTORY:
            case "com.sos.jitl.checkhistory.JobSchedulerCheckHistoryJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.checkhistory.CheckHistoryJob";
                break;
            // FILE Operations
            case "sos.scheduler.file.JobSchedulerExistsFile":
                javaClassName = "com.sos.jitl.jobs.file.FileExistsJob";
                break;
            case "sos.scheduler.file.JobSchedulerCanWrite":
                javaClassName = "com.sos.jitl.jobs.file.CanWriteJob";
                break;
            case "sos.scheduler.file.JobSchedulerCopyFile":
                javaClassName = "com.sos.jitl.jobs.file.CopyFileJob";
                break;
            case "sos.scheduler.file.JobSchedulerNotExistsFile":
                javaClassName = "com.sos.jitl.jobs.file.FileNotExistsJob";
                break;
            case "sos.scheduler.file.JobSchedulerRemoveFile":
                javaClassName = "com.sos.jitl.jobs.file.RemoveFileJob";
                break;
            case "sos.scheduler.file.JobSchedulerRenameFile":
                javaClassName = "com.sos.jitl.jobs.file.RenameFileJob";
                break;
            // MAIL
            case "sos.mail.SOSMailProcessInbox":
            case "com.sos.jitl.mailprocessor.SOSMailProcessInboxJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.mail.MailInboxJob";
                break;
            case "sos.scheduler.job.JobSchedulerDequeueMailJob":
            case "com.sos.jitl.housekeeping.dequeuemail.JobSchedulerDequeueMailJobJSAdapterClass":
                javaClassName = "com.sos.jitl.jobs.mail.MailJob";
                break;
            // SSH
            case "sos.scheduler.job.SOSSSHJob2JSAdapter":
                javaClassName = "com.sos.jitl.jobs.ssh.SSHJob";
                break;
            // YADE
            case "sos.scheduler.jade.JadeJob":
            case "sos.scheduler.jade.SOSJade4DMZJSAdapter":
                isYADE = true;
                break;
            default:
                className = jc;
                break;
            }

            break;
        }

        j.setExecutable(javaClassName == null ? getExecutableScript(j, job, language, className, isYADE) : getInternalExecutable(j, job,
                javaClassName));
        return j;
    }

    private ExecutableJava getInternalExecutable(Job j, ACommonJob job, String javaClassName) {
        ExecutableJava e = new ExecutableJava();
        e.setClassName(javaClassName);
        return e;
    }

    private ExecutableScript getExecutableScript(Job j, ACommonJob job, String language, String className, boolean isYADE) {
        Platform platform = CONFIG.getAgentConfig().getForcedPlatform() == null ? Platform.UNIX : CONFIG.getAgentConfig().getForcedPlatform();

        StringBuilder scriptHeader = new StringBuilder();
        StringBuilder scriptCommand = new StringBuilder();
        if (platform.equals(Platform.UNIX)) {
            scriptHeader.append("#!/bin/bash");
            scriptHeader.append(CONFIG.getJobConfig().getScriptNewLine());
            if (isYADE) {
                scriptCommand.append("$YADE_BIN -settings $settings -profile $profile");
            }
        } else {
            if (isYADE) {
                scriptCommand.append("%YADE_BIN% -settings %settings% -profile %profile%");
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

        ExecutableScript e = new ExecutableScript();
        e.setScript(script.toString());
        return e;
    }

    private Job setArgumentsOrResources(Job j, ACommonJob job) {
        if (job.getParams() != null && job.getParams().hasParams()) {
            // ARGUMENTS
            Environment env = new Environment();
            job.getParams().getParams().entrySet().forEach(e -> {
                env.setAdditionalProperty(e.getKey(), e.getValue());
            });
            j.setDefaultArguments(env);

            // JOB RESOURCES
            List<String> names = new ArrayList<>();
            for (Include i : job.getParams().getIncludes()) {
                Path p = findIncludeFile(job.getPath(), i.getIncludeFile());
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

    private Path findIncludeFile(Path jobPath, Path p) {
        if (p.isAbsolute()) {
            return p;
        }
        Path includePath = null;
        String ps = p.toString();
        if (ps.startsWith("/") || ps.startsWith("\\")) {
            includePath = pr.getRoot().getPath().resolve(p).normalize();
        } else {
            includePath = jobPath.getParent().resolve(p).normalize();
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

    private Workflow setArgumentsOrResources(Workflow w, JobChainOrder o) {
        if (o.getParams() != null && o.getParams().hasParams()) {
            // ARGUMENTS
            Parameters parameters = new Parameters();
            o.getParams().getParams().entrySet().forEach(e -> {
                Parameter p = new Parameter();
                p.setType(ParameterType.String);
                p.setDefault(e.getValue());
                parameters.setAdditionalProperty(e.getKey(), p);
            });
            w.setOrderPreparation(new Requirements(parameters, false));

            // JOB RESOURCES
            List<String> names = new ArrayList<>();
            for (Include i : o.getParams().getIncludes()) {
                Path p = findIncludeFile(o.getPath(), i.getIncludeFile());
                String name = resolveJobResource(p);
                if (name != null) {
                    names.add(name);
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
                if (o.getRunTime() == null || o.getRunTime().isEmpty()) {
                    w = setArgumentsOrResources(w, o);
                }
            }
        }

        Map<String, OrderJob> uniqueJobs = new LinkedHashMap<>();
        Map<String, JobChainStateHelper> states = new LinkedHashMap<>();
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
                    Path job = findIncludeFile(jobChain.getPath(), Paths.get(jcn.getJob() + EConfigFileExtensions.JOB.extension()));
                    // TODO NPE when not exists or wrong findIncludeFile handling ...
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
                        LOGGER.warn("[jobChain " + jobChain.getPath() + "/node=" + SOSString.toString(n) + "]" + e.toString(), e);
                        ConverterReport.INSTANCE.addWarningRecord(job, "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n) + "]", e
                                .toString());
                    }
                }
                break;
            case ORDER_SINK:
                JobChainNodeFileOrderSink os = (JobChainNodeFileOrderSink) n;

                ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
                        + "]", "not implemented yet");
                break;
            case ORDER_SOURCE:
                JobChainNodeFileOrderSource oso = (JobChainNodeFileOrderSource) n;

                ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
                        + "]", "not implemented yet");
                break;
            case JOB_CHAIN:
                ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
                        + "]", "not implemented yet");
                break;
            case END:
                ConverterReport.INSTANCE.addAnalyzerRecord(jobChain.getPath(), "[jobChain " + jobChain.getName() + "/node=" + SOSString.toString(n)
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
        Path wp = getWorkflowPath(result, jobChain, counter);
        result.add(wp, w);

        convertJobChainOrders(result, jobChain, wp);

    }

    private void convertJobChainOrders(JS7ConverterResult result, JobChain jobChain, Path jobChainPath) {
        if (jobChain.getOrders().size() > 0 && CONFIG.getGenerateConfig().getSchedules()) {
            List<JobChainOrder> orders = jobChain.getOrders().stream().filter(o -> o.getRunTime() != null && !o.getRunTime().isEmpty()).collect(
                    Collectors.toList());
            if (orders.size() > 0) {
                Schedule s = new Schedule();
                AssignedCalendars ac = new AssignedCalendars();
                ac.setCalendarName(CONFIG.getScheduleConfig().getDefaultCalendarName() == null ? "TODO" : CONFIG.getScheduleConfig()
                        .getDefaultCalendarName());
                s.setCalendars(Collections.singletonList(ac));

                s.setPlanOrderAutomatically(CONFIG.getScheduleConfig().planOrders());
                s.setSubmitOrderToControllerWhenPlanned(CONFIG.getScheduleConfig().submitOrders());

                List<VariableSet> l = new ArrayList<>();
                for (JobChainOrder o : orders) {
                    if (o.getParams() != null && o.getParams().hasParams()) {
                        VariableSet set = new VariableSet();
                        set.setOrderName(o.getName());

                        Variables vs = new Variables();
                        o.getParams().getParams().entrySet().forEach(e -> {
                            vs.setAdditionalProperty(e.getKey(), e.getValue());
                        });
                        set.setVariables(vs);
                        l.add(set);
                    }
                }
                if (l.size() > 0) {
                    s.setVariableSets(l);
                }
                String workflowName = jobChainPath.getFileName().toString().replace(".workflow.json", "");
                s.setWorkflowNames(Collections.singletonList(workflowName));

                result.add(jobChainPath.getParent().resolve(workflowName + ".schedule.json"), s);
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
            in.add(getNamedJobInstruction(job.getName()));

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

}
