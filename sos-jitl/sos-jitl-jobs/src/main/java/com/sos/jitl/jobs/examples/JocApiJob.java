package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobApiExecutor;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class JocApiJob extends ABlockingInternalJob<JocApiJobArguments> {

    public JocApiJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<JocApiJobArguments> step) throws Exception {
//        JobApiExecutor ex = new JobApiExecutor(step.getArguments().getJocUri().getValue(), step.getArguments().getTrustoreFileName());
        JobApiExecutor ex = new JobApiExecutor(step.getLogger());
        try {
            String token = ex.login();

            step.getLogger().info("Logged in!");
            step.getLogger().info("accessToken: " + token);

            ex.logout(token);
            step.getLogger().info("Logged out!");
        } catch (Throwable e) {
            throw e;
        } finally {
            ex.close();
        }
        return step.success();
    }
}
