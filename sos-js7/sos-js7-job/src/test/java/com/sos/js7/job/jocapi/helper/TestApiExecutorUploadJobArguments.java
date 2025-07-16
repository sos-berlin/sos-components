package com.sos.js7.job.jocapi.helper;

import java.nio.file.Path;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class TestApiExecutorUploadJobArguments extends JobArguments {

    private JobArgument<String> apiURL = new JobArgument<String>("api_url", true);
    private JobArgument<String> name = new JobArgument<String>("name", true);
    private JobArgument<String> format = new JobArgument<String>("format", true);
    private JobArgument<Boolean> overwrite = new JobArgument<Boolean>("overwrite", false, false);
    private JobArgument<Path> file = new JobArgument<Path>("file", true);
    
    public JobArgument<String> getApiURL() {
        return apiURL;
    }
    
    public JobArgument<String> getName() {
        return name;
    }
    
    public JobArgument<Boolean> getOverwrite() {
        return overwrite;
    }
    
    public JobArgument<Path> getFile() {
        return file;
    }
    
    public JobArgument<String> getFormat() {
        return format;
    }
    
}