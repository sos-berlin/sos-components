package com.sos.commons.util.ssl;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.keystore.KeyStoreArguments;

/** Named SslArguments (not SSLArguments) to follow modern Java naming conventions,<br/>
 * where acronyms use only the first letter capitalized (e.g., HttpClient, XmlParser). */
public class SslArguments extends ASOSArguments {

    private static String ARG_NAME_TRUSTSTORE_DISPLAY_NAME = "TrustStore";
    private static String ARG_NAME_KEYSTORE_DISPLAY_NAME = "KeyStore";

    private KeyStoreArguments trustedSsl;

    private SOSArgument<Boolean> untrustedSsl = new SOSArgument<>("untrusted_ssl", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> untrustedSslVerifyCertificateHostname = new SOSArgument<>("verify_certificate_hostname", false, Boolean.valueOf(
            true));

    private String untrustedSslNameAlias;
    // e.g. YADE uses DisableCertificateHostnameVerification
    private String untrustedSslVerifyCertificateHostnameOppositeName;

    // SOSSSLContextFactory uses always the SOSSSLContextFactory.DEFAULT_PROTOCOL for initialization
    // e.g - TLSv1.3,TLSv1.2
    private SOSArgument<String> enabledProtocols = new SOSArgument<>("enabled_protocols", false);

    public KeyStoreArguments getTrustedSsl() {
        if (trustedSsl == null) {
            trustedSsl = new KeyStoreArguments();
            trustedSsl.applyDefaultIfNullQuietly();
        }
        return trustedSsl;
    }

    public String getTrustedSslInfo() {
        if (trustedSsl == null) {
            return null;
        }
        if (!trustedSsl.isCustomStoresEnabled()) {
            return "Default " + ARG_NAME_TRUSTSTORE_DISPLAY_NAME;
        }

        List<String> l = new ArrayList<>();
        if (!trustedSsl.getKeyStoreFile().isEmpty()) {
            l.add(ARG_NAME_KEYSTORE_DISPLAY_NAME + " " + trustedSsl.getKeyStoreType().getValue() + " " + trustedSsl.getKeyStoreFile().getValue());
        }
        if (!trustedSsl.getTrustStoreFile().isEmpty()) {
            l.add(ARG_NAME_TRUSTSTORE_DISPLAY_NAME + " " + trustedSsl.getTrustStoreType().getValue() + " " + trustedSsl.getTrustStoreFile()
                    .getValue());
        }
        return String.join(", ", l);
    }

    public void setTrustedSsl(KeyStoreArguments val) {
        trustedSsl = val;
    }

    public SOSArgument<Boolean> getUntrustedSsl() {
        return untrustedSsl;
    }

    public SOSArgument<Boolean> getUntrustedSslVerifyCertificateHostname() {
        return untrustedSslVerifyCertificateHostname;
    }

    public SOSArgument<String> getEnabledProtocols() {
        return enabledProtocols;
    }

    public void setUntrustedSslNameAlias(String val) {
        untrustedSslNameAlias = val;
    }

    public String getUntrustedSslVerifyCertificateHostnameOppositeName() {
        return untrustedSslVerifyCertificateHostnameOppositeName;
    }

    public void setUntrustedSslVerifyCertificateHostnameOppositeName(String val) {
        untrustedSslVerifyCertificateHostnameOppositeName = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(SslArguments.class.getSimpleName()).append("]");
        if (untrustedSslNameAlias == null) {
            sb.append(untrustedSsl.getName());
        } else {
            sb.append(untrustedSslNameAlias);
        }
        sb.append("=").append(untrustedSsl.getValue());
        sb.append(", ");
        if (untrustedSslVerifyCertificateHostnameOppositeName == null) {
            sb.append(untrustedSslVerifyCertificateHostname.getName());
            sb.append("=").append(untrustedSslVerifyCertificateHostname.getValue());
        } else {
            sb.append(untrustedSslVerifyCertificateHostnameOppositeName);
            sb.append("=").append(!untrustedSslVerifyCertificateHostname.getValue());
        }
        if (enabledProtocols.getValue() != null) {
            sb.append(", ");
            sb.append(enabledProtocols.getName());
            sb.append("=").append(String.join(", ", enabledProtocols.getValue()));
        }
        sb.append(", ");
        sb.append("[").append(KeyStoreArguments.class.getSimpleName()).append("]");
        sb.append(trustedSsl);
        return sb.toString();
    }
}
