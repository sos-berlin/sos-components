package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.ApiExecutor;
import com.sos.jitl.jobs.common.JobStep;

import js7.data_for_java.order.JOutcome;

public class JocApiJob extends ABlockingInternalJob<JocApiJobArguments> {

    public JocApiJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<JocApiJobArguments> step) throws Exception {
        ApiExecutor ex = new ApiExecutor(step.getLogger());
        try {
            String token = ex.login();

            step.getLogger().info("Logged in!");
            step.getLogger().info("accessToken: " + token);
            
            String relativeApiUrl = "/joc/api/orders/history";
            String body = "{\"controllerId\":\"standalone\",\"dateFrom\":\"0d\",\"dateTo\":\"0d\",\"limit\":5000,\"timeZone\":\"Europe/Berlin\"}";
            String response = ex.post(token, relativeApiUrl, body);
            step.getLogger().info(response);
            
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
