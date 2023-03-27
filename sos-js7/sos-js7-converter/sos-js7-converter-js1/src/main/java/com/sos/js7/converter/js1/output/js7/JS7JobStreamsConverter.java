package com.sos.js7.converter.js1.output.js7;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.schedule.OrderParameterisation;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.BranchWorkflow;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.report.ConverterReport;
import com.sos.js7.converter.js1.common.EConfigFileExtensions;
import com.sos.js7.converter.js1.common.jobstreams.condition.Condition;
import com.sos.js7.converter.js1.common.jobstreams.condition.Condition.ConditionType;
import com.sos.js7.converter.js1.common.jobstreams.condition.Conditions;
import com.sos.js7.converter.js1.common.json.NameValuePair;
import com.sos.js7.converter.js1.common.json.jobstreams.InCondition;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStream;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamJob;
import com.sos.js7.converter.js1.common.json.jobstreams.JobStreamStarter;
import com.sos.js7.converter.js1.common.runtime.RunTime;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamClosingJob;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamConditionsHelper;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamFindChildsResult;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamForkJoinResult;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamJS1JS7Job;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamJS7Branch;
import com.sos.js7.converter.js1.output.js7.helper.JobStreamsHelper;

public class JS7JobStreamsConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7JobStreamsConverter.class);

    private JS12JS7Converter js7Converter;
    private Map<String, List<JobStreamJS1JS7Job>> added = new HashMap<>();
    private Map<String, JobStreamConditionsHelper> notResolvedConditions = new HashMap<>();
    private Map<String, List<Instruction>> allInstructions = new HashMap<>();
    private Set<String> notFoundInConditionJobs = new HashSet<>();
    private List<JobStreamClosingJob> nestedClosingJobs = new ArrayList<>();

    public static List<JobStream> read(Path file) throws Exception {
        return JS7ConverterHelper.JSON_OM.readerForListOf(JobStream.class).readValue(SOSPath.readFile(file, StandardCharsets.UTF_8));
    }

    public static void convert(JS12JS7Converter js7Converter, JS7ConverterResult result, Path file, List<JobStream> jobStreams) {
        try {
            JS7JobStreamsConverter c = new JS7JobStreamsConverter();
            c.js7Converter = js7Converter;
            for (JobStream jobStream : jobStreams) {
                c.convertJobStream(result, file, jobStream);
            }
        } catch (Throwable e) {
            ConverterReport.INSTANCE.addErrorRecord(file, "convert", e);
        }

    }

    private void convertJobStream(JS7ConverterResult result, Path file, JobStream jobStream) {
        String method = "convertJobStream";
        try {
            if (jobStream.getJobstreamStarters() == null || jobStream.getJobstreamStarters().size() == 0) {
                ConverterReport.INSTANCE.addAnalyzerRecord(file, "convertJobStream=" + jobStream.getJobStream(), "[skip]missing JobStream Starter");
                return;
            }
            if (jobStream.getJobs() == null || jobStream.getJobs().size() == 0) {
                ConverterReport.INSTANCE.addAnalyzerRecord(file, "convertJobStream=" + jobStream.getJobStream(), "[skip]missing JobStream Jobs");
                return;
            }

            List<JobStreamStarter> starters = new ArrayList<>();
            for (JobStreamStarter starter : jobStream.getJobstreamStarters()) {
                if (starter.getJobs() == null || starter.getJobs().size() == 0) {
                    ConverterReport.INSTANCE.addAnalyzerRecord(file, "convertJobStream=" + jobStream.getJobStream() + ", starterName=" + starter
                            .getStarterName(), "[skip]missing Starter Jobs");
                    continue;
                }
                starters.add(starter);
            }

            if (starters.size() > 0) {
                Map<String, JobStreamJS1JS7Job> allConvertedJobStreamsJobs = JobStreamsHelper.convert(js7Converter, result, file, jobStream);

                Path workflowPath = getWorkflowPath(result, jobStream);
                String workflowName = JS7ConverterHelper.getWorkflowName(workflowPath);

                List<Instruction> in = new ArrayList<>();
                if (starters.size() == 1) {
                    JobStreamStarter starter = starters.get(0);
                    if (starter.getJobs().size() == 1) {
                        JobStreamJS1JS7Job startJob = allConvertedJobStreamsJobs.get(starter.getJobs().get(0).getJob()); // js7Converter.findStandaloneJobByPath(path);
                        if (startJob == null) {
                            ConverterReport.INSTANCE.addAnalyzerRecord(file, "convertJobStream=" + jobStream.getJobStream(), "[" + starter.getJobs()
                                    .get(0).getJob() + "]StandaloneJob not found");
                        } else {
                            in.add(js7Converter.getNamedJobInstruction(startJob.getJS1Job(), startJob.getJS7JobName(), startJob.getJS7JobName(), null,
                                    null, null));
                            LOGGER.info(String.format("[%s][%s][added]", method, startJob.getJS1Job().getName()));

                            List<JobStreamJob> allJobs = new ArrayList<>(jobStream.getJobs());

                            allJobs.remove(startJob.getJS1JobStreamJob());

                            startJob.setJS7WorkflowName(workflowName);
                            startJob.setJS7WorkflowPath(workflowPath);
                            startJob.setJS7BranchPath(null);

                            added = new HashMap<>();
                            addJob(workflowName, startJob);

                            notResolvedConditions = new HashMap<>();
                            convertWorkflow(result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, startJob);
                            createWorkflow(result, workflowPath, workflowName, in, added.get(workflowName));

                            // TODO startPosition
                            createSchedule(result, file, workflowPath, workflowName, starter, null);

                            int i = 0;
                            for (Map.Entry<String, JobStreamConditionsHelper> e : notResolvedConditions.entrySet()) {
                                Path f = i == 0 ? file : null;
                                ConverterReport.INSTANCE.addWarningRecord(f, "NOT RESOLVED CONDITIONS, Job=" + JS7ConverterHelper.getFileName(e
                                        .getKey()), e.getValue().getConditionParts().toString());
                                i++;
                            }

                        }
                    }
                }
            }

        } catch (Throwable e) {
            ConverterReport.INSTANCE.addErrorRecord(file, "convertJobStream=" + jobStream.getJobStream(), e);
        }
    }

    private void createSchedule(JS7ConverterResult result, Path file, Path workflowPath, String workflowName, JobStreamStarter starter,
            String startPosition) {
        try {
            if (starter.getRunTime() == null) {
                return;
            }
            RunTime rt = new RunTime(starter.getRunTime());
            if (!rt.isEmpty()) {
                List<OrderParameterisation> l = null;
                if (starter.getParams() != null && starter.getParams().size() > 0) {
                    l = new ArrayList<>();
                    OrderParameterisation set = new OrderParameterisation();
                    set.setOrderName(workflowName);
                    Variables vs = new Variables();
                    for (NameValuePair nv : starter.getParams()) {
                        vs.setAdditionalProperty(nv.getName(), js7Converter.getParamValue(nv.getValue()));
                    }
                    set.setVariables(vs);
                    l.add(set);
                }
                js7Converter.addJS1ScheduleFromScheduleOrRunTime(rt, l, startPosition, workflowPath, workflowName, startPosition);
            }

        } catch (Throwable e) {
            ConverterReport.INSTANCE.addErrorRecord(file, "createSchedule=" + starter.getRunTime(), e);
        }
    }

    private void createWorkflow(JS7ConverterResult result, Path workflowPath, String title, List<Instruction> in, List<JobStreamJS1JS7Job> jobs) {
        Workflow w = new Workflow();
        w.setTitle(title);
        w.setTimeZone(JS12JS7Converter.CONFIG.getWorkflowConfig().getDefaultTimeZone());
        w.setInstructions(in);

        Jobs js = new Jobs();
        for (JobStreamJS1JS7Job j : jobs) {
            js.setAdditionalProperty(j.getJS7JobName(), j.getJS7Job());
        }
        w.setJobs(js);
        result.add(workflowPath, w);

        ConverterReport.INSTANCE.addAnalyzerRecord(workflowPath, "from JOBSTREAM", "jobs=" + jobs.size());
    }

    private void convertWorkflow(JS7ConverterResult result, Map<String, JobStreamJS1JS7Job> allConvertedJobStreamsJobs, List<JobStreamJob> allJobs,
            Path workflowPath, String workflowName, List<Instruction> in, JobStreamJS1JS7Job startJob) {

        allInstructions.put(workflowName, in);

        String method = "convertWorkflow";
        LOGGER.info(String.format("[%s][%s][startJob=%s]...", method, workflowName, startJob.getJS1Job().getName()));

        List<JobStreamClosingJob> closingJobs = convertJobChilds(method, result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in,
                in, startJob);
        if (closingJobs != null && closingJobs.size() > 0) {
            LOGGER.debug("[CLOSING]WORKFLOW_END");
            add2nestedClosingJobs(closingJobs);
            handleNestedClosingJobs(method + "-closingJobs", result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, in);
        }
        LOGGER.debug("[CLOSING]WORKFLOW_END_END");

        handleNestedClosingJobs(method, result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, in);

        // convertClosingJobs(result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, closingJobs);

        // for (JobStreamClosingJob jcj : closingJobsAtEnd) {
        // addNamedInstruction(jcj.getJob(), in, workflowPath, workflowName);
        // closingJobs = convertJobChilds(result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, in, jcj.getJob());
        // LOGGER.info("[CLOSING][CONVERTWORKFLOW][ATEND][" + jcj.getJob().getJS7JobName() + "]" + closingJobs);

        // }

        // LOGGER.info("NOTRESOLVED=" + notResolvedConditions + "=" + closingJobs);

        LOGGER.info(String.format("[%s][%s][startJob=%s]end", method, workflowName, startJob.getJS1Job().getName()));
    }

    private List<JobStreamClosingJob> convertJobChilds(String range, JS7ConverterResult result,
            Map<String, JobStreamJS1JS7Job> allConvertedJobStreamsJobs, List<JobStreamJob> allJobs, Path workflowPath, String workflowName,
            List<Instruction> in, List<Instruction> currentIn, JobStreamJS1JS7Job js1js7Job) {

        String method = "convertJobChilds";
        LOGGER.debug(String.format("[%s][%s][%s][startJob=%s]...", method, workflowName, range, js1js7Job.getJS1Job().getName()));

        JobStreamJS1JS7Job child = js1js7Job;
        List<JobStreamClosingJob> closingJobs = null;
        while (child != null) {
            JobStreamFindChildsResult fcr = findChilds(allConvertedJobStreamsJobs, allJobs, workflowName, child);
            List<JobStreamJS1JS7Job> children = fcr.getJobs();
            switch (children.size()) {
            case 0:
                closingJobs = fcr.getClosingJobs();
                if (closingJobs != null && closingJobs.size() > 0) {
                    if (range.endsWith("-end")) {
                        add2nestedClosingJobs(closingJobs);
                    }
                    // add2nestedClosingJobs(closingJobs);
                    // closingJobs = null;
                }
                LOGGER.debug(String.format("[%s][%s][%s][child=%s][children=0]closingJobs=%s", method, workflowName, range, child, closingJobs));
                child = null;

                handleNestedClosingJobs(method + "-childen=0", result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in,
                        currentIn);
                break;
            case 1:
                LOGGER.debug(String.format("[%s][%s][%s][child=%s][children=1]...", method, workflowName, range, child));

                child = children.get(0);
                addNamedInstruction(child, currentIn, workflowPath, workflowName);

                // JobStreamClosingJob cl = convertJobChilds(result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, currentIn,
                // child, parentBranchPath, branchName);
                // child = null;
                handleNestedClosingJobs(method + "-childen=1", result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in,
                        currentIn);
                break;
            default:
                LOGGER.debug(String.format("  [%s][%s][][child=%s][children=%s]createForkJoin ...", method, workflowName, range, child, children
                        .size()));
                JobStreamForkJoinResult fjr = createForkJoin(result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, currentIn, null,
                        js1js7Job, children);

                LOGGER.debug("[FORK_JOIN]" + fjr);
                // in.add(fjr.getForkJoin());
                child = null; // TODO
                break;
            }
        }

        // handleNestedClosingJobs(result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, currentIn);

        LOGGER.debug(String.format("[%s][%s][%s][%s]end", method, workflowName, range, js1js7Job.getJS1Job().getName()));
        return closingJobs;
    }

    private void handleNestedClosingJobs(String range, JS7ConverterResult result, Map<String, JobStreamJS1JS7Job> allConvertedJobStreamsJobs,
            List<JobStreamJob> allJobs, Path workflowPath, String workflowName, List<Instruction> in, List<Instruction> currentIn) {
        String method = "handleNestedClosingJobs";

        List<JobStreamClosingJob> toRemove = new ArrayList<>();
        for (JobStreamClosingJob cj : nestedClosingJobs) {
            boolean found = false;
            x: for (JobStreamJS1JS7Job j : cj.getUsed()) {
                found = namedJobAdded(in, j.getJS7JobName());
                LOGGER.debug(String.format("[%s][%s][%s][%s][used=%s]found=%s", method, workflowName, range, cj, j.getJS7JobName(), found));

                if (!found) {
                    break x;
                }
            }
            if (found) {
                toRemove.add(cj);
            }
        }
        if (toRemove.size() > 0) {
            nestedClosingJobs.removeAll(toRemove);
            for (JobStreamClosingJob cj : toRemove) {
                addNamedInstruction(cj.getJob(), in, workflowPath, workflowName);
                convertJobChilds(method + "-" + range, result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, currentIn, cj
                        .getJob());
            }
        }
    }

    private boolean namedJobAdded(List<Instruction> in, String jobName) {
        if (in == null || in.size() == 0) {
            return false;
        }

        for (Instruction inst : in) {
            if (inst instanceof NamedJob) {
                NamedJob nj = (NamedJob) inst;
                if (nj.getJobName() != null && nj.getJobName().equals(jobName)) {
                    return true;
                }
            } else if (inst instanceof ForkJoin) {
                ForkJoin fj = (ForkJoin) inst;
                if (fj.getBranches() != null) {
                    for (Branch b : fj.getBranches()) {
                        BranchWorkflow bw = b.getWorkflow();
                        if (bw != null) {
                            boolean r = namedJobAdded(bw.getInstructions(), jobName);
                            if (r) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private void addNamedInstruction(JobStreamJS1JS7Job job, List<Instruction> in, Path workflowPath, String workflowName) {
        job.setJS7WorkflowPath(workflowPath);
        job.setJS7WorkflowName(workflowName);
        // child.setJS7BranchPath(getBranchPath(parentBranchPath, branchName));
        LOGGER.debug(String.format("[addNamedInstruction][%s]job=%s", workflowName, job.getJS7JobName()));

        in.add(js7Converter.getNamedJobInstruction(job.getJS1Job(), job.getJS7JobName(), job.getJS7JobName(), null, null, null));
        addJob(workflowName, job);

    }

    private void addJob(String workflowName, JobStreamJS1JS7Job job) {
        List<JobStreamJS1JS7Job> l = added.get(workflowName);
        if (l == null) {
            l = new ArrayList<>();
        }
        l.add(job);
        added.put(workflowName, l);
        LOGGER.debug(String.format("[addJob][%s][added]job=%s", workflowName, job.getJS7JobName()));
    }

    private JobStreamForkJoinResult createForkJoin(JS7ConverterResult result, Map<String, JobStreamJS1JS7Job> allConvertedJobStreamsJobs,
            List<JobStreamJob> allJobs, Path workflowPath, String workflowName, List<Instruction> in, String parentBranchPath,
            JobStreamJS1JS7Job js1js7Job, List<JobStreamJS1JS7Job> children) {
        String method = "createForkJoin";

        LOGGER.debug(String.format("[%s][%s][%s]children=%s...", method, workflowName, js1js7Job.getJS1Job().getName(), children.size()));

        Map<String, List<JobStreamJS1JS7Job>> forkChildrenTmp = new HashMap<>();
        int i = 1;
        List<JobStreamJS7Branch> lb = new ArrayList<>();

        List<JobStreamJS1JS7Job> fc;
        JobStreamJS1JS7Job c;
        for (JobStreamJS1JS7Job child : children) {
            JobStreamFindChildsResult fcr = findChildsPreview(allConvertedJobStreamsJobs, allJobs, workflowName, child);
            switch (fcr.getJobs().size()) {
            case 0:
                LOGGER.debug(String.format("  [%s][%s][child=%s]child jobs=0", method, workflowName, child.getJS7JobName()));

                fc = new ArrayList<>();
                fc.add(child);
                forkChildrenTmp.put(child.getJS7JobName(), fc);

                break;
            case 1:
                c = fcr.getJobs().get(0);
                LOGGER.debug(String.format("  [%s][%s][child=%s][child jobs=1]%s", method, workflowName, child.getJS7JobName(), c.getJS7JobName()));

                fc = forkChildrenTmp.get(c.getJS7JobName());
                if (fc == null) {
                    fc = new ArrayList<>();
                }
                fc.add(child);
                forkChildrenTmp.put(c.getJS7JobName(), fc);
                break;
            default:
                LOGGER.debug(String.format("  [%s][%s][child=%s][child jobs=%s]%s", method, workflowName, child.getJS7JobName(), fcr.getJobs().size(),
                        child.getJS7JobName()));

                fc = new ArrayList<>();
                fc.add(child);
                forkChildrenTmp.put(child.getJS7JobName(), fc);
                break;
            }
        }

        Map<String, List<JobStreamJS1JS7Job>> forkChildren = new HashMap<>();
        for (Map.Entry<String, List<JobStreamJS1JS7Job>> e : forkChildrenTmp.entrySet()) {
            if (e.getValue().size() == 1) {
                forkChildren.put(e.getKey(), e.getValue());
            } else {
                JobStreamJS1JS7Job js = getJob(allConvertedJobStreamsJobs, e.getKey());
                if (js != null) {
                    // TODO check if exist etc
                    if (js.getJS1InEventNames().size() > e.getValue().size()) {
                        for (JobStreamJS1JS7Job cj : e.getValue()) {
                            forkChildren.put(cj.getJS7JobName(), Collections.singletonList(cj));
                        }
                    } else {
                        forkChildren.put(e.getKey(), e.getValue());
                    }
                } else {
                    forkChildren.put(e.getKey(), e.getValue());
                }
            }

        }

        for (Map.Entry<String, List<JobStreamJS1JS7Job>> e : forkChildren.entrySet()) {
            JobStreamJS7Branch b = new JobStreamJS7Branch(parentBranchPath, "branch_" + i);
            if (e.getValue().size() == 1) {
                b.setChildJob(e.getValue().get(0));
            } else {
                int j = 1;
                for (JobStreamJS1JS7Job child : e.getValue()) {
                    JobStreamJS7Branch bb = new JobStreamJS7Branch(b.getPath(), "branch_" + j);
                    bb.setChildJob(child);
                    b.addChildBranch(bb);
                    j++;
                }
            }
            lb.add(b);
            i++;
        }

        LOGGER.debug(String.format("[%s][%s][%s][child branches=%s]forkChildren=%s", method, workflowName, js1js7Job.getJS1Job().getName(), lb.size(),
                forkChildren));

        ForkJoin forkJoin = new ForkJoin();
        List<Branch> branches = new ArrayList<>();
        List<JobStreamClosingJob> closingJobs = null;
        for (JobStreamJS7Branch b : lb) {

            LOGGER.debug(String.format("[%s][%s][%s][child branch]%s", method, workflowName, js1js7Job.getJS1Job().getName(), b));

            List<Instruction> bwIn = new ArrayList<>();
            if (b.getChildBranches().size() > 0) {
                ForkJoin subForkJoin = new ForkJoin();
                List<Branch> subBranches = new ArrayList<>();
                for (JobStreamJS7Branch bb : b.getChildBranches()) {
                    List<Instruction> subBwIn = new ArrayList<>();

                    addNamedInstruction(bb.getChildJob(), subBwIn, workflowPath, workflowName);
                    closingJobs = convertJobChilds(method + "-subbranches", result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName,
                            bwIn, subBwIn, bb.getChildJob());
                    LOGGER.debug("[CLOSING][SUBBRANCH][" + bb.getChildJob() + "]" + closingJobs);

                    BranchWorkflow bw = new BranchWorkflow(subBwIn, null);
                    subBranches.add(new Branch(bb.getName(), bw));
                }
                if (subBranches.size() == 1) {
                    for (Instruction inst : subBranches.get(0).getWorkflow().getInstructions()) {
                        bwIn.add(inst);
                    }
                } else {
                    subForkJoin.setBranches(subBranches);
                    bwIn.add(subForkJoin);
                }

                if (closingJobs != null) {
                    for (JobStreamClosingJob jcj : closingJobs) {
                        addNamedInstruction(jcj.getJob(), bwIn, workflowPath, workflowName);
                        closingJobs = convertJobChilds(method + "-closingjobs-nested", result, allConvertedJobStreamsJobs, allJobs, workflowPath,
                                workflowName, bwIn, bwIn, jcj.getJob());

                        if (closingJobs != null && closingJobs.size() > 0) {
                            add2nestedClosingJobs(closingJobs);
                            handleNestedClosingJobs(method + "-closingjobs-nested", result, allConvertedJobStreamsJobs, allJobs, workflowPath,
                                    workflowName, bwIn, bwIn);
                            // TODO
                            LOGGER.debug("[CLOSING][NESTED]" + closingJobs);
                        }
                    }
                }
                closingJobs = null;

                BranchWorkflow bw = new BranchWorkflow(bwIn, null);
                branches.add(new Branch(b.getName(), bw));

            } else if (b.getChildJob() != null) {
                addNamedInstruction(b.getChildJob(), bwIn, workflowPath, workflowName);

                closingJobs = convertJobChilds(method + "-childJob", result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in,
                        bwIn, b.getChildJob());
                if (closingJobs != null && closingJobs.size() > 0) {
                    add2nestedClosingJobs(closingJobs);
                }

                BranchWorkflow bw = new BranchWorkflow(bwIn, null);
                branches.add(new Branch(b.getName(), bw));

                LOGGER.debug("[CLOSING][CHILD_JOB][" + b.getChildJob() + "]" + closingJobs);
            }
            LOGGER.debug(String.format("[%s][%s][%s][branch]%s", method, workflowName, js1js7Job.getJS1Job().getName(), b));

            handleNestedClosingJobs(method + "-branches", result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, bwIn);
        }

        switch (branches.size()) {
        case 1:
            for (Instruction inst : branches.get(0).getWorkflow().getInstructions()) {
                in.add(inst);
            }
            break;
        default:
            forkJoin.setBranches(branches);
            in.add(forkJoin);

            break;
        }

        handleNestedClosingJobs(method + "-end", result, allConvertedJobStreamsJobs, allJobs, workflowPath, workflowName, in, in);

        LOGGER.debug(String.format("[%s][%s][%s]end-", method, workflowName, js1js7Job.getJS1Job().getName()));

        return null;
    }

    private JobStreamFindChildsResult findChildsPreview(Map<String, JobStreamJS1JS7Job> allConvertedJobStreamsJobs, List<JobStreamJob> allJobs,
            String workflowName, JobStreamJS1JS7Job js1js7Job) {
        String method = "findChildsPreview";

        JobStreamFindChildsResult result = new JobStreamFindChildsResult();
        List<JobStreamJob> allJobs2 = allConvertedJobStreamsJobs.entrySet().stream().map(e -> e.getValue().getJS1JobStreamJob()).collect(Collectors
                .toList());

        for (JobStreamJob jobStreamJob : allJobs2) {
            if (jobStreamJob.getInconditions() != null && jobStreamJob.getInconditions().size() > 0) {
                for (InCondition in : jobStreamJob.getInconditions()) {
                    if (in.getConditionExpression() != null && in.getConditionExpression().getExpression() != null) {
                        Conditions conditions = new Conditions();
                        try {
                            String expr = in.getConditionExpression().getExpression().trim();

                            List<Object> allConditionParts = conditions.parse(expr);
                            List<Condition> allConditions = Conditions.getConditions(allConditionParts);

                            List<Condition> conditionParts = new ArrayList<>();
                            for (Condition c : allConditions) {
                                if (c.getType().equals(ConditionType.EVENT)) {
                                    boolean cf = allConvertedJobStreamsJobs.entrySet().stream().filter(j -> JS7ConverterHelper.getFileName(j.getKey())
                                            .equals(c.getName())).count() > 0;
                                    if (cf) {
                                        conditionParts.add(c);
                                    } else {
                                        String k = jobStreamJob.getJob() + "-" + c.getName();
                                        if (!notFoundInConditionJobs.contains(k)) {
                                            ConverterReport.INSTANCE.addWarningRecord("Job[" + jobStreamJob.getJob()
                                                    + "]conditionExpression JOB not found", c.getName());
                                            notFoundInConditionJobs.add(k);
                                        }
                                    }
                                }
                            }

                            for (Condition c : conditionParts) {
                                if (c.getName().equals(js1js7Job.getJS1Job().getName())) {
                                    JobStreamJS1JS7Job job = allConvertedJobStreamsJobs.get(jobStreamJob.getJob());
                                    if (job != null) {
                                        result.getJobs().add(job);
                                    }
                                }
                            }

                        } catch (Exception e) {
                            ConverterReport.INSTANCE.addErrorRecord(js1js7Job.getJS1Job().getPath(), method + "=" + jobStreamJob.getJob(), e);
                        }
                    }
                }
            }
        }

        LOGGER.debug(String.format("[%s][%s][child=%s][child jobs]%s", method, workflowName, js1js7Job.getJS1Job().getName(), result));
        return result;
    }

    private JobStreamJS1JS7Job getJob(Map<String, JobStreamJS1JS7Job> allConvertedJobStreamsJobs, String jobName) {
        return allConvertedJobStreamsJobs.entrySet().stream().filter(j -> JS7ConverterHelper.getFileName(j.getKey()).equals(jobName)).map(j -> {
            return j.getValue();
        }).findFirst().orElse(null);
    }

    private JobStreamFindChildsResult findChilds(Map<String, JobStreamJS1JS7Job> allConvertedJobStreamsJobs, List<JobStreamJob> allJobs,
            String workflowName, JobStreamJS1JS7Job js1js7Job) {
        String method = "findChilds";
        JobStreamFindChildsResult result = new JobStreamFindChildsResult();

        LOGGER.trace(String.format("[%s][%s][%s]...", method, workflowName, js1js7Job.getJS1Job().getName()));

        List<JobStreamJob> toRemove = new ArrayList<>();
        for (JobStreamJob jobStreamJob : allJobs) {
            if (jobStreamJob.getInconditions() != null && jobStreamJob.getInconditions().size() > 0) {
                for (InCondition in : jobStreamJob.getInconditions()) {
                    if (in.getConditionExpression() != null && in.getConditionExpression().getExpression() != null) {
                        Conditions conditions = new Conditions();
                        try {
                            String expr = in.getConditionExpression().getExpression().trim();

                            List<Object> allConditionParts = conditions.parse(expr);
                            List<Condition> allConditions = Conditions.getConditions(allConditionParts);

                            List<Condition> conditionParts = new ArrayList<>();
                            for (Condition c : allConditions) {
                                if (c.getType().equals(ConditionType.EVENT)) {
                                    boolean cf = allConvertedJobStreamsJobs.entrySet().stream().filter(j -> JS7ConverterHelper.getFileName(j.getKey())
                                            .equals(c.getName())).count() > 0;
                                    if (cf) {
                                        conditionParts.add(c);
                                    } else {
                                        String k = jobStreamJob.getJob() + "-" + c.getName();
                                        if (!notFoundInConditionJobs.contains(k)) {
                                            ConverterReport.INSTANCE.addWarningRecord("Job[" + jobStreamJob.getJob()
                                                    + "]conditionExpression JOB not found", c.getName());
                                            notFoundInConditionJobs.add(k);
                                        }
                                    }
                                }
                            }
                            switch (conditionParts.size()) {
                            case 0:
                                LOGGER.debug(String.format("[%s][%s][%s][%s][expr=%s]0 conditions", method, workflowName, js1js7Job.getJS1Job()
                                        .getName(), jobStreamJob.getJob(), expr));

                                toRemove.add(jobStreamJob);
                                // TODO report
                                break;
                            case 1:
                                if (conditions.hasJob(conditionParts, js1js7Job.getJS1Job())) {
                                    JobStreamJS1JS7Job job = allConvertedJobStreamsJobs.get(jobStreamJob.getJob());
                                    if (job != null) {
                                        result.getJobs().add(job);
                                    }
                                    toRemove.add(jobStreamJob);

                                    LOGGER.debug(String.format("[%s][%s][%s][%s][expr=%s][hasJob]found", method, workflowName, js1js7Job.getJS1Job()
                                            .getName(), jobStreamJob.getJob(), expr));
                                    // break js;
                                } else {

                                    LOGGER.trace(String.format("[%s][%s][%s][%s][expr=%s][hasJob][skip]not found", method, workflowName, js1js7Job
                                            .getJS1Job().getName(), jobStreamJob.getJob(), expr));
                                }
                                break;
                            default:
                                if (conditions.hasAllJobs(conditionParts, added)) {
                                    if (notResolvedConditions.containsKey(jobStreamJob.getJob())) {
                                        notResolvedConditions.remove(jobStreamJob.getJob());
                                    }

                                    JobStreamJS1JS7Job job = allConvertedJobStreamsJobs.get(jobStreamJob.getJob());
                                    if (job != null) {
                                        List<JobStreamJS1JS7Job> usedJobs = conditions.getAllJobs(conditionParts, added);
                                        // result.getJobs().add(js1JobChild);

                                        JobStreamClosingJob jscj = new JobStreamClosingJob(job, usedJobs);
                                        result.getClosingJobs().add(jscj);
                                    }
                                    toRemove.add(jobStreamJob);

                                    LOGGER.debug(String.format("[%s][%s][%s][%s][expr=%s][hasAllJobs]all jobs found", method, workflowName, js1js7Job
                                            .getJS1Job().getName(), jobStreamJob.getJob(), expr));
                                } else {
                                    notResolvedConditions.put(jobStreamJob.getJob(), new JobStreamConditionsHelper(js1js7Job, conditions,
                                            conditionParts));
                                    String msg = String.format("[%s][%s][%s][%s][expr=%s][hasAllJobs][skip]not all jobs found", method, workflowName,
                                            js1js7Job.getJS1Job().getName(), jobStreamJob.getJob(), expr);

                                    String tmpExpr = " " + expr + " ";// job1 -> job10
                                    if (tmpExpr.contains(" " + js1js7Job.getJS1Job().getName() + " ")) {
                                        LOGGER.debug(msg);
                                    } else {
                                        LOGGER.trace(msg);
                                    }
                                }

                                break;
                            }
                        } catch (Exception e) {
                            ConverterReport.INSTANCE.addErrorRecord(js1js7Job.getJS1Job().getPath(), method + "=" + jobStreamJob.getJob(), e);
                        }
                    }
                }
            }
        }
        if (toRemove.size() > 0) {
            allJobs.removeAll(toRemove);
        }

        LOGGER.trace(String.format("[%s][%s][%s]end", method, workflowName, js1js7Job.getJS1Job().getName()));

        return result;
    }

    private void add2nestedClosingJobs(List<JobStreamClosingJob> l) {
        if (l != null && l.size() > 0) {
            for (JobStreamClosingJob j : l) {
                add2nestedClosingJobs(j);
            }
        }
    }

    private void add2nestedClosingJobs(JobStreamClosingJob j) {
        boolean fd = nestedClosingJobs.stream().filter(f -> f.getJob().getJS7JobName().equals(j.getJob().getJS7JobName())).count() > 0;
        if (!fd) {
            nestedClosingJobs.add(j);
        }
    }

    private Path getWorkflowPath(JS7ConverterResult result, JobStream js1JobStream) {
        Path firstStarterFirstJob = Paths.get(js1JobStream.getJobstreamStarters().get(0).getJobs().get(0).getJob());
        Path js7Path = JS7ConverterHelper.getJS7ObjectPath(firstStarterFirstJob.getParent());

        StringBuilder sb = new StringBuilder();
        return js7Path.resolve(sb.append(js7Converter.getUniqueWorkflowName(JS7ConverterHelper.getJS7ObjectName(js7Path, js1JobStream.getJobStream()))
                .toString()) + ".workflow.json");
    }

    public static Path getJobPath(Path live, JobStreamJob job) {
        String pathRel = job.getJob();
        if (pathRel.startsWith("/") || pathRel.startsWith("\\")) {
            pathRel = pathRel.substring(1);
        }
        return live.resolve(pathRel + EConfigFileExtensions.JOB.extension()).normalize();
    }

}
