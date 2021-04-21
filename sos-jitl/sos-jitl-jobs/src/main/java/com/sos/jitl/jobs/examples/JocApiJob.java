package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobApiExecutor;

import js7.data_for_java.order.JOutcome;
import js7.executor.forjava.internal.BlockingInternalJob;

public class JocApiJob extends ABlockingInternalJob<JocApiJobArguments> {

    public JOutcome.Completed onOrderProcess(BlockingInternalJob.Step step, JocApiJobArguments args) throws Exception {
        JobApiExecutor ex = new JobApiExecutor(args.getTrustoreFileName(), args.getJocUri());
        try {
            String token = ex.login();

            Job.info(step, "Logged in!");
            Job.info(step, "accessToken: " + token);

            ex.logout(token);
            Job.info(step, "Logged out!");
        } catch (Throwable e) {
            throw e;
        } finally {
            ex.close();
        }
        return Job.success();
    }
}
