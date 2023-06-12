
package com.sos.joc.model.security.fido;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido Registration List Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accountName",
    "identityServiceName",
    "deferred",
    "confirmed"
})
public class FidoRegistrationListFilter {

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
    /**
     * deferred parameter
     * <p>
     * true if the account is deferred
     * 
     */
    @JsonProperty("deferred")
    @JsonPropertyDescription("true if the account is deferred")
    private Boolean deferred;
    /**
     * confirmed parameter
     * <p>
     * true if the registration is confirmed
     * 
     */
    @JsonProperty("confirmed")
    @JsonPropertyDescription("true if the registration is confirmed")
    private Boolean confirmed;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FidoRegistrationListFilter() {
    }

    /**
     * 
     * @param deferred
     * @param identityServiceName
     * @param accountName
     * @param confirmed
     */
    public FidoRegistrationListFilter(String accountName, String identityServiceName, Boolean deferred, Boolean confirmed) {
        super();
        this.accountName = accountName;
        this.identityServiceName = identityServiceName;
        this.deferred = deferred;
        this.confirmed = confirmed;
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

    /**
     * deferred parameter
     * <p>
     * true if the account is deferred
     * 
     */
    @JsonProperty("deferred")
    public Boolean getDeferred() {
        return deferred;
    }

    /**
     * deferred parameter
     * <p>
     * true if the account is deferred
     * 
     */
    @JsonProperty("deferred")
    public void setDeferred(Boolean deferred) {
        this.deferred = deferred;
    }

    /**
     * confirmed parameter
     * <p>
     * true if the registration is confirmed
     * 
     */
    @JsonProperty("confirmed")
    public Boolean getConfirmed() {
        return confirmed;
    }

    /**
     * confirmed parameter
     * <p>
     * true if the registration is confirmed
     * 
     */
    @JsonProperty("confirmed")
    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accountName", accountName).append("identityServiceName", identityServiceName).append("deferred", deferred).append("confirmed", confirmed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deferred).append(identityServiceName).append(accountName).append(confirmed).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FidoRegistrationListFilter) == false) {
            return false;
        }
        FidoRegistrationListFilter rhs = ((FidoRegistrationListFilter) other);
        return new EqualsBuilder().append(deferred, rhs.deferred).append(identityServiceName, rhs.identityServiceName).append(accountName, rhs.accountName).append(confirmed, rhs.confirmed).isEquals();
    }

}
