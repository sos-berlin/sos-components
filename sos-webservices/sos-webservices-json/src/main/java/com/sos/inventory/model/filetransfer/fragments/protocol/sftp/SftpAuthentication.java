
package com.sos.inventory.model.filetransfer.fragments.protocol.sftp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SSH Authentication
 * <p>
 * SSH authentication. Provide 'account' plus exactly one of 'password', 'publicKey'.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "account",
    "preferredAuthentications",
    "requiredAuthentications",
    "password",
    "publicKey"
})
public class SftpAuthentication {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    private String account;
    /**
     * Preferred SFTP authentication methods: 'password,publickey' or 'publickey,password'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("preferredAuthentications")
    private String preferredAuthentications;
    /**
     * Required SFTP authentication methods: 'password,publickey' or 'publickey,password'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("requiredAuthentications")
    private String requiredAuthentications;
    /**
     * Password authentication.
     * 
     */
    @JsonProperty("password")
    @JsonPropertyDescription("Password authentication.")
    private SftpAuthPassword password;
    /**
     * Public key authentication.
     * 
     */
    @JsonProperty("publicKey")
    @JsonPropertyDescription("Public key authentication.")
    private SftpAuthPublicKey publicKey;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SftpAuthentication() {
    }

    /**
     * 
     * @param password
     * @param preferredAuthentications
     * @param publicKey
     * @param account
     * @param requiredAuthentications
     */
    public SftpAuthentication(String account, String preferredAuthentications, String requiredAuthentications, SftpAuthPassword password, SftpAuthPublicKey publicKey) {
        super();
        this.account = account;
        this.preferredAuthentications = preferredAuthentications;
        this.requiredAuthentications = requiredAuthentications;
        this.password = password;
        this.publicKey = publicKey;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * Preferred SFTP authentication methods: 'password,publickey' or 'publickey,password'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("preferredAuthentications")
    public String getPreferredAuthentications() {
        return preferredAuthentications;
    }

    /**
     * Preferred SFTP authentication methods: 'password,publickey' or 'publickey,password'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("preferredAuthentications")
    public void setPreferredAuthentications(String preferredAuthentications) {
        this.preferredAuthentications = preferredAuthentications;
    }

    /**
     * Required SFTP authentication methods: 'password,publickey' or 'publickey,password'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("requiredAuthentications")
    public String getRequiredAuthentications() {
        return requiredAuthentications;
    }

    /**
     * Required SFTP authentication methods: 'password,publickey' or 'publickey,password'. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("requiredAuthentications")
    public void setRequiredAuthentications(String requiredAuthentications) {
        this.requiredAuthentications = requiredAuthentications;
    }

    /**
     * Password authentication.
     * 
     */
    @JsonProperty("password")
    public SftpAuthPassword getPassword() {
        return password;
    }

    /**
     * Password authentication.
     * 
     */
    @JsonProperty("password")
    public void setPassword(SftpAuthPassword password) {
        this.password = password;
    }

    /**
     * Public key authentication.
     * 
     */
    @JsonProperty("publicKey")
    public SftpAuthPublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Public key authentication.
     * 
     */
    @JsonProperty("publicKey")
    public void setPublicKey(SftpAuthPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("account", account).append("preferredAuthentications", preferredAuthentications).append("requiredAuthentications", requiredAuthentications).append("password", password).append("publicKey", publicKey).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(password).append(publicKey).append(preferredAuthentications).append(account).append(requiredAuthentications).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SftpAuthentication) == false) {
            return false;
        }
        SftpAuthentication rhs = ((SftpAuthentication) other);
        return new EqualsBuilder().append(password, rhs.password).append(publicKey, rhs.publicKey).append(preferredAuthentications, rhs.preferredAuthentications).append(account, rhs.account).append(requiredAuthentications, rhs.requiredAuthentications).isEquals();
    }

}
