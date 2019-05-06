
package com.sos.jobscheduler.model.deploy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "signatureString"
})
public class Signature {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private SignatureType tYPE = SignatureType.fromValue("PGP");
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signatureString")
    private String signatureString;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public SignatureType getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(SignatureType tYPE) {
        this.tYPE = tYPE;
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("signatureString", signatureString).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(signatureString).toHashCode();
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
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(signatureString, rhs.signatureString).isEquals();
    }

}
