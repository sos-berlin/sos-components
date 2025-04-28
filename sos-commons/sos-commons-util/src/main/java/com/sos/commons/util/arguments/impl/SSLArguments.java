package com.sos.commons.util.arguments.impl;

import java.util.Arrays;
import java.util.List;

import com.sos.commons.util.SOSSSLContextFactory;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class SSLArguments extends ASOSArguments {

    private static String ARG_NAME_TRUSTSTORE_DISPLAY_NAME = "TrustStore";
    private static String ARG_NAME_KEYSTORE_DISPLAY_NAME = "KeyStore";

    private JavaKeyStoreArguments javaKeyStore;

    // e.g - TLSv1.3,TLSv1.2
    // SOSSSLContextFactory uses always the SOSSSLContextFactory.DEFAULT_PROTOCOL for initialization
    private SOSArgument<List<String>> protocols = new SOSArgument<>("protocols", false, Arrays.asList(SOSSSLContextFactory.DEFAULT_PROTOCOL));
    private SOSArgument<Boolean> verifyCertificateHostname = new SOSArgument<>("verify_certificate_hostname", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> acceptUntrustedCertificate = new SOSArgument<>("accept_untrusted_certificate", false, Boolean.valueOf(false));

    // e.g. YADE uses DisableCertificateHostnameVerification
    private String verifyCertificateHostnameOppositeName;
    private String acceptUntrustedCertificateNameAlias;

    public JavaKeyStoreArguments getJavaKeyStore() {
        if (javaKeyStore == null) {
            javaKeyStore = new JavaKeyStoreArguments();
            javaKeyStore.applyDefaultIfNullQuietly();
        }
        return javaKeyStore;
    }

    public String getTrustStoreInfo() {
        if (javaKeyStore == null || javaKeyStore.getTrustStoreFile().isEmpty()) {
            return null;
        }
        return ARG_NAME_TRUSTSTORE_DISPLAY_NAME + "=" + javaKeyStore.getTrustStoreType().getValue();
    }

    public String getKeyStoreTrustStoreFullInfo() {
        if (javaKeyStore == null || !javaKeyStore.isEnabled()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (!javaKeyStore.getKeyStoreFile().isEmpty()) {
            sb.append("[");
            sb.append(ARG_NAME_KEYSTORE_DISPLAY_NAME);
            sb.append(" ").append(javaKeyStore.getKeyStoreType().getValue());
            sb.append(" ").append(javaKeyStore.getKeyStoreFile().getValue());
            sb.append("]");
        }
        if (!javaKeyStore.getTrustStoreFile().isEmpty()) {
            sb.append("[");
            sb.append(ARG_NAME_TRUSTSTORE_DISPLAY_NAME);
            sb.append(" ").append(javaKeyStore.getTrustStoreFile().getValue());
            sb.append(" ").append(javaKeyStore.getTrustStoreFile().getValue());
            sb.append("]");
        }
        return sb.toString();
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

    public String getAcceptUntrustedCertificateName() {
        return acceptUntrustedCertificateNameAlias == null ? acceptUntrustedCertificate.getName() : acceptUntrustedCertificateNameAlias;
    }

    public SOSArgument<List<String>> getProtocols() {
        return protocols;
    }

    public String getVerifyCertificateHostnameOppositeName() {
        return verifyCertificateHostnameOppositeName;
    }

    public void setVerifyCertificateHostnameOppositeName(String val) {
        verifyCertificateHostnameOppositeName = val;
    }

    public String getAcceptUntrustedCertificateNameAlias() {
        return acceptUntrustedCertificateNameAlias;
    }

    public void setAcceptUntrustedCertificateNameAlias(String val) {
        acceptUntrustedCertificateNameAlias = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(SSLArguments.class.getSimpleName()).append("]");
        if (acceptUntrustedCertificateNameAlias == null) {
            sb.append(acceptUntrustedCertificate.getName());
        } else {
            sb.append(acceptUntrustedCertificateNameAlias);
        }
        sb.append("=").append(acceptUntrustedCertificate.getValue());
        sb.append(", ");
        if (verifyCertificateHostnameOppositeName == null) {
            sb.append(verifyCertificateHostname.getName());
            sb.append("=").append(verifyCertificateHostname.getValue());
        } else {
            sb.append(verifyCertificateHostnameOppositeName);
            sb.append("=").append(!verifyCertificateHostname.getValue());
        }
        if (protocols.getValue() != null) {
            sb.append(", ");
            sb.append(protocols.getName());
            sb.append("=").append(String.join(", ", protocols.getValue()));
        }
        sb.append(", ");
        sb.append("[").append(JavaKeyStoreArguments.class.getSimpleName()).append("]");
        sb.append(javaKeyStore);
        return sb.toString();
    }
}
