
package com.sos.joc.model.pgp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * SOS PGP Key Pair
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "privateKey",
    "publicKey"
})
public class SOSPGPKeyPair {

    @JsonProperty("privateKey")
    private String privateKey;
    @JsonProperty("publicKey")
    private String publicKey;

    @JsonProperty("privateKey")
    public String getPrivateKey() {
        return privateKey;
    }

    @JsonProperty("privateKey")
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @JsonProperty("publicKey")
    public String getPublicKey() {
        return publicKey;
    }

    @JsonProperty("publicKey")
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("privateKey", privateKey).append("publicKey", publicKey).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(privateKey).append(publicKey).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SOSPGPKeyPair) == false) {
            return false;
        }
        SOSPGPKeyPair rhs = ((SOSPGPKeyPair) other);
        return new EqualsBuilder().append(privateKey, rhs.privateKey).append(publicKey, rhs.publicKey).isEquals();
    }

}
