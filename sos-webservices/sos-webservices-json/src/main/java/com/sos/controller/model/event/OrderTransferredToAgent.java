
package com.sos.controller.model.event;

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
    "agentPath"
})
public class OrderTransferredToAgent
    extends Event
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    private String agentPath;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderTransferredToAgent() {
    }

    /**
     * 
     * @param agentPath
     * @param eventId
     * 
     */
    public OrderTransferredToAgent(String agentPath, Long eventId) {
        super(eventId);
        this.agentPath = agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("agentPath", agentPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentPath).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentPath, rhs.agentPath).isEquals();
    }

}
