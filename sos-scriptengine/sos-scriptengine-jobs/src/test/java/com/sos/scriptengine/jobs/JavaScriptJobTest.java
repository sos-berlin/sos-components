package com.sos.scriptengine.jobs;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSReflection;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class JavaScriptJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        boolean useSSHProvider = false;
        boolean useCredentialStore = false;
        boolean useApiAxecutor = true;

        Map<String, Object> args = new HashMap<>();
        args.put("my_arg1", "xyz");
        args.put("my_arg2", "xyz");

        String file = getTestCase(args, useSSHProvider, useCredentialStore, useApiAxecutor);
        String script = SOSPath.readFile(Paths.get(file));

        UnitTestJobHelper<JobArguments> h = new UnitTestJobHelper<>(new JavaScriptJob(null));
        SOSReflection.setDeclaredFieldValue(h.getJobs(), "script", script);
        h.onStart(args);

        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
        h.onStop();
    }

    private String getTestCase(Map<String, Object> args, boolean useSSHProvider, boolean useCredentialStore, boolean useApiAxecutor) {
        LOGGER.info(String.format("[getTestCase]useSSHProvider=%s, useCredentialStore=%s, useApiAxecutor=%s", useSSHProvider, useCredentialStore,
                useApiAxecutor));
        String file = "src/test/resources/jobs/javascript/JS7Job.js";
        if (useSSHProvider) {
            file = "src/test/resources/jobs/javascript/JS7Job-SSHProvider.js";
            putSSHProviderArguments(args);
        } else if (useCredentialStore) {
            file = "src/test/resources/jobs/javascript/JS7Job-CredentialStore.js";
        } else if (useApiAxecutor) {
            file = "src/test/resources/jobs/javascript/JS7Job-JOCApiExecutor.js";
        }
        if (useCredentialStore) {// extra because can be used by the SSHProvider case too
            putCredentialStoreArguments(args);
        }
        LOGGER.info(String.format("[getTestCase][file]%s", file));
        return file;
    }

    private void putCredentialStoreArguments(Map<String, Object> args) {
        args.put("credential_store_file", "kdbx-p.kdbx");
        args.put("credential_store_password", "test");
        args.put("credential_store_entry_path", "/server/SFTP/localhost");
    }

    private void putSSHProviderArguments(Map<String, Object> args) {
        args.put("host", "localhost");
        args.put("port", 22);
        args.put("auth_method", "password");
        args.put("user", "sos");
        args.put("password", "sos");
    }
}
