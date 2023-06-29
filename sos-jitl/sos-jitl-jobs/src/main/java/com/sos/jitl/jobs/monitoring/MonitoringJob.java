package com.sos.jitl.jobs.monitoring;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.common.JobStepOutcome;
import com.sos.jitl.jobs.monitoring.classes.MonitoringCheckReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringParameters;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;

import js7.data_for_java.order.JOutcome;

public class MonitoringJob extends ABlockingInternalJob<MonitoringJobArguments> {

    private static final String REPORTFILE_FILENAME_DATEFORMAT = "yyyy-MM-dd.HH-mm-ss.SSS'Z'";
    private static final String REPORTFILE_SUBJECT_DATEFORMAT = "yyyy-MM-dd.HH:mm:ss.SSS'Z'";

    public MonitoringJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<MonitoringJobArguments> step) throws Exception {
        return step.success(process(step, step.getDeclaredArguments()));
    }

    private JobStepOutcome process(JobStep<MonitoringJobArguments> step, MonitoringJobArguments args) throws Exception {
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

        JobStepOutcome outcome = step.newJobStepOutcome();
        outcome.putVariable("monitor_report_date", monitoringParameters.getMonitorSubjectReportDate());
        outcome.putVariable("monitor_report_file", monitoringParameters.getMonitorReportFile());
        outcome.putVariable("subject", monitoringCheckReturn.getSubject());
        outcome.putVariable("body", monitoringCheckReturn.getBody());
        outcome.putVariable("result", monitoringCheckReturn.getCount());
        return outcome;
    }
}