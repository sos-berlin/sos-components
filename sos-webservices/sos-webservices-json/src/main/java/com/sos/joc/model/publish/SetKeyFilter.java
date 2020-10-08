
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.pgp.JocKeyPair;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * set key filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "keys",
    "keyAlgorithm"
})
public class SetKeyFilter {

    /**
     * SOS PGP Key Pair
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("keys")
    private JocKeyPair keys;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    private String keyAlgorithm;

    /**
     * SOS PGP Key Pair
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("keys")
    public JocKeyPair getKeys() {
        return keys;
    }

    /**
     * SOS PGP Key Pair
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("keys")
    public void setKeys(JocKeyPair keys) {
        this.keys = keys;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("keys", keys).append("keyAlgorithm", keyAlgorithm).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(keys).append(keyAlgorithm).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SetKeyFilter) == false) {
            return false;
        }
        SetKeyFilter rhs = ((SetKeyFilter) other);
        return new EqualsBuilder().append(keys, rhs.keys).append(keyAlgorithm, rhs.keyAlgorithm).isEquals();
    }

}
