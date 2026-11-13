
package com.sos.inventory.model.filetransfer.fragments.protocol.azure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Shared key authentication.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "accountKey"
})
public class AzureBSAuthSharedKey {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountKey")
    private String accountKey;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AzureBSAuthSharedKey() {
    }

    /**
     * 
     * @param accountKey
     */
    public AzureBSAuthSharedKey(String accountKey) {
        super();
        this.accountKey = accountKey;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountKey")
    public String getAccountKey() {
        return accountKey;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("accountKey")
    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accountKey", accountKey).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(accountKey).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AzureBSAuthSharedKey) == false) {
            return false;
        }
        AzureBSAuthSharedKey rhs = ((AzureBSAuthSharedKey) other);
        return new EqualsBuilder().append(accountKey, rhs.accountKey).isEquals();
    }

}
