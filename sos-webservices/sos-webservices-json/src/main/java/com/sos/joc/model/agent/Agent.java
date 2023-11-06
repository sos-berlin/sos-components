
package com.sos.joc.model.agent;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.common.SyncState;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * single agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentId",
    "agentName",
    "agentNameAliases",
    "url",
    "isClusterWatcher",
    "title",
    "processLimit",
    "hidden",
    "disabled",
    "syncState",
    "deployed",
    "ordering",
    "version"
})
public class Agent {

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
    @JsonProperty("agentName")
    private String agentName;
    @JsonProperty("agentNameAliases")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> agentNameAliases = new LinkedHashSet<String>();
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    private String url;
    @JsonProperty("isClusterWatcher")
    private Boolean isClusterWatcher = false;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("processLimit")
    private Integer processLimit;
    @JsonProperty("hidden")
    private Boolean hidden = false;
    @JsonProperty("disabled")
    private Boolean disabled = false;
    /**
     * sync state
     * <p>
     * 
     * 
     */
    @JsonProperty("syncState")
    private SyncState syncState;
    @JsonProperty("deployed")
    private Boolean deployed = false;
    @JsonProperty("ordering")
    private Integer ordering;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    private String version;

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
    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @JsonProperty("agentNameAliases")
    public Set<String> getAgentNameAliases() {
        return agentNameAliases;
    }

    @JsonProperty("agentNameAliases")
    public void setAgentNameAliases(Set<String> agentNameAliases) {
        this.agentNameAliases = agentNameAliases;
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
        if (url != null && !"/".equals(url) && url.endsWith("/")) {
            url = url.replaceFirst("/$", ""); 
        }
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

    @JsonProperty("isClusterWatcher")
    public Boolean getIsClusterWatcher() {
        return isClusterWatcher;
    }

    @JsonProperty("isClusterWatcher")
    public void setIsClusterWatcher(Boolean isClusterWatcher) {
        this.isClusterWatcher = isClusterWatcher;
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
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("processLimit")
    public Integer getProcessLimit() {
        return processLimit;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("processLimit")
    public void setProcessLimit(Integer processLimit) {
        this.processLimit = processLimit;
    }

    @JsonProperty("hidden")
    public Boolean getHidden() {
        return hidden;
    }

    @JsonProperty("hidden")
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
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

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentId", agentId).append("agentName", agentName).append("agentNameAliases", agentNameAliases).append("url", url).append("isClusterWatcher", isClusterWatcher).append("title", title).append("processLimit", processLimit).append("hidden", hidden).append("disabled", disabled).append("syncState", syncState).append("deployed", deployed).append("ordering", ordering).append("version", version).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(controllerId).append(hidden).append(agentNameAliases).append(ordering).append(syncState).append(agentName).append(deployed).append(isClusterWatcher).append(title).append(version).append(url).append(processLimit).append(disabled).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Agent) == false) {
            return false;
        }
        Agent rhs = ((Agent) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(controllerId, rhs.controllerId).append(hidden, rhs.hidden).append(agentNameAliases, rhs.agentNameAliases).append(ordering, rhs.ordering).append(syncState, rhs.syncState).append(agentName, rhs.agentName).append(deployed, rhs.deployed).append(isClusterWatcher, rhs.isClusterWatcher).append(title, rhs.title).append(version, rhs.version).append(url, rhs.url).append(processLimit, rhs.processLimit).append(disabled, rhs.disabled).isEquals();
    }

}
