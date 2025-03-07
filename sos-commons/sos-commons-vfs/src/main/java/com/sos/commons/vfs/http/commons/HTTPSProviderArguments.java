package com.sos.commons.vfs.http.commons;

import java.nio.file.Path;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;

public class HTTPSProviderArguments extends HTTPProviderArguments {

    private static final int DEFAULT_PORT = 443;

    // TODO FTPS/HTTPS use PROXY/KEYSTORE as IncludedArguments
    // Java Keystore/Truststore
    private SOSArgument<KeyStoreType> keystoreType = new SOSArgument<>("keystore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> keystoreFile = new SOSArgument<>("keystore_file", false);
    private SOSArgument<String> keystorePassword = new SOSArgument<>("keystore_password", false, DisplayMode.MASKED);

    private SOSArgument<Boolean> verifyCertificateHostname = new SOSArgument<>("verify_certificate_hostname", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> acceptUntrustedCertificate = new SOSArgument<>("accept_untrusted_certificate", false, Boolean.valueOf(false));

    public HTTPSProviderArguments() {
        getProtocol().setValue(Protocol.HTTPS);
    }

    @Override
    public SOSArgument<Integer> getPort() {
        if (getPort().isEmpty()) {
            getPort().setValue(DEFAULT_PORT);
        }
        return getPort();
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

    public SOSArgument<Boolean> getVerifyCertificateHostname() {
        return verifyCertificateHostname;
    }

    public SOSArgument<Boolean> getAcceptUntrustedCertificate() {
        return acceptUntrustedCertificate;
    }
}
