
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.jobscheduler.model.workflow.AgentId;
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
    "agentId"
})
public class OrderTransferredToAgent
    extends Event
    implements IEvent
{

    /**
     * agentId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    @JacksonXmlProperty(localName = "agentId")
    private AgentId agentId;

    /**
     * agentId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    @JacksonXmlProperty(localName = "agentId")
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
    @JacksonXmlProperty(localName = "agentId")
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
