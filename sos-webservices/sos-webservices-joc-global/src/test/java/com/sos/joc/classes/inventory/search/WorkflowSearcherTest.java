package com.sos.joc.classes.inventory.search;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.instruction.Instruction;
import com.sos.jobscheduler.model.instruction.InstructionType;
import com.sos.jobscheduler.model.instruction.NamedJob;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.search.WorkflowSearcher.WorkflowJob;

public class WorkflowSearcherTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowSearcherTest.class);

    private static final Path WORKFLOW_FILE = Paths.get("src/test/resources/workflow.json");

    @Ignore
    @Test
    public void testJobs() throws Exception {
        Workflow w = (Workflow) Globals.objectMapper.readValue(getFileContent(WORKFLOW_FILE), Workflow.class);
        WorkflowSearcher ws = new WorkflowSearcher(w);

        List<WorkflowJob> jobs = ws.getJobs();
        LOGGER.info("[getJobs()][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
        }

        jobs = ws.getJobs("job_2.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobs(job_2.*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
        }

        jobs = ws.getUnusedJobs();
        LOGGER.info(" ");
        LOGGER.info("[getUnusedJobs()][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
        }

        jobs = ws.getJobsByAgentName("/agent-.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByAgentRefPath(/agent-.*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getAgentName());
        }

        jobs = ws.getJobsByJobClass("/job_class.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByJobClass(/job_class.*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getAgentName());
        }

        jobs = ws.getJobsByScript("job_[0-9]? script.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByScript(job_[0-9]? script.*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getExecutable().getScript());
        }

        jobs = ws.getJobsByArgument("xxx");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByArgument(xxx)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getDefaultArguments().getAdditionalProperties().get("xxx"));
        }

        jobs = ws.getJobsByArgument("xxx");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByArgument(xxx)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getDefaultArguments().getAdditionalProperties().get("xxx"));
        }

        jobs = ws.getJobsByArgument("job_[0-9]_arg_[0-9].*");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByArgument(job_[0-9]_arg_[0-9].*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getDefaultArguments().getAdditionalProperties().toString());
        }

        jobs = ws.getJobsByArgumentValue("xxx");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByArgumentValue(xxx)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getDefaultArguments().getAdditionalProperties().get("xxx"));
        }

        jobs = ws.getJobsByArgumentValue("job_[0-9]_arg_[0-9].*");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByArgumentValue(job_[0-9]_arg_[0-9].*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getDefaultArguments().getAdditionalProperties().toString());
        }

        jobs = ws.getJobsByArgumentAndValue("job_[0-9]_arg_[0-9].*", "job_[0-9]_arg_[0-9].*");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByArgumentAndValue(job_[0-9]_arg_[0-9].*,job_[0-9]_arg_[0-9].*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getDefaultArguments().getAdditionalProperties().toString());
        }

        WorkflowJob job = ws.getJob("job_10");
        LOGGER.info(" ");
        LOGGER.info("[getJob(job_10)] " + SOSString.toString(job));

        job = ws.getJob("job_1");
        LOGGER.info(" ");
        LOGGER.info("[getJob(job_1)] " + SOSString.toString(job));

        String value = ws.getJobArgument("job_10", "xxx");
        LOGGER.info(" ");
        LOGGER.info("[getJobArgument(job_10,xxx)] " + value);

        value = ws.getJobArgument("job_1", "job_1_arg_1");
        LOGGER.info(" ");
        LOGGER.info("[getJobArgument(job_1,job_1_arg_1)] " + value);

        Map<String, String> values = ws.getJobArguments("job_10");
        LOGGER.info(" ");
        LOGGER.info("[getJobArguments(job_10)] " + values);

        values = ws.getJobArguments("job_1");
        LOGGER.info(" ");
        LOGGER.info("[getJobArguments(job_1)] " + values);

        values = ws.getJobArguments("job_1", ".*");
        LOGGER.info(" ");
        LOGGER.info("[getJobArguments(job_1,.*)] " + values);
    }

    @Ignore
    @Test
    public void testInstructions() throws Exception {
        Workflow w = (Workflow) Globals.objectMapper.readValue(getFileContent(WORKFLOW_FILE), Workflow.class);
        WorkflowSearcher ws = new WorkflowSearcher(w);

        List<Instruction> instructions = ws.getInstructions();
        LOGGER.info("[getInstructions()][size] " + instructions.size());
        for (Instruction i : instructions) {
            LOGGER.info("  INSTRUCTION: " + i.getTYPE());
        }

        instructions = ws.getInstructions(InstructionType.PUBLISH, InstructionType.AWAIT, InstructionType.FORK);
        LOGGER.info(" ");
        LOGGER.info("[getInstructions(PUBLISH,AWAIT,FORK)][size] " + instructions.size());
        for (Instruction i : instructions) {
            LOGGER.info("  INSTRUCTION: " + i.getTYPE());
        }

        List<NamedJob> jobs = ws.getJobInstructions();
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructions()][size] " + jobs.size());
        for (NamedJob j : jobs) {
            LOGGER.info("  JOB: " + j.getJobName());
        }

        jobs = ws.getJobInstructions("job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructions(job.*)][size] " + jobs.size());
        for (NamedJob j : jobs) {
            LOGGER.info("  JOB: " + j.getJobName());
        }

        jobs = ws.getJobInstructionsByLabel("job_10");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByLabel(job_10)][size] " + jobs.size());
        for (NamedJob j : jobs) {
            LOGGER.info("  JOB: " + j.getJobName());
            LOGGER.info("           " + j.getLabel());
        }

        jobs = ws.getJobInstructionsByLabel("job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByLabel(job.*)][size] " + jobs.size());
        for (NamedJob j : jobs) {
            LOGGER.info("  JOB: " + j.getJobName());
            LOGGER.info("           " + j.getLabel());
        }

        jobs = ws.getJobInstructionsByArgument("job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByArgument(job.*)][size] " + jobs.size());
        for (NamedJob j : jobs) {
            LOGGER.info("  JOB: " + j.getJobName());
            LOGGER.info("           " + j.getLabel());
        }

        jobs = ws.getJobInstructionsByArgumentValue("job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByArgumentValue(job.*)][size] " + jobs.size());
        for (NamedJob j : jobs) {
            LOGGER.info("  JOB: " + j.getJobName());
            LOGGER.info("           " + j.getLabel());
        }

        jobs = ws.getJobInstructionsByArgumentAndValue("job.*", "job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByArgumentAndValue(job.*,job.*)][size] " + jobs.size());
        for (NamedJob j : jobs) {
            LOGGER.info("  JOB: " + j.getJobName());
            LOGGER.info("           " + j.getLabel());
        }
    }

    private String getFileContent(Path file) throws Exception {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

}
