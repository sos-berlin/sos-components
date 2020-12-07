
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
 * read agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentIds",
    "states",
    "onlyEnabledAgents"
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
    @JsonProperty("states")
    private List<AgentStateText> states = new ArrayList<AgentStateText>();
    @JsonProperty("onlyEnabledAgents")
    private Boolean onlyEnabledAgents = false;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentIds", agentIds).append("states", states).append("onlyEnabledAgents", onlyEnabledAgents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentIds).append(controllerId).append(onlyEnabledAgents).append(states).toHashCode();
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
        return new EqualsBuilder().append(agentIds, rhs.agentIds).append(controllerId, rhs.controllerId).append(onlyEnabledAgents, rhs.onlyEnabledAgents).append(states, rhs.states).isEquals();
    }

}
