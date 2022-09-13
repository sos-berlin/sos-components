package com.sos.js7.converter.autosys.output.js7;

import java.io.IOException;
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
import com.sos.commons.util.SOSString;
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
import com.sos.inventory.model.instruction.ForkJoin;
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
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.BranchWorkflow;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition.ConditionType;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions;
import com.sos.js7.converter.autosys.input.AFileParser;
import com.sos.js7.converter.autosys.input.DirectoryParser;
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.report.AutosysReport;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.JS7ExportObjects.JS7ExportObject;
import com.sos.js7.converter.commons.agent.JS7AgentConverter;
import com.sos.js7.converter.commons.agent.JS7AgentHelper;
import com.sos.js7.converter.commons.agent.JS7AgentConverter.JS7AgentConvertType;
import com.sos.js7.converter.commons.config.JS7ConverterConfig;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.SubFolderConfig;
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
public class JS7Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7Converter.class);

    public static JS7ConverterConfig CONFIG = new JS7ConverterConfig();

    private Map<String, JS7Agent> machine2js7Agent = new HashMap<>();

    public static void convert(AFileParser parser, Path input, Path outputDir, Path reportDir) throws IOException {

        String method = "convert";

        // APP start
        Instant appStart = Instant.now();
        LOGGER.info(String.format("[%s][start]...", method));

        OutputWriter.prepareDirectory(outputDir);
        OutputWriter.prepareDirectory(reportDir);

        // 1 - Config Report
        ConverterReportWriter.writeConfigReport(reportDir.resolve("config_errors.csv"), reportDir.resolve("config_warnings.csv"), reportDir.resolve(
                "config_analyzer.csv"));

        // 2 - Parse Autosys files
        LOGGER.info(String.format("[%s][JIL][parse][start]...", method));
        DirectoryParserResult pr = DirectoryParser.parse(CONFIG.getParserConfig(), parser, input);
        LOGGER.info(String.format("[%s][JIL][parse][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));
        // 2.1 - Parser Reports
        ConverterReportWriter.writeParserReport(reportDir.resolve("parser_summary.csv"), reportDir.resolve("parser_errors.csv"), reportDir.resolve(
                "parser_warnings.csv"), reportDir.resolve("parser_analyzer.csv"));

        // 3 - Convert to JS7
        Instant start = Instant.now();
        LOGGER.info(String.format("[%s][JS7][convert][start]...", method));
        JS7ConverterResult result = convert(pr);
        LOGGER.info(String.format("[%s][JS7][convert][end]%s", method, SOSDate.getDuration(start, Instant.now())));
        // 3.1 - Converter Reports
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

        // 4.1 - Summary Report
        ConverterReportWriter.writeSummaryReport(reportDir.resolve("converter_summary.csv"));

        LOGGER.info(String.format("[%s][[JS7]write][end]%s", method, SOSDate.getDuration(start, Instant.now())));

        // APP end
        LOGGER.info(String.format("[%s][end]%s", method, SOSDate.getDuration(appStart, Instant.now())));
    }

    private static JS7ConverterResult convert(DirectoryParserResult pr) {
        String method = "convert";

        JS7Converter c = new JS7Converter();
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
                LOGGER.info(String.format("[%s][%s jobs=%]not implemented yet", method, key, size));
                for (ACommonJob j : value) {
                    ConverterReport.INSTANCE.addAnalyzerRecord(j.getSource(), j.getInsertJob().getValue(), j.getJobType().getValue()
                            + ":not implemented yet");
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
        AutosysReport.analyze(standaloneJobs, boxJobs);

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
            String jobName = normalizeName(result, fullJobName, "[postProcessing][postNotice][success]");
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflowByPath(jobName);

            result.getWorkflows().getItems().forEach(i -> {
                LOGGER.debug("[postProcessing][postNotice][success]workflow=" + i.getOriginalPath().getPath());
            });
            // TODO for all types - failed, done etc. check create instructions ...
            if (eo == null) {
                eo = result.getExportObjectWorkflowByJobName(jobName);
            }
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]workflow not found", method, jobName));
                ConverterReport.INSTANCE.addErrorRecord("[postProcessing][postNotice][success][workflow not found]" + jobName);
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

            String jobName = normalizeName(result, fullJobName, "[postProcessing][postNotice][failed]");
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflowByPath(jobName);
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]not found", method, jobName));
                ConverterReport.INSTANCE.addErrorRecord("[postProcessing][postNotice][failed][workflow not found]" + jobName);
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

            String jobName = normalizeName(result, fullJobName, "[postProcessing][postNotice][done]");
            @SuppressWarnings("rawtypes")
            JS7ExportObject eo = result.getExportObjectWorkflowByPath(jobName);
            if (eo == null) {
                LOGGER.error(String.format("[%s][%s]not found", method, jobName));
                ConverterReport.INSTANCE.addErrorRecord("[postProcessing][postNotice][done][workflow not found]" + jobName);
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

    private void convertBoxWorkflow(JS7ConverterResult result, JobBOX jilJob) {
        if (jilJob.getJobs() == null) {
            return;
        }
        int size = jilJob.getJobs().size();
        if (size == 0) {
            return;
        }
        // WORKFLOW
        String jobName = normalizeName(result, jilJob, jilJob.getInsertJob().getValue());
        Workflow w = new Workflow();
        w.setTitle(jilJob.getDescription().getValue());
        w.setTimeZone(jilJob.getRunTime().getTimezone().getValue() == null ? CONFIG.getWorkflowConfig().getDefaultTimeZone() : jilJob.getRunTime()
                .getTimezone().getValue());

        Jobs jobs = new Jobs();
        for (ACommonJob j : jilJob.getJobs()) {
            String jn = normalizeName(result, j, j.getInsertJob().getValue());
            if (j instanceof JobCMD) {
                jobs.setAdditionalProperty(jn, getJob(result, (JobCMD) j));
            } else {
                ConverterReport.INSTANCE.addErrorRecord("[convertBoxWorkflow][box=" + jobName + "][job=" + jn + "][not impemented yet]type=" + j
                        .getConverterJobType());
            }
            // TODO FW etc jobs
        }
        w.setJobs(jobs);

        List<Instruction> in = getExpectNoticeInstructions(result, jobName, jilJob);
        // in.add(getNamedJobInstruction(jobName));
        // in = getCyclicWorkflowInstructions(jilJob, in);

        if (size == 1) {
            ACommonJob jJob = jilJob.getJobs().get(0);
            in.add(getNamedJobInstruction(normalizeName(result, jJob, jJob.getInsertJob().getValue())));
        } else {
            List<ACommonJob> children = removeBoxJobConditionsFromChildren(jilJob);
            List<ACommonJob> childrenCopy = new ArrayList<>(children);
            List<ACommonJob> firstFork = getFirstForkChildren(jilJob, childrenCopy);
            List<ACommonJob> added = new ArrayList<>();
            // childrenCopy after getFirstForkChildren is without firstFork jobs and contains job dependent of the firstFork jobs
            if (firstFork.size() < 2) {
                ACommonJob child = firstFork.get(0);
                String cn = normalizeName(result, child, child.getInsertJob().getValue());
                in.add(getNamedJobInstruction(cn));
                added.add(child);

                while (child != null) {
                    List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child, childrenCopy, added);
                    if (js.size() == 1) {
                        cn = normalizeName(result, js.get(0), js.get(0).getInsertJob().getValue());
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
                String cn = normalizeName(result, child, child.getInsertJob().getValue());
                in.add(getNamedJobInstruction(cn));
                added.add(child);
                childrenCopy.remove(child);

                while (child != null) {
                    List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child, childrenCopy, added);
                    if (js.size() == 1) {
                        cn = normalizeName(result, js.get(0), js.get(0).getInsertJob().getValue());
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
                ConverterReport.INSTANCE.addErrorRecord("[convertBoxWorkflow][box=" + jobName + "][not converted jobs]" + childrenCopy);
            }

        }
        in = getCyclicWorkflowInstructions(jilJob, in);
        w.setInstructions(in);
        result.add(getWorkflowPath(jilJob, jobName), w);
    }

    private static ForkJoin createForkJoin(JS7ConverterResult result, JobBOX jilJob, List<ACommonJob> children, List<ACommonJob> childrenCopy,
            List<ACommonJob> added) {
        List<Branch> branches = new ArrayList<>();
        Boolean joinIfFailed = false;
        int i = 1;
        added.addAll(children);
        for (ACommonJob child : children) {
            List<Instruction> bwIn = new ArrayList<>();
            String cn = normalizeName(result, child, child.getInsertJob().getValue());
            bwIn.add(getNamedJobInstruction(cn));

            ACommonJob child2 = child;
            while (child2 != null) {
                List<ACommonJob> js = findBoxJobChildSuccessor(jilJob, child2, childrenCopy, added);
                if (js.size() == 1) {
                    cn = normalizeName(result, js.get(0), js.get(0).getInsertJob().getValue());
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
            LOGGER.info("[findBoxJobChildSuccessor][child " + j.getInsertJob().getValue() + "]child conditions=" + jConditions + "=" + Conditions
                    .getConditions(j.getCondition().getCondition().getValue()));
            boolean found = jConditions.stream().filter(c -> c.getName().equals(currentChild.getInsertJob().getValue())).count() > 0;
            if (found) {
                if (jConditions.size() == 1) {// only parent job
                    // TODO remove only box jobs conditions
                    j.getCondition().getCondition().getValue().clear();
                    result.add(j);
                } else {

                    long count = jConditions.stream().filter(c -> children.stream().filter(jj -> jj.getInsertJob().getValue().equals(c.getName()))
                            .count() > 0).count();
                    if (count == 0) {
                        count = jConditions.stream().filter(c -> added.stream().filter(jj -> jj.getInsertJob().getValue().equals(c.getName()))
                                .count() > 0).count();
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
        List<ConditionType> excludedTypes = Arrays.asList(ConditionType.GLOBAL_VARIABLE, ConditionType.NOTRUNNING, ConditionType.TERMINATED,
                ConditionType.EXITCODE);
        List<Condition> conditions = Conditions.getConditions(currentChild.getCondition().getCondition().getValue());
        return conditions.stream().filter(c -> !excludedTypes.contains(c.getType())).filter(x -> {
            return boxJob.getJobs().stream().filter(cj -> cj.getInsertJob().getValue().equals(x.getName())).count() > 0;
        }).collect(Collectors.toList());
    }

    private static List<ACommonJob> removeBoxJobConditionsFromChildren(JobBOX jilJob) {
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
                if (c.getType().equals(ConditionType.GLOBAL_VARIABLE)) {
                    continue;
                }
                // TODO - remove if supported
                if (c.getType().equals(ConditionType.NOTRUNNING) || c.getType().equals(ConditionType.TERMINATED) || c.getType().equals(
                        ConditionType.EXITCODE)) {
                    continue;
                }
                count += boxJob.getJobs().stream().filter(cj -> cj.getInsertJob().getValue().equals(c.getName())).count();
            }
            if (count == 0) {
                LOGGER.info("getBoxJobChildrenConditions ADD=" + j.getInsertJob().getValue());
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
                if (c.getType().equals(ConditionType.GLOBAL_VARIABLE)) {
                    continue;
                }
                // TODO - remove if supported
                if (c.getType().equals(ConditionType.NOTRUNNING) || c.getType().equals(ConditionType.TERMINATED) || c.getType().equals(
                        ConditionType.EXITCODE)) {
                    continue;
                }
                count += boxJob.getJobs().stream().filter(cj -> cj.getInsertJob().getValue().equals(c.getName())).count();
            }
            if (count == 0) {
                LOGGER.info("[getFirstForkChildren][add][notThisBoxCondition]" + j.getInsertJob().getValue());
                withNotThisBoxConditions.add(j);
            }
        }
        result.addAll(withNotThisBoxConditions);
        children.removeAll(withNotThisBoxConditions);
        LOGGER.info("[getFirstForkChildren][afterRemoveNotThisBoxConditions]" + children);
        return result;
    }

    private void convertStandalone(JS7ConverterResult result, JobCMD jilJob) {
        convertStandaloneWorkflow(result, jilJob);
        convertSchedule(result, jilJob);
    }

    private void convertStandaloneWorkflow(JS7ConverterResult result, JobCMD jilJob) {
        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(jilJob.getDescription().getValue());
        w.setTimeZone(jilJob.getRunTime().getTimezone().getValue() == null ? CONFIG.getWorkflowConfig().getDefaultTimeZone() : jilJob.getRunTime()
                .getTimezone().getValue());

        String jobName = normalizeName(result, jilJob, jilJob.getInsertJob().getValue());
        Jobs js = new Jobs();
        js.setAdditionalProperty(jobName, getJob(result, jilJob));
        w.setJobs(js);

        List<Instruction> in = getExpectNoticeInstructions(result, jobName, jilJob);
        in.add(getNamedJobInstruction(jobName));
        in = getCyclicWorkflowInstructions(jilJob, in);
        w.setInstructions(in);
        result.add(getWorkflowPath(jilJob, jobName), w);
    }

    private Job getJob(JS7ConverterResult result, JobCMD jilJob) {
        Job j = new Job();
        j.setTitle(jilJob.getDescription().getValue());
        j = setFromConfig(j);

        JS7Agent js7Agent = getAgent(result, j, jilJob);
        j = JS7AgentHelper.setAgent(j, js7Agent);
        j = setExecutable(j, jilJob);
        j = setJobOptions(j, jilJob);
        return j;
    }

    private void convertSchedule(JS7ConverterResult result, ACommonJob jilJob) {
        String calendarName = getCalendarName(result, CONFIG, jilJob);
        if (calendarName == null) {
            ConverterReport.INSTANCE.addWarningRecord(null, jilJob.getInsertJob().getValue(), "[convertSchedule][job without " + jilJob.getRunTime()
                    .getRunCalendar().getName() + "][missing callendar]scheduleConfig.forced- or defaultWorkingDayCalendarName is not configured");
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
        calendar.setPeriods(periods);

        String jobName = normalizeName(result, jilJob, jilJob.getInsertJob().getValue());
        Schedule s = new Schedule();
        s.setWorkflowNames(Collections.singletonList(jobName));
        s.setPlanOrderAutomatically(CONFIG.getScheduleConfig().planOrders());
        s.setSubmitOrderToControllerWhenPlanned(CONFIG.getScheduleConfig().submitOrders());
        s.setCalendars(Collections.singletonList(calendar));
        result.add(getSchedulePath(jilJob, jobName), s);
    }

    private static String getCalendarName(JS7ConverterResult result, JS7ConverterConfig config, ACommonJob jilJob) {
        String name = null;
        if (config.getScheduleConfig().getForcedWorkingDayCalendarName() != null) {
            name = config.getScheduleConfig().getForcedWorkingDayCalendarName();
        } else if (jilJob.getRunTime().getRunCalendar().getValue() != null) {
            name = normalizeName(result, jilJob, jilJob.getRunTime().getRunCalendar().getValue());
        } else if (config.getScheduleConfig().getDefaultWorkingDayCalendarName() != null) {
            name = config.getScheduleConfig().getDefaultWorkingDayCalendarName();
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
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getAgent][%s]autosys.machine=%s", JS7AgentConvertType.CONFIG_FORCED.name(), machine));
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
                LOGGER.debug(String.format("[getAgent][%s][autosys.machine==%s]", JS7AgentConvertType.CONFIG_DEFAULT.name(), machine));
            }
            agent = convertAgentFrom(JS7AgentConvertType.CONFIG_DEFAULT, CONFIG.getAgentConfig().getDefaultAgent(), null, machine);
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
        return source;
    }

    private Job setExecutable(Job j, JobCMD jilJob) {
        ExecutableScript e = new ExecutableScript();
        // TODO unix/windows
        StringBuilder script = new StringBuilder("#!/bin/sh");
        script.append(CONFIG.getJobConfig().getScriptNewLine());
        if (jilJob.getProfile().getValue() != null) {
            script.append(jilJob.getProfile().getValue()).append(CONFIG.getJobConfig().getScriptNewLine());
        }
        if (CONFIG.getMockConfig().hasScript()) {
            // TODO unix/windows
            script.append(CONFIG.getMockConfig().getUnixScript()).append(" ");
        }
        script.append(jilJob.getCommand().getValue());
        e.setScript(script.toString());

        j.setExecutable(e);
        return j;
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
            case GLOBAL_VARIABLE:
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
            p.setOffsets(jilJob.getRunTime().getStartMins().getValue().stream().map(e -> new Long(e * 60)).collect(Collectors.toList()));

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

    private static ExpectNotice createExpectNotice(JS7ConverterResult result, ACommonJob jilJob, String boardName, String boardTitle) {
        ExpectNotice en = new ExpectNotice(boardName);

        Board b = new Board();
        b.setTitle(boardTitle);
        b.setEndOfLife("$js7EpochMilli + 1 * 24 * 60 * 60 * 1000");
        b.setExpectOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");
        b.setPostOrderToNoticeId("replaceAll($js7OrderId, '^#([0-9]{4}-[0-9]{2}-[0-9]{2})#.*$', '$1')");

        result.add(getNoticeBoardPath(jilJob, en.getNoticeBoardName()), b);
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
            ConverterReport.INSTANCE.addErrorRecord(sb.toString());
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
