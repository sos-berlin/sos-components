
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
    "onlyVisibleAgents"
})
public class ReadAgents {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();
    @JsonProperty("onlyVisibleAgents")
    private Boolean onlyVisibleAgents = false;

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

    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    @JsonProperty("agentIds")
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
    }

    @JsonProperty("onlyVisibleAgents")
    public Boolean getOnlyVisibleAgents() {
        return onlyVisibleAgents;
    }

    @JsonProperty("onlyVisibleAgents")
    public void setOnlyVisibleAgents(Boolean onlyVisibleAgents) {
        this.onlyVisibleAgents = onlyVisibleAgents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentIds", agentIds).append("onlyVisibleAgents", onlyVisibleAgents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentIds).append(controllerId).append(onlyVisibleAgents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadAgents) == false) {
            return false;
        }
        ReadAgents rhs = ((ReadAgents) other);
        return new EqualsBuilder().append(agentIds, rhs.agentIds).append(controllerId, rhs.controllerId).append(onlyVisibleAgents, rhs.onlyVisibleAgents).isEquals();
    }

}
