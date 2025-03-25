package com.sos.commons.util.arguments.impl;

import java.util.Arrays;
import java.util.List;

import com.sos.commons.util.SOSSSLContextFactory;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class SSLArguments extends ASOSArguments {

    private JavaKeyStoreArguments javaKeyStore;

    // e.g - TLSv1.3,TLSv1.2
    // SOSSSLContextFactory uses always the SOSSSLContextFactory.DEFAULT_PROTOCOL for initialization
    private SOSArgument<List<String>> protocols = new SOSArgument<>("protocols", false, Arrays.asList(SOSSSLContextFactory.DEFAULT_PROTOCOL));
    private SOSArgument<Boolean> verifyCertificateHostname = new SOSArgument<>("verify_certificate_hostname", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> acceptUntrustedCertificate = new SOSArgument<>("accept_untrusted_certificate", false, Boolean.valueOf(false));

    public JavaKeyStoreArguments getJavaKeyStore() {
        if (javaKeyStore == null) {
            javaKeyStore = new JavaKeyStoreArguments();
            javaKeyStore.applyDefaultIfNullQuietly();
        }
        return javaKeyStore;
    }

    public void setJavaKeyStore(JavaKeyStoreArguments val) {
        javaKeyStore = val;
    }

    public SOSArgument<Boolean> getVerifyCertificateHostname() {
        return verifyCertificateHostname;
    }

    public SOSArgument<Boolean> getAcceptUntrustedCertificate() {
        return acceptUntrustedCertificate;
    }

    public SOSArgument<List<String>> getProtocols() {
        return protocols;
    }
}
