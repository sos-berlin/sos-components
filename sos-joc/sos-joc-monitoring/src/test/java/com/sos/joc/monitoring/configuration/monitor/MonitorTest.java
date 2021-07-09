package com.sos.joc.monitoring.configuration.monitor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.configuration.monitor.mail.MonitorMail;
import com.sos.joc.monitoring.configuration.objects.workflow.Workflow;
import com.sos.joc.monitoring.configuration.objects.workflow.WorkflowJob;

public class MonitorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        Configuration c = new Configuration("http://localhost");
        c.process(new String(Files.readAllBytes(Paths.get("src/test/resources/Configurations.xml")), Charsets.UTF_8));

        LOGGER.info("---TYPE ALL DEFINED---:" + c.getCounterDefinedTypeAll());

        LOGGER.info("---TYPE ON_ERROR DEFINED---:" + c.getCounterDefinedTypeOnError());
        showNotifications(c.getTypeOnError());

        LOGGER.info("---TYPE ON_SUCCESS DEFINED---:" + c.getCounterDefinedTypeOnSuccess());
        showNotifications(c.getTypeOnSuccess());

        LOGGER.info("---MATCHES---:");
        List<Notification> r = c.findWorkflowMatches(c.getTypeOnError(), "js7.x", "my_workflow", "my_job_name", "my_job_label", JobCriticality.NORMAL
                .intValue(), 0);
        LOGGER.info("---               MATCHES---:" + r.size());
        for (Notification n : r) {
            LOGGER.info("                  " + SOSString.toString(n));
        }

    }

    private void showNotifications(List<Notification> list) throws Exception {
        for (Notification n : list) {
            for (AMonitor monitor : n.getMonitors()) {
                LOGGER.info("MONITOR: " + monitor.getElementName());
                LOGGER.info("   " + SOSString.toString(monitor));
                LOGGER.info("   " + monitor.getMessage());

                if (monitor instanceof MonitorCommand) {
                    MonitorCommand mc = (MonitorCommand) monitor;
                    LOGGER.info("   " + mc.getCommand());
                } else if (monitor instanceof MonitorMail) {
                    // MonitorMail mm = (MonitorMail) monitor;
                }
            }

            for (Workflow workflow : n.getWorkflows()) {
                LOGGER.info("WORKFLOW: " + workflow.getElementName());
                LOGGER.info("   " + SOSString.toString(workflow));
                LOGGER.info("       JOBS: " + workflow.getJobs().size());
                for (WorkflowJob wj : workflow.getJobs()) {
                    LOGGER.info("                  " + SOSString.toString(wj));
                }
            }

            LOGGER.info("-----------------------------------");
        }
    }

}
