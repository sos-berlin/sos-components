package com.sos.commons.vfs.ftp.commons;

import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.ssl.SOSSSLContextFactory;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.exceptions.ProviderInitializationException;

public class FTPSProviderArguments extends FTPProviderArguments {

    private SSLArguments ssl;

    private SOSArgument<FTPSSecurityMode> securityMode = new SOSArgument<>("security_mode", false, FTPSSecurityMode.EXPLICIT);

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
        StringBuilder ftpsInfo = new StringBuilder();
        ftpsInfo.append(SOSSSLContextFactory.DEFAULT_PROTOCOL);
        String[] sslEnabledPrtocols = SOSSSLContextFactory.getFilteredEnabledProtocols(getSSL());
        if (sslEnabledPrtocols.length > 0) {
            ftpsInfo.append("(").append(String.join(", ", sslEnabledPrtocols)).append(")");
        }
        ftpsInfo.append(" ").append(getSecurityModeValue());
        return String.format("%s %s", super.getAccessInfo(), ftpsInfo);
    }

    /** Overrides {@link AProviderArguments#getAdvancedAccessInfo() */
    @Override
    public String getAdvancedAccessInfo() {
        return getSSL().getTrustStoreInfo();
    }

    public boolean isSecurityModeImplicit() {
        return FTPSSecurityMode.IMPLICIT.equals(securityMode.getValue());
    }

    public SOSArgument<FTPSSecurityMode> getSecurityMode() {
        return securityMode;
    }

    public String getSecurityModeValue() {
        return securityMode.getValue() == null ? null : securityMode.getValue().name().toLowerCase();
    }

}
