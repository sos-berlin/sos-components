
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "agentName"
})
public class OrderTransferredToAgent
    extends Event
{

    @JsonProperty("agentName")
    private String agentName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderTransferredToAgent() {
    }

    /**
     * 
     * @param eventId
     * @param agentName
     * 
     */
    public OrderTransferredToAgent(String agentName, Long eventId) {
        super(eventId);
        this.agentName = agentName;
    }

    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("agentName", agentName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentName).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentName, rhs.agentName).isEquals();
    }

}
