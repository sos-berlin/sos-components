
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
    "credentialStoreFragmentName",
    "decryptionFragmentName"
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
    @JsonProperty("credentialStoreFragmentName")
    private String credentialStoreFragmentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragmentName")
    private String decryptionFragmentName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AProtocolFragment() {
    }

    /**
     * 
     * @param decryptionFragmentName
     * @param credentialStoreFragmentName
     */
    public AProtocolFragment(String credentialStoreFragmentName, String decryptionFragmentName) {
        super();
        this.credentialStoreFragmentName = credentialStoreFragmentName;
        this.decryptionFragmentName = decryptionFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreFragmentName")
    public String getCredentialStoreFragmentName() {
        return credentialStoreFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialStoreFragmentName")
    public void setCredentialStoreFragmentName(String credentialStoreFragmentName) {
        this.credentialStoreFragmentName = credentialStoreFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragmentName")
    public String getDecryptionFragmentName() {
        return decryptionFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("decryptionFragmentName")
    public void setDecryptionFragmentName(String decryptionFragmentName) {
        this.decryptionFragmentName = decryptionFragmentName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("credentialStoreFragmentName", credentialStoreFragmentName).append("decryptionFragmentName", decryptionFragmentName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(decryptionFragmentName).append(credentialStoreFragmentName).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(decryptionFragmentName, rhs.decryptionFragmentName).append(credentialStoreFragmentName, rhs.credentialStoreFragmentName).isEquals();
    }

}
