
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    "agentId",
    "subagentId",
    "url",
    "isDirector",
    "isClusterWatcher"
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
     * 
     */
    @JsonProperty("isDirector")
    private SubagentDirectorType isDirector = SubagentDirectorType.fromValue("NO_DIRECTOR");
    @JsonProperty("isClusterWatcher")
    private Boolean isClusterWatcher = false;

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
        return new ToStringBuilder(this).append("agentId", agentId).append("subagentId", subagentId).append("url", url).append("isDirector", isDirector).append("isClusterWatcher", isClusterWatcher).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(subagentId).append(isClusterWatcher).append(url).append(isDirector).toHashCode();
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
        return new EqualsBuilder().append(agentId, rhs.agentId).append(subagentId, rhs.subagentId).append(isClusterWatcher, rhs.isClusterWatcher).append(url, rhs.url).append(isDirector, rhs.isDirector).isEquals();
    }

}
