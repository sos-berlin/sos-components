package com.sos.jitl.jobs.fileordervariables;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.job.UnitTestJobHelper;
import com.sos.jitl.jobs.fileordervariablesjob.FileOrderVariablesJob;
import com.sos.jitl.jobs.fileordervariablesjob.FileOrderVariablesJobArguments;

import js7.data_for_java.order.JOutcome;

public class FileOrderVariablesJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileOrderVariablesJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("js7_source_file", "c:/temp/1.txt");

        UnitTestJobHelper<FileOrderVariablesJobArguments> h = new UnitTestJobHelper<>(new FileOrderVariablesJob(null));
        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
