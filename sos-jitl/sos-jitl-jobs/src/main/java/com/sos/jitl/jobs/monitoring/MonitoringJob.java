package com.sos.jitl.jobs.monitoring;

import java.util.HashMap;
import java.util.Map;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.monitoring.classes.MonitoringCheckReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringJobReturn;
import com.sos.jitl.jobs.monitoring.classes.MonitoringReturnParameters;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;

import js7.data_for_java.order.JOutcome;

public class MonitoringJob extends ABlockingInternalJob<MonitoringJobArguments> {

    public MonitoringJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<MonitoringJobArguments> step) throws Exception {

        try {
            MonitoringJobReturn monitoringJobReturn = process(step, step.getArguments());
            return step.success(monitoringJobReturn.getExitCode(), monitoringJobReturn.getResultMap());

        } catch (Throwable e) {
            throw e;
        }
    }

    private MonitoringJobReturn process(JobStep<MonitoringJobArguments> step, MonitoringJobArguments args) throws Exception {
        JobLogger logger = null;
        if (step != null) {
            logger = step.getLogger();
        }
        MonitoringJobReturn monitoringJobReturn = new MonitoringJobReturn();
        monitoringJobReturn.setExitCode(0);
        Map<String, Object> resultMap = new HashMap<String, Object>();

        ExecuteMonitoring executeMonitoring = new ExecuteMonitoring(logger, args);
        MonitoringStatus monitoringStatus = executeMonitoring.getStatusInformations();
        MonitoringReturnParameters monitoringReturnParameters = executeMonitoring.prepareOuput(monitoringStatus);
        resultMap.put("monitorReportDate", monitoringReturnParameters.getMonitorReportDate());
        resultMap.put("monitorReportFile", monitoringReturnParameters.getMonitorReportFile());
        MonitoringCheckReturn monitoringCheckReturn = executeMonitoring.checkStatusInformation(monitoringStatus, monitoringReturnParameters);
        resultMap.put("subject", monitoringCheckReturn.getSubject());
        resultMap.put("body", monitoringCheckReturn.getSubject());

        monitoringJobReturn.setResultMap(resultMap);
      //  if (monitoringCheckReturn.isSuccess()) {
      //      monitoringJobReturn.setExitCode(1);
      //  }

        return monitoringJobReturn;
    }

    public static void main(String[] args) {

        MonitoringJobArguments arguments = new MonitoringJobArguments();

        // arguments.setQuery("isCompletedSuccessful(startedFrom=-1d,startedTo=-1d)");
        // arguments.setQuery("isCompleted(startedFrom=-100d, count>5)");

        // arguments.setQuery("lastCompletedSuccessful");
        // arguments.setJob("job2");
        // arguments.setJob("jobCheckHistory2");
        arguments.setControllerId("controller");
        arguments.setMailSmtpFrom("a@b.de");
        arguments.setMonitorReportDir("c:/temp/1111");
        arguments.setMonitorReportMaxFiles(3L);

        MonitoringJob monitoringJob = new MonitoringJob(null);

        try {
            monitoringJob.process(null, arguments);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}