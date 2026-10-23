package com.sos.commons.vfs.ssh;

import java.nio.charset.StandardCharsets;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.proxy.ProxyConfig;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.util.proxy.ProxySocketFactory;

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

        ProxyConfigArguments proxyArgs = new ProxyConfigArguments();
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
                client.setSocketFactory(new ProxySocketFactory(ProxyConfig.createInstance(proxyArgs)));
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

    @Ignore
    @Test
    public void decodePacketHexOutput() throws Exception {
        // Encoder/Decoder outputs
        String hex = "48 61 6c 6c 6f";
        hex = "07 00 00 00 03 00 00 00 0f 73 65 72 76 65 72 2d 73 69 67 2d 61 6c 67 73 00 00 00 db 73 73 68 2d 65 64 32 35 35 31 39 2c 73 6b 2d 73 73 68 2d 65 64 32 35 35 31 39 40 6f 70 65 6e 73 73 68 2e 63 6f 6d 2c 65 63 64 73 61 2d 73 68 61 32 2d 6e 69 73 74 70 32 35 36 2c 65 63 64 73 61 2d 73 68 61 32 2d 6e 69 73 74 70 33 38 34 2c 65 63 64 73 61 2d 73 68 61 32 2d 6e 69 73 74 70 35 32 31 2c 73 6b 2d 65 63 64 73 61 2d 73 68 61 32 2d 6e 69 73 74 70 32 35 36 40 6f 70 65 6e 73 73 68 2e 63 6f 6d 2c 77 65 62 61 75 74 68 6e 2d 73 6b 2d 65 63 64 73 61 2d 73 68 61 32 2d 6e 69 73 74 70 32 35 36 40 6f 70 65 6e 73 73 68 2e 63 6f 6d 2c 73 73 68 2d 64 73 73 2c 73 73 68 2d 72 73 61 2c 72 73 61 2d 73 68 61 32 2d 32 35 36 2c 72 73 61 2d 73 68 61 32 2d 35 31 32 00 00 00 1f 70 75 62 6c 69 63 6b 65 79 2d 68 6f 73 74 62 6f 75 6e 64 40 6f 70 65 6e 73 73 68 2e 63 6f 6d 00 00 00 01 30 00 00 00 10 70 69 6e 67 40 6f 70 65 6e 73 73 68 2e 63 6f 6d 00 00 00 01 30";
        
        byte[] bytes = java.util.HexFormat.of().parseHex(hex.replaceAll("\\s+", ""));// ByteArrayUtils.parseHex(hex.replace(" ", ""));
        LOGGER.info(new String(bytes, StandardCharsets.UTF_8));
    }

}
