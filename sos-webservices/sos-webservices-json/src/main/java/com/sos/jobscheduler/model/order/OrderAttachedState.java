
package com.sos.jobscheduler.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.AgentId;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderAttachedState
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "agentId"
})
public class OrderAttachedState {

    @JsonProperty("TYPE")
    private String tYPE;
    /**
     * agentId
     * <p>
     * 
     * 
     */
    @JsonProperty("agentId")
    private AgentId agentId;

    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * agentId
     * <p>
     * 
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
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(AgentId agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("agentId", agentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(agentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderAttachedState) == false) {
            return false;
        }
        OrderAttachedState rhs = ((OrderAttachedState) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(agentId, rhs.agentId).isEquals();
    }

}
