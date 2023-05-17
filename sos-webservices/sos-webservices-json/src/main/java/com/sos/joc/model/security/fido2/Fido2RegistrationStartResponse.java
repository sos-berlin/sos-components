
package com.sos.joc.model.security.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.fido2.Fido2Properties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Registration Start Response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "challenge",
    "fido2Properties"
})
public class Fido2RegistrationStartResponse {

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
    public Fido2RegistrationStartResponse() {
    }

    /**
     * 
     * @param fido2Properties
     * @param challenge
     */
    public Fido2RegistrationStartResponse(String challenge, Fido2Properties fido2Properties) {
        super();
        this.challenge = challenge;
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
        return new ToStringBuilder(this).append("challenge", challenge).append("fido2Properties", fido2Properties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fido2Properties).append(challenge).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2RegistrationStartResponse) == false) {
            return false;
        }
        Fido2RegistrationStartResponse rhs = ((Fido2RegistrationStartResponse) other);
        return new EqualsBuilder().append(fido2Properties, rhs.fido2Properties).append(challenge, rhs.challenge).isEquals();
    }

}
