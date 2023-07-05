package com.sos.jitl.jobs.maintenance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobs.common.UnitTestJobHelper;
import com.sos.jitl.jobs.maintenance.MaintenanceWindowJobArguments.StateValues;

import js7.data_for_java.order.JOutcome;

public class MaintenanceWindowJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceWindowJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("controller_host", "controller-2-0-primary");
        args.put("controller_id", "testsuite");
        args.put("state", StateValues.ACTIVE);
        args.put("joc_host", "joc-2-0-primary");
        args.put("agent_ids", Collections.singletonList("agent_101"));
        args.put("subagent_ids", Collections.singletonList("subagent_third_001"));

        UnitTestJobHelper<MaintenanceWindowJobArguments> h = new UnitTestJobHelper<>(new MaintenanceWindowJob(null));
        // h.setEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "...");
        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
