package com.sos.jitl.jobs.sap;

import java.util.Collections;
import java.util.Map;

import com.sos.jitl.jobs.sap.common.ASAPS4HANAJob;
import com.sos.jitl.jobs.sap.common.CommonJobArguments;
import com.sos.jitl.jobs.sap.common.HttpClient;
import com.sos.jitl.jobs.sap.common.bean.Job;
import com.sos.jitl.jobs.sap.common.bean.ResponseJob;
import com.sos.jitl.jobs.sap.common.bean.RunIds;
import com.sos.jitl.jobs.sap.common.bean.Schedule;
import com.sos.jitl.jobs.sap.common.bean.ScheduleData;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.exception.JobProblemException;

public class SAPS4HANACreateJob extends ASAPS4HANAJob {

    public SAPS4HANACreateJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<CommonJobArguments> step) throws Exception {
        CommonJobArguments args = step.getDeclaredArguments();
        execute(step, args, RunIds.Scope.JOB);
    }

    @Override
    public void createInactiveSchedule(OrderProcessStep<CommonJobArguments> step, CommonJobArguments args, HttpClient httpClient,
            OrderProcessStepLogger logger) throws Exception {
        Map<String, Object> undeclaredArgs = step.getUndeclaredArgumentsAsNameValueMap();
        logger.info(undeclaredArgs.toString());
        ScheduleData data = new ScheduleData();
        undeclaredArgs.entrySet().stream().filter(arg -> !arg.getKey().startsWith("js7")).filter(arg -> arg.getValue() instanceof String).forEach(
                arg -> data.setAdditionalProperty(arg.getKey(), (String) arg.getValue()));
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

    private static String getJobName(OrderProcessStep<CommonJobArguments> step) throws JobProblemException {
        return String.format("%s#%s", step.getWorkflowName(), step.getJobInstructionLabel());
    }

}
