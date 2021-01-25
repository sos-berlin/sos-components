package com.sos.joc.classes.inventory.search;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;

public class WorkflowConverterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowConverterTest.class);
    private static final Path WORKFLOW_FILE = Paths.get("src/test/resources/workflow.json");

    @Ignore
    @Test
    public void testHashCode() throws Exception {
        int hashCodeCase = 1;

        Workflow w = (Workflow) Globals.objectMapper.readValue(getFileContent(WORKFLOW_FILE), Workflow.class);
        if (hashCodeCase == 1) {
            LOGGER.info("HASHCODE=" + w.hashCode()); // OUTPUT: -409785335
        }
        LOGGER.info("HASH=" + JocInventory.hash(w));
        LOGGER.info("---START");

        if (hashCodeCase != 1) {
            LOGGER.info("HASHCODE=" + w.hashCode()); // OUTPUT : 239951249 ???
        }
        LOGGER.info("HASH=" + JocInventory.hash(w));
    }

    @Ignore
    @Test
    public void test() throws Exception {

        Workflow w = (Workflow) Globals.objectMapper.readValue(getFileContent(WORKFLOW_FILE), Workflow.class);
        WorkflowConverter c = new WorkflowConverter();
        LOGGER.info("---START");
        c.process(w);

        LOGGER.info("JOB NAMES:" + c.getJobs().getNames().size());
        LOGGER.info("JOB TITELS:" + c.getJobs().getTitels().size());
        LOGGER.info("JOB AGENT_IDS:" + c.getJobs().getAgentIds().size());
        LOGGER.info("JOB JOB_CLASSES:" + c.getJobs().getJobClasses().size());
        LOGGER.info("JOB CRITICALITIES:" + c.getJobs().getCriticalities().size());
        LOGGER.info("JOB SCRIPTS:" + c.getJobs().getScripts().size());
        LOGGER.info("JOB ARG NAMES:" + c.getJobs().getArgNames().size());
        LOGGER.info("JOB ARG VALUES:" + c.getJobs().getArgValues().size());
        LOGGER.info("--------------------------");
        LOGGER.info("INSTR LOCK IDS:" + c.getInstructions().getLockIds().size());
        LOGGER.info("INSTR LOCKS:" + c.getInstructions().getLocks().size());
        LOGGER.info("INSTR JOB NAMES:" + c.getInstructions().getJobNames().size());
        LOGGER.info("INSTR JOB LABELS:" + c.getInstructions().getJobLabels().size());
        LOGGER.info("INSTR JOB ARG NAMES:" + c.getInstructions().getJobArgNames().size());
        LOGGER.info("JOB ARG VALUES:" + c.getInstructions().getJobArgValues().size());

        LOGGER.info(" ");
        LOGGER.info("----JSON");
        LOGGER.info(" ");

        LOGGER.info("JOB MAIN INFO:" + c.getJobs().getMainInfo());
        LOGGER.info("JOB SCRIPT INFO:" + c.getJobs().getScriptInfo());
        LOGGER.info("JOB ARG INFO:" + c.getJobs().getArgInfo());
        LOGGER.info("--------------------------");

        LOGGER.info("INSTR JOB MAIN INFO:" + c.getInstructions().getMainInfo());
        LOGGER.info("INSTR JOB ARG INFO:" + c.getInstructions().getArgInfo());

    }

    private String getFileContent(Path file) throws Exception {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
