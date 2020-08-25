package com.sos.joc.classes.proxy;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.SSLContext;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBMissingDataException;

import js7.common.akkahttp.https.KeyStoreRef;
import js7.common.akkahttp.https.TrustStoreRef;
import js7.proxy.javaapi.data.auth.JCredentials;
import js7.proxy.javaapi.data.auth.JHttpsConfig;

public class ProxyCredentialsBuilder {

    private String jobschedulerId;
    private String url;
    private JCredentials account = null;
    private ProxyUser user = null;
    private String backupUrl;
    private JHttpsConfig httpsConfig = null;
    private boolean withHttps = false;

    private ProxyCredentialsBuilder(String jobschedulerId, String url) {
        this.jobschedulerId = jobschedulerId;
        this.url = url;
        if (url != null && url.startsWith("https://")) {
            withHttps = true;
        }
    }

    public static ProxyCredentialsBuilder withJobSchedulerIdAndUrl(String jobschedulerId, String url) {
        return new ProxyCredentialsBuilder(jobschedulerId, url);
    }

    public static ProxyCredentialsBuilder withDbInstancesOfCluster(Collection<DBItemInventoryJSInstance> dbItems) throws DBMissingDataException {
        if (dbItems == null || dbItems.isEmpty()) {
            throw new DBMissingDataException("unknown conroller (cluster)");
        }
        if (dbItems.size() > 1) { // cluster
            Comparator<DBItemInventoryJSInstance> clusterComp = Comparator.comparingInt(item -> Boolean.compare(true, item.getIsPrimary()));
            Iterator<DBItemInventoryJSInstance> iter = dbItems.stream().sorted(clusterComp).iterator();
            return withPrimaryDbInstance(iter.next()).withBackupUrl(iter.next().getUri());
        } else { // standalone
            return withPrimaryDbInstance(dbItems.iterator().next());
        }
    }

    public ProxyCredentialsBuilder withBackupUrl(String url) {
        this.backupUrl = url;
        if (url != null && url.startsWith("https://")) {
            withHttps = true;
        }
        return this;
    }

//    public ProxyCredentialsBuilder withAccount(JCredentials account) {
//        this.account = account;
//        try {
//            this.user = ProxyUser.fromValue(account.asScala().get().userId().string());
//        } catch (Exception e) {
//            //
//        }
//        return this;
//    }
    
    public ProxyCredentialsBuilder withAccount(ProxyUser user) {
        this.account = user.value();
        this.user = user;
        return this;
    }

    public ProxyCredentialsBuilder withHttpsConfig(JHttpsConfig httpsConfig) {
        if (withHttps) {
            this.httpsConfig = httpsConfig;
        } else {
            this.httpsConfig = JHttpsConfig.empty();
        }
        return this;
    }

    public ProxyCredentialsBuilder withHttpsConfig(JocCockpitProperties jocProperties) {
        if (withHttps) {
            httpsConfig = getHttpsConfig(jocProperties);
        } else {
            httpsConfig = JHttpsConfig.empty();
        }
        return this;
    }

    public ProxyCredentialsBuilder withHttpsConfig(KeyStoreRef keyStoreRef, TrustStoreRef trustStoreRef) {
        if (withHttps) {
            httpsConfig = SSLContext.getInstance().getHttpsConfig(keyStoreRef, trustStoreRef);
        } else {
            httpsConfig = JHttpsConfig.empty();
        }
        return this;
    }

    protected static JHttpsConfig getHttpsConfig(JocCockpitProperties jocProperties) {
        SSLContext sslContext = SSLContext.getInstance();
        if (sslContext.getHttpsConfig() != null) {
            return sslContext.getHttpsConfig();
        }
        sslContext.setJocProperties(jocProperties);
        return sslContext.loadHttpsConfig();
    }

//    public ProxyCredentialsBuilder withAccount(String userId, String password) {
//        if (userId == null) {
//            account = JCredentials.noCredentials();
//        } else {
//            account = JCredentials.of(userId, password);
//            try {
//                user = ProxyUser.fromValue(userId);
//            } catch (Exception e) {
//                //
//            }
//        }
//        return this;
//    }

    public ProxyCredentials build() {
        if (httpsConfig == null) {
            withHttpsConfig(Globals.sosCockpitProperties);
        }
        if (account == null) {
            account = JCredentials.noCredentials();
            //account = ProxyCredentials.jocAccount;
        }
        return new ProxyCredentials(jobschedulerId, url, user, account, backupUrl, httpsConfig);
    }

    private static ProxyCredentialsBuilder withPrimaryDbInstance(DBItemInventoryJSInstance dbItem) {
        return new ProxyCredentialsBuilder(dbItem.getSchedulerId(), dbItem.getUri());
    }
}
