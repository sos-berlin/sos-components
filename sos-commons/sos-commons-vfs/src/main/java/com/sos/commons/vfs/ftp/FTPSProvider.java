package com.sos.commons.vfs.ftp;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.AProviderArguments.KeyStoreType;
import com.sos.commons.vfs.exceptions.SOSProviderConnectException;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;
import com.sos.commons.vfs.ftp.common.AFTPProvider;
import com.sos.commons.vfs.ftp.common.FTPSProviderArguments;

public class FTPSProvider extends AFTPProvider {

    public FTPSProvider(ISOSLogger logger, FTPSProviderArguments arguments) throws SOSProviderInitializationException {
        super(logger, arguments);
    }

    @Override
    public void connect() throws SOSProviderConnectException {
        super.connect();

        FTPSClient client = (FTPSClient) super.getClient();
        try {
            client.execPBSZ(0);
            debugCommand("execPBSZ(0)");
        } catch (Throwable e) {
            getLogger().warn("[execPBSZ(0)]" + e);
        }
        try {
            client.execPROT("P");
            debugCommand("execPROT(P)");
        } catch (Throwable e) {
            getLogger().warn("[execPROT(P)]" + e);
        }
        client.enterLocalPassiveMode();
        debugCommand("enterLocalPassiveMode");
    }

    @Override
    // TODO test PROXY ...
    public FTPClient createClient() throws Exception {
        FTPSProviderArguments args = (FTPSProviderArguments) getArguments();

        FTPSClient client = new FTPSClient(args.getSecureSocketProtocol().getValue(), args.isSecurityModeImplicit());
        if (args.getProxy() != null) {
            client.setProxy(args.getProxy().getProxy());
        }
        setTrustManager(client);
        setProtocolCommandListener(client);
        return client;
    }

    private void setTrustManager(FTPSClient client) throws Exception {
        Path path = getArguments().getKeystoreFile().getValue();
        if (path == null) {
            return;
        }

        KeyStoreType type = getArguments().getKeystoreType().getValue();
        getLogger().info(String.format("%s[using keystore]type=%s, file=%s", getLogPrefix(), type, path));

        client.setTrustManager(TrustManagerUtils.getDefaultTrustManager(loadKeyStore(path, type, getArguments().getKeystorePassword().getValue())));
    }

    private KeyStore loadKeyStore(Path path, KeyStoreType type, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(type.name());
        char[] pass = password == null ? "".toCharArray() : password.toCharArray();
        try (InputStream is = Files.newInputStream(path)) {
            ks.load(is, pass);
        }
        return ks;
    }

}
