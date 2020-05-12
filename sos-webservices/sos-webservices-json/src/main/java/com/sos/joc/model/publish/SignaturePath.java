
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Signature of a JS object
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "objectPath",
    "signature"
})
public class SignaturePath {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("objectPath")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String objectPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signature")
    private Signature signature;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("objectPath")
    public String getObjectPath() {
        return objectPath;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("objectPath")
    public void setObjectPath(String objectPath) {
        this.objectPath = objectPath;
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
        return new ToStringBuilder(this).append("objectPath", objectPath).append("signature", signature).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(objectPath).append(signature).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SignaturePath) == false) {
            return false;
        }
        SignaturePath rhs = ((SignaturePath) other);
        return new EqualsBuilder().append(objectPath, rhs.objectPath).append(signature, rhs.signature).isEquals();
    }

}
