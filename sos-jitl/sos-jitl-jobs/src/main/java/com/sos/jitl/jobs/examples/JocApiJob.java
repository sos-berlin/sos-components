package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class JocApiJob extends Job<JocApiJobArguments> {

    public JocApiJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<JocApiJobArguments> step) throws Exception {
        ApiExecutor ex = new ApiExecutor(step.getLogger());
        try {
            ApiResponse apiResponse = ex.login();
            String token = apiResponse.getResponseBody();
            step.getLogger().info("Logged in!");
            step.getLogger().info("accessToken: " + token);
            String apiUrl = "/joc/api/orders/history";
            String body = "{\"controllerId\":\"standalone\",\"dateFrom\":\"0d\",\"dateTo\":\"0d\",\"limit\":5000,\"timeZone\":\"Europe/Berlin\"}";
            apiResponse = ex.post(token, apiUrl, body);
            if (apiResponse.getStatusCode() == 200) {
                String response = apiResponse.getResponseBody();
                step.getLogger().info(response);
                if (apiResponse.getStatusCode() == 200) {
                    apiUrl = "/orders/history";
                    body = "{\"controllerId\":\"standalone\",\"dateFrom\":\"0d\",\"dateTo\":\"0d\",\"limit\":5000,\"timeZone\":\"Europe/Berlin\"}";
                    apiResponse = ex.post(token, apiUrl, body);
                    response = apiResponse.getResponseBody();
                    step.getLogger().info(response);
                    if (apiResponse.getStatusCode() == 200) {
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

        } catch (Throwable e) {
            throw e;
        } finally {
            ex.close();
        }
    }
}
