
package com.sos.joc.model.encipherment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * filter for an assignment of a certificate to an agent for encipherment
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "certAlias"
})
public class AgentAssignmentRequestFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    private String certAlias;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    public String getCertAlias() {
        return certAlias;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("certAlias")
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("certAlias", certAlias).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(certAlias).append(agentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentAssignmentRequestFilter) == false) {
            return false;
        }
        AgentAssignmentRequestFilter rhs = ((AgentAssignmentRequestFilter) other);
        return new EqualsBuilder().append(certAlias, rhs.certAlias).append(agentId, rhs.agentId).isEquals();
    }

}
