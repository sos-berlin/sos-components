package com.sos.joc.classes.inventory;

import javax.json.JsonObject;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.InventoryMeta.ConfigurationType;

public class JocInventoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocInventoryTest.class);

    @Ignore
    @Test
    public static void test1() throws Exception {

        String content =
                "{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"}],\"jobs\":{\"job\":{\"taskLimit\":1,\"executable\":{\"TYPE\":\"ExecutableScript\",\"script\":\"\"},\"returnCodeMeaning\":{\"success\":\"0\"}}}}";

        JsonObject jo = Globals.objectMapper.readValue(content, JsonObject.class);

        LOGGER.info(jo.toString());

    }

    public static void main(String[] args) throws Exception {

        String content = "{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job222\",\"label\":\"\"}]}";

        content =
                "{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job222\",\"label\":\"\"}],\"jobs\":{\"job222\":{\"taskLimit\":1,\"executable\":{\"TYPE\":\"ExecutableScript\",\"script\":\"\"},\"returnCodeMeaning\":{\"success\":\"0\"}}}}";

        content =
                "{\"instructions\":[{\"TYPE\":\"Fork\",\"branches\":[{\"id\":\"branch1\",\"workflow\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"},{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"},{\"TYPE\":\"If\",\"predicate\":\"returnCode > 0\",\"then\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"}]}}]}},{\"id\":\"branch2\",\"workflow\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"}]}},{\"id\":\"branch3\",\"workflow\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"}]}},{\"id\":\"branch4\",\"workflow\":{\"instructions\":[{\"TYPE\":\"Try\",\"catch\":{\"instructions\":[]},\"try\":{\"instructions\":[]}}]}}]}],\"jobs\":{\"job\":{\"taskLimit\":1,\"executable\":{\"TYPE\":\"ExecutableScript\",\"script\":\"\"},\"returnCodeMeaning\":{\"success\":[0]}}}}";
        content =
                "{\"instructions\":[{\"TYPE\":\"Fork\",\"branches\":[{\"id\":\"branch1\",\"workflow\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"},{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"},{\"TYPE\":\"If\",\"predicate\":\"returnCode > 0\",\"then\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"}]}}]}},{\"id\":\"branch2\",\"workflow\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"}]}},{\"id\":\"branch3\",\"workflow\":{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"}]}},{\"id\":\"branch4\",\"workflow\":{\"instructions\":[{\"TYPE\":\"Try\",\"catch\":{\"instructions\":[]},\"try\":{\"instructions\":[]}}]}}]}]}}}";

        content =
                "{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job1\",\"label\":\"mylabel\",\"defaultArguments\":{\"x\":\"x\"}}],\"jobs\":{\"job1\":{\"executable\":{\"TYPE\":\"ExecutableScript\",\"script\":\"\"},\"returnCodeMeaning\":{\"success\":\"0\",\"failure\":\"15\"},\"taskLimit\":1,\"timeout1\":\"100\",\"timeout\":0}}}";
        content =
                "{\"instructions\":[{\"TYPE\":\"Execute.Named\",\"jobName\":\"job\",\"label\":\"\"}],\"jobs\":{\"job\":{\"taskLimit\":1,\"executable\":{\"TYPE\":\"ExecutableScript\",\"script\":\"\"},\"returnCodeMeaning\":{\"success\":[1]}}}}";

        LOGGER.info(String.format("[workflow][joc]%s", content));
        Workflow w = (Workflow) JocInventory.convertJocContent2Deployable(content, ConfigurationType.WORKFLOW);
        LOGGER.info(String.format("[workflow][deployment]%s", Globals.objectMapper.writeValueAsString(w)));

        
        content = "{\"maxProcess\":1000,\"hosts\":[{\"url\":\"http://localhost\"}]}";
        LOGGER.info(String.format("[agentRef][joc]%s", content));
        AgentRef ar = (AgentRef) JocInventory.convertJocContent2Deployable(content, ConfigurationType.AGENTCLUSTER);
        LOGGER.info(String.format("[agentRef][deployment]%s", Globals.objectMapper.writeValueAsString(ar)));
    }
}
