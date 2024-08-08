package com.sos.js7.converter.autosys.common.v12;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.attr.AJobAttributes;
import com.sos.js7.converter.commons.JS7ConverterHelper;

public class JobParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobParserTest.class);

    @Ignore
    @Test
    public void test() {
        JobParser p = new JobParser();
        ACommonJob job = p.parse(null, getJob());

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

    @Ignore
    @Test
    public void testStartTimes() {
        String val = "00:02,05:02,6:02,7:02,8:02,9:02,10:02,11:02,12:02,13:02,14:02,15:02,16:02,17:02,18:02,19:02,20:02,21:02,22:02,23:02";

        List<String> l = AJobAttributes.stringListValue(val);

        LOGGER.info("[l]" + l);
        LOGGER.info("[result]" + JS7ConverterHelper.getTimes(l));

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
