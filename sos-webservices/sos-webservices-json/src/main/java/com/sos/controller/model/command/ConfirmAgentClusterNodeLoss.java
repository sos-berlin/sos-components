
package com.sos.controller.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ConfirmAgentClusterNodeLoss
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentPath",
    "lostNodeId",
    "confirmer"
})
public class ConfirmAgentClusterNodeLoss
    extends Command
{

    @JsonProperty("agentPath")
    private String agentPath;
    @JsonProperty("lostNodeId")
    private String lostNodeId;
    @JsonProperty("confirmer")
    private String confirmer;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ConfirmAgentClusterNodeLoss() {
    }

    /**
     * 
     * @param agentPath
     * @param lostNodeId
     * @param confirmer
     */
    public ConfirmAgentClusterNodeLoss(String agentPath, String lostNodeId, String confirmer) {
        super();
        this.agentPath = agentPath;
        this.lostNodeId = lostNodeId;
        this.confirmer = confirmer;
    }

    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
    }

    @JsonProperty("lostNodeId")
    public String getLostNodeId() {
        return lostNodeId;
    }

    @JsonProperty("lostNodeId")
    public void setLostNodeId(String lostNodeId) {
        this.lostNodeId = lostNodeId;
    }

    @JsonProperty("confirmer")
    public String getConfirmer() {
        return confirmer;
    }

    @JsonProperty("confirmer")
    public void setConfirmer(String confirmer) {
        this.confirmer = confirmer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("agentPath", agentPath).append("lostNodeId", lostNodeId).append("confirmer", confirmer).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(agentPath).append(lostNodeId).append(confirmer).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfirmAgentClusterNodeLoss) == false) {
            return false;
        }
        ConfirmAgentClusterNodeLoss rhs = ((ConfirmAgentClusterNodeLoss) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(agentPath, rhs.agentPath).append(lostNodeId, rhs.lostNodeId).append(confirmer, rhs.confirmer).isEquals();
    }

}
