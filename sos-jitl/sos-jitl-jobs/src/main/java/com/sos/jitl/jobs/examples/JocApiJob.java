package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;

import js7.data_for_java.order.JOutcome;

public class JocApiJob extends ABlockingInternalJob<JocApiJobArguments> {

    public JocApiJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<JocApiJobArguments> step) throws Exception {
        ApiExecutor ex = new ApiExecutor(step.getLogger());
        try {
            ApiResponse apiResponse = ex.login();
            if(apiResponse.getStatusCode() == 200) {
                String token = apiResponse.getResponseBody();
                step.getLogger().info("Logged in!");
                step.getLogger().info("accessToken: " + token);
                String apiUrl = "/joc/api/orders/history";
                String body = "{\"controllerId\":\"standalone\",\"dateFrom\":\"0d\",\"dateTo\":\"0d\",\"limit\":5000,\"timeZone\":\"Europe/Berlin\"}";
                apiResponse = ex.post(token, apiUrl, body);
                if(apiResponse.getStatusCode() == 200) {
                    String response = apiResponse.getResponseBody();
                    step.getLogger().info(response);
                    if(apiResponse.getStatusCode() == 200) {
                        apiUrl = "/orders/history";
                        body = "{\"controllerId\":\"standalone\",\"dateFrom\":\"0d\",\"dateTo\":\"0d\",\"limit\":5000,\"timeZone\":\"Europe/Berlin\"}";
                        apiResponse = ex.post(token, apiUrl, body);
                        response = apiResponse.getResponseBody();
                        step.getLogger().info(response);
                        if(apiResponse.getStatusCode() == 200) {
                            apiResponse = ex.logout(token);
                            response = apiResponse.getResponseBody();
                            step.getLogger().info("Logged out!");
                        } else {
                            step.getLogger().info("Error occured on Logout!");
                        }
                    } else {
                        step.getLogger().info("Error occured on second post!");
                    }
                } else {
                    step.getLogger().info("Error occured on first post!");
                }
            } else {
                step.getLogger().info("Error occured on Login!");
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            ex.close();
        }
        return step.success();
    }
}
