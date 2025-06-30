package com.sos.commons.vfs.azure;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.vfs.azure.commons.AzureBlobStorageProviderArguments;

public class AzureBlobStorageProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobStorageProviderTest.class);

    private static String HOST = "localhost";
    private static String USER = "sos";
    private static String PASSWORD = "sos";

    @Ignore
    @Test
    public void test() throws Exception {
        HOST = "https://account.blob.core.windows.net";
        USER = "yade";
        PASSWORD = "xyz";

        resetSystemProperties();

        AzureBlobStorageProviderArguments args = new AzureBlobStorageProviderArguments();
        args.getHost().setValue(HOST);

        args.getUser().setValue(USER);
        args.getPassword().setValue(PASSWORD);

        CredentialStoreArguments csArgs = null;// new CredentialStoreArguments();
        args.setCredentialStore(csArgs);

        AzureBlobStorageProvider p = new AzureBlobStorageProvider(new SLF4JLogger(), args);
        try {
            p.connect();
            cancelAfterSeconds(p, 2);

        } catch (Throwable e) {
            throw e;
        } finally {
            if (p != null) {
                p.disconnect();
            }
        }
    }

    private void cancelAfterSeconds(AzureBlobStorageProvider p, int seconds) {
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

    @SuppressWarnings("unused")
    private ProxyConfigArguments getProxyArguments() {
        ProxyConfigArguments args = new ProxyConfigArguments();
        args.getType().setValue(java.net.Proxy.Type.SOCKS);
        args.getHost().setValue("proxy_host");
        args.getPort().setValue(1080);
        args.getUser().setValue("proxy_user");
        args.getPassword().setValue("12345");
        args.getConnectTimeout().setValue("30s");
        return args;
    }

    public static void resetSystemProperties() {
        System.setProperty("javax.net.ssl.keyStore", "");
        System.setProperty("javax.net.ssl.keyStorePassword", "");

        System.setProperty("javax.net.ssl.trustStore", "");
        System.setProperty("javax.net.ssl.trustStorePassword", "");
    }

}
