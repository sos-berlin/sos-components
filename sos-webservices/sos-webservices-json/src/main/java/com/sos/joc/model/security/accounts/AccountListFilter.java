
package com.sos.joc.model.security.accounts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Account List Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accountName",
    "identityServiceName",
    "enabled",
    "disabled"
})
public class AccountListFilter {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    private String accountName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    @JsonProperty("enabled")
    private Boolean enabled = false;
    @JsonProperty("disabled")
    private Boolean disabled = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AccountListFilter() {
    }

    /**
     * 
     * @param identityServiceName
     * @param accountName
     * @param disabled
     * @param enabled
     */
    public AccountListFilter(String accountName, String identityServiceName, Boolean enabled, Boolean disabled) {
        super();
        this.accountName = accountName;
        this.identityServiceName = identityServiceName;
        this.enabled = enabled;
        this.disabled = disabled;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    public String getAccountName() {
        return accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("accountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return enabled;
    }

    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accountName", accountName).append("identityServiceName", identityServiceName).append("enabled", enabled).append("disabled", disabled).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(disabled).append(accountName).append(enabled).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AccountListFilter) == false) {
            return false;
        }
        AccountListFilter rhs = ((AccountListFilter) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(disabled, rhs.disabled).append(accountName, rhs.accountName).append(enabled, rhs.enabled).isEquals();
    }

}
