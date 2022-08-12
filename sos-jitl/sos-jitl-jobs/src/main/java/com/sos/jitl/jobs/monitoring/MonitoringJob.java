package com.sos.jitl.jobs.monitoring;

import java.util.HashMap;
import java.util.Map;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.common.JitlJobReturn;
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

        JitlJobReturn monitoringJobReturn = new JitlJobReturn();
        monitoringJobReturn.setExitCode(0);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        ExecuteMonitoring executeMonitoring = new ExecuteMonitoring(logger, args);
        MonitoringStatus monitoringStatus = executeMonitoring.getStatusInformations();
        MonitoringReturnParameters monitoringReturnParameters = executeMonitoring.prepareOuput(monitoringStatus);
        resultMap.put("monitorReportDate", monitoringReturnParameters.getMonitorReportDate());
        resultMap.put("monitorReportFile", monitoringReturnParameters.getMonitorReportFile());
        MonitoringCheckReturn monitoringCheckReturn = executeMonitoring.checkStatusInformation(monitoringStatus, monitoringReturnParameters);
        resultMap.put("subject", monitoringCheckReturn.getSubject());
        resultMap.put("body", monitoringCheckReturn.getBody());

        monitoringJobReturn.setResultMap(resultMap);
        if (monitoringCheckReturn.isSuccess()) {
            resultMap.put("result", HEALTHY);
        }else {
            resultMap.put("result", NOT_HEALTHY);
        }
        

        return monitoringJobReturn;
    }


}