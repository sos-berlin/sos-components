package com.sos.joc.classes.proxy;

import java.util.Optional;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.SSLContext;

import js7.common.akkahttp.https.KeyStoreRef;
import js7.common.akkahttp.https.TrustStoreRef;
import js7.proxy.javaapi.JCredentials;
import js7.proxy.javaapi.data.JHttpsConfig;

public class ProxyCredentialsBuilder {
    
    private String url;
    private JCredentials account = null;
    private JHttpsConfig httpsConfig = null;

    private ProxyCredentialsBuilder(String url) {
        this.url = url;
    }

    public static ProxyCredentialsBuilder withUrl(String url) {
        return new ProxyCredentialsBuilder(url);
    }

    public ProxyCredentialsBuilder withAccount(JCredentials account) {
        this.account = account;
        return this;
    }

    public ProxyCredentialsBuilder withHttpsConfig(JHttpsConfig httpsConfig) {
        if (this.url != null && this.url.startsWith("https://")) {
            this.httpsConfig = httpsConfig;
        } else {
            this.httpsConfig = JHttpsConfig.empty();
        }
        return this;
    }

    public ProxyCredentialsBuilder withHttpsConfig(JocCockpitProperties jocProperties) {
        if (this.url != null && this.url.startsWith("https://")) {
            httpsConfig = getHttpsConfig(jocProperties);
        } else {
            httpsConfig = JHttpsConfig.empty();
        }
        return this;
    }
    
    public ProxyCredentialsBuilder withHttpsConfig(KeyStoreRef keyStoreRef, TrustStoreRef trustStoreRef) {
        if (this.url != null && this.url.startsWith("https://")) {
            httpsConfig = getHttpsConfig(keyStoreRef, trustStoreRef);
        } else {
            httpsConfig = JHttpsConfig.empty();
        }
        return this;
    }
    
    public static JHttpsConfig getHttpsConfig(JocCockpitProperties jocProperties) {
        SSLContext sslContext = SSLContext.getInstance();
        sslContext.setJocProperties(jocProperties);
        KeyStoreRef keyStoreRef = sslContext.loadKeyStore();
        TrustStoreRef trustStoreRef = sslContext.loadTrustStore();
        return getHttpsConfig(keyStoreRef, trustStoreRef);
    }
    
    public static JHttpsConfig getHttpsConfig(KeyStoreRef keyStoreRef, TrustStoreRef trustStoreRef) {
        if (keyStoreRef == null && trustStoreRef == null) {
            return JHttpsConfig.empty();
        } else {
            Optional<KeyStoreRef> oKeyStoreRef = Optional.empty();
            if (keyStoreRef != null) {
                oKeyStoreRef = Optional.of(keyStoreRef);
            }
            // Collections.unmodifiableCollection(Arrays.asList(SSLContext.loadTrustStore().get()))
            ImmutableCollection<TrustStoreRef> trustStoreRefs = ImmutableSet.of();
            if (trustStoreRef != null) {
                trustStoreRefs = ImmutableSet.of(trustStoreRef);
            }
            return JHttpsConfig.apply(oKeyStoreRef, trustStoreRefs);
        }
    }

    public ProxyCredentialsBuilder withAccount(String userId, String password) {
        if (userId == null) {
            account = JCredentials.noCredentials();
        } else {
            account = JCredentials.of(userId, password);
        }
        return this;
    }

    public ProxyCredentials build() {
        if (httpsConfig == null) {
            withHttpsConfig(Globals.sosCockpitProperties);
        }
        if (account == null) {
            account = JCredentials.noCredentials();
        }
        return new ProxyCredentials(url, account, httpsConfig);
    }

}
