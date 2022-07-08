
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Register Cluster Watch Agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "agentName",
    "url",
    "asStandaloneAgent",
    "primaryDirectorId"
})
public class RegisterClusterWatchAgent {

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    private String url;
    @JsonProperty("asStandaloneAgent")
    private Boolean asStandaloneAgent = false;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("primaryDirectorId")
    private String primaryDirectorId;

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

    @JsonProperty("asStandaloneAgent")
    public Boolean getAsStandaloneAgent() {
        return asStandaloneAgent;
    }

    @JsonProperty("asStandaloneAgent")
    public void setAsStandaloneAgent(Boolean asStandaloneAgent) {
        this.asStandaloneAgent = asStandaloneAgent;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("primaryDirectorId")
    public String getPrimaryDirectorId() {
        return primaryDirectorId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("primaryDirectorId")
    public void setPrimaryDirectorId(String primaryDirectorId) {
        this.primaryDirectorId = primaryDirectorId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("agentName", agentName).append("url", url).append("asStandaloneAgent", asStandaloneAgent).append("primaryDirectorId", primaryDirectorId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentName).append(asStandaloneAgent).append(agentId).append(primaryDirectorId).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RegisterClusterWatchAgent) == false) {
            return false;
        }
        RegisterClusterWatchAgent rhs = ((RegisterClusterWatchAgent) other);
        return new EqualsBuilder().append(agentName, rhs.agentName).append(asStandaloneAgent, rhs.asStandaloneAgent).append(agentId, rhs.agentId).append(primaryDirectorId, rhs.primaryDirectorId).append(url, rhs.url).isEquals();
    }

}
