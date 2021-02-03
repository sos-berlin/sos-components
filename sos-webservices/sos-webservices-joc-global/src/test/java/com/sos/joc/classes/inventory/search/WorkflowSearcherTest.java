package com.sos.joc.classes.inventory.search;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.search.WorkflowSearcher.WorkflowInstruction;
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

        jobs = ws.getJobsByAgentId("agent-.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByAgentId(agent-.*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getAgentId());
        }

        jobs = ws.getJobsByJobClass("/job_class.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobsByJobClass(/job_class.*)][size] " + jobs.size());
        for (WorkflowJob j : jobs) {
            LOGGER.info("  JOB: " + j.getName());
            LOGGER.info("           " + j.getJob().getAgentId());
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

        //String value = ws.getJobArgument("job_10", "xxx");
        //LOGGER.info(" ");
        //LOGGER.info("[getJobArgument(job_10,xxx)] " + value);

        //value = ws.getJobArgument("job_1", "job_1_arg_1");
        //LOGGER.info(" ");
        //LOGGER.info("[getJobArgument(job_1,job_1_arg_1)] " + value);

        //Map<String, String> values = ws.getJobArguments("job_10");
        //LOGGER.info(" ");
        //LOGGER.info("[getJobArguments(job_10)] " + values);

        //values = ws.getJobArguments("job_1");
        //LOGGER.info(" ");
        //LOGGER.info("[getJobArguments(job_1)] " + values);

        //values = ws.getJobArguments("job_1", ".*");
        //LOGGER.info(" ");
        //LOGGER.info("[getJobArguments(job_1,.*)] " + values);
    }

    @Ignore
    @Test
    public void testInstructions() throws Exception {
        Workflow w = (Workflow) Globals.objectMapper.readValue(getFileContent(WORKFLOW_FILE), Workflow.class);
        WorkflowSearcher ws = new WorkflowSearcher(w);

        List<WorkflowInstruction<?>> instructions = ws.getInstructions();
        LOGGER.info("[getInstructions()][size] " + instructions.size());
        for (WorkflowInstruction<?> i : instructions) {
            LOGGER.info("  INSTRUCTION: " + SOSString.toString(i));
        }

        instructions = ws.getInstructions(InstructionType.PUBLISH, InstructionType.AWAIT, InstructionType.FORK);
        LOGGER.info(" ");
        LOGGER.info("[getInstructions(PUBLISH,AWAIT,FORK)][size] " + instructions.size());
        for (WorkflowInstruction<?> i : instructions) {
            LOGGER.info("  INSTRUCTION: " + SOSString.toString(i));
        }

        instructions = ws.getInstructions(InstructionType.IF, InstructionType.LOCK);
        LOGGER.info(" ");
        LOGGER.info("[getInstructions(IF,LOCK)][size] " + instructions.size());
        for (WorkflowInstruction<?> i : instructions) {
            LOGGER.info("  INSTRUCTION: " + SOSString.toString(i));
        }

        List<WorkflowInstruction<Lock>> locks = ws.getLockInstructions();
        LOGGER.info(" ");
        LOGGER.info("[getLockInstructions()][size] " + locks.size());
        for (WorkflowInstruction<Lock> l : locks) {
            LOGGER.info("  LOCK: " + l.getInstruction().getLockId());
        }

        locks = ws.getLockInstructions("lock_10");
        LOGGER.info(" ");
        LOGGER.info("[getLockInstructions(lock_10)][size] " + locks.size());
        for (WorkflowInstruction<Lock> l : locks) {
            LOGGER.info("  LOCK: " + l.getInstruction().getLockId());
        }

        // Predicate<WorkflowInstruction<Lock>> filter = isCountGreaterThan(1);
        // locks = ws.getLockInstructions(filter);
        // locks = ws.getLockInstructions(l -> l.getInstruction().getCount() != null && l.getInstruction().getCount() > 0);
        locks = ws.getLockInstructions(l -> {
            Lock lock = l.getInstruction();
            return lock.getCount() != null && lock.getCount() > 0;
        });
        LOGGER.info(" ");
        LOGGER.info("[getLockInstructions(filter)][size] " + locks.size());
        for (WorkflowInstruction<Lock> l : locks) {
            LOGGER.info("  LOCK: " + l.getInstruction().getLockId());
        }

        List<WorkflowInstruction<NamedJob>> jobs = ws.getJobInstructions();
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructions()][size] " + jobs.size());
        for (WorkflowInstruction<NamedJob> j : jobs) {
            LOGGER.info("  JOB: " + SOSString.toString(j));
        }

        jobs = ws.getJobInstructions("job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructions(job.*)][size] " + jobs.size());
        for (WorkflowInstruction<NamedJob> j : jobs) {
            LOGGER.info("  JOB: " + SOSString.toString(j));
        }

        jobs = ws.getJobInstructionsByLabel("job_10");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByLabel(job_10)][size] " + jobs.size());
        for (WorkflowInstruction<NamedJob> j : jobs) {
            LOGGER.info("  JOB: " + SOSString.toString(j));
        }

        jobs = ws.getJobInstructionsByLabel("job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByLabel(job.*)][size] " + jobs.size());
        for (WorkflowInstruction<NamedJob> j : jobs) {
            LOGGER.info("  JOB: " + SOSString.toString(j));
        }

        jobs = ws.getJobInstructionsByArgument("job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByArgument(job.*)][size] " + jobs.size());
        for (WorkflowInstruction<NamedJob> j : jobs) {
            LOGGER.info("  JOB: " + SOSString.toString(j));
        }

        jobs = ws.getJobInstructionsByArgumentValue("job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByArgumentValue(job.*)][size] " + jobs.size());
        for (WorkflowInstruction<NamedJob> j : jobs) {
            LOGGER.info("  JOB: " + SOSString.toString(j));
        }

        jobs = ws.getJobInstructionsByArgumentAndValue("job.*", "job.*");
        LOGGER.info(" ");
        LOGGER.info("[getJobInstructionsByArgumentAndValue(job.*,job.*)][size] " + jobs.size());
        for (WorkflowInstruction<NamedJob> j : jobs) {
            LOGGER.info("  JOB: " + SOSString.toString(j));
        }
    }

    public static Predicate<WorkflowInstruction<Lock>> isCountGreaterThan(int value) {
        return l -> l.getInstruction().getCount() != null && l.getInstruction().getCount() > value;
    }

    private String getFileContent(Path file) throws Exception {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

}
