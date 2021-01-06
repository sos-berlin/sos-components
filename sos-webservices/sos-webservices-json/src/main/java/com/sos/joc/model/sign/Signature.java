
package com.sos.joc.model.sign;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * signature string of a signed object
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "signatureType",
    "signatureString"
})
public class Signature {

    /**
     * signature type of a signed object
     * <p>
     * 
     * 
     */
    @JsonProperty("signatureType")
    private SignatureType signatureType = SignatureType.fromValue("X509");
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("signatureString")
    private String signatureString;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Signature() {
    }

    /**
     * 
     * @param signatureType
     * @param signatureString
     */
    public Signature(SignatureType signatureType, String signatureString) {
        super();
        this.signatureType = signatureType;
        this.signatureString = signatureString;
    }

    /**
     * signature type of a signed object
     * <p>
     * 
     * 
     */
    @JsonProperty("signatureType")
    public SignatureType getSignatureType() {
        return signatureType;
    }

    /**
     * signature type of a signed object
     * <p>
     * 
     * 
     */
    @JsonProperty("signatureType")
    public void setSignatureType(SignatureType signatureType) {
        this.signatureType = signatureType;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("signatureType", signatureType).append("signatureString", signatureString).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(signatureType).append(signatureString).append(additionalProperties).toHashCode();
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
        return new EqualsBuilder().append(signatureType, rhs.signatureType).append(signatureString, rhs.signatureString).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
