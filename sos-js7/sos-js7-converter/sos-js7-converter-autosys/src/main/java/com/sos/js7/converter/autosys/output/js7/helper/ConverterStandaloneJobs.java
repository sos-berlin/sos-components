package com.sos.js7.converter.autosys.output.js7.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonMachineJob;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.WorkflowResult;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterResult;

public class ConverterStandaloneJobs {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterStandaloneJobs.class);

    private final Autosys2JS7Converter converter;
    private final JS7ConverterResult result;

    public ConverterStandaloneJobs(Autosys2JS7Converter converter, JS7ConverterResult result) {
        this.converter = converter;
        this.result = result;
    }

    public void convert(List<ACommonJob> standaloneJobs) {
        String method = "convert";
        int size = standaloneJobs.size();

        LOGGER.info(String.format("[%s][standalone][jobs=%s][start]...", method, size));
        for (ACommonJob j : standaloneJobs) {
            convertStandalone(result, j);
        }
        LOGGER.info(String.format("[%s][standalone][jobs=%s][end]", method, size));
    }

    private void convertStandalone(JS7ConverterResult result, ACommonJob jilJob) {
        WorkflowResult wr = convertStandaloneWorkflow(result, converter.getAnalyzer(), jilJob);

        if (!jilJob.hasRunTime()) {
            jilJob.getRunTime().setTimezone(wr.getTimezone());
            jilJob.getRunTime().setStartTimes("00:00");
        }
        converter.convertSchedule(result, wr, jilJob);
    }

    private WorkflowResult convertStandaloneWorkflow(JS7ConverterResult result, AutosysAnalyzer analyzer, ACommonJob jilJob) {
        String runTimeTimezone = jilJob.getRunTime().getTimezone().getValue();

        converter.getAnalyzer().getConditionAnalyzer().handleStandaloneJobConditions(analyzer, jilJob);

        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle(jilJob.getDescription().getValue()));
        w.setTimeZone(runTimeTimezone == null ? Autosys2JS7Converter.CONFIG.getWorkflowConfig().getDefaultTimeZone() : runTimeTimezone);

        WorkflowResult wr = new WorkflowResult();
        wr.setName(JS7ConverterHelper.getJS7ObjectName(jilJob.getName()));
        wr.setPath(PathResolver.getJS7WorkflowPath(jilJob, wr.getName()));
        wr.setTimezone(w.getTimeZone(), runTimeTimezone != null);
        wr.setAutosysRunWindow(jilJob.hasRunTime() && jilJob.getRunTime().hasRunWindow());
        // LOGGER.info("[convertStandalone]" + wr.getPath());

        // WORKFLOW JOBS
        Jobs js = new Jobs();
        js.setAdditionalProperty(wr.getName(), converter.getJob(result, (ACommonMachineJob) jilJob));
        w.setJobs(js);

        // INSTRUCTIONS
        List<Instruction> in = new ArrayList<>();
        BoardExpectConsumHelper nh = BoardHelper.expectNotices(converter.getAnalyzer(), jilJob);
        if (nh != null) {
            ExpectNotices en = nh.toExpectNotices();
            if (en != null) {
                in.add(en);
            }
        }

        BoardTryCatchHelper btch = new BoardTryCatchHelper(jilJob, converter.getAnalyzer(), nh);

        // always Try Catch
        TryCatch tryCatch = new TryCatch();
        // ##################################
        // 1) Try
        // ##################################
        List<Instruction> tryInstructions = new ArrayList<>();
        // 1.1) Named Instruction
        // tryInstructions.add(Autosys2JS7Converter.getNamedJobInstruction(wr.getName()));
        // 1.2) Retry around Named Instruction
        // tryInstructions = RetryHelper.getRetryInstructions(jilJob, tryInstructions);
        tryInstructions = Autosys2JS7Converter.getCommonJobInstructions(jilJob, wr.getName());

        // 1.3) Lock around Retry Instruction
        tryInstructions = LockHelper.getLockInstructions(converter.getAnalyzer(), wr, jilJob, tryInstructions);
        // 1.4) PostNoticeToItSelf
        PostNotices postNoticeToItSelf = AdditionalInstructionsHelper.tryCreatePostNoticeToStandaloneWorkflowItSelf(converter.getAnalyzer(), wr,
                jilJob);
        // 1.5) Cyclic around all previous instructions
        tryInstructions = RunTimeHelper.getCyclicWorkflowInstructions(wr, jilJob, tryInstructions, btch, postNoticeToItSelf);
        // 1.6)
        wr.addPostNotices(btch); // after cyclic
        if (postNoticeToItSelf != null) {
            wr.addPostNotices(postNoticeToItSelf, 0);
        }

        tryInstructions = AdditionalInstructionsHelper.consumeNoticesIfExists(analyzer, wr, tryInstructions);

        if (btch.getTryPostNotices() != null) {
            if (wr.isCycle()) {
                tryInstructions.add(btch.getTryPostNotices());
            } else {
                tryInstructions.add(BoardHelper.mergePostNoticesSecondAsFirst(btch.getTryPostNotices(), postNoticeToItSelf));
            }
        } else {
            if (!wr.isCycle()) {
                if (postNoticeToItSelf != null) {
                    tryInstructions.add(postNoticeToItSelf);
                }
            }
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
        catchInstructions.add(new Finish("'job terminates with return code: ' ++ $returnCode", true));
        tryCatch.setCatch(new Instructions(catchInstructions));

        // ##################################
        // 3) add TryCatch
        // ##################################
        in.add(tryCatch);

        if (postNoticeToItSelf != null) {
            String postNoticeToBoxSelfName = "'" + postNoticeToItSelf.getNoticeBoardNames().get(0) + "'";
            if (AdditionalInstructionsHelper.WORKFLOW_ITSELF_BOARDS_CREATE_AS_SEPARATE_EXPECT_NOTICE) {
                ExpectNotices en = new ExpectNotices();
                en.setNoticeBoardNames(postNoticeToBoxSelfName);
                in.add(0, en);
            } else {
                Instruction firstInstruction = in.get(0);
                if (firstInstruction != null) {
                    if (firstInstruction instanceof ExpectNotices) {
                        ExpectNotices en = ((ExpectNotices) firstInstruction);
                        en.setNoticeBoardNames(postNoticeToBoxSelfName + " && " + en.getNoticeBoardNames());
                    } else {
                        ExpectNotices en = new ExpectNotices();
                        en.setNoticeBoardNames(postNoticeToBoxSelfName);
                        in.add(0, en);
                    }
                }
            }
        }

        w.setInstructions(in);

        // w = AdditionalInstructionsHelper.consumeNoticesIfExists(wr, w, btch);

        result.add(wr.getPath(), w, jilJob.isReference());
        return wr;
    }

}
