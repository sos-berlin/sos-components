package com.sos.js7.converter.autosys.output.js7.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.Finish;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
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

    public void convert(List<ACommonJob> stabdaloneJobs, ConverterJobType type) {
        String method = "convert";
        int size = stabdaloneJobs.size();

        LOGGER.info(String.format("[%s][standalone][%s jobs=%s][start]...", method, type, size));
        switch (type) {
        case CMD:
            for (ACommonJob j : stabdaloneJobs) {
                convertStandalone(result, (JobCMD) j);
            }
            break;
        default:
            break;
        }
        LOGGER.info(String.format("[%s][standalone][%s jobs=%s][end]", method, type, size));
    }

    private void convertStandalone(JS7ConverterResult result, JobCMD jilJob) {
        WorkflowResult wr = convertStandaloneWorkflow(result, jilJob);

        if (!jilJob.hasRunTime()) {
            jilJob.getRunTime().setTimezone(wr.getTimezone());
            jilJob.getRunTime().setStartTimes("00:00");
        }
        converter.convertSchedule(result, wr, jilJob);
    }

    private WorkflowResult convertStandaloneWorkflow(JS7ConverterResult result, JobCMD jilJob) {
        String runTimeTimezone = jilJob.getRunTime().getTimezone().getValue();

        converter.getAnalyzer().getConditionAnalyzer().handleStandaloneJobConditions(jilJob);

        // WORKFLOW
        Workflow w = new Workflow();
        w.setTitle(JS7ConverterHelper.getJS7InventoryObjectTitle(jilJob.getDescription().getValue()));
        w.setTimeZone(runTimeTimezone == null ? Autosys2JS7Converter.CONFIG.getWorkflowConfig().getDefaultTimeZone() : runTimeTimezone);

        WorkflowResult wr = new WorkflowResult();
        wr.setName(JS7ConverterHelper.getJS7ObjectName(jilJob.getName()));
        wr.setPath(PathResolver.getJS7WorkflowPath(jilJob, wr.getName()));
        wr.setTimezone(w.getTimeZone(), runTimeTimezone != null);
        // LOGGER.info("[convertStandalone]" + wr.getPath());

        // WORKFLOW JOBS
        Jobs js = new Jobs();
        js.setAdditionalProperty(wr.getName(), converter.getJob(result, jilJob));
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
        // 1.4) Cyclic around all previous instructions
        tryInstructions = RunTimeHelper.getCyclicWorkflowInstructions(jilJob, tryInstructions, btch);
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
        catchInstructions.add(new Finish("'job terminates with return code: ' ++ $returnCode", true));
        tryCatch.setCatch(new Instructions(catchInstructions));

        // ##################################
        // 3) add TryCatch
        // ##################################
        in.add(tryCatch);

        w.setInstructions(in);
        result.add(wr.getPath(), w);
        return wr;
    }

}
