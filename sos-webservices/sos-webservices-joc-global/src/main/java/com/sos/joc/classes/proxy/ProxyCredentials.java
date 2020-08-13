package com.sos.joc.classes.proxy;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import js7.proxy.javaapi.JCredentials;
import js7.proxy.javaapi.data.JHttpsConfig;

public class ProxyCredentials {

    private String jobschedulerId;
    private String url;
    private ProxyUser user = null;
    private JCredentials account = JCredentials.noCredentials();
    private String backupUrl;
    private JHttpsConfig httpsConfig = JHttpsConfig.empty();

    protected ProxyCredentials(String id, String url, ProxyUser user, JCredentials account, String backupUrl, JHttpsConfig httpsConfig) {
        this.jobschedulerId = id;
        this.url = url;
        if (user != null) {
            this.user = user;
            this.account = user.value();
        } else if (account != null) {
            this.account = account;
        }
        this.backupUrl = backupUrl;
        if (httpsConfig != null) {
            this.httpsConfig = httpsConfig;
        }
    }
    
    protected String getJobSchedulerId() {
        return jobschedulerId;
    }

    protected String getUrl() {
        return url;
    }

    protected JCredentials getAccount() {
        return account;
    }
    
    protected ProxyUser getUser() {
        return user;
    }
    
    protected String getBackupUrl() {
        return backupUrl;
    }

    protected JHttpsConfig getHttpsConfig() {
        return httpsConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("url", url).append("account", account).append("backupUrl", backupUrl).append("httpsConfig", httpsConfig).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(account).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProxyCredentials) == false) {
            return false;
        }
        ProxyCredentials rhs = ((ProxyCredentials) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(account, rhs.account).isEquals();
    }
    
    public boolean identical(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProxyCredentials) == false) {
            return false;
        }
        ProxyCredentials rhs = ((ProxyCredentials) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(account, rhs.account).append(url, rhs.url).append(backupUrl, rhs.backupUrl).isEquals();
    }

}
