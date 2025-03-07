package com.sos.commons.vfs.local;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.logger.SOSSlf4jLogger;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;

public class LocalProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProviderTest.class);

    @Ignore
    @Test
    public void testExecuteCommand() throws Exception {
        LocalProviderArguments args = new LocalProviderArguments();
        CredentialStoreArguments csArgs = null; // new CredentialStoreArguments();

        args.setCredentialStore(csArgs);
        LocalProvider p = new LocalProvider(new SOSSlf4jLogger(), args);
        try {
            p.connect();
            SOSCommandResult result = p.executeCommand("ls -la /var/opt/apache-archiva/data/repositories");
            LOGGER.info("STDOUT:\n" + result.getStdOut());
            LOGGER.info("STERR: " + result.getStdErr());
            LOGGER.info("Exit Code: " + result.getExitCode());
            SOSCommandResult result2 = p.executeCommand("df -h");
            LOGGER.info("STDOUT:\n" + result2.getStdOut());
            LOGGER.info("STERR: " + result2.getStdErr());
            LOGGER.info("Exit Code: " + result2.getExitCode());
        } catch (Throwable e) {
            throw e;
        } finally {
            if (p != null) {
                p.disconnect();
            }
        }
    }

    @Ignore
    @Test
    public void testOperationsWindows() throws Exception {
        LocalProviderArguments args = new LocalProviderArguments();
        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();

        args.setCredentialStore(csArgs);
        LocalProvider p = new LocalProvider(new SOSSlf4jLogger(), args);
        try {
            p.connect();
            // p.put("D://tmp.log", "D://tmp_target.log");
            // p.get("D://tmp_target.log", "D://tmp_get.log");

            // p.rename("D://tmp_target.log", "D://tmp_target_renamed.log");

            // p.createDirectory("D://tmp");
            // p.createDirectories("D://tmp/1/2/");

            LOGGER.info("[EXISTS]" + p.exists("D://tmp/test.txt"));
            LOGGER.info("[EXISTS]" + p.exists("D://tmp/sos"));
            // LOGGER.info("[SIZE]" + p.getSize("D://tmp/sos"));
            // LOGGER.info("[SIZE]" + p.getSize("D://tmp/sos/test.txt"));

            // p.delete("D://tmp/1");
            // p.delete("D://tmp/sos/test");
            // LOGGER.info("[DELETED]" + p.deleteIfExists("D://tmp/sos/test/"));

        } catch (Throwable e) {
            throw e;
        } finally {
            if (p != null) {
                p.disconnect();
            }
        }
    }

    @Ignore
    @Test
    public void testEnvs() throws Exception {
        LocalProviderArguments args = new LocalProviderArguments();
        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();

        args.setCredentialStore(csArgs);
        LocalProvider p = new LocalProvider(new SOSSlf4jLogger(), args);
        try {
            p.connect();
            Map<String, String> envVars = new HashMap<>();
            envVars.put("sos_test", "12345");
            SOSCommandResult result = p.executeCommand("printenv", new SOSEnv(envVars));
            LOGGER.info(SOSString.toString(result));
        } catch (Throwable e) {
            throw e;
        } finally {
            if (p != null) {
                p.disconnect();
            }
        }
    }

}
