package com.sos.js7.job.jocapi.helper;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class TestApiExecutorJob extends Job<TestApiExecutorJobArguments> {

    @Override
    public void processOrder(OrderProcessStep<TestApiExecutorJobArguments> step) throws Exception {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Export-Directory", Paths.get(System.getProperty("user.dir")).resolve("target/exported").toString()
                .replace('\\', '/'));
        try (ApiExecutor executor = new ApiExecutor(step, headers)) {
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
