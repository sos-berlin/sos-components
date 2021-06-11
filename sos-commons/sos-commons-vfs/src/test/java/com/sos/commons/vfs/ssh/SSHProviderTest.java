package com.sos.commons.vfs.ssh;

import java.net.Proxy.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.vfs.common.AProviderArguments.Protocol;
import com.sos.commons.vfs.common.proxy.Proxy;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.commons.vfs.ssh.helper.SSHProviderTestArguments;

public class SSHProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHProviderTest.class);

    private static final String SSH_HOST = "localhost";
    private static final String SSH_USER = "sos";
    private static final String SSH_PASSWORD = "sos";
    private static final int SSH_PORT = 22;
    private static final Path SSH_AUTH_FILE = Paths.get("C://id_rsa.ppk");

    private static final Proxy PROXY = new Proxy(Type.SOCKS, "proxy_host", 1080, "proxy_user", "12345", 30);

    @Ignore
    @Test
    public void testPasswordAuthentication() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setProtocol(Protocol.SFTP);// not necessary - default
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(AuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        SSHProvider p = new SSHProvider(args);
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
    public void testPublicKeyAuthentication() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(AuthMethod.PUBLICKEY);
        args.setUser(SSH_USER);
        args.setAuthFile(SSH_AUTH_FILE);

        SSHProvider p = new SSHProvider(args);
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

        args.setPreferredAuthentications(AuthMethod.PASSWORD, AuthMethod.PUBLICKEY);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);
        args.setAuthFile(SSH_AUTH_FILE);

        SSHProvider p = new SSHProvider(args);
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

        args.setRequiredAuthentications(AuthMethod.PASSWORD, AuthMethod.PUBLICKEY);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);
        args.setAuthFile(SSH_AUTH_FILE);

        SSHProvider p = new SSHProvider(args);
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

        args.setAuthMethod(AuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        args.setProxy(PROXY);

        SSHProvider p = new SSHProvider(args);
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
    public void testExecuteCommand() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setProtocol(Protocol.SSH);
        args.setHost(SSH_HOST);
        args.setPort(SSH_PORT);

        args.setAuthMethod(AuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        SSHProvider p = new SSHProvider(args);
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

        args.setAuthMethod(AuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        SSHProvider p = new SSHProvider(args);
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

            LOGGER.info("[IS_FILE]" + p.isFile("/home/sos"));
            LOGGER.info("[IS_FILE]" + p.isFile("/home/sos/test.txt"));
            LOGGER.info("[IS_DIRECTORY]" + p.isDirectory("/home/sos"));
            LOGGER.info("[IS_DIRECTORY]" + p.isDirectory("/home/sos/test.txt"));

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

        args.setAuthMethod(AuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        SSHProvider p = new SSHProvider(args);
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

            LOGGER.info("[IS_FILE]" + p.isFile("D://tmp/sos"));
            LOGGER.info("[IS_FILE]" + p.isFile("D://tmp/sos/test.txt"));
            LOGGER.info("[IS_DIRECTORY]" + p.isDirectory("D://tmp/sos"));
            LOGGER.info("[IS_DIRECTORY]" + p.isDirectory("D://tmp/sos/test.txt"));

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

        args.setAuthMethod(AuthMethod.PASSWORD);
        args.setUser(SSH_USER);
        args.setPassword(SSH_PASSWORD);

        SSHProvider p = new SSHProvider(args);
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
