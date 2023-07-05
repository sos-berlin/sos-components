package com.sos.jitl.jobs.inventory;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobs.common.UnitTestJobHelper;
import com.sos.jitl.jobs.inventory.setjobresource.SetJobResourceJobArguments;

import js7.data_for_java.order.JOutcome;

public class SetJobResourceJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetJobResourceJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("controller_id", "");
        args.put("environment_variable", "env");
        args.put("job_resource", "/tttt");
        args.put("key", "x");
        args.put("value", "[yyyy-MM-dd]");
        args.put("time_zone", "Europe/Berlin");

        UnitTestJobHelper<SetJobResourceJobArguments> h = new UnitTestJobHelper<>(new SetJobResourceJob(null));
        // h.setEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "...");
        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
