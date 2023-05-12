
package com.sos.joc.model.security.properties.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.fido2.CipherTypes;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FIDO2 Connect Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamFido2UserVerification",
    "iamFido2Timeout",
    "iamFido2Attestation",
    "iamFido2Transports",
    "iamFido2EmailSettings",
    "iamFido2CipherType"
})
public class Fido2Properties {

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
     * Fido2 Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2EmailSettings")
    private Fido2EmailSettings iamFido2EmailSettings;
    /**
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2CipherType")
    private CipherTypes iamFido2CipherType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2Properties() {
    }

    /**
     * 
     * @param iamFido2EmailSettings
     * @param iamFido2Attestation
     * @param iamFido2Timeout
     * @param iamFido2CipherType
     * @param iamFido2Transports
     * @param iamFido2UserVerification
     */
    public Fido2Properties(Fido2Userverification iamFido2UserVerification, Integer iamFido2Timeout, Fido2Attestation iamFido2Attestation, Fido2Transports iamFido2Transports, Fido2EmailSettings iamFido2EmailSettings, CipherTypes iamFido2CipherType) {
        super();
        this.iamFido2UserVerification = iamFido2UserVerification;
        this.iamFido2Timeout = iamFido2Timeout;
        this.iamFido2Attestation = iamFido2Attestation;
        this.iamFido2Transports = iamFido2Transports;
        this.iamFido2EmailSettings = iamFido2EmailSettings;
        this.iamFido2CipherType = iamFido2CipherType;
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

    /**
     * Fido2 Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2EmailSettings")
    public Fido2EmailSettings getIamFido2EmailSettings() {
        return iamFido2EmailSettings;
    }

    /**
     * Fido2 Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2EmailSettings")
    public void setIamFido2EmailSettings(Fido2EmailSettings iamFido2EmailSettings) {
        this.iamFido2EmailSettings = iamFido2EmailSettings;
    }

    /**
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2CipherType")
    public CipherTypes getIamFido2CipherType() {
        return iamFido2CipherType;
    }

    /**
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2CipherType")
    public void setIamFido2CipherType(CipherTypes iamFido2CipherType) {
        this.iamFido2CipherType = iamFido2CipherType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamFido2UserVerification", iamFido2UserVerification).append("iamFido2Timeout", iamFido2Timeout).append("iamFido2Attestation", iamFido2Attestation).append("iamFido2Transports", iamFido2Transports).append("iamFido2EmailSettings", iamFido2EmailSettings).append("iamFido2CipherType", iamFido2CipherType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamFido2EmailSettings).append(iamFido2Attestation).append(iamFido2Timeout).append(iamFido2CipherType).append(iamFido2Transports).append(iamFido2UserVerification).toHashCode();
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
        return new EqualsBuilder().append(iamFido2EmailSettings, rhs.iamFido2EmailSettings).append(iamFido2Attestation, rhs.iamFido2Attestation).append(iamFido2Timeout, rhs.iamFido2Timeout).append(iamFido2CipherType, rhs.iamFido2CipherType).append(iamFido2Transports, rhs.iamFido2Transports).append(iamFido2UserVerification, rhs.iamFido2UserVerification).isEquals();
    }

}
