package com.sos.commons.vfs.ssh;

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

    @Ignore
    @Test
    public void test() throws Exception {
        SSHProviderTestArguments args = new SSHProviderTestArguments();
        args.setProtocol(Protocol.SFTP);// not necessary - default
        args.setHost("localhost");
        args.setPort(22);
        args.setUser("user");
        args.setAuthMethod(AuthMethod.PUBLICKEY);
        args.setAuthFile(Paths.get("C://id_rsa.ppk"));

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
}
