
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.AgentId;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderTransferredToAgent event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "agentId"
})
public class OrderTransferredToAgent
    extends Event
{

    /**
     * agentId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private AgentId agentId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderTransferredToAgent() {
    }

    /**
     * 
     * @param agentId
     */
    public OrderTransferredToAgent(AgentId agentId) {
        super();
        this.agentId = agentId;
    }

    /**
     * agentId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public AgentId getAgentId() {
        return agentId;
    }

    /**
     * agentId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(AgentId agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("agentId", agentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderTransferredToAgent) == false) {
            return false;
        }
        OrderTransferredToAgent rhs = ((OrderTransferredToAgent) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentId, rhs.agentId).isEquals();
    }

}
