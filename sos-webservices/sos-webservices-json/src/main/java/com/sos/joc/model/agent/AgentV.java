
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "controllerId",
    "agentId",
    "agentName",
    "state",
    "failedMessage",
    "url",
    "isClusterWatcher"
})
public class AgentV {

    /**
     * filename
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
    /**
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private AgentState state;
    /**
     * if state == couplngFailed
     * 
     */
    @JsonProperty("failedMessage")
    @JsonPropertyDescription("if state == couplngFailed")
    private String failedMessage;
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
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
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

    /**
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public AgentState getState() {
        return state;
    }

    /**
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(AgentState state) {
        this.state = state;
    }

    /**
     * if state == couplngFailed
     * 
     */
    @JsonProperty("failedMessage")
    public String getFailedMessage() {
        return failedMessage;
    }

    /**
     * if state == couplngFailed
     * 
     */
    @JsonProperty("failedMessage")
    public void setFailedMessage(String failedMessage) {
        this.failedMessage = failedMessage;
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

    @JsonProperty("isClusterWatcher")
    public Boolean getIsClusterWatcher() {
        return isClusterWatcher;
    }

    @JsonProperty("isClusterWatcher")
    public void setIsClusterWatcher(Boolean isClusterWatcher) {
        this.isClusterWatcher = isClusterWatcher;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentId", agentId).append("agentName", agentName).append("state", state).append("failedMessage", failedMessage).append("url", url).append("isClusterWatcher", isClusterWatcher).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(controllerId).append(agentName).append(isClusterWatcher).append(state).append(failedMessage).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentV) == false) {
            return false;
        }
        AgentV rhs = ((AgentV) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(controllerId, rhs.controllerId).append(agentName, rhs.agentName).append(isClusterWatcher, rhs.isClusterWatcher).append(state, rhs.state).append(failedMessage, rhs.failedMessage).append(url, rhs.url).isEquals();
    }

}
