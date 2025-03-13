package com.sos.commons.util.arguments.impl;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class SSLArguments extends ASOSArguments {

    private JavaKeyStoreArguments javaKeyStore;

    private SOSArgument<Boolean> verifyCertificateHostname = new SOSArgument<>("verify_certificate_hostname", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> acceptUntrustedCertificate = new SOSArgument<>("accept_untrusted_certificate", false, Boolean.valueOf(false));

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
