package com.sos.js7.job.jocapi.helper;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class TestApiExecutorJobArguments extends JobArguments {

    private JobArgument<String> apiURL = new JobArgument<String>("api_url", true);
    private JobArgument<String> body = new JobArgument<String>("body", true);

    public JobArgument<String> getApiURL() {
        return apiURL;
    }

    public JobArgument<String> getBody() {
        return body;
    }

}