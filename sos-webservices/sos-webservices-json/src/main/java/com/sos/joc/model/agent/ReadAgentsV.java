
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
    "states",
    "onlyVisibleAgents",
    "compact",
    "flat"
})
public class ReadAgentsV {

    /**
     * controllerId
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
    @JsonProperty("onlyVisibleAgents")
    private Boolean onlyVisibleAgents = false;
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    @JsonProperty("flat")
    private Boolean flat = false;

    /**
     * controllerId
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
     * controllerId
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

    @JsonProperty("onlyVisibleAgents")
    public Boolean getOnlyVisibleAgents() {
        return onlyVisibleAgents;
    }

    @JsonProperty("onlyVisibleAgents")
    public void setOnlyVisibleAgents(Boolean onlyVisibleAgents) {
        this.onlyVisibleAgents = onlyVisibleAgents;
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

    @JsonProperty("flat")
    public Boolean getFlat() {
        return flat;
    }

    @JsonProperty("flat")
    public void setFlat(Boolean flat) {
        this.flat = flat;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentIds", agentIds).append("states", states).append("onlyVisibleAgents", onlyVisibleAgents).append("compact", compact).append("flat", flat).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentIds).append(controllerId).append(compact).append(flat).append(onlyVisibleAgents).append(states).toHashCode();
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
        return new EqualsBuilder().append(agentIds, rhs.agentIds).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(flat, rhs.flat).append(onlyVisibleAgents, rhs.onlyVisibleAgents).append(states, rhs.states).isEquals();
    }

}
