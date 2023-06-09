
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
    "agentPath"
})
public class ClusterSwitchOver
    extends Command
{

    @JsonProperty("agentPath")
    private String agentPath;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterSwitchOver() {
    }

    /**
     * 
     * @param agentPath
     */
    public ClusterSwitchOver(String agentPath) {
        super();
        this.agentPath = agentPath;
    }

    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

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
        if ((other instanceof ClusterSwitchOver) == false) {
            return false;
        }
        ClusterSwitchOver rhs = ((ClusterSwitchOver) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentPath, rhs.agentPath).isEquals();
    }

}
