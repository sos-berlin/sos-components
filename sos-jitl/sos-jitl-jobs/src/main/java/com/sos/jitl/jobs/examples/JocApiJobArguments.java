package com.sos.jitl.jobs.examples;

import java.net.URI;

import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class JocApiJobArguments extends JobArguments {

    private JobArgument<URI> jocUri = new JobArgument<URI>("joc_uri", false);
    private JobArgument<String> trustoreFileName = new JobArgument<String>("trustore_file_name", false);
    private JobArgument<Boolean> useHttps = new JobArgument<Boolean>("use_https", false, true);

    public JobArgument<URI> getJocUri() {
        return jocUri;
    }

    public String getTrustoreFileName() {
        return trustoreFileName.getValue();
    }

    
    public JobArgument<Boolean> getUseHttps() {
        return useHttps;
    }

}
