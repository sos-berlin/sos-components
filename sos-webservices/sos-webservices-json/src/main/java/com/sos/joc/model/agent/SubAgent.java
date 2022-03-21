
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "subagentId",
    "url",
    "isDirector",
    "title",
    "syncState",
    "disabled",
    "deployed",
    "isClusterWatcher",
    "position"
})
public class SubAgent {

    /**
     * string without < and >
     * <p>
     * 
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
    @JsonProperty("subagentId")
    private String subagentId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    private String url;
    /**
     * SubagentDiretoryType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("isDirector")
    private SubagentDirectorType isDirector;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("syncState")
    private SyncState syncState;
    @JsonProperty("disabled")
    private Boolean disabled = false;
    @JsonProperty("deployed")
    private Boolean deployed = false;
    @JsonProperty("isClusterWatcher")
    private Boolean isClusterWatcher = false;
    @JsonProperty("position")
    private Integer position;

    /**
     * string without < and >
     * <p>
     * 
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
    @JsonProperty("subagentId")
    public String getSubagentId() {
        return subagentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("subagentId")
    public void setSubagentId(String subagentId) {
        this.subagentId = subagentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * SubagentDiretoryType
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("isDirector")
    public void setIsDirector(SubagentDirectorType isDirector) {
        this.isDirector = isDirector;
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

    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    @JsonProperty("isClusterWatcher")
    public Boolean getIsClusterWatcher() {
        return isClusterWatcher;
    }

    @JsonProperty("isClusterWatcher")
    public void setIsClusterWatcher(Boolean isClusterWatcher) {
        this.isClusterWatcher = isClusterWatcher;
    }

    @JsonProperty("position")
    public Integer getPosition() {
        return position;
    }

    @JsonProperty("position")
    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("subagentId", subagentId).append("url", url).append("isDirector", isDirector).append("title", title).append("syncState", syncState).append("disabled", disabled).append("deployed", deployed).append("isClusterWatcher", isClusterWatcher).append("position", position).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(isDirector).append(syncState).append(deployed).append(subagentId).append(disabled).append(isClusterWatcher).append(position).append(title).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubAgent) == false) {
            return false;
        }
        SubAgent rhs = ((SubAgent) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(isDirector, rhs.isDirector).append(syncState, rhs.syncState).append(deployed, rhs.deployed).append(subagentId, rhs.subagentId).append(disabled, rhs.disabled).append(isClusterWatcher, rhs.isClusterWatcher).append(position, rhs.position).append(title, rhs.title).append(url, rhs.url).isEquals();
    }

}
