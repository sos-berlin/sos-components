
package com.sos.joc.model.security.identityservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.fido2.CipherTypes;
import com.sos.joc.model.security.properties.fido2.Fido2Attestation;
import com.sos.joc.model.security.properties.fido2.Fido2Transports;
import com.sos.joc.model.security.properties.fido2.Fido2Userverification;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FIDO2 Identity Provider
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "iamFido2RpName",
    "iamFido2UserVerification",
    "iamFido2Timeout",
    "iamFido2Attestation",
    "iamFido2Transports",
    "iamIconUrl",
    "iamCipherType"
})
public class Fido2IdentityProvider {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    private String iamIconUrl;
    /**
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("iamCipherType")
    private CipherTypes iamCipherType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2IdentityProvider() {
    }

    /**
     * 
     * @param iamFido2Attestation
     * @param iamCipherType
     * @param identityServiceName
     * @param iamFido2RpName
     * @param iamIconUrl
     * @param iamFido2Timeout
     * @param iamFido2Transports
     * @param iamFido2UserVerification
     */
    public Fido2IdentityProvider(String identityServiceName, String iamFido2RpName, Fido2Userverification iamFido2UserVerification, Integer iamFido2Timeout, Fido2Attestation iamFido2Attestation, Fido2Transports iamFido2Transports, String iamIconUrl, CipherTypes iamCipherType) {
        super();
        this.identityServiceName = identityServiceName;
        this.iamFido2RpName = iamFido2RpName;
        this.iamFido2UserVerification = iamFido2UserVerification;
        this.iamFido2Timeout = iamFido2Timeout;
        this.iamFido2Attestation = iamFido2Attestation;
        this.iamFido2Transports = iamFido2Transports;
        this.iamIconUrl = iamIconUrl;
        this.iamCipherType = iamCipherType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    public String getIamIconUrl() {
        return iamIconUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    public void setIamIconUrl(String iamIconUrl) {
        this.iamIconUrl = iamIconUrl;
    }

    /**
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("iamCipherType")
    public CipherTypes getIamCipherType() {
        return iamCipherType;
    }

    /**
     * Cipher Types
     * <p>
     * 
     * 
     */
    @JsonProperty("iamCipherType")
    public void setIamCipherType(CipherTypes iamCipherType) {
        this.iamCipherType = iamCipherType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("iamFido2RpName", iamFido2RpName).append("iamFido2UserVerification", iamFido2UserVerification).append("iamFido2Timeout", iamFido2Timeout).append("iamFido2Attestation", iamFido2Attestation).append("iamFido2Transports", iamFido2Transports).append("iamIconUrl", iamIconUrl).append("iamCipherType", iamCipherType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamFido2Attestation).append(iamCipherType).append(identityServiceName).append(iamFido2RpName).append(iamIconUrl).append(iamFido2Timeout).append(iamFido2Transports).append(iamFido2UserVerification).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2IdentityProvider) == false) {
            return false;
        }
        Fido2IdentityProvider rhs = ((Fido2IdentityProvider) other);
        return new EqualsBuilder().append(iamFido2Attestation, rhs.iamFido2Attestation).append(iamCipherType, rhs.iamCipherType).append(identityServiceName, rhs.identityServiceName).append(iamFido2RpName, rhs.iamFido2RpName).append(iamIconUrl, rhs.iamIconUrl).append(iamFido2Timeout, rhs.iamFido2Timeout).append(iamFido2Transports, rhs.iamFido2Transports).append(iamFido2UserVerification, rhs.iamFido2UserVerification).isEquals();
    }

}
