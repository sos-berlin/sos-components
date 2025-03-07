package com.sos.commons.vfs.ftp.commons;

import java.nio.file.Path;

import org.apache.commons.net.ftp.FTPSClient;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;

public class FTPSProviderArguments extends FTPProviderArguments {

    public enum SecurityMode {
        EXPLICIT, IMLICIT;
    }

    private SOSArgument<SecurityMode> securityMode = new SOSArgument<>("ftps_client_security", false, SecurityMode.EXPLICIT);
    // SSL,TLS
    // TODO YADE1 default SSL, FTPSClient.DEFAULT_PROTOCOL=TLS
    private SOSArgument<String> secureSocketProtocol = new SOSArgument<>("ftps_protocol", false, "SSL");

    // Java Keystore/Truststore
    private SOSArgument<KeyStoreType> keystoreType = new SOSArgument<>("keystore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> keystoreFile = new SOSArgument<>("keystore_file", false);
    private SOSArgument<String> keystorePassword = new SOSArgument<>("keystore_password", false, DisplayMode.MASKED);

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

    public SOSArgument<KeyStoreType> getKeystoreType() {
        return keystoreType;
    }

    public SOSArgument<Path> getKeystoreFile() {
        return keystoreFile;
    }

    public SOSArgument<String> getKeystorePassword() {
        return keystorePassword;
    }
}
