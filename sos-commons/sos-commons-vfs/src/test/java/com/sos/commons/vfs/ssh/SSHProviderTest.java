package com.sos.commons.vfs.ssh;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.proxy.ProxyProvider;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.commons.vfs.ssh.helper.SSHProviderTestArguments;

public class SSHProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHProviderTest.class);

    private static final String SSH_HOST = "localhost";
    private static final String SSH_USER = "sos";
    private static final String SSH_PASSWORD = "sos";
    private static final int SSH_PORT = 22;
    private static final String SSH_AUTH_FILE = "C://id_rsa.ppk";

    @Ignore
    @Test
    public void testPasswordAuthentication() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setProtocol(Protocol.SFTP);// not necessary - default
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(SSHAuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            LOGGER.info(p.getServerInfo().toString());
            cancelAfterSeconds(p, 2);

            LOGGER.info(p.executeCommand("@powershell Start-Sleep -Seconds 10").toString());

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
    public void testPublicKeyAuthentication() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(SSHAuthMethod.PUBLICKEY);
        args.setUser(SSH_USER);
        args.setAuthFile(SSH_AUTH_FILE);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            LOGGER.info(p.executeCommand("ping -n 2 google.com").toString());
            LOGGER.info(p.getServerInfo().toString());

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
    public void testPublicKeyAuthenticationFromCredentialStore() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(SSHAuthMethod.PUBLICKEY);
        args.setUser("cs://@user");
        args.setAuthFile("cs://@attachment");

        CredentialStoreArguments csArgs = new CredentialStoreArguments();
        csArgs.setFile("/tmp/kdbx-p.kdbx");
        csArgs.setPassword("test");
        csArgs.setEntryPath("/server/SFTP/localhost");
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            LOGGER.info(p.executeCommand("ping -n 2 google.com").toString());
            LOGGER.info(p.getServerInfo().toString());

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
    public void testPreferredAuthentications() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setPreferredAuthentications(SSHAuthMethod.PASSWORD, SSHAuthMethod.PUBLICKEY);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);
        args.setAuthFile(SSH_AUTH_FILE);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            LOGGER.info(p.getServerInfo().toString());
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
    public void testRequiredAuthentications() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setRequiredAuthentications(SSHAuthMethod.PASSWORD, SSHAuthMethod.PUBLICKEY);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);
        args.setAuthFile(SSH_AUTH_FILE);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            LOGGER.info(p.getServerInfo().toString());
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
    public void testProxy() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(SSHAuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        args.setProxy(getProxyArguments());

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            LOGGER.info(p.getServerInfo().toString());
            LOGGER.info(ProxyProvider.createInstance(args.getProxy()).toString());
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
    public void testExecuteCommand() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setProtocol(Protocol.SSH);
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(SSHAuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            LOGGER.info(p.getServerInfo().toString());
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
    public void testSFTPOperationsUnix() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(SSHAuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            // p.put("D://tmp.log", "/home/sos/tmp_target.log");
            // p.get("/home/sos/tmp_target.log", "D://tmp_get.log");

            // p.rename("/home/sos/tmp_target.log", "/home/sos/tmp_target_renamed.log");

            // p.createDirectory("/home/sos/test");
            // p.createDirectories("/home/sos/test/1/2/");

            LOGGER.info("[EXISTS]" + p.exists("/home/sos/test.txt"));
            LOGGER.info("[EXISTS]" + p.exists("/home/sos"));
            // LOGGER.info("[SIZE]" + p.getSize("/home/sos"));
            // LOGGER.info("[SIZE]" + p.getSize("/home/sos/test.txt"));

            // p.delete("/home/sos/tmp_target.log");
            // p.delete("/home/sos/test");
            // LOGGER.info("[DELETED]" + p.deleteIfExists("/home/sos/test/"));

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
    public void testSFTPOperationsWindowsOpenSSH() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(SSHAuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
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
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(SSHAuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        SSHProvider p = new SSHProvider(new SLF4JLogger(), args);
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

    private void cancelAfterSeconds(SSHProvider p, int seconds) {
        CompletableFuture.supplyAsync(() -> {
            try {
                LOGGER.info("cancel after " + seconds + "s");
                TimeUnit.SECONDS.sleep(seconds);
                LOGGER.info("    " + p.cancelCommands());
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
            }
            return true;
        });
    }

    private ProxyArguments getProxyArguments() {
        ProxyArguments args = new ProxyArguments();
        args.getType().setValue(java.net.Proxy.Type.SOCKS);
        args.getHost().setValue("proxy_host");
        args.getPort().setValue(1080);
        args.getUser().setValue("proxy_user");
        args.getPassword().setValue("12345");
        args.getConnectTimeout().setValue(30);
        return args;
    }

}
