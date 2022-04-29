package com.sos.js7.converter.autosys.common.v12;

import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;

public class JobParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobParserTest.class);

    @Ignore
    @Test
    public void test() {
        JobParser p = new JobParser();
        ACommonJob job = p.parse(getJob());

        LOGGER.info(SOSString.toString(job));
        LOGGER.info(" " + SOSString.toString(job.getFolder()));
        LOGGER.info(" " + SOSString.toString(job.getBox()));
        LOGGER.info(" " + SOSString.toString(job.getCondition()));
        LOGGER.info(" " + SOSString.toString(job.getMonitoring()));
        LOGGER.info(" " + SOSString.toString(job.getNotification()));
        LOGGER.info(" " + SOSString.toString(job.getRunTime()));

        LOGGER.info("NOT SUPPORTED:");
        for (SOSArgument<String> ua : job.getUnknown()) {
            LOGGER.info(" " + SOSString.toString(ua));
        }
    }

    private Properties getJob() {
        Properties p = new Properties();
        p.setProperty("job_type", "cmd");
        p.setProperty("insert_job", "my_job");
        p.setProperty("command", "dir");
        p.setProperty("application", "my_application");
        p.setProperty("xxx", "yyy");
        return p;
    }
}
