package com.sos.js7.job.jocapi.helper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.sos.commons.httpclient.commons.mulitpart.HttpFormData;
import com.sos.commons.httpclient.commons.mulitpart.HttpFormDataCloseable;
import com.sos.commons.httpclient.commons.mulitpart.formdata.FormDataFile;
import com.sos.commons.httpclient.commons.mulitpart.formdata.FormDataString;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class TestApiExecutorUploadJob extends Job<TestApiExecutorUploadJobArguments> {
    
    private Map<String,String> headers;
    
    public TestApiExecutorUploadJob() {
        this.headers = new HashMap<String, String>();
    }

    @Override
    public void processOrder(OrderProcessStep<TestApiExecutorUploadJobArguments> step) throws Exception {
        try (ApiExecutor executor = new ApiExecutor(step)) {
            ApiResponse apiResponse = null;
            try (HttpFormDataCloseable formData = new HttpFormDataCloseable()) {
                apiResponse = executor.login();
                headers.put("X-Access-Token", apiResponse.getAccessToken());
                headers.put("accept", "application/json, text/plain, */*");
                headers.put("accept-encoding", "gzip, deflate, br, zstd");
                executor.setAdditionalHeaders(headers);
                
                Path path = step.getDeclaredArguments().getFile().getValue();
                if("ZIP".equalsIgnoreCase(step.getDeclaredArguments().getFormat().getValue())) {
                    formData.addPart(new FormDataFile("file", path.getFileName().toString(), path, HttpFormData.CONTENT_TYPE_ZIP));
                }else if ("TAR_GZ".equalsIgnoreCase(step.getDeclaredArguments().getFormat().getValue())) {
                    formData.addPart(new FormDataFile("file", path.getFileName().toString(), path, HttpFormData.CONTENT_TYPE_GZIP));
                }
                formData.addPart(new FormDataString("overwrite", step.getDeclaredArguments().getOverwrite().getValue().toString()));
                formData.addPart(new FormDataString("format", step.getDeclaredArguments().getFormat().getValue()));
                
                apiResponse = executor.post(apiResponse.getAccessToken(), step.getDeclaredArguments().getApiURL().getValue(), formData);
                
                step.getLogger().info("[TestApiExecutorJob][post][responseBody]%s", apiResponse.getResponseBody());
            } finally {
                if (apiResponse != null) {
                    executor.logout(apiResponse.getAccessToken());
                }
            }
        }
    }
}
