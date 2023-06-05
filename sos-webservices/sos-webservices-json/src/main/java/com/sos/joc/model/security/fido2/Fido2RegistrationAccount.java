
package com.sos.joc.model.security.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Registration Account
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accountName",
    "origin"
})
public class Fido2RegistrationAccount {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
    @JsonProperty("origin")
    private String origin;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2RegistrationAccount() {
    }

    /**
     * 
     * @param accountName
     * @param origin
     */
    public Fido2RegistrationAccount(String accountName, String origin) {
        super();
        this.accountName = accountName;
        this.origin = origin;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
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
    @JsonProperty("origin")
    public String getOrigin() {
        return origin;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("origin")
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accountName", accountName).append("origin", origin).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(accountName).append(origin).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2RegistrationAccount) == false) {
            return false;
        }
        Fido2RegistrationAccount rhs = ((Fido2RegistrationAccount) other);
        return new EqualsBuilder().append(accountName, rhs.accountName).append(origin, rhs.origin).isEquals();
    }

}
