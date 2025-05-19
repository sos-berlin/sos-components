package com.sos.commons.vfs.ssh;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.arguments.impl.ProxyArguments;
import com.sos.commons.util.proxy.SOSProxyProvider;
import com.sos.commons.util.proxy.socket.ProxySocketFactory;

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

        ProxyArguments proxyArgs = new ProxyArguments();
        proxyArgs.getType().setValue(java.net.Proxy.Type.HTTP);
        proxyArgs.getHost().setValue("proxy_host");
        proxyArgs.getPort().setValue(3128);
        proxyArgs.getUser().setValue("proxy_user");
        proxyArgs.getPassword().setValue("12345");
        boolean useProxy = false;

        try {
            client.addHostKeyVerifier(new PromiscuousVerifier());

            client.loadKnownHosts();
            client.useCompression();

            client.setTimeout(timeout); // socket.setSoTimeout
            client.setConnectTimeout(connectTimeout);// socket.connect

            if (useProxy) {
                client.setSocketFactory(new ProxySocketFactory(SOSProxyProvider.createInstance(proxyArgs)));
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
