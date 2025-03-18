package com.sos.commons.vfs.ftp.commons;

import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.JavaKeyStoreArguments;

public class FTPSProviderArguments extends FTPProviderArguments {

    public enum SecurityMode {
        EXPLICIT, IMLICIT;
    }

    private JavaKeyStoreArguments javaKeyStore;

    private SOSArgument<SecurityMode> securityMode = new SOSArgument<>("ftps_client_security", false, SecurityMode.EXPLICIT);
    // SSL,TLS - "TLSv1.3,TLSv1.2" ???
    // TODO YADE1 default SSL, FTPSClient.DEFAULT_PROTOCOL=TLS
    private SOSArgument<String> protocols = new SOSArgument<>("ftps_protocol", false, "SSL");

    public FTPSProviderArguments() {
        getProtocol().setValue(Protocol.FTPS);
        getUser().setRequired(true);
    }

    @Override
    public SOSArgument<Integer> getPort() {
        if (getPort().isEmpty()) {
            getPort().setValue(isSecurityModeImplicit() ? Integer.valueOf(FTPSClient.DEFAULT_FTPS_PORT) : Integer.valueOf(DEFAULT_PORT));
        }
        return getPort();
    }

    public boolean isSecurityModeImplicit() {
        return SecurityMode.IMLICIT.equals(securityMode.getValue());
    }

    public SOSArgument<String> getProtocols() {
        return protocols;
    }

    @SuppressWarnings("unused")
    private SOSArgument<SecurityMode> getSecurityMode() {
        return securityMode;
    }

    public JavaKeyStoreArguments getJavaKeyStore() {
        return javaKeyStore;
    }

    public void setJavaKeyStore(JavaKeyStoreArguments val) {
        javaKeyStore = val;
    }

}
