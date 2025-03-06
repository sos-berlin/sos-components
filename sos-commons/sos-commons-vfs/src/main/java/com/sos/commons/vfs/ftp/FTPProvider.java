package com.sos.commons.vfs.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPHTTPClient;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.proxy.ProxySocketFactory;
import com.sos.commons.vfs.exceptions.SOSProviderInitializationException;
import com.sos.commons.vfs.ftp.common.AFTPProvider;
import com.sos.commons.vfs.ftp.common.FTPProviderArguments;

public class FTPProvider extends AFTPProvider {

    public FTPProvider(ISOSLogger logger, FTPProviderArguments arguments) throws SOSProviderInitializationException {
        super(logger, arguments);
    }

    // TODO test PROXY ...
    /** Overrides {@link AFTPProvider#createClient()} */
    @Override
    public FTPClient createClient() throws Exception {
        FTPClient client = null;
        if (getArguments().getProxy() == null) {
            client = new FTPClient();
        } else {
            // SOCKS PROXY
            if (java.net.Proxy.Type.SOCKS.equals(getArguments().getProxy().getProxy().type())) {
                client = new FTPClient();
                client.setSocketFactory(new ProxySocketFactory(getArguments().getProxy()));
            }
            // HTTP PROXY
            else {
                if (getArguments().getProxy().getUser().isEmpty()) {
                    client = new FTPHTTPClient(getArguments().getProxy().getHost(), getArguments().getProxy().getPort());
                } else {
                    client = new FTPHTTPClient(getArguments().getProxy().getHost(), getArguments().getProxy().getPort(), getArguments().getProxy()
                            .getUser(), getArguments().getProxy().getPassword());
                }
            }
        }
        setProtocolCommandListener(client);
        return client;
    }

}
