
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.controller.ClusterNodeState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * subagent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "isDirector",
    "clusterNodeState"
})
public class SubagentV
    extends AgentStateV
{

    /**
     * SubagentDiretoryType
     * <p>
     * 
     * 
     */
    @JsonProperty("isDirector")
    private SubagentDirectorType isDirector = SubagentDirectorType.fromValue("NO_DIRECTOR");
    /**
     * active state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterNodeState")
    private ClusterNodeState clusterNodeState;

    /**
     * SubagentDiretoryType
     * <p>
     * 
     * 
     */
    @JsonProperty("isDirector")
    public SubagentDirectorType getIsDirector() {
        return isDirector;
    }

    /**
     * SubagentDiretoryType
     * <p>
     * 
     * 
     */
    @JsonProperty("isDirector")
    public void setIsDirector(SubagentDirectorType isDirector) {
        this.isDirector = isDirector;
    }

    /**
     * active state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterNodeState")
    public ClusterNodeState getClusterNodeState() {
        return clusterNodeState;
    }

    /**
     * active state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterNodeState")
    public void setClusterNodeState(ClusterNodeState clusterNodeState) {
        this.clusterNodeState = clusterNodeState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("isDirector", isDirector).append("clusterNodeState", clusterNodeState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(clusterNodeState).append(isDirector).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubagentV) == false) {
            return false;
        }
        SubagentV rhs = ((SubagentV) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(clusterNodeState, rhs.clusterNodeState).append(isDirector, rhs.isDirector).isEquals();
    }

}
