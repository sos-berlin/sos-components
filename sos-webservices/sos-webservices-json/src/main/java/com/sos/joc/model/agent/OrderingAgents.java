
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
    "ordering"
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    private Integer ordering;

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ordering")
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("ordering", ordering).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(ordering).toHashCode();
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
        return new EqualsBuilder().append(agentId, rhs.agentId).append(ordering, rhs.ordering).isEquals();
    }

}
