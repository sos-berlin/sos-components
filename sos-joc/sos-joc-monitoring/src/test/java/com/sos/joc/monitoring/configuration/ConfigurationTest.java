package com.sos.joc.monitoring.configuration;

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
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.configuration.monitor.MonitorCommand;
import com.sos.joc.monitoring.configuration.monitor.mail.MonitorMail;
import com.sos.joc.monitoring.configuration.objects.workflow.Workflow;
import com.sos.joc.monitoring.configuration.objects.workflow.WorkflowJob;
import com.sos.monitoring.notification.OrderNotificationRange;

public class ConfigurationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationTest.class);

    @Ignore
    @Test
    public void testReverseProxy() throws Exception {
        Configuration.INSTANCE.setJocReverseProxyUri("https://reverse.proxy.com/");
        LOGGER.info(Configuration.INSTANCE.getJocReverseProxyUri());
    }

    @Ignore
    @Test
    public void testTypes() throws Exception {
        Configuration.INSTANCE.process(new String(Files.readAllBytes(Paths.get("src/test/resources/Configurations.xml")), Charsets.UTF_8));

        LOGGER.info("---TYPE ON_ERROR---:" + Configuration.INSTANCE.getOnError().size());
        showNotifications(Configuration.INSTANCE.getOnError());

        LOGGER.info("---TYPE ON_WARNING---:" + Configuration.INSTANCE.getOnWarning().size());
        showNotifications(Configuration.INSTANCE.getOnWarning());

        LOGGER.info("---TYPE ON_SUCCESS---:" + Configuration.INSTANCE.getOnSuccess().size());
        showNotifications(Configuration.INSTANCE.getOnSuccess());
    }

    @Ignore
    @Test
    public void testMatches() throws Exception {
        Configuration.INSTANCE.process(new String(Files.readAllBytes(Paths.get("src/test/resources/Configurations.xml")), Charsets.UTF_8));

        LOGGER.info("---WORKFLOW MATCHES---:");
        List<Notification> r = Configuration.INSTANCE.findWorkflowMatches(OrderNotificationRange.WORKFLOW, Configuration.INSTANCE.getOnError(), "js7.x",
                "/my_workflow");
        LOGGER.info("---               MATCHES---:" + r.size());
        for (Notification n : r) {
            LOGGER.info("                  " + SOSString.toString(n));
        }

        LOGGER.info("---JOB MATCHES 1---:");
        r = Configuration.INSTANCE.findWorkflowMatches(OrderNotificationRange.WORKFLOW_JOB, Configuration.INSTANCE.getOnError(), "js7.x", "/my_workflow",
                "my_job_name", "my_job_label", JobCriticality.NORMAL.intValue(), 0);
        LOGGER.info("---               MATCHES---:" + r.size());
        for (Notification n : r) {
            LOGGER.info("                  " + SOSString.toString(n));
        }

        LOGGER.info("---JOB MATCHES 2---:");
        r = Configuration.INSTANCE.findWorkflowMatches(OrderNotificationRange.WORKFLOW_JOB, Configuration.INSTANCE.getOnSuccess(), "js7.x", "/my_workflow",
                "my_job_name", "my_job_label", JobCriticality.CRITICAL.intValue(), 0);
        LOGGER.info("---               MATCHES---:" + r.size());
        for (Notification n : r) {
            LOGGER.info("                  " + SOSString.toString(n));
        }
    }

    @Ignore
    @Test
    public void testMatch() {
        String configured = "/M.*";
        String current = "/Match";

        LOGGER.info("fixed=" + current.matches(configured));
        LOGGER.info("old=" + configured.matches(current));
    }

    private void showNotifications(List<Notification> list) throws Exception {
        for (Notification n : list) {
            LOGGER.info("Notification: " + n.getNotificationId());
            for (AMonitor monitor : n.getMonitors()) {
                LOGGER.info("   MONITOR: " + monitor.getElementName());
                LOGGER.info("       " + SOSString.toString(monitor));
                LOGGER.info("       " + monitor.getMessage());

                if (monitor instanceof MonitorCommand) {
                    MonitorCommand mc = (MonitorCommand) monitor;
                    LOGGER.info("       " + mc.getCommand());
                } else if (monitor instanceof MonitorMail) {
                    // MonitorMail mm = (MonitorMail) monitor;
                }
            }

            for (Workflow workflow : n.getWorkflows()) {
                LOGGER.info("   WORKFLOW: ");
                LOGGER.info("       " + SOSString.toString(workflow));
                LOGGER.info("           JOBS: " + workflow.getJobs().size());
                for (WorkflowJob wj : workflow.getJobs()) {
                    LOGGER.info("                   " + SOSString.toString(wj));
                }
            }

            LOGGER.info("-----------------------------------");
        }
    }

}
