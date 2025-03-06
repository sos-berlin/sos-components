package com.sos.commons.vfs.ftp.common;

import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.util.common.SOSArgument;

public class FTPSProviderArguments extends FTPProviderArguments {

    public enum SecurityMode {
        EXPLICIT, IMLICIT;
    }

    private SOSArgument<SecurityMode> securityMode = new SOSArgument<>("ftps_client_security", false, SecurityMode.EXPLICIT);
    // SSL,TLS
    // TODO YADE1 default SSL, FTPSClient.DEFAULT_PROTOCOL=TLS
    private SOSArgument<String> secureSocketProtocol = new SOSArgument<>("ftps_protocol", false, "SSL");

    public FTPSProviderArguments() {
        getProtocol().setValue(Protocol.FTPS);
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

    public SOSArgument<String> getSecureSocketProtocol() {
        return secureSocketProtocol;
    }

    @SuppressWarnings("unused")
    private SOSArgument<SecurityMode> getSecurityMode() {
        return securityMode;
    }
}
