package com.sos.commons.util.keystore;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.X509TrustManager;

/** Combines multiple X509TrustManagers into a single TrustManager that delegates to each until one accepts the certificate. */
public class CombinedX509TrustManager implements X509TrustManager {

    private final List<X509TrustManager> delegates;

    public CombinedX509TrustManager(List<X509TrustManager> delegates) {
        this.delegates = Objects.requireNonNullElseGet(delegates, Collections::emptyList);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        List<String> errors = new ArrayList<>();
        for (X509TrustManager tm : delegates) {
            try {
                tm.checkClientTrusted(chain, authType);
                return;
            } catch (CertificateException e) {
                errors.add(e.getMessage());
            }
        }
        throw new CertificateException("[No TrustManager trusted the client certificate]Reasons: " + String.join(" | ", errors));
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        List<String> errors = new ArrayList<>();
        for (X509TrustManager tm : delegates) {
            try {
                tm.checkServerTrusted(chain, authType);
                return;
            } catch (CertificateException e) {
                errors.add(e.getMessage());
            }
        }
        throw new CertificateException("[No TrustManager trusted the server certificate]Reasons: " + String.join(" | ", errors));
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return delegates.stream().flatMap(tm -> Arrays.stream(tm.getAcceptedIssuers())).distinct().toArray(X509Certificate[]::new);
    }
}
