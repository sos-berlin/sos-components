package com.sos.js7.job.jocapi.helper;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sos.commons.util.http.HttpUtils;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class TestApiExecutorOIDCJob extends Job<TestApiExecutorOIDCJobArguments> {

    public TestApiExecutorOIDCJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<TestApiExecutorOIDCJobArguments> step) throws Exception {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Export-Directory", Paths.get(System.getProperty("user.dir")).resolve("target/exported").toString().replace('\\', '/'));
        headers.put(HttpUtils.HEADER_CONTENT_TYPE, HttpUtils.HEADER_CONTENT_TYPE_JSON);

        try (ApiExecutor executor = new ApiExecutor(step)) {
            ApiResponse apiResponse = null;
            try {
                apiResponse = executor.loginWithOIDC(step.getDeclaredArguments().getIssuer().getValue(), 
                        step.getDeclaredArguments().getClientId().getValue(), 
                        step.getDeclaredArguments().getClientSecret().getValue(),
                        step.getDeclaredArguments().getIdentityService().getValue(),
                        step.getDeclaredArguments().getOidcTrustStorePath().getValue(),
                        step.getDeclaredArguments().getOidcTrustStorePasswd().getValue(), 
                        step.getDeclaredArguments().getOidcTrustStoreType().getValue());
                apiResponse = executor.post(apiResponse.getAccessToken(), step.getDeclaredArguments().getApiURL().getValue(), step
                        .getDeclaredArguments().getBody().getValue(), headers);

                step.getLogger().info("[TestApiExecutorJob][post][responseBody]%s", apiResponse.getResponseBody());

            } finally {
                if (apiResponse != null) {
                    executor.logout(apiResponse.getAccessToken());
                }
            }
        }
    }
}
