package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobApiExecutor;
import com.sos.jitl.jobs.common.JobLogger;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class JocApiJob extends ABlockingInternalJob<JocApiJobArguments> {

    public JocApiJob(JobContext jobContext) {
        super(jobContext);
    }

    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, JobLogger logger, JocApiJobArguments args) throws Exception {
        JobApiExecutor ex = new JobApiExecutor(args.getJocUri().getValue(), args.getTrustoreFileName());
        try {
            String token = ex.login();

            logger.info("Logged in!");
            logger.info("accessToken: " + token);

            ex.logout(token);
            logger.info("Logged out!");
        } catch (Throwable e) {
            throw e;
        } finally {
            ex.close();
        }
        return Job.success();
    }
}
