package com.sos.joc.classes.proxy;

import java.util.Optional;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.SSLContext;
import com.sos.joc.db.inventory.DBItemInventoryInstance;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;

import js7.common.akkahttp.https.KeyStoreRef;
import js7.common.akkahttp.https.TrustStoreRef;
import js7.proxy.javaapi.JCredentials;
import js7.proxy.javaapi.JMasterProxy;
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
    
    public static ProxyCredentialsBuilder withUrl(JOCResourceImpl jocResourceImpl) {
        return new ProxyCredentialsBuilder(jocResourceImpl.getUrl());
    }
    
    public static ProxyCredentialsBuilder withUrl(DBItemInventoryInstance dbItemInventoryInstance) {
        return new ProxyCredentialsBuilder(dbItemInventoryInstance.getUri());
    }

    public ProxyCredentialsBuilder withAccount(JCredentials account) {
        this.account = account;
        return this;
    }

    public ProxyCredentialsBuilder withHttpsConfig(JHttpsConfig httpsConfig) {
        if (this.url.startsWith("https://")) {
            this.httpsConfig = httpsConfig;
        } else {
            this.httpsConfig = JHttpsConfig.empty();
        }
        return this;
    }

    public ProxyCredentialsBuilder withHttpsConfig(JocCockpitProperties jocProperties) {
        if (this.url.startsWith("https://")) {
            httpsConfig = getHttpsConfig(jocProperties);
        } else {
            httpsConfig = JHttpsConfig.empty();
        }
        return this;
    }
    
    public static JHttpsConfig getHttpsConfig(JocCockpitProperties jocProperties) {
        SSLContext.setJocProperties(jocProperties);
        KeyStoreRef keyStoreRef = SSLContext.loadKeyStore();
        TrustStoreRef trustStoreRef = SSLContext.loadTrustStore();
        if (keyStoreRef == null && trustStoreRef == null) {
            return JHttpsConfig.empty();
        } else {
            Optional<KeyStoreRef> oKeyStoreRef = Optional.empty();
            if (keyStoreRef != null) {
                oKeyStoreRef = Optional.of(keyStoreRef);
            }
            // Collections.unmodifiableCollection(Arrays.asList(SSLContext.loadTrustStore().get()))
            ImmutableCollection<TrustStoreRef> ctrustStoreRef = ImmutableSet.of();
            if (trustStoreRef != null) {
                ctrustStoreRef = ImmutableSet.of(trustStoreRef);
            }
            return JHttpsConfig.apply(oKeyStoreRef, ctrustStoreRef);
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
            withHttpsConfig(Globals.jocConfigurationProperties);
        }
        if (account == null) {
            account = JCredentials.noCredentials();
        }
        return new ProxyCredentials(url, account, httpsConfig);
    }

    public JMasterProxy connect() throws JobSchedulerConnectionRefusedException, JobSchedulerConnectionResetException {
        return Proxies.connect(build());
    }
}
