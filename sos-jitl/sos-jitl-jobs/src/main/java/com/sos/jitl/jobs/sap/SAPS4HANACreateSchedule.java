package com.sos.jitl.jobs.sap;

import java.util.Optional;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.jitl.jobs.sap.common.ASAPS4HANAJob;
import com.sos.jitl.jobs.sap.common.CommonJobArguments;
import com.sos.jitl.jobs.sap.common.HttpClient;
import com.sos.jitl.jobs.sap.common.bean.ResponseJob;
import com.sos.jitl.jobs.sap.common.bean.ResponseSchedule;
import com.sos.jitl.jobs.sap.common.bean.RunIds;
import com.sos.jitl.jobs.sap.common.bean.Schedule;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.exception.JobException;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

public class SAPS4HANACreateSchedule extends ASAPS4HANAJob {

    public SAPS4HANACreateSchedule(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<CommonJobArguments> step) throws Exception {
        CommonJobArguments args = step.getDeclaredArguments();
        checkJobIdName(args.getJobId(), args.getJobName());
        execute(step, args, RunIds.Scope.SCHEDULE);
    }

    @Override
    public void createInactiveSchedule(OrderProcessStep<CommonJobArguments> step, CommonJobArguments args, HttpClient httpClient, ISOSLogger logger)
            throws Exception {
        httpClient.fetchCSRFToken();
        ResponseJob job = httpClient.retrieveJob(args.getJobId().getValue(), args.getJobName().getValue());
        args.setIJobId(job.getJobId());
        Optional<Schedule> scheduleOpt = job.getSchedules().stream().filter(s -> args.getScheduleId().getValue().equals(s.getScheduleId())).findAny()
                .map(responseSchedule -> {
                    return new Schedule().withActive(false).withData(responseSchedule.getData());
                });
        if (scheduleOpt.isPresent()) {
            ResponseSchedule respSchedule = httpClient.createSchedule(args.getJobId().getValue(), scheduleOpt.get().withDescription(
                    setScheduleDescription(step)));
            args.setIScheduleId(respSchedule.getScheduleId());
            logger.info("Schedule jobId=%d scheduleId=%s is created", args.getJobId().getValue(), respSchedule.getScheduleId());
        } else {
            throw new JobException(String.format("A Schedule '%s' doesn't exist in the Job '%d'", job.getJobId(), args.getScheduleId().getValue()));
        }
    }

    private void checkJobIdName(JobArgument<Long> jobId, JobArgument<String> jobName) throws JobRequiredArgumentMissingException {
        if (jobId.isEmpty() && jobName.isEmpty()) {
            throw new JobRequiredArgumentMissingException(String.format("Either the %s or the %s must be specified", jobId.getName(), jobName
                    .getName()));
        }
    }

}
