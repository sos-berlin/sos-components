
package com.sos.controller.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ClusterSwitchOver
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId"
})
public class ClusterSwitchOver
    extends Command
{

    @JsonProperty("agentId")
    private String agentId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterSwitchOver() {
    }

    /**
     * 
     * @param agentId
     */
    public ClusterSwitchOver(String agentId) {
        super();
        this.agentId = agentId;
    }

    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
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
        if ((other instanceof ClusterSwitchOver) == false) {
            return false;
        }
        ClusterSwitchOver rhs = ((ClusterSwitchOver) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentId, rhs.agentId).isEquals();
    }

}
