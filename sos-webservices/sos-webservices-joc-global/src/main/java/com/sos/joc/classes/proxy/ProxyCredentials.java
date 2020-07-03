package com.sos.joc.classes.proxy;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import js7.proxy.javaapi.JCredentials;
import js7.proxy.javaapi.data.JHttpsConfig;

public class ProxyCredentials {

    private String url;
    private JCredentials account = JCredentials.noCredentials();
    private JHttpsConfig httpsConfig = JHttpsConfig.empty();

    public ProxyCredentials(String url, JCredentials account, JHttpsConfig httpsConfig) {
        this.url = url;
        this.account = account;
        this.httpsConfig = httpsConfig;
    }

    public String getUrl() {
        return url;
    }

    public JCredentials getAccount() {
        return account;
    }

    public JHttpsConfig getHttpsConfig() {
        return httpsConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("url", url).append("account", account).append("httpsConfig", httpsConfig).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(url).append(account).append(httpsConfig).toHashCode();
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
        return new EqualsBuilder().append(url, rhs.url).append(account, rhs.account).append(httpsConfig, rhs.httpsConfig).isEquals();
    }

}
