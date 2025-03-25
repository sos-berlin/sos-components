package com.sos.commons.vfs.ftp.commons;

import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.SSLArguments;

public class FTPSProviderArguments extends FTPProviderArguments {

    public enum SecurityMode {
        EXPLICIT, IMLICIT;
    }

    private SSLArguments ssl;

    private SOSArgument<SecurityMode> securityMode = new SOSArgument<>("ftps_client_security", false, SecurityMode.EXPLICIT);

    public FTPSProviderArguments() {
        super(null);// use super dummy,no-op constructor
        getProtocol().setValue(Protocol.FTPS);
    }

    public SSLArguments getSSL() {
        if (ssl == null) {
            ssl = new SSLArguments();
            ssl.applyDefaultIfNullQuietly();
        }
        return ssl;
    }

    public void setSSL(SSLArguments val) {
        ssl = val;
    }

    @Override
    public SOSArgument<Integer> getPort() {
        if (super.getPort().isEmpty()) {
            super.getPort().setValue(isSecurityModeImplicit() ? Integer.valueOf(FTPSClient.DEFAULT_FTPS_PORT) : Integer.valueOf(DEFAULT_PORT));
        }
        return super.getPort();
    }

    public boolean isSecurityModeImplicit() {
        return SecurityMode.IMLICIT.equals(securityMode.getValue());
    }

    public SOSArgument<SecurityMode> getSecurityMode() {
        return securityMode;
    }

}
