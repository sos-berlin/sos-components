
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
 * cluster agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "subagentClusterId",
    "subagentIds"
})
public class SubagentCluster {

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentClusterId")
    private String subagentClusterId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentIds")
    private List<SubAgentId> subagentIds = new ArrayList<SubAgentId>();

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentClusterId")
    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentClusterId")
    public void setSubagentClusterId(String subagentClusterId) {
        this.subagentClusterId = subagentClusterId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentIds")
    public List<SubAgentId> getSubagentIds() {
        return subagentIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentIds")
    public void setSubagentIds(List<SubAgentId> subagentIds) {
        this.subagentIds = subagentIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("subagentClusterId", subagentClusterId).append("subagentIds", subagentIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(subagentClusterId).append(subagentIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubagentCluster) == false) {
            return false;
        }
        SubagentCluster rhs = ((SubagentCluster) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(subagentClusterId, rhs.subagentClusterId).append(subagentIds, rhs.subagentIds).isEquals();
    }

}
