
package com.sos.joc.model.joc;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter to get versions of controllers, agents and JOC Cockpit
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerIds",
    "agentIds"
})
public class VersionsFilter {

    @JsonProperty("controllerIds")
    private List<String> controllerIds = new ArrayList<String>();
    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();

    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    @JsonProperty("agentIds")
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerIds", controllerIds).append("agentIds", agentIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerIds).append(agentIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VersionsFilter) == false) {
            return false;
        }
        VersionsFilter rhs = ((VersionsFilter) other);
        return new EqualsBuilder().append(controllerIds, rhs.controllerIds).append(agentIds, rhs.agentIds).isEquals();
    }

}
