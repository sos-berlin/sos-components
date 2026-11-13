
package com.sos.inventory.model.filetransfer.fragments.cs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Password authentication.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "password"
})
public class CredentialStoreAuthPassword {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("password")
    private String password;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CredentialStoreAuthPassword() {
    }

    /**
     * 
     * @param password
     */
    public CredentialStoreAuthPassword(String password) {
        super();
        this.password = password;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("password", password).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(password).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CredentialStoreAuthPassword) == false) {
            return false;
        }
        CredentialStoreAuthPassword rhs = ((CredentialStoreAuthPassword) other);
        return new EqualsBuilder().append(password, rhs.password).isEquals();
    }

}
