package com.sos.commons.vfs.ssh;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.vfs.common.AProviderArguments.Protocol;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.commons.vfs.ssh.helper.SSHProviderTestArguments;

public class SSHProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHProviderTest.class);

    private static final String HOST = "localhost";
    private static final int PORT = 22;
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final Path AUTH_FILE = Paths.get("C://id_rsa.ppk");

    @Ignore
    @Test
    public void testPasswordAuthentication() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setProtocol(Protocol.SFTP);// not necessary - default
        args.setHost(HOST);
        args.setPort(PORT);

        args.setAuthMethod(AuthMethod.PASSWORD);
        args.setUser(USER);
        args.setPassword(PASSWORD);

        SSHProvider p = new SSHProvider(args);
        try {
            p.connect();
            LOGGER.info(p.getShellInfo().toString());
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
        args.setHost(HOST);
        args.setPort(PORT);

        args.setAuthMethod(AuthMethod.PUBLICKEY);
        args.setUser(USER);
        args.setAuthFile(AUTH_FILE);

        SSHProvider p = new SSHProvider(args);
        try {
            p.connect();
            LOGGER.info(p.executeCommand("ping -n 2 google.com").toString());
            LOGGER.info(p.getShellInfo().toString());

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
        args.setProtocol(Protocol.SFTP);// not necessary - default
        args.setHost(HOST);
        args.setPort(PORT);

        args.setPreferredAuthentications(AuthMethod.PASSWORD, AuthMethod.PUBLICKEY);
        args.setUser(USER);
        args.setPassword(PASSWORD);
        args.setAuthFile(AUTH_FILE);

        SSHProvider p = new SSHProvider(args);
        try {
            p.connect();
            LOGGER.info(p.getShellInfo().toString());
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
        args.setHost(HOST);
        args.setPort(PORT);

        args.setRequiredAuthentications(AuthMethod.PASSWORD, AuthMethod.PUBLICKEY);
        args.setUser(USER);
        args.setPassword(PASSWORD);
        args.setAuthFile(AUTH_FILE);

        SSHProvider p = new SSHProvider(args);
        try {
            p.connect();
            LOGGER.info(p.getShellInfo().toString());
        } catch (Throwable e) {
            throw e;
        } finally {
            if (p != null) {
                p.disconnect();
            }
        }
    }
}
