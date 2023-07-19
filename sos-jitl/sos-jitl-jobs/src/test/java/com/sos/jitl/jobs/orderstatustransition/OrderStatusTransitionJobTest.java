package com.sos.jitl.jobs.orderstatustransition;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.job.JobHelper;
import com.sos.commons.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class OrderStatusTransitionJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusTransitionJobTest.class);

    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("workflow_folders", "/training2/*;/test");
        args.put("workflow_search_patterns", "trai*g2");
        args.put("controller_id", "controller");
        args.put("state_transition_source", "FAILED");
        args.put("state_transition_target", "CANCELLED");
        args.put("persist_duration", "120d");
        args.put("order_search_patterns", "*e3_1*");

        UnitTestJobHelper<OrderStateTransitionJobArguments> h = new UnitTestJobHelper<>(new OrderStateTransitionJob(null));
        h.setEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "C:/Program Files/sos-berlin.com/js7/agent/var_4425/config");
        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }
}
