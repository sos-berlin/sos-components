package com.sos.js7.job;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

import com.sos.commons.util.SOSShell;

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
            systemEncoding = SOSShell.getConsoleEncoding();
            jobKey = UUID.randomUUID().toString();
        } else {
            engineArguments = jc.jobArguments();
            systemEncoding = jc.systemEncoding();
            jobKey = jc.jobKey().toString();
        }
    }

    // execute another job
    protected <AJ extends JobArguments> JobEnvironment(String clazzName, JobEnvironment<AJ> je) {
        engineArguments = je.getEngineArguments();
        systemEncoding = je.getSystemEncoding();
        jobKey = "execute-job-" + clazzName + "-" + UUID.randomUUID().toString();

        // TODO set declaredArguments
        allArguments = je.allArguments;
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
