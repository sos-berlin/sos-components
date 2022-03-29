
package com.sos.joc.model.agent;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent names
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "agentNames",
    "clusterAgentNames",
    "subagentClusterIds"
})
public class AgentNames {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * Agent names of standalone Agents
     * 
     */
    @JsonProperty("agentNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Agent names of standalone Agents")
    private Set<String> agentNames = new LinkedHashSet<String>();
    /**
     * Agent names of Agent clusters
     * 
     */
    @JsonProperty("clusterAgentNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Agent names of Agent clusters")
    private Set<String> clusterAgentNames = new LinkedHashSet<String>();
    /**
     * Subagent Cluster IDs of Agent clusters
     * 
     */
    @JsonProperty("subagentClusterIds")
    @JsonPropertyDescription("Subagent Cluster IDs of Agent clusters")
    private SelectionIdsPerAgentName subagentClusterIds;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * Agent names of standalone Agents
     * 
     */
    @JsonProperty("agentNames")
    public Set<String> getAgentNames() {
        return agentNames;
    }

    /**
     * Agent names of standalone Agents
     * 
     */
    @JsonProperty("agentNames")
    public void setAgentNames(Set<String> agentNames) {
        this.agentNames = agentNames;
    }

    /**
     * Agent names of Agent clusters
     * 
     */
    @JsonProperty("clusterAgentNames")
    public Set<String> getClusterAgentNames() {
        return clusterAgentNames;
    }

    /**
     * Agent names of Agent clusters
     * 
     */
    @JsonProperty("clusterAgentNames")
    public void setClusterAgentNames(Set<String> clusterAgentNames) {
        this.clusterAgentNames = clusterAgentNames;
    }

    /**
     * Subagent Cluster IDs of Agent clusters
     * 
     */
    @JsonProperty("subagentClusterIds")
    public SelectionIdsPerAgentName getSubagentClusterIds() {
        return subagentClusterIds;
    }

    /**
     * Subagent Cluster IDs of Agent clusters
     * 
     */
    @JsonProperty("subagentClusterIds")
    public void setSubagentClusterIds(SelectionIdsPerAgentName subagentClusterIds) {
        this.subagentClusterIds = subagentClusterIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("agentNames", agentNames).append("clusterAgentNames", clusterAgentNames).append("subagentClusterIds", subagentClusterIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentNames).append(clusterAgentNames).append(deliveryDate).append(subagentClusterIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentNames) == false) {
            return false;
        }
        AgentNames rhs = ((AgentNames) other);
        return new EqualsBuilder().append(agentNames, rhs.agentNames).append(clusterAgentNames, rhs.clusterAgentNames).append(deliveryDate, rhs.deliveryDate).append(subagentClusterIds, rhs.subagentClusterIds).isEquals();
    }

}
