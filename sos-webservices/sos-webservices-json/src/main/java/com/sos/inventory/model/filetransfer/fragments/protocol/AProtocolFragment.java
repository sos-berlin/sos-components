
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.AFragment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Protocol Fragment
 * <p>
 * Abstract base for all protocol fragments. Do not instantiate directly.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "credentialStoreRef",
    "decryptionRef"
})
public class AProtocolFragment
    extends AFragment
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreRef")
    private String credentialStoreRef;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    private String decryptionRef;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AProtocolFragment() {
    }

    /**
     * 
     * @param path
     * @param credentialStoreRef
     * @param documentationName
     * @param title
     * @param decryptionRef
     * @param version
     */
    public AProtocolFragment(String credentialStoreRef, String decryptionRef, String version, String path, String title, String documentationName) {
        super(version, path, title, documentationName);
        this.credentialStoreRef = credentialStoreRef;
        this.decryptionRef = decryptionRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreRef")
    public String getCredentialStoreRef() {
        return credentialStoreRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreRef")
    public void setCredentialStoreRef(String credentialStoreRef) {
        this.credentialStoreRef = credentialStoreRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    public String getDecryptionRef() {
        return decryptionRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionRef")
    public void setDecryptionRef(String decryptionRef) {
        this.decryptionRef = decryptionRef;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("credentialStoreRef", credentialStoreRef).append("decryptionRef", decryptionRef).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(credentialStoreRef).append(decryptionRef).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AProtocolFragment) == false) {
            return false;
        }
        AProtocolFragment rhs = ((AProtocolFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(credentialStoreRef, rhs.credentialStoreRef).append(decryptionRef, rhs.decryptionRef).isEquals();
    }

}
