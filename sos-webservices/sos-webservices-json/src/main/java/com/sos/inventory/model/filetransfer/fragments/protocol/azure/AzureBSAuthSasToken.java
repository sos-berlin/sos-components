
package com.sos.inventory.model.filetransfer.fragments.protocol.azure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SAS token authentication.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "accountKey",
    "token"
})
public class AzureBSAuthSasToken {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("accountKey")
    private String accountKey;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("token")
    private String token;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AzureBSAuthSasToken() {
    }

    /**
     * 
     * @param accountKey
     * @param token
     */
    public AzureBSAuthSasToken(String accountKey, String token) {
        super();
        this.accountKey = accountKey;
        this.token = token;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
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
     * 
     */
    @JsonProperty("accountKey")
    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accountKey", accountKey).append("token", token).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(accountKey).append(token).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AzureBSAuthSasToken) == false) {
            return false;
        }
        AzureBSAuthSasToken rhs = ((AzureBSAuthSasToken) other);
        return new EqualsBuilder().append(accountKey, rhs.accountKey).append(token, rhs.token).isEquals();
    }

}
