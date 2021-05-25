package com.sos.commons.vfs.ssh;

import java.net.Proxy.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.vfs.common.AProviderArguments.Protocol;
import com.sos.commons.vfs.common.proxy.Proxy;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.commons.vfs.ssh.helper.SSHProviderTestArguments;

public class SSHProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHProviderTest.class);

    private static final String SSH_HOST = "localhost";
    private static final int SSH_PORT = 22;
    private static final String SSH_USER = "sos";
    private static final String SSH_PASSWORD = "sos";
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

}
