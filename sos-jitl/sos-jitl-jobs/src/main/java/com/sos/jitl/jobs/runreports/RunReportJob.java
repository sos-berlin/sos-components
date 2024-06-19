package com.sos.jitl.jobs.runreports;

import com.sos.jitl.jobs.runreports.classes.RunReportImpl;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class RunReportJob extends Job<RunReportJobArguments> {

    public RunReportJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<RunReportJobArguments> step) throws Exception {
        RunReportImpl runReport = new RunReportImpl(step);
        runReport.execute();
    }
}