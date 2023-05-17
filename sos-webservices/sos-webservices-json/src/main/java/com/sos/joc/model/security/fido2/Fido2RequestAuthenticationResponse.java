
package com.sos.joc.model.security.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.fido2.Fido2Properties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Request Authentication Response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "credentialId",
    "challenge",
    "fido2Properties"
})
public class Fido2RequestAuthenticationResponse {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("credentialId")
    private String credentialId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("challenge")
    private String challenge;
    /**
     * FIDO2 Connect Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido2Properties")
    private Fido2Properties fido2Properties;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2RequestAuthenticationResponse() {
    }

    /**
     * 
     * @param fido2Properties
     * @param credentialId
     * @param challenge
     */
    public Fido2RequestAuthenticationResponse(String credentialId, String challenge, Fido2Properties fido2Properties) {
        super();
        this.credentialId = credentialId;
        this.challenge = challenge;
        this.fido2Properties = fido2Properties;
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
     * FIDO2 Connect Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido2Properties")
    public Fido2Properties getFido2Properties() {
        return fido2Properties;
    }

    /**
     * FIDO2 Connect Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido2Properties")
    public void setFido2Properties(Fido2Properties fido2Properties) {
        this.fido2Properties = fido2Properties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("credentialId", credentialId).append("challenge", challenge).append("fido2Properties", fido2Properties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(credentialId).append(challenge).append(fido2Properties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2RequestAuthenticationResponse) == false) {
            return false;
        }
        Fido2RequestAuthenticationResponse rhs = ((Fido2RequestAuthenticationResponse) other);
        return new EqualsBuilder().append(credentialId, rhs.credentialId).append(challenge, rhs.challenge).append(fido2Properties, rhs.fido2Properties).isEquals();
    }

}
