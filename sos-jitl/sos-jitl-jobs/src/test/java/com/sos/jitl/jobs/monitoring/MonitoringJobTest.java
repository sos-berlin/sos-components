package com.sos.jitl.jobs.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobs.common.JobHelper;
import com.sos.jitl.jobs.common.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class MonitoringJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringJobTest.class);

    @Ignore
    @Test
    public void testMonitoringJob() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("controller_id", "controller");
        args.put("from", "a@b.de");
        args.put("monitor_report_dir", "c:/temp/1111");
        args.put("monitor_report_max_files", 3L);

        UnitTestJobHelper<MonitoringJobArguments> h = new UnitTestJobHelper<>(new MonitoringJob(null));

        System.out.println(System.getenv(JobHelper.ENV_NAME_AGENT_CONFIG_DIR));
        // h.setEnvVar(JobHelper.ENV_NAME_AGENT_CONFIG_DIR, "...");

        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
