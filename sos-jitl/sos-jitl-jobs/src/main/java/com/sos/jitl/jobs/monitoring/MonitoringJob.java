package com.sos.jitl.jobs.monitoring;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.sos.jitl.jobs.monitoring.classes.MonitoringCheckReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringParameters;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class MonitoringJob extends Job<MonitoringJobArguments> {

    private static final String REPORTFILE_FILENAME_DATEFORMAT = "yyyy-MM-dd.HH-mm-ss.SSS'Z'";
    private static final String REPORTFILE_SUBJECT_DATEFORMAT = "yyyy-MM-dd.HH:mm:ss.SSS'Z'";

    public MonitoringJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<MonitoringJobArguments> step) throws Exception {
        process(step, step.getDeclaredArguments());
    }

    private void process(OrderProcessStep<MonitoringJobArguments> step, MonitoringJobArguments args) throws Exception {
        if (args.getControllerId() == null || args.getControllerId().isEmpty()) {
            step.getLogger().info("Setting controller_id=" + step.getControllerId());
            args.setControllerId(step.getControllerId());
        }
        if (args.getMonitorReportMaxFiles() == null) {
            args.setMonitorReportMaxFiles(30L);
        }
        MonitoringParameters monitoringParameters = new MonitoringParameters();

        DateTimeFormatter formatterFile = DateTimeFormatter.ofPattern(REPORTFILE_FILENAME_DATEFORMAT).withZone(ZoneId.of("UTC"));
        DateTimeFormatter formatterSubject = DateTimeFormatter.ofPattern(REPORTFILE_SUBJECT_DATEFORMAT).withZone(ZoneId.of("UTC"));

        monitoringParameters.setMonitorFileReportDate(formatterFile.format(Instant.now()));
        monitoringParameters.setMonitorSubjectReportDate(formatterSubject.format(Instant.now()));
        monitoringParameters.setMaxFailedOrders(args.getMaxFailedOrders());

        step.getLogger().debug("Setting controller_id=" + step.getControllerId());

        ExecuteMonitoring executeMonitoring = new ExecuteMonitoring(step.getLogger(), args);
        MonitoringStatus monitoringStatus = executeMonitoring.getStatusInformations();

        MonitoringCheckReturn monitoringCheckReturn = executeMonitoring.checkStatusInformation(monitoringStatus, monitoringParameters);
        executeMonitoring.result2File(monitoringStatus, monitoringParameters, monitoringCheckReturn.getCount());

        step.getLogger().info("monitor report date: " + monitoringParameters.getMonitorSubjectReportDate());
        step.getLogger().info("monitor report file: " + monitoringParameters.getMonitorReportFile());

        step.getOutcome().putVariable("monitor_report_date", monitoringParameters.getMonitorSubjectReportDate());
        step.getOutcome().putVariable("monitor_report_file", monitoringParameters.getMonitorReportFile());
        step.getOutcome().putVariable("subject", monitoringCheckReturn.getSubject());
        step.getOutcome().putVariable("body", monitoringCheckReturn.getBody());
        step.getOutcome().putVariable("result", monitoringCheckReturn.getCount());
    }
}