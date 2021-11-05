package com.sos.jitl.jobs.sap;

import java.util.Collections;
import java.util.Map;

import com.sos.jitl.jobs.common.JobArgument.Type;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.exception.SOSJobProblemException;
import com.sos.jitl.jobs.sap.common.ASAPS4HANAJob;
import com.sos.jitl.jobs.sap.common.CommonJobArguments;
import com.sos.jitl.jobs.sap.common.HttpClient;
import com.sos.jitl.jobs.sap.common.bean.Job;
import com.sos.jitl.jobs.sap.common.bean.ResponseJob;
import com.sos.jitl.jobs.sap.common.bean.RunIds;
import com.sos.jitl.jobs.sap.common.bean.Schedule;
import com.sos.jitl.jobs.sap.common.bean.ScheduleData;

import js7.data_for_java.order.JOutcome.Completed;

public class SAPS4HANACreateJob extends ASAPS4HANAJob {

    public SAPS4HANACreateJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public Completed onOrderProcess(JobStep<CommonJobArguments> step) throws Exception {
        CommonJobArguments args = step.getArguments();
        execute(step, args, RunIds.Scope.JOB);
        return step.success(0);
    }
    
    @Override
    public void createInactiveSchedule(JobStep<CommonJobArguments> step, CommonJobArguments args, HttpClient httpClient, JobLogger logger) throws Exception {
        Map<String, Object> unknownArgs = com.sos.jitl.jobs.common.Job.asNameValueMap(step.getAllCurrentArguments(Type.UNKNOWN));
        ScheduleData data = new ScheduleData();
        unknownArgs.entrySet().stream().filter(arg -> !arg.getKey().startsWith("js7")).filter(arg -> arg
                .getValue() instanceof String).forEach(arg -> data.setAdditionalProperty(arg.getKey(), (String) arg.getValue()));
        Schedule schedule = new Schedule().withActive(false).withData(data).withDescription(setScheduleDescription(step));
        Job job = new Job().withAction(args.getActionEndpoint().getValue().toString()).withHttpMethod(args.getActionEndpointHTTPMethod().getValue())
                .withActive(true).withDescription(args.getJobDescription().getValue()).withName(getJobName(step)).withSchedules(Collections
                        .singletonList(schedule));

        httpClient.fetchCSRFToken();
        ResponseJob respJob = httpClient.createJob(job);
        args.setIJobId(respJob.getJobId());
        args.setIScheduleId(respJob.getSchedules().get(0).getScheduleId());
        logger.info("Schedule jobId=%d scheduleId=%s is created", respJob.getJobId(), respJob.getSchedules().get(0).getScheduleId());
    }

    private static String getJobName(JobStep<CommonJobArguments> step) throws SOSJobProblemException {
        return String.format("%s#%s", step.getWorkflowName(), step.getJobInstructionLabel());
    }
    
}
