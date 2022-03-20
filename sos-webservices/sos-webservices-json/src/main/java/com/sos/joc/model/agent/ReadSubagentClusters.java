
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * read subagent clusters
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentIds",
    "subagentClusterIds"
})
public class ReadSubagentClusters {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();
    @JsonProperty("subagentClusterIds")
    private List<String> subagentClusterIds = new ArrayList<String>();

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    @JsonProperty("agentIds")
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
    }

    @JsonProperty("subagentClusterIds")
    public List<String> getSubagentClusterIds() {
        return subagentClusterIds;
    }

    @JsonProperty("subagentClusterIds")
    public void setSubagentClusterIds(List<String> subagentClusterIds) {
        this.subagentClusterIds = subagentClusterIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentIds", agentIds).append("subagentClusterIds", subagentClusterIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentIds).append(controllerId).append(subagentClusterIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadSubagentClusters) == false) {
            return false;
        }
        ReadSubagentClusters rhs = ((ReadSubagentClusters) other);
        return new EqualsBuilder().append(agentIds, rhs.agentIds).append(controllerId, rhs.controllerId).append(subagentClusterIds, rhs.subagentClusterIds).isEquals();
    }

}
