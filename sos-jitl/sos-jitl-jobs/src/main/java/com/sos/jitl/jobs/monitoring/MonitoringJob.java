package com.sos.jitl.jobs.monitoring;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JitlJobReturn;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.monitoring.classes.MonitoringCheckReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringParameters;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;

import js7.data_for_java.order.JOutcome;

public class MonitoringJob extends ABlockingInternalJob<MonitoringJobArguments> {

    private static final String REPORTFILE_FILENAME_DATEFORMAT = "yyyy-MM-dd.HH-mm-ss.K'Z'";
    private static final String REPORTFILE_SUBJECT_DATEFORMAT = "yyyy-MM-dd.HH:mm:ss.K'Z'";

    public MonitoringJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<MonitoringJobArguments> step) throws Exception {

        try {
            JitlJobReturn monitoringJobReturn = process(step, step.getArguments());
            return step.success(monitoringJobReturn.getExitCode(), monitoringJobReturn.getResultMap());

        } catch (Throwable e) {
            throw e;
        }
    }

    protected JitlJobReturn process(JobStep<MonitoringJobArguments> step, MonitoringJobArguments args) throws Exception {
        JobLogger logger = null;
        logger = step.getLogger();

        JitlJobReturn monitoringJobReturn = new JitlJobReturn();

        monitoringJobReturn.setExitCode(0);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (args.getControllerId() == null || args.getControllerId().isEmpty()) {
            Globals.log(logger, "Setting controller_id=" + step.getControllerId());
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
        monitoringParameters.setAlertdOnFailedOrders(args.getAlertdOnFailedOrders());

        Globals.debug(logger, "Setting controller_id=" + step.getControllerId());

        ExecuteMonitoring executeMonitoring = new ExecuteMonitoring(logger, args);
        MonitoringStatus monitoringStatus = executeMonitoring.getStatusInformations();

        MonitoringCheckReturn monitoringCheckReturn = executeMonitoring.checkStatusInformation(monitoringStatus, monitoringParameters);
        executeMonitoring.result2File(monitoringStatus, monitoringParameters, monitoringCheckReturn.getCount());

        Globals.log(logger, "monitor report date: " + monitoringParameters.getMonitorSubjectReportDate());
        Globals.log(logger, "monitor report file: " + monitoringParameters.getMonitorReportFile());

        resultMap.put("monitor_report_date", monitoringParameters.getMonitorSubjectReportDate());
        resultMap.put("monitor_report_file", monitoringParameters.getMonitorReportFile());
        resultMap.put("subject", monitoringCheckReturn.getSubject());
        resultMap.put("body", monitoringCheckReturn.getBody());

        monitoringJobReturn.setResultMap(resultMap);
        resultMap.put("result", monitoringCheckReturn.getCount());

        return monitoringJobReturn;
    }

}