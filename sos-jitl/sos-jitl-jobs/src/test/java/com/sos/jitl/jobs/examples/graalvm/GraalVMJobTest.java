package com.sos.jitl.jobs.examples.graalvm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.jitl.jobs.common.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class GraalVMJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraalVMJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        LOGGER.info(SOSShell.getJVMInfos());

        List<ScriptEngineFactory> factories = (new ScriptEngineManager()).getEngineFactories();
        for (ScriptEngineFactory factory : factories) {
            LOGGER.info(String.format("[ScriptEngineFactory]engineName=%s, engineVersion=%s, name=%s", factory.getEngineName(), factory
                    .getEngineVersion(), factory.getNames()));
        }
        if (factories.isEmpty()) {
            LOGGER.info("No Script Engines found");
        }

        Map<String, Object> args = new HashMap<>();
        args.put("script_file", "src/test/resources/jobs/examples/graalvm/GraalVMJob.js");

        UnitTestJobHelper<GraalVMJobArguments> h = new UnitTestJobHelper<>(new GraalVMJob(null));

        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }
}
