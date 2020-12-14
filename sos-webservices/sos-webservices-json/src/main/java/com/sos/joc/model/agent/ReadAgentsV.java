
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * read agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentIds",
    "urls",
    "states",
    "onlyEnabledAgents",
    "compact"
})
public class ReadAgentsV {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();
    @JsonProperty("urls")
    private List<String> urls = new ArrayList<String>();
    @JsonProperty("states")
    private List<AgentStateText> states = new ArrayList<AgentStateText>();
    @JsonProperty("onlyEnabledAgents")
    private Boolean onlyEnabledAgents = false;
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;

    /**
     * filename
     * <p>
     * 
     * (Required)
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
     * (Required)
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

    @JsonProperty("urls")
    public List<String> getUrls() {
        return urls;
    }

    @JsonProperty("urls")
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    @JsonProperty("states")
    public List<AgentStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<AgentStateText> states) {
        this.states = states;
    }

    @JsonProperty("onlyEnabledAgents")
    public Boolean getOnlyEnabledAgents() {
        return onlyEnabledAgents;
    }

    @JsonProperty("onlyEnabledAgents")
    public void setOnlyEnabledAgents(Boolean onlyEnabledAgents) {
        this.onlyEnabledAgents = onlyEnabledAgents;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentIds", agentIds).append("urls", urls).append("states", states).append("onlyEnabledAgents", onlyEnabledAgents).append("compact", compact).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentIds).append(urls).append(controllerId).append(compact).append(onlyEnabledAgents).append(states).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadAgentsV) == false) {
            return false;
        }
        ReadAgentsV rhs = ((ReadAgentsV) other);
        return new EqualsBuilder().append(agentIds, rhs.agentIds).append(urls, rhs.urls).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(onlyEnabledAgents, rhs.onlyEnabledAgents).append(states, rhs.states).isEquals();
    }

}
