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
    public void testJob() throws Exception {
        String file = "src/test/resources/jobs/javascript/JS7Job.js";

        Map<String, Object> args = new HashMap<>();
        args.put("my_arg1", "xyz");
        args.put("my_arg2", "xyz");

        execute(file, args);
    }

    @Ignore
    @Test
    public void testJobWithCredentialStore() throws Exception {
        String file = "src/test/resources/jobs/javascript/JS7Job-CredentialStore.js";

        Map<String, Object> args = new HashMap<>();
        addCredentialStoreArguments(args);

        execute(file, args);
    }

    @Ignore
    @Test
    public void testJobWithSSHProvider() throws Exception {
        String file = "src/test/resources/jobs/javascript/JS7Job-SSHProvider.js";

        Map<String, Object> args = new HashMap<>();
        addCredentialStoreArguments(args);
        addSSHProviderArguments(args);

        execute(file, args);
    }

    @Ignore
    @Test
    public void testJobWithJOCApiExecutor() throws Exception {
        String file = "src/test/resources/jobs/javascript/JS7Job-JOCApiExecutor.js";

        Map<String, Object> args = new HashMap<>();
        addCredentialStoreArguments(args);
        addSSHProviderArguments(args);

        execute(file, args);
    }

    @Ignore
    @Test
    public void testJobWithSOSHibernate() throws Exception {
        String file = "src/test/resources/jobs/javascript/JS7Job-SOSHibernate.js";

        Map<String, Object> args = new HashMap<>();

        execute(file, args);
    }

    private void execute(String file, Map<String, Object> args) throws Exception {
        String script = SOSPath.readFile(Paths.get(file));

        UnitTestJobHelper<JobArguments> h = new UnitTestJobHelper<>(new JavaScriptJob(null));
        SOSReflection.setDeclaredFieldValue(h.getJobs(), "script", script);
        h.onStart(args);

        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
        h.onStop();
    }

    private void addCredentialStoreArguments(Map<String, Object> args) {
        args.put("credential_store_file", "kdbx-p.kdbx");
        args.put("credential_store_password", "test");
        args.put("credential_store_entry_path", "/server/SFTP/localhost");
    }

    private void addSSHProviderArguments(Map<String, Object> args) {
        args.put("host", "localhost");
        args.put("port", 22);
        args.put("auth_method", "password");
        args.put("user", "sos");
        args.put("password", "sos");
    }
}
