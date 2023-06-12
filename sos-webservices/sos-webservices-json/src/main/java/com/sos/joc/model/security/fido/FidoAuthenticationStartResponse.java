
package com.sos.joc.model.security.fido;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.fido.FidoProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido Authentication Start Response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "challenge",
    "credentialId",
    "fidoProperties"
})
public class FidoAuthenticationStartResponse {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("challenge")
    private String challenge;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialId")
    private String credentialId;
    /**
     * FIDO Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fidoProperties")
    private FidoProperties fidoProperties;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FidoAuthenticationStartResponse() {
    }

    /**
     * 
     * @param fidoProperties
     * @param challenge
     * @param credentialId
     */
    public FidoAuthenticationStartResponse(String challenge, String credentialId, FidoProperties fidoProperties) {
        super();
        this.challenge = challenge;
        this.credentialId = credentialId;
        this.fidoProperties = fidoProperties;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("challenge")
    public String getChallenge() {
        return challenge;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("challenge")
    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialId")
    public String getCredentialId() {
        return credentialId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialId")
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    /**
     * FIDO Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fidoProperties")
    public FidoProperties getFidoProperties() {
        return fidoProperties;
    }

    /**
     * FIDO Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fidoProperties")
    public void setFidoProperties(FidoProperties fidoProperties) {
        this.fidoProperties = fidoProperties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("challenge", challenge).append("credentialId", credentialId).append("fidoProperties", fidoProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fidoProperties).append(challenge).append(credentialId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FidoAuthenticationStartResponse) == false) {
            return false;
        }
        FidoAuthenticationStartResponse rhs = ((FidoAuthenticationStartResponse) other);
        return new EqualsBuilder().append(fidoProperties, rhs.fidoProperties).append(challenge, rhs.challenge).append(credentialId, rhs.credentialId).isEquals();
    }

}
