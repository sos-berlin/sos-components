package com.sos.commons.job;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

import js7.data.value.Value;
import js7.launcher.forjava.internal.BlockingInternalJob.JobContext;

public class JobEnvironment<A extends JobArguments> {

    private final Map<String, Value> engineArguments;
    private final Charset systemEncoding;
    private final String jobKey;

    private A declaredArguments;
    private Map<String, Object> allArguments;

    protected JobEnvironment(JobContext jc) {
        if (jc == null) {
            engineArguments = null;
            systemEncoding = null;
            jobKey = UUID.randomUUID().toString();
        } else {
            engineArguments = jc.jobArguments();
            systemEncoding = jc.systemEncoding();
            jobKey = jc.jobKey().toString();
        }
    }

    public A getDeclaredArguments() {
        return declaredArguments;
    }

    public Map<String, Object> getAllArgumentsAsNameValueMap() {
        if (allArguments == null) {
            allArguments = JobHelper.asJavaValues(engineArguments);
        }
        return allArguments;
    }

    public Charset getSystemEncoding() {
        return systemEncoding;
    }

    public String getJobKey() {
        return jobKey;
    }

    protected Map<String, Value> getEngineArguments() {
        return engineArguments;
    }

    protected void setDeclaredArguments(A args) {
        declaredArguments = args;
    }
}
