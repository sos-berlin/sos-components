package com.sos.joc.classes.inventory;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sos.sign.model.job.JobReturnCode;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.sign.model.job.ExecutableScript;


public class ConverterTest {

    @Test
    public void testReturnCodeWarnings() {
        try {
            List<String> jsons = Arrays.asList(
                    "{\"TYPE\": \"Workflow\",\"jobs\":{\"job1\":{\"executable\":{\"TYPE\": \"ShellScriptExecutable\", \"returnCodeMeaning\": {\"warning\": [1,2]}}}}}",
                    "{\"TYPE\": \"Workflow\",\"jobs\":{\"job1\":{\"executable\":{\"TYPE\": \"ShellScriptExecutable\", \"returnCodeMeaning\": {\"success\": [1,3],\"warning\": [1,2]}}}}}",
                    "{\"TYPE\": \"Workflow\",\"jobs\":{\"job1\":{\"executable\":{\"TYPE\": \"ShellScriptExecutable\", \"returnCodeMeaning\": {\"failure\": [1,3],\"warning\": [1,2]}}}}}");
            List<JobReturnCode> results = Arrays.asList(new JobReturnCode(Arrays.asList(0, 1, 2), null), new JobReturnCode(Arrays.asList(1, 2, 3),
                    null), new JobReturnCode(null, Arrays.asList(3)));
            for (int i = 0; i < jsons.size(); i++) {
                com.sos.sign.model.workflow.Workflow signWorkflow = Globals.objectMapper.readValue(jsons.get(i),
                        com.sos.sign.model.workflow.Workflow.class);
                Workflow invWorkflow = Globals.objectMapper.readValue(jsons.get(i), Workflow.class);
                JsonConverter.considerReturnCodeWarnings(invWorkflow.getJobs(), signWorkflow.getJobs());
                // System.out.println(Globals.prettyPrintObjectMapper.writeValueAsString(signWorkflow));
                ExecutableScript es = signWorkflow.getJobs().getAdditionalProperties().get("job1").getExecutable().cast();
                assertTrue(es.getReturnCodeMeaning().equals(results.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
