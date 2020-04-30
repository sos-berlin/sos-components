
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
    "signatureString"
})
public class Signature {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
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
    @JsonProperty("signatureString")
    private String signatureString;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
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
    @JsonProperty("signatureString")
    public String getSignatureString() {
        return signatureString;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signatureString")
    public void setSignatureString(String signatureString) {
        this.signatureString = signatureString;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("objectPath", objectPath).append("signatureString", signatureString).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(objectPath).append(signatureString).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Signature) == false) {
            return false;
        }
        Signature rhs = ((Signature) other);
        return new EqualsBuilder().append(objectPath, rhs.objectPath).append(signatureString, rhs.signatureString).isEquals();
    }

}
