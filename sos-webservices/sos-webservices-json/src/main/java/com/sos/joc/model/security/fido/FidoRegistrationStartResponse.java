
package com.sos.joc.model.security.fido;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.fido.FidoProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Fido Registration Start Response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "challenge",
    "fidoProperties"
})
public class FidoRegistrationStartResponse {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("challenge")
    private String challenge;
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
    public FidoRegistrationStartResponse() {
    }

    /**
     * 
     * @param fidoProperties
     * @param challenge
     */
    public FidoRegistrationStartResponse(String challenge, FidoProperties fidoProperties) {
        super();
        this.challenge = challenge;
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
        return new ToStringBuilder(this).append("challenge", challenge).append("fidoProperties", fidoProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fidoProperties).append(challenge).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FidoRegistrationStartResponse) == false) {
            return false;
        }
        FidoRegistrationStartResponse rhs = ((FidoRegistrationStartResponse) other);
        return new EqualsBuilder().append(fidoProperties, rhs.fidoProperties).append(challenge, rhs.challenge).isEquals();
    }

}
