
package com.sos.joc.model.security.identityservice;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "iamFido2UserVerification",
    "iamFido2Timeout",
    "iamFido2Attestation",
    "iamFido2Transports",
    "iamIconUrl"
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
    @JsonProperty("iamFido2Transports")
    private List<Fido2Transports> iamFido2Transports = new ArrayList<Fido2Transports>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    private String iamIconUrl;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2IdentityProvider() {
    }

    /**
     * 
     * @param iamFido2Attestation
     * @param identityServiceName
     * @param iamIconUrl
     * @param iamFido2Timeout
     * @param iamFido2Transports
     * @param iamFido2UserVerification
     */
    public Fido2IdentityProvider(String identityServiceName, Fido2Userverification iamFido2UserVerification, Integer iamFido2Timeout, Fido2Attestation iamFido2Attestation, List<Fido2Transports> iamFido2Transports, String iamIconUrl) {
        super();
        this.identityServiceName = identityServiceName;
        this.iamFido2UserVerification = iamFido2UserVerification;
        this.iamFido2Timeout = iamFido2Timeout;
        this.iamFido2Attestation = iamFido2Attestation;
        this.iamFido2Transports = iamFido2Transports;
        this.iamIconUrl = iamIconUrl;
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

    @JsonProperty("iamFido2Transports")
    public List<Fido2Transports> getIamFido2Transports() {
        return iamFido2Transports;
    }

    @JsonProperty("iamFido2Transports")
    public void setIamFido2Transports(List<Fido2Transports> iamFido2Transports) {
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("iamFido2UserVerification", iamFido2UserVerification).append("iamFido2Timeout", iamFido2Timeout).append("iamFido2Attestation", iamFido2Attestation).append("iamFido2Transports", iamFido2Transports).append("iamIconUrl", iamIconUrl).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamFido2Attestation).append(identityServiceName).append(iamIconUrl).append(iamFido2Timeout).append(iamFido2Transports).append(iamFido2UserVerification).toHashCode();
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
        return new EqualsBuilder().append(iamFido2Attestation, rhs.iamFido2Attestation).append(identityServiceName, rhs.identityServiceName).append(iamIconUrl, rhs.iamIconUrl).append(iamFido2Timeout, rhs.iamFido2Timeout).append(iamFido2Transports, rhs.iamFido2Transports).append(iamFido2UserVerification, rhs.iamFido2UserVerification).isEquals();
    }

}
