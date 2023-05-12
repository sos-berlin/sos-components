
package com.sos.joc.model.security.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Registration List Filter
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
public class Fido2RegistrationListFilter {

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
    @JsonProperty("deferred")
    private Boolean deferred = false;
    @JsonProperty("confirmed")
    private Boolean confirmed = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2RegistrationListFilter() {
    }

    /**
     * 
     * @param deferred
     * @param identityServiceName
     * @param accountName
     * @param confirmed
     */
    public Fido2RegistrationListFilter(String accountName, String identityServiceName, Boolean deferred, Boolean confirmed) {
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

    @JsonProperty("deferred")
    public Boolean getDeferred() {
        return deferred;
    }

    @JsonProperty("deferred")
    public void setDeferred(Boolean deferred) {
        this.deferred = deferred;
    }

    @JsonProperty("confirmed")
    public Boolean getConfirmed() {
        return confirmed;
    }

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
        if ((other instanceof Fido2RegistrationListFilter) == false) {
            return false;
        }
        Fido2RegistrationListFilter rhs = ((Fido2RegistrationListFilter) other);
        return new EqualsBuilder().append(deferred, rhs.deferred).append(identityServiceName, rhs.identityServiceName).append(accountName, rhs.accountName).append(confirmed, rhs.confirmed).isEquals();
    }

}
