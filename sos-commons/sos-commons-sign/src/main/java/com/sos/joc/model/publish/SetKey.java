
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.sign.JocKeyPair;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * set key
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "keys"
})
public class SetKey {

    /**
     * SOS Key Pair
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("keys")
    private JocKeyPair keys;

    /**
     * SOS Key Pair
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
     * SOS Key Pair
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("keys")
    public void setKeys(JocKeyPair keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("keys", keys).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(keys).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SetKey) == false) {
            return false;
        }
        SetKey rhs = ((SetKey) other);
        return new EqualsBuilder().append(keys, rhs.keys).isEquals();
    }

}
