package com.sos.jitl.jobs.monitoring;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.jitl.jobs.common.UnitTestJobHelper;

public class MonitoringJobTest {

    @Ignore
    @Test
    public void testMonitoringJob() throws Exception {

        MonitoringJobArguments arguments = new MonitoringJobArguments();
        arguments.setControllerId("controller");
        arguments.setMailSmtpFrom("a@b.de");
        arguments.setMonitorReportDir("c:/temp/1111");
        arguments.setMonitorReportMaxFiles(3L);
        MonitoringJob job = new MonitoringJob(null);

        UnitTestJobHelper<MonitoringJobArguments> h = new UnitTestJobHelper<>(job);

        job.process(h.newJobStep(arguments), arguments);
    }

}
