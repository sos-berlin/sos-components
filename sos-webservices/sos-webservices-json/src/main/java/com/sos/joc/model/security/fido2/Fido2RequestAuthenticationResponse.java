
package com.sos.joc.model.security.fido2;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "requestId"
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("requestId")
    private Long requestId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2RequestAuthenticationResponse() {
    }

    /**
     * 
     * @param requestId
     * @param challenge
     * @param credentialIds
     */
    public Fido2RequestAuthenticationResponse(List<String> credentialIds, String challenge, Long requestId) {
        super();
        this.credentialIds = credentialIds;
        this.challenge = challenge;
        this.requestId = requestId;
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("requestId")
    public Long getRequestId() {
        return requestId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("requestId")
    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("credentialIds", credentialIds).append("challenge", challenge).append("requestId", requestId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(challenge).append(credentialIds).append(requestId).toHashCode();
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
        return new EqualsBuilder().append(challenge, rhs.challenge).append(credentialIds, rhs.credentialIds).append(requestId, rhs.requestId).isEquals();
    }

}
