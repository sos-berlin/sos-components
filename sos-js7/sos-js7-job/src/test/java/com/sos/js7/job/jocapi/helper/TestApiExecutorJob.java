package com.sos.js7.job.jocapi.helper;

import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class TestApiExecutorJob extends Job<TestApiExecutorJobArguments> {

    @Override
    public void processOrder(OrderProcessStep<TestApiExecutorJobArguments> step) throws Exception {
        try (ApiExecutor executor = new ApiExecutor(step)) {
            ApiResponse apiResponse = null;
            try {
                apiResponse = executor.login();
                apiResponse = executor.post(apiResponse.getAccessToken(), step.getDeclaredArguments().getApiURL().getValue(), step
                        .getDeclaredArguments().getBody().getValue());

                step.getLogger().info("[TestApiExecutorJob][post][responseBody]%s", apiResponse.getResponseBody());

            } finally {
                if (apiResponse != null) {
                    executor.logout(apiResponse.getAccessToken());
                }
            }
        }
    }
}
