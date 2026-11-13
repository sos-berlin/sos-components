
package com.sos.inventory.model.filetransfer.fragments.cs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Credential store authentication. Provide either 'password' or 'keyFile'.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "password",
    "keyFile"
})
public class CredentialStoreAuthentication {

    /**
     * Password authentication.
     * 
     */
    @JsonProperty("password")
    @JsonPropertyDescription("Password authentication.")
    private CredentialStoreAuthPassword password;
    /**
     * Key file authentication.
     * 
     */
    @JsonProperty("keyFile")
    @JsonPropertyDescription("Key file authentication.")
    private CredentialStoreAuthKeyFile keyFile;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CredentialStoreAuthentication() {
    }

    /**
     * 
     * @param password
     * @param keyFile
     */
    public CredentialStoreAuthentication(CredentialStoreAuthPassword password, CredentialStoreAuthKeyFile keyFile) {
        super();
        this.password = password;
        this.keyFile = keyFile;
    }

    /**
     * Password authentication.
     * 
     */
    @JsonProperty("password")
    public CredentialStoreAuthPassword getPassword() {
        return password;
    }

    /**
     * Password authentication.
     * 
     */
    @JsonProperty("password")
    public void setPassword(CredentialStoreAuthPassword password) {
        this.password = password;
    }

    /**
     * Key file authentication.
     * 
     */
    @JsonProperty("keyFile")
    public CredentialStoreAuthKeyFile getKeyFile() {
        return keyFile;
    }

    /**
     * Key file authentication.
     * 
     */
    @JsonProperty("keyFile")
    public void setKeyFile(CredentialStoreAuthKeyFile keyFile) {
        this.keyFile = keyFile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("password", password).append("keyFile", keyFile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(keyFile).append(password).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CredentialStoreAuthentication) == false) {
            return false;
        }
        CredentialStoreAuthentication rhs = ((CredentialStoreAuthentication) other);
        return new EqualsBuilder().append(keyFile, rhs.keyFile).append(password, rhs.password).isEquals();
    }

}
