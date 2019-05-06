
package com.sos.jobscheduler.model.deploy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * signed object
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "string",
    "signature"
})
public class SignedObject {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("string")
    private String string;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signature")
    private Signature signature;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("string")
    public String getString() {
        return string;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("string")
    public void setString(String string) {
        this.string = string;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signature")
    public Signature getSignature() {
        return signature;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signature")
    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("string", string).append("signature", signature).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(string).append(signature).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SignedObject) == false) {
            return false;
        }
        SignedObject rhs = ((SignedObject) other);
        return new EqualsBuilder().append(string, rhs.string).append(signature, rhs.signature).isEquals();
    }

}
