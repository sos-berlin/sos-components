package com.sos.jitl.jobs.sap;

import java.util.Optional;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.exception.SOSJobException;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;
import com.sos.jitl.jobs.sap.common.ASAPS4HANAJob;
import com.sos.jitl.jobs.sap.common.CommonJobArguments;
import com.sos.jitl.jobs.sap.common.HttpClient;
import com.sos.jitl.jobs.sap.common.bean.ResponseJob;
import com.sos.jitl.jobs.sap.common.bean.ResponseSchedule;
import com.sos.jitl.jobs.sap.common.bean.RunIds;
import com.sos.jitl.jobs.sap.common.bean.Schedule;

import js7.data_for_java.order.JOutcome.Completed;

public class SAPS4HANACreateSchedule extends ASAPS4HANAJob {

    public SAPS4HANACreateSchedule(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public Completed onOrderProcess(JobStep<CommonJobArguments> step) throws Exception {
        CommonJobArguments args = step.getArguments();
        checkJobIdName(args.getJobId(), args.getJobName());
        execute(step, args, args.setCreateScheduleArgumentsRequired());
        return step.success(0);
    }
    
    @Override
    public void startSchedule(JobStep<CommonJobArguments> step, CommonJobArguments args, HttpClient httpClient, JobLogger logger) throws Exception {
        httpClient.fetchCSRFToken();
        ResponseJob job = httpClient.retrieveJob(args.getJobId().getValue(), args.getJobName().getValue());
        args.setIJobId(job.getJobId());
        Optional<Schedule> scheduleOpt = job.getSchedules().stream().filter(s -> args.getScheduleId().getValue().equals(s.getScheduleId())).findAny()
                .map(responseSchedule -> {
                    return new Schedule().withActive(true).withData(responseSchedule.getData()).withDescription(responseSchedule.getDescription());
                });
        if (scheduleOpt.isPresent()) {
            ResponseSchedule respSchedule = httpClient.createSchedule(args.getJobId().getValue(), scheduleOpt.get());
            args.setIScheduleId(respSchedule.getScheduleId());
        } else {
            throw new SOSJobException(String.format("A Schedule '%s' doesn't exist in the Job '%d'", job.getJobId(), args.getScheduleId()
                    .getValue()));
        }
    }

    @Override
    public boolean cleanUpSchedule(RunIds runIds, HttpClient httpClient) throws Exception {
        return httpClient.deleteSchedule(runIds.getJobId(), runIds.getScheduleId());
    }
    
    private void checkJobIdName(JobArgument<Long> jobId, JobArgument<String> jobName) throws SOSJobRequiredArgumentMissingException {
        if (jobId.isEmpty() && jobName.isEmpty()) {
            throw new SOSJobRequiredArgumentMissingException(String.format("Either the %s or the %s must be specified", jobId.getName(), jobName
                    .getName()));
        }
    }
    
}
