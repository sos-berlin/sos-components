
package com.sos.joc.model.security.properties.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Openid Connect Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamFido2RpName",
    "iamFido2UserVerification",
    "iamFido2Timeout",
    "iamFido2Attestation",
    "iamFido2Transports"
})
public class Fido2Properties {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2RpName")
    private String iamFido2RpName;
    /**
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2UserVerification")
    private Fido2Userverification iamFido2UserVerification;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Timeout")
    private Integer iamFido2Timeout;
    /**
     * Fido2 Attestation
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attestation")
    private Fido2Attestation iamFido2Attestation;
    /**
     * Fido2 Transports
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Transports")
    private Fido2Transports iamFido2Transports;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2Properties() {
    }

    /**
     * 
     * @param iamFido2Attestation
     * @param iamFido2RpName
     * @param iamFido2Timeout
     * @param iamFido2Transports
     * @param iamFido2UserVerification
     */
    public Fido2Properties(String iamFido2RpName, Fido2Userverification iamFido2UserVerification, Integer iamFido2Timeout, Fido2Attestation iamFido2Attestation, Fido2Transports iamFido2Transports) {
        super();
        this.iamFido2RpName = iamFido2RpName;
        this.iamFido2UserVerification = iamFido2UserVerification;
        this.iamFido2Timeout = iamFido2Timeout;
        this.iamFido2Attestation = iamFido2Attestation;
        this.iamFido2Transports = iamFido2Transports;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2RpName")
    public String getIamFido2RpName() {
        return iamFido2RpName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2RpName")
    public void setIamFido2RpName(String iamFido2RpName) {
        this.iamFido2RpName = iamFido2RpName;
    }

    /**
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2UserVerification")
    public Fido2Userverification getIamFido2UserVerification() {
        return iamFido2UserVerification;
    }

    /**
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2UserVerification")
    public void setIamFido2UserVerification(Fido2Userverification iamFido2UserVerification) {
        this.iamFido2UserVerification = iamFido2UserVerification;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Timeout")
    public Integer getIamFido2Timeout() {
        return iamFido2Timeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Timeout")
    public void setIamFido2Timeout(Integer iamFido2Timeout) {
        this.iamFido2Timeout = iamFido2Timeout;
    }

    /**
     * Fido2 Attestation
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attestation")
    public Fido2Attestation getIamFido2Attestation() {
        return iamFido2Attestation;
    }

    /**
     * Fido2 Attestation
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attestation")
    public void setIamFido2Attestation(Fido2Attestation iamFido2Attestation) {
        this.iamFido2Attestation = iamFido2Attestation;
    }

    /**
     * Fido2 Transports
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Transports")
    public Fido2Transports getIamFido2Transports() {
        return iamFido2Transports;
    }

    /**
     * Fido2 Transports
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Transports")
    public void setIamFido2Transports(Fido2Transports iamFido2Transports) {
        this.iamFido2Transports = iamFido2Transports;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamFido2RpName", iamFido2RpName).append("iamFido2UserVerification", iamFido2UserVerification).append("iamFido2Timeout", iamFido2Timeout).append("iamFido2Attestation", iamFido2Attestation).append("iamFido2Transports", iamFido2Transports).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamFido2Attestation).append(iamFido2RpName).append(iamFido2Transports).append(iamFido2Timeout).append(iamFido2UserVerification).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2Properties) == false) {
            return false;
        }
        Fido2Properties rhs = ((Fido2Properties) other);
        return new EqualsBuilder().append(iamFido2Attestation, rhs.iamFido2Attestation).append(iamFido2RpName, rhs.iamFido2RpName).append(iamFido2Transports, rhs.iamFido2Transports).append(iamFido2Timeout, rhs.iamFido2Timeout).append(iamFido2UserVerification, rhs.iamFido2UserVerification).isEquals();
    }

}
