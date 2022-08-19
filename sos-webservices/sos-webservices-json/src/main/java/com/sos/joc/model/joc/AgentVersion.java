
package com.sos.joc.model.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * version of a specifc agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "subagentId",
    "uri",
    "version",
    "compatibility"
})
public class AgentVersion {

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
    @JsonProperty("uri")
    private String uri;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * Compatibility Level of JS7 components
     * <p>
     * 
     * 
     */
    @JsonProperty("compatibility")
    private CompatibilityLevel compatibility;

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
    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Compatibility Level of JS7 components
     * <p>
     * 
     * 
     */
    @JsonProperty("compatibility")
    public CompatibilityLevel getCompatibility() {
        return compatibility;
    }

    /**
     * Compatibility Level of JS7 components
     * <p>
     * 
     * 
     */
    @JsonProperty("compatibility")
    public void setCompatibility(CompatibilityLevel compatibility) {
        this.compatibility = compatibility;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("subagentId", subagentId).append("uri", uri).append("version", version).append("compatibility", compatibility).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(subagentId).append(uri).append(version).append(compatibility).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentVersion) == false) {
            return false;
        }
        AgentVersion rhs = ((AgentVersion) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(subagentId, rhs.subagentId).append(uri, rhs.uri).append(version, rhs.version).append(compatibility, rhs.compatibility).isEquals();
    }

}
