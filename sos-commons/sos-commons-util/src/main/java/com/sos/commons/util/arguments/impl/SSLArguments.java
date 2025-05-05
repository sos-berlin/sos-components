package com.sos.commons.util.arguments.impl;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;

public class SSLArguments extends ASOSArguments {

    private static String ARG_NAME_TRUSTSTORE_DISPLAY_NAME = "TrustStore";
    private static String ARG_NAME_KEYSTORE_DISPLAY_NAME = "KeyStore";

    private JavaKeyStoreArguments trustedSSL;

    private SOSArgument<Boolean> untrustedSSL = new SOSArgument<>("untrusted_ssl", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> untrustedSSLVerifyCertificateHostname = new SOSArgument<>("verify_certificate_hostname", false, Boolean.valueOf(
            true));

    private String untrustedSSLNameAlias;
    // e.g. YADE uses DisableCertificateHostnameVerification
    private String untrustedSSLVerifyCertificateHostnameOppositeName;

    // SOSSSLContextFactory uses always the SOSSSLContextFactory.DEFAULT_PROTOCOL for initialization
    // e.g - TLSv1.3,TLSv1.2
    private SOSArgument<String> enabledProtocols = new SOSArgument<>("enabled_protocols", false);

    public JavaKeyStoreArguments getTrustedSSL() {
        if (trustedSSL == null) {
            trustedSSL = new JavaKeyStoreArguments();
            trustedSSL.applyDefaultIfNullQuietly();
        }
        return trustedSSL;
    }

    public String getTrustStoreInfo() {
        if (trustedSSL == null || trustedSSL.getTrustStoreFile().isEmpty()) {
            return null;
        }
        return ARG_NAME_TRUSTSTORE_DISPLAY_NAME + "=" + trustedSSL.getTrustStoreType().getValue();
    }

    public String getTrustedSSLFullInfo() {
        if (trustedSSL == null || !trustedSSL.isEnabled()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (!trustedSSL.getKeyStoreFile().isEmpty()) {
            sb.append("[");
            sb.append(ARG_NAME_KEYSTORE_DISPLAY_NAME);
            sb.append(" ").append(trustedSSL.getKeyStoreType().getValue());
            sb.append(" ").append(trustedSSL.getKeyStoreFile().getValue());
            sb.append("]");
        }
        if (!trustedSSL.getTrustStoreFile().isEmpty()) {
            sb.append("[");
            sb.append(ARG_NAME_TRUSTSTORE_DISPLAY_NAME);
            sb.append(" ").append(trustedSSL.getTrustStoreFile().getValue());
            sb.append(" ").append(trustedSSL.getTrustStoreFile().getValue());
            sb.append("]");
        }
        return sb.toString();
    }

    public void setTrustedSSL(JavaKeyStoreArguments val) {
        trustedSSL = val;
    }

    public SOSArgument<Boolean> getUntrustedSSL() {
        return untrustedSSL;
    }

    public SOSArgument<Boolean> getUntrustedSSLVerifyCertificateHostname() {
        return untrustedSSLVerifyCertificateHostname;
    }

    public SOSArgument<String> getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setUntrustedSSLNameAlias(String val) {
        untrustedSSLNameAlias = val;
    }

    public String getUntrustedSSLVerifyCertificateHostnameOppositeName() {
        return untrustedSSLVerifyCertificateHostnameOppositeName;
    }

    public void setUntrustedSSLVerifyCertificateHostnameOppositeName(String val) {
        untrustedSSLVerifyCertificateHostnameOppositeName = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(SSLArguments.class.getSimpleName()).append("]");
        if (untrustedSSLNameAlias == null) {
            sb.append(untrustedSSL.getName());
        } else {
            sb.append(untrustedSSLNameAlias);
        }
        sb.append("=").append(untrustedSSL.getValue());
        sb.append(", ");
        if (untrustedSSLVerifyCertificateHostnameOppositeName == null) {
            sb.append(untrustedSSLVerifyCertificateHostname.getName());
            sb.append("=").append(untrustedSSLVerifyCertificateHostname.getValue());
        } else {
            sb.append(untrustedSSLVerifyCertificateHostnameOppositeName);
            sb.append("=").append(!untrustedSSLVerifyCertificateHostname.getValue());
        }
        if (enabledProtocols.getValue() != null) {
            sb.append(", ");
            sb.append(enabledProtocols.getName());
            sb.append("=").append(String.join(", ", enabledProtocols.getValue()));
        }
        sb.append(", ");
        sb.append("[").append(JavaKeyStoreArguments.class.getSimpleName()).append("]");
        sb.append(trustedSSL);
        return sb.toString();
    }
}
