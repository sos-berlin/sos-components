package com.sos.jitl.jobs.examples;

import java.net.URI;

import com.sos.jitl.jobs.common.JobArgument;

public class JocApiJobArguments {

    private JobArgument<URI> jocUri = new JobArgument<URI>("joc_uri");
    private JobArgument<String> trustoreFileName = new JobArgument<String>("trustore_file_name");

    public URI getJocUri() {
        return jocUri.getValue();
    }

    public String getTrustoreFileName() {
        return trustoreFileName.getValue();
    }
}
