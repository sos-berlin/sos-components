
package com.sos.joc.model.security.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.fido2.Fido2Properties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Authentication Start Response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "challenge",
    "credentialId",
    "fido2Properties"
})
public class Fido2AuthenticationStartResponse {

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
     * FIDO2 Properties
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
    public Fido2AuthenticationStartResponse() {
    }

    /**
     * 
     * @param fido2Properties
     * @param challenge
     * @param credentialId
     */
    public Fido2AuthenticationStartResponse(String challenge, String credentialId, Fido2Properties fido2Properties) {
        super();
        this.challenge = challenge;
        this.credentialId = credentialId;
        this.fido2Properties = fido2Properties;
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
     * FIDO2 Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido2Properties")
    public Fido2Properties getFido2Properties() {
        return fido2Properties;
    }

    /**
     * FIDO2 Properties
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
        return new ToStringBuilder(this).append("challenge", challenge).append("credentialId", credentialId).append("fido2Properties", fido2Properties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(challenge).append(credentialId).append(fido2Properties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2AuthenticationStartResponse) == false) {
            return false;
        }
        Fido2AuthenticationStartResponse rhs = ((Fido2AuthenticationStartResponse) other);
        return new EqualsBuilder().append(challenge, rhs.challenge).append(credentialId, rhs.credentialId).append(fido2Properties, rhs.fido2Properties).isEquals();
    }

}
