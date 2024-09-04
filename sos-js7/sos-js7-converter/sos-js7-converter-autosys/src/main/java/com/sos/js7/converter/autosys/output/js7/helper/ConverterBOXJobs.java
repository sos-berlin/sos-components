package com.sos.js7.converter.autosys.output.js7.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.BranchWorkflow;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.ParameterType;
import com.sos.inventory.model.workflow.Parameters;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.JobFW;
import com.sos.js7.converter.autosys.input.AFileParser.BoxJobsHandler;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.WorkflowResult;
import com.sos.js7.converter.autosys.output.js7.helper.fork.BOXJobHelper;
import com.sos.js7.converter.autosys.output.js7.helper.fork.BOXJobsHelper;
import com.sos.js7.converter.autosys.output.js7.helper.fork.BranchHelper;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;
import com.sos.js7.converter.commons.config.json.JS7Agent;
import com.sos.js7.converter.commons.report.ConverterReport;

public class ConverterBOXJobs {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterBOXJobs.class);

    public static Map<String, List<BOXJobHelper>> USED_JOBS_PER_BOX = new HashMap<>();

    private final Autosys2JS7Converter converter;
    private final JS7ConverterResult result;

    public static void clear() {
        USED_JOBS_PER_BOX.clear();
    }

    public ConverterBOXJobs(Autosys2JS7Converter converter, JS7ConverterResult result) {
        this.converter = converter;
        this.result = result;
    }

    public void convert(List<ACommonJob> boxJobs) {
        String method = "convert";
        int size = boxJobs.size();
        if (size > 0) {
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s][start]...", method, size));
            for (ACommonJob j : boxJobs) {
                try {
                    convertBoxWorkflow((JobBOX) j);
                } catch (StackOverflowError e) {
                    String msg = "[convert][StackOverflowError]box=" + j;
                    LOGGER.error(msg);
                }
            }
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s][end]", method, size));
        } else {
            LOGGER.info(String.format("[%s][workflow][BOX main jobs=%s]skip", method, size));
        }

    }

    private String getBOXTimezone(JobBOX box) {
        String timezone = box.getRunTime().getTimezone().getValue();
        Set<String> childrenJobsTimezones = box.getJobs().stream().filter(j -> !SOSString.isEmpty(j.getRunTime().getTimezone().getValue())).map(j -> j
                .getRunTime().getTimezone().getValue()).collect(Collectors.toSet());
        String oneOfTheChildrenTimeZone = null;
        if (childrenJobsTimezones.size() > 0) {
            oneOfTheChildrenTimeZone = childrenJobsTimezones.iterator().next();
        }
        if (timezone == null && oneOfTheChildrenTimeZone != null) {
            timezone = oneOfTheChildrenTimeZone;
        }
        Report.writeJobReportJobsBoxByRuntimeTimezoneChildrenJobs(converter.getAnalyzer().getReportDir(), box, childrenJobsTimezones);
        return timezone;
    }

    private void convertBoxWorkflow(JobBOX box) {
        if (box.getJobs() == null) {
            return;
        }
        int size = box.getJobs().size();
        if (size == 0) {
            return;
        }
        List<ACommonJob> fileWatchers = box.getJobs().stream().filter(j -> j instanceof JobFW).collect(Collectors.toList());
        if (fileWatchers.size() > 0) {
            box.getJobs().removeAll(fileWatchers);
        }

        String boxRunTimeTimezone = getBOXTimezone(box);

        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle(box.getDescription().getValue()));
        w.setTimeZone(boxRunTimeTimezone == null ? Autosys2JS7Converter.CONFIG.getWorkflowConfig().getDefaultTimeZone() : boxRunTimeTimezone);

        WorkflowResult wr = new WorkflowResult();
        wr.setName(JS7ConverterHelper.getJS7ObjectName(box.getName()));
        wr.setPath(PathResolver.getJS7WorkflowPath(box, wr.getName()));
        wr.setTimezone(w.getTimeZone(), boxRunTimeTimezone != null);
        // LOGGER.info("[convertBoxWorkflow]" + wr.getPath());

        // WORKFLOW JOBS
        Jobs jobs = new Jobs();
        for (ACommonJob j : box.getJobs()) {
            // String jn = normalizeName(result, j, j.getName());
            String jn = JS7ConverterHelper.getJS7ObjectName(j.getName());
            if (j instanceof JobCMD) {
                jobs.setAdditionalProperty(jn, converter.getJob(result, (JobCMD) j));
            } else {
                String add = "[not impemented yet]type=" + j.getConverterJobType();
                if (ConverterJobType.BOX.equals(j.getConverterJobType())) {
                    add = "[nested BOX detected]type=" + j.getConverterJobType();
                }
                ConverterReport.INSTANCE.addErrorRecord("[convertBoxWorkflow][box=" + wr.getName() + "][job=" + jn + "]" + add);
            }
        }
        w.setJobs(jobs);

        // INSTRUCTIONS
        List<Instruction> in = new ArrayList<>();
        BoardExpectConsumHelper nh = BoardHelper.expectNotices(converter.getAnalyzer(), box);
        if (nh != null) {
            ExpectNotices en = nh.toExpectNotices();
            if (en != null) {
                in.add(en);
            }
            nh.removeConditionsFromChildrenJobs(box);
        }

        BoardTryCatchHelper btch = new BoardTryCatchHelper(box, converter.getAnalyzer(), nh);
        // always Try Catch
        TryCatch tryCatch = new TryCatch();
        // ##################################
        // 1) Try
        // ##################################
        List<Instruction> tryInstructions = new ArrayList<>();

        if (size == 1) {

            BOXJobHelper bh = new BOXJobHelper(box, box.getJobs().get(0), null);

            ACommonJob bj = bh.getJob();
            bh.isUsed(true);

            if (nh == null) {// BOX itself not have condition
                btch = new BoardTryCatchHelper(box, converter.getAnalyzer(), nh);
                nh = BoardHelper.expectNotices(converter.getAnalyzer(), bj);
                if (nh != null) {
                    ExpectNotices en = nh.toExpectNotices();
                    if (en != null) {
                        in.add(en);
                    }
                    nh.removeConditionsFromChildrenJobs(box);
                }
            }

            // 1.1) Named Instruction
            tryInstructions.add(Autosys2JS7Converter.getNamedJobInstruction(JS7ConverterHelper.getJS7ObjectName(bj.getName())));
            // 2.2) Retry around Named Instruction
            tryInstructions = RetryHelper.getRetryInstructions(bj, tryInstructions);
        } else {
            List<Instruction> bin = new ArrayList<>();

            List<BOXJobHelper> firstLevelChildren = BOXJobsHelper.getFirstLevelChildren(box);
            if (nh == null && firstLevelChildren.size() > 0) {// BOX itself not have condition
                btch = new BoardTryCatchHelper(box, converter.getAnalyzer(), nh);
                // TODO not easy from the first job ... from first
                // for(BOXJobHelper jh : firstLevelChildren) {
                // jh.
                // }
                nh = BoardHelper.expectNotices(converter.getAnalyzer(), firstLevelChildren.get(0).getJob());
                if (nh != null) {
                    ExpectNotices en = nh.toExpectNotices();
                    if (en != null) {
                        in.add(en);
                    }
                    nh.removeConditionsFromChildrenJobs(box);
                }
            }

            //LOGGER.info("SSSSS[" + box.getName() + "][" + firstLevelChildren.size() + "]" + firstLevelChildren);
            tryInstructions.addAll(getInstructions(box, box, firstLevelChildren, bin));

            //LOGGER.info("SSS2=" + BOXJobsHelper.CLOSING_BOX_JOB_HELPERS);

        }
        // 1.3) Lock around Retry Instruction ???
        tryInstructions = LockHelper.getLockInstructions(box, tryInstructions);
        // 1.4) Cyclic around all previous instructions
        tryInstructions = RunTimeHelper.getCyclicWorkflowInstructions(box, tryInstructions, btch);
        if (btch.getTryPostNotices() != null) {
            tryInstructions.add(btch.getTryPostNotices());
        }

        Instructions inst;
        ConsumeNotices cn = btch.getConsumeNotices();
        if (cn != null) {
            cn.setSubworkflow(new Instructions(tryInstructions));
            inst = new Instructions(Collections.singletonList(cn));
        } else {
            inst = new Instructions(tryInstructions);
        }
        tryCatch.setTry(inst);
        // ##################################
        // 2) Catch
        // ##################################
        List<Instruction> catchInstructions = new ArrayList<>();
        if (btch.getCatchPostNotices() != null) {
            catchInstructions.add(btch.getCatchPostNotices());
        }
        catchInstructions.add(new Finish("'workflow terminates with return code: ' ++ $returnCode", true));
        tryCatch.setCatch(new Instructions(catchInstructions));

        // ##################################
        // 3) add TryCatch
        // ##################################
        in.add(tryCatch);

        w.setInstructions(in);
        w = setWorkflowOrderPreparation(w, fileWatchers);
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

        Report.writeJS7BOXReport(converter.getAnalyzer().getReportDir(), box, w);

        converter.convertSchedule(result, wr, box);
    }

    private List<Instruction> getInstructions(JobBOX box, ACommonJob j, List<BOXJobHelper> children, List<Instruction> in) {
        switch (children.size()) {
        case 0:
            break;
        case 1:
            BOXJobHelper bjh = children.get(0);
            ACommonJob bj = bjh.getJob();
            bjh.isUsed(true);

            BoardExpectConsumHelper nh = BoardHelper.expectNotices(converter.getAnalyzer(), bj);
            ConsumeNotices cn = null;
            boolean setNotices = false;

            if (setNotices && nh != null) {
                ExpectNotices en = nh.toExpectNotices();
                if (en != null) {
                    in.add(en);
                }
                cn = nh.toConsumeNotices();
            }
            BoardTryCatchHelper btch = new BoardTryCatchHelper(bj, converter.getAnalyzer(), nh);

            in.addAll(Autosys2JS7Converter.getCommonJobInstructions(bj, JS7ConverterHelper.getJS7ObjectName(bj.getName())));

            if (btch.getTryPostNotices() != null) {
                in.add(btch.getTryPostNotices());
            }

            BranchHelper bh = BOXJobsHelper.getJobChildren(box, bj);
            in = getInstructions(box, bj, bh.getChildrenJobs(), in);

            // TODO check !!!!
            List<BOXJobHelper> oj = bh.getClosingJobs().stream().filter(x -> !x.isUsed()).collect(Collectors.toList());
            if (oj.size() > 0) {
                in = getInstructions(box, bj, oj, in);
            }

            if (cn != null) {
                cn.setSubworkflow(new Instructions(in));
                in = Collections.singletonList(cn);
            }

            break;
        default:
            in = createForkJoin(box, j, children, in);
            break;
        }
        return in;
    }

    private List<Instruction> createForkJoin(JobBOX box, ACommonJob currentJob, List<BOXJobHelper> children, List<Instruction> in) {
        List<Branch> branches = new ArrayList<>();
        Boolean joinIfFailed = false;

        int i = 1;
        List<BranchHelper> bhToClose = new ArrayList<>();
        for (BOXJobHelper bjh : children) {
            ACommonJob bj = bjh.getJob();
            bjh.isUsed(true);

            List<Instruction> bwIn = new ArrayList<>();

            // JOB Expect/Consume notices
            BoardExpectConsumHelper nh = BoardHelper.expectNotices(converter.getAnalyzer(), bj);
            if (nh != null) {
                ExpectNotices en = nh.toExpectNotices();
                if (en != null) {
                    bwIn.add(en);
                }
            }

            // JOB Instructions
            bwIn.addAll(Autosys2JS7Converter.getCommonJobInstructions(bj, JS7ConverterHelper.getJS7ObjectName(bj.getName())));
            BranchHelper bh = BOXJobsHelper.getJobChildren(box, bj);
            if (bh.getClosingJobs().size() > 0) {
                bhToClose.add(bh);
            }
            bwIn = getInstructions(box, bj, bh.getChildrenJobs(), bwIn);

            BoardTryCatchHelper btch = new BoardTryCatchHelper(bj, converter.getAnalyzer(), nh);
            if (btch.getTryPostNotices() != null) {
                bwIn.add(btch.getTryPostNotices());
            }

            // JOB Expect/Consume notices
            ConsumeNotices cn = btch.getConsumeNotices();
            if (cn != null) {
                cn.setSubworkflow(new Instructions(bwIn));
                bwIn = Collections.singletonList(cn);
            }

            // TODO catch, done

            Branch branch = new Branch("branch_" + i, new BranchWorkflow(bwIn, null));
            bh.setBranch(branch);

            branches.add(branch);
            i++;
        }
        in.add(new ForkJoin(branches, joinIfFailed));

        if (bhToClose.size() > 0) {
            for (BranchHelper bh : bhToClose) {
                for (BOXJobHelper bjh : bh.getClosingJobs()) {
                    if (bjh.isUsed()) {
                        continue;
                    }
                    if (!bjh.allThisBoxConditionsCompleted()) {
                        continue;
                    }

                    ACommonJob j = bjh.getJob();
                    in.addAll(Autosys2JS7Converter.getCommonJobInstructions(j, JS7ConverterHelper.getJS7ObjectName(j.getName())));
                    bjh.isUsed(true);

                    BranchHelper bh2 = BOXJobsHelper.getJobChildren(box, j);
                    // LOGGER.info("AAAAA="+j+"="+bh2.getChildrenJobs());
                    in = getInstructions(box, j, bh2.getChildrenJobs(), in);
                }
            }
        }

        return in;
    }

    private Workflow setWorkflowOrderPreparation(Workflow w, List<ACommonJob> fileWatchers) {
        if (fileWatchers.size() > 0) {
            Parameter p = new Parameter();
            p.setType(ParameterType.String);
            p.setDefault("${file}");

            Parameters ps = new Parameters();
            ps.getAdditionalProperties().put("file", p);

            w.setOrderPreparation(new Requirements(ps, false));
        }
        return w;
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
                fos.setTimeZone(wr.getTimezone());
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

}
