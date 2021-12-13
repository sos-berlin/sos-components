
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "schedulingType"
})
public class DeployClusterAgent {

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
     * AgentClusterSchedulingType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulingType")
    private AgentClusterSchedulingType schedulingType = AgentClusterSchedulingType.fromValue("ROUND_ROBIN");

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
     * AgentClusterSchedulingType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulingType")
    public AgentClusterSchedulingType getSchedulingType() {
        return schedulingType;
    }

    /**
     * AgentClusterSchedulingType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("schedulingType")
    public void setSchedulingType(AgentClusterSchedulingType schedulingType) {
        this.schedulingType = schedulingType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("schedulingType", schedulingType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schedulingType).append(agentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployClusterAgent) == false) {
            return false;
        }
        DeployClusterAgent rhs = ((DeployClusterAgent) other);
        return new EqualsBuilder().append(schedulingType, rhs.schedulingType).append(agentId, rhs.agentId).isEquals();
    }

}
