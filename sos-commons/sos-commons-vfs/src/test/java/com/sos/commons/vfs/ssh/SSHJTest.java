package com.sos.commons.vfs.ssh;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.vfs.common.proxy.Proxy;
import com.sos.commons.vfs.common.proxy.ProxySocketFactory;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class SSHJTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHJTest.class);

    private static final int DEFAULT_CONNECT_TIMEOUT = 0;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int timeout = 0;

    @Ignore
    @Test
    public void test() throws Exception {

        Config config = new DefaultConfig();
        SSHClient client = new SSHClient(config);
        Session session = null;

        String host = "localhost";
        String user = "user";
        String authKey = "C://id_rsa.ppk";

        String proxyHost = "proxy_host";
        int proxyPort = 3128;
        String proxyUser = "proxy_user";
        String proxyPassword = "12345";
        boolean useProxy = false;

        try {
            client.addHostKeyVerifier(new PromiscuousVerifier());

            client.loadKnownHosts();
            client.useCompression();

            client.setTimeout(timeout); // socket.setSoTimeout
            client.setConnectTimeout(connectTimeout);// socket.connect

            if (useProxy) {
                Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, proxyHost, proxyPort, proxyUser, proxyPassword);
                client.setSocketFactory(new ProxySocketFactory(proxy));
            }

            client.connect(host);

            client.authPublickey(user, authKey);
            session = client.startSession();
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
            client.close();
        }
    }

}
