
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
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
    "controllerId",
    "agentId",
    "subagentClusterId",
    "title",
    "deployed",
    "syncState",
    "ordering",
    "subagentIds"
})
public class SubagentCluster {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    @JsonProperty("deployed")
    private Boolean deployed = false;
    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("syncState")
    private SyncState syncState;
    @JsonProperty("ordering")
    private Integer ordering;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentIds")
    private List<SubAgentId> subagentIds = new ArrayList<SubAgentId>();

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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("syncState")
    public SyncState getSyncState() {
        return syncState;
    }

    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("syncState")
    public void setSyncState(SyncState syncState) {
        this.syncState = syncState;
    }

    @JsonProperty("ordering")
    public Integer getOrdering() {
        return ordering;
    }

    @JsonProperty("ordering")
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentId", agentId).append("subagentClusterId", subagentClusterId).append("title", title).append("deployed", deployed).append("syncState", syncState).append("ordering", ordering).append("subagentIds", subagentIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(subagentIds).append(controllerId).append(ordering).append(syncState).append(deployed).append(subagentClusterId).append(title).toHashCode();
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
        return new EqualsBuilder().append(agentId, rhs.agentId).append(subagentIds, rhs.subagentIds).append(controllerId, rhs.controllerId).append(ordering, rhs.ordering).append(syncState, rhs.syncState).append(deployed, rhs.deployed).append(subagentClusterId, rhs.subagentClusterId).append(title, rhs.title).isEquals();
    }

}
