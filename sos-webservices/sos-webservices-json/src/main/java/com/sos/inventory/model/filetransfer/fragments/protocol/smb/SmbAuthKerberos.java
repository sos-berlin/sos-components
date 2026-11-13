
package com.sos.inventory.model.filetransfer.fragments.protocol.smb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Kerberos authentication.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "account",
    "password",
    "domain",
    "loginContextName"
})
public class SmbAuthKerberos {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    private String account;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("password")
    private String password;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("domain")
    private String domain;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("loginContextName")
    private String loginContextName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SmbAuthKerberos() {
    }

    /**
     * 
     * @param loginContextName
     * @param password
     * @param domain
     * @param account
     */
    public SmbAuthKerberos(String account, String password, String domain, String loginContextName) {
        super();
        this.account = account;
        this.password = password;
        this.domain = domain;
        this.loginContextName = loginContextName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("domain")
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("loginContextName")
    public String getLoginContextName() {
        return loginContextName;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("loginContextName")
    public void setLoginContextName(String loginContextName) {
        this.loginContextName = loginContextName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("account", account).append("password", password).append("domain", domain).append("loginContextName", loginContextName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(loginContextName).append(password).append(account).append(domain).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SmbAuthKerberos) == false) {
            return false;
        }
        SmbAuthKerberos rhs = ((SmbAuthKerberos) other);
        return new EqualsBuilder().append(loginContextName, rhs.loginContextName).append(password, rhs.password).append(account, rhs.account).append(domain, rhs.domain).isEquals();
    }

}
