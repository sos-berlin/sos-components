package com.sos.commons.vfs.http.commons;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.impl.JavaKeyStoreArguments;

public class HTTPSProviderArguments extends HTTPProviderArguments {

    private static final int DEFAULT_PORT = 443;

    private JavaKeyStoreArguments javaKeyStore;

    private SOSArgument<Boolean> verifyCertificateHostname = new SOSArgument<>("verify_certificate_hostname", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> acceptUntrustedCertificate = new SOSArgument<>("accept_untrusted_certificate", false, Boolean.valueOf(false));

    public HTTPSProviderArguments() {
        getProtocol().setValue(Protocol.HTTPS);
        getPort().setDefaultValue(DEFAULT_PORT);
    }

    public SOSArgument<Boolean> getVerifyCertificateHostname() {
        return verifyCertificateHostname;
    }

    public SOSArgument<Boolean> getAcceptUntrustedCertificate() {
        return acceptUntrustedCertificate;
    }

    public JavaKeyStoreArguments getJavaKeyStore() {
        return javaKeyStore;
    }

    public void setJavaKeyStore(JavaKeyStoreArguments val) {
        javaKeyStore = val;
    }
}
