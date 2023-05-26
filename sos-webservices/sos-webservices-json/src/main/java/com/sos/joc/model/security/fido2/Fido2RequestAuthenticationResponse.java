
package com.sos.joc.model.security.fido2;

import java.util.ArrayList;
import java.util.List;
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
    "credentialIds",
    "challenge",
    "fido2Properties"
})
public class Fido2RequestAuthenticationResponse {

    @JsonProperty("credentialIds")
    private List<String> credentialIds = new ArrayList<String>();
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
     * @param challenge
     * @param credentialIds
     */
    public Fido2RequestAuthenticationResponse(List<String> credentialIds, String challenge, Fido2Properties fido2Properties) {
        super();
        this.credentialIds = credentialIds;
        this.challenge = challenge;
        this.fido2Properties = fido2Properties;
    }

    @JsonProperty("credentialIds")
    public List<String> getCredentialIds() {
        return credentialIds;
    }

    @JsonProperty("credentialIds")
    public void setCredentialIds(List<String> credentialIds) {
        this.credentialIds = credentialIds;
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
        return new ToStringBuilder(this).append("credentialIds", credentialIds).append("challenge", challenge).append("fido2Properties", fido2Properties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(challenge).append(credentialIds).append(fido2Properties).toHashCode();
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
        return new EqualsBuilder().append(challenge, rhs.challenge).append(credentialIds, rhs.credentialIds).append(fido2Properties, rhs.fido2Properties).isEquals();
    }

}
