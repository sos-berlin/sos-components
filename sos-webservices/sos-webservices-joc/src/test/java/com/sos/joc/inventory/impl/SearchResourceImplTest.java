package com.sos.joc.inventory.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.Job;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.UnitTestSimpleWSImplHelper;

public class SearchResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResourceImplTest.class);

    @Ignore
    @Test
    public void testPostSearch() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new SearchResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.oracle-12c.xml");
        try {
            h.init();

            h.post("postSearch", Paths.get("src/test/resources/ws/inventory/impl/request-SearchResourceImpl-postSearch.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testGenerateWorkflow() throws Exception {
        Jobs jobs = new Jobs();
        for (int i = 1; i <= 50; i++) {
            Job j = new Job();
            j.setTitle("job_" + i + "_" + UUID.randomUUID());
            j.setAgentName("standaloneAgent");
            j.setExecutable(getExecutable(j));

            jobs.getAdditionalProperties().put(j.getTitle(), j);
        }

        List<Instruction> in = new ArrayList<>();
        jobs.getAdditionalProperties().keySet().forEach(jobName -> {
            NamedJob nj = new NamedJob(jobName);
            nj.setLabel(nj.getJobName());
            in.add(nj);
        });
        Workflow w = new Workflow();
        w.setJobs(jobs);
        w.setInstructions(in);
        String r = Globals.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(w);
        LOGGER.info(r);
        Path output = Paths.get("src/test/resources/output/generated_workflow.json").toAbsolutePath();
        if (!Files.exists(output.getParent())) {
            Files.createDirectories(output.getParent());
        }
        SOSPath.overwrite(output, r);
    }

    private ExecutableScript getExecutable(Job j) {
        ExecutableScript es = new ExecutableScript();
        es.setScript(getScript(j));
        return es;
    }

    private String getScript(Job j) {
        StringBuilder sb = new StringBuilder();
        sb.append("echo ").append(j.getTitle()).append("-START-----------------------------------------\n");
        for (int i = 1; i <= 20; i++) {
            sb.append("echo ").append(j.getTitle()).append("-LINE-").append(i).append("----------------------------------------\n");
        }
        sb.append("echo ").append(j.getTitle()).append("-END-----------------------------------------");
        return sb.toString();
    }
}
