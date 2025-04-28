package com.sos.commons.vfs.ftp.commons;

import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public class FTPSProviderArguments extends FTPProviderArguments {

    private SSLArguments ssl;

    private SOSArgument<FTPSSecurityMode> securityMode = new SOSArgument<>("ftps_client_security", false, FTPSSecurityMode.EXPLICIT);

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

    /** Overrides {@link AProviderArguments#getAccessInfo() */
    @Override
    public String getAccessInfo() throws ProviderInitializationException {
        String ftpsInfo = String.join(",", getSSL().getProtocols().getValue()) + " " + getSecurityMode().getValue().name().toLowerCase();
        return String.format("%s %s", super.getAccessInfo(), ftpsInfo);
    }

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        return getSSL().getTrustStoreInfo(ARG_NAME_KEYSTORE_DISPLAY_NAME);
    }

    public boolean isSecurityModeImplicit() {
        return FTPSSecurityMode.IMPLICIT.equals(securityMode.getValue());
    }

    public SOSArgument<FTPSSecurityMode> getSecurityMode() {
        return securityMode;
    }

}
