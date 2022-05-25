
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ordering agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "predecessorAgentId"
})
public class OrderingAgents {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorAgentId")
    private String predecessorAgentId;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorAgentId")
    public String getPredecessorAgentId() {
        return predecessorAgentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("predecessorAgentId")
    public void setPredecessorAgentId(String predecessorAgentId) {
        this.predecessorAgentId = predecessorAgentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("predecessorAgentId", predecessorAgentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(predecessorAgentId).append(agentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderingAgents) == false) {
            return false;
        }
        OrderingAgents rhs = ((OrderingAgents) other);
        return new EqualsBuilder().append(predecessorAgentId, rhs.predecessorAgentId).append(agentId, rhs.agentId).isEquals();
    }

}
