package com.sos.jitl.jobs.monitoring;

import java.util.HashMap;
import java.util.Map;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.common.JitlJobReturn;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.monitoring.classes.MonitoringCheckReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringReturnParameters;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;

import js7.data_for_java.order.JOutcome;

public class MonitoringJob extends ABlockingInternalJob<MonitoringJobArguments> {

    private static final String NOT_HEALTHY = "not healthy";
    private static final String HEALTHY = "healthy";

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
        Map<String, Object> jobArguments = Job.asNameValueMap(step.getAllCurrentArguments());

        JitlJobReturn monitoringJobReturn = new JitlJobReturn();

        monitoringJobReturn.setExitCode(0);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if (args.getControllerId() == null || args.getControllerId().isEmpty()) {
            args.setControllerId((String) jobArguments.get("js7ControllerId"));
        }
        ExecuteMonitoring executeMonitoring = new ExecuteMonitoring(logger, args);
        MonitoringStatus monitoringStatus = executeMonitoring.getStatusInformations();
        MonitoringReturnParameters monitoringReturnParameters = executeMonitoring.prepareOuput(monitoringStatus);
        resultMap.put("monitor_report_date", monitoringReturnParameters.getMonitorReportDate());
        resultMap.put("monitor_reportFile", monitoringReturnParameters.getMonitorReportFile());
        MonitoringCheckReturn monitoringCheckReturn = executeMonitoring.checkStatusInformation(monitoringStatus, monitoringReturnParameters);
        resultMap.put("subject", monitoringCheckReturn.getSubject());
        resultMap.put("body", monitoringCheckReturn.getBody());

        monitoringJobReturn.setResultMap(resultMap);
        resultMap.put("result", monitoringCheckReturn.getCount());

        return monitoringJobReturn;
    }

}