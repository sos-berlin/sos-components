
package com.sos.joc.model.security.properties.fido2;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "iamFido2ResidenKey",
    "iamFido2Transports",
    "iamFido2EmailSettings"
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
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2ResidenKey")
    private Fido2Userverification iamFido2ResidenKey;
    @JsonProperty("iamFido2Transports")
    private List<Fido2Transports> iamFido2Transports = new ArrayList<Fido2Transports>();
    /**
     * Fido2 Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2EmailSettings")
    private Fido2EmailSettings iamFido2EmailSettings;

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
     * @param iamFido2Transports
     * @param iamFido2UserVerification
     * @param iamFido2ResidenKey
     */
    public Fido2Properties(Fido2Userverification iamFido2UserVerification, Integer iamFido2Timeout, Fido2Attestation iamFido2Attestation, Fido2Userverification iamFido2ResidenKey, List<Fido2Transports> iamFido2Transports, Fido2EmailSettings iamFido2EmailSettings) {
        super();
        this.iamFido2UserVerification = iamFido2UserVerification;
        this.iamFido2Timeout = iamFido2Timeout;
        this.iamFido2Attestation = iamFido2Attestation;
        this.iamFido2ResidenKey = iamFido2ResidenKey;
        this.iamFido2Transports = iamFido2Transports;
        this.iamFido2EmailSettings = iamFido2EmailSettings;
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
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2ResidenKey")
    public Fido2Userverification getIamFido2ResidenKey() {
        return iamFido2ResidenKey;
    }

    /**
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2ResidenKey")
    public void setIamFido2ResidenKey(Fido2Userverification iamFido2ResidenKey) {
        this.iamFido2ResidenKey = iamFido2ResidenKey;
    }

    @JsonProperty("iamFido2Transports")
    public List<Fido2Transports> getIamFido2Transports() {
        return iamFido2Transports;
    }

    @JsonProperty("iamFido2Transports")
    public void setIamFido2Transports(List<Fido2Transports> iamFido2Transports) {
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamFido2UserVerification", iamFido2UserVerification).append("iamFido2Timeout", iamFido2Timeout).append("iamFido2Attestation", iamFido2Attestation).append("iamFido2ResidenKey", iamFido2ResidenKey).append("iamFido2Transports", iamFido2Transports).append("iamFido2EmailSettings", iamFido2EmailSettings).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamFido2EmailSettings).append(iamFido2Attestation).append(iamFido2Timeout).append(iamFido2Transports).append(iamFido2UserVerification).append(iamFido2ResidenKey).toHashCode();
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
        return new EqualsBuilder().append(iamFido2EmailSettings, rhs.iamFido2EmailSettings).append(iamFido2Attestation, rhs.iamFido2Attestation).append(iamFido2Timeout, rhs.iamFido2Timeout).append(iamFido2Transports, rhs.iamFido2Transports).append(iamFido2UserVerification, rhs.iamFido2UserVerification).append(iamFido2ResidenKey, rhs.iamFido2ResidenKey).isEquals();
    }

}
