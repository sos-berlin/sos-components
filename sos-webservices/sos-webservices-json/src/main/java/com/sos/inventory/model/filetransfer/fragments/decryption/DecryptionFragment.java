
package com.sos.inventory.model.filetransfer.fragments.decryption;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.AFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Decryption Fragment
 * <p>
 * Decryption Fragment.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "enciphermentPrivateKey",
    "encryptionKey"
})
public class DecryptionFragment
    extends AFragment
{

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("enciphermentPrivateKey")
    private String enciphermentPrivateKey;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("encryptionKey")
    private String encryptionKey;

    /**
     * No args constructor for use in serialization
     * 
     */
    public DecryptionFragment() {
    }

    /**
     * 
     * @param path
     * @param encryptionKey
     * @param documentationName
     * @param title
     * @param version
     * @param enciphermentPrivateKey
     */
    public DecryptionFragment(String enciphermentPrivateKey, String encryptionKey, String version, String path, String title, String documentationName) {
        super(version, path, title, documentationName);
        this.enciphermentPrivateKey = enciphermentPrivateKey;
        this.encryptionKey = encryptionKey;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("enciphermentPrivateKey")
    public String getEnciphermentPrivateKey() {
        return enciphermentPrivateKey;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("enciphermentPrivateKey")
    public void setEnciphermentPrivateKey(String enciphermentPrivateKey) {
        this.enciphermentPrivateKey = enciphermentPrivateKey;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("encryptionKey")
    public String getEncryptionKey() {
        return encryptionKey;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("encryptionKey")
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("enciphermentPrivateKey", enciphermentPrivateKey).append("encryptionKey", encryptionKey).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(enciphermentPrivateKey).append(encryptionKey).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DecryptionFragment) == false) {
            return false;
        }
        DecryptionFragment rhs = ((DecryptionFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(enciphermentPrivateKey, rhs.enciphermentPrivateKey).append(encryptionKey, rhs.encryptionKey).isEquals();
    }

}
