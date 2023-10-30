
package com.sos.joc.model.workflow.search;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.model.inventory.search.RequestSearchFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Filter workflow search
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "states",
    "instructionStates"
})
public class DeployedWorkflowSearchFilter
    extends RequestSearchFilter
{

    @JsonProperty("states")
    private List<SyncStateText> states = new ArrayList<SyncStateText>();
    @JsonProperty("instructionStates")
    private List<InstructionStateText> instructionStates = new ArrayList<InstructionStateText>();

    @JsonProperty("states")
    public List<SyncStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<SyncStateText> states) {
        this.states = states;
    }

    @JsonProperty("instructionStates")
    public List<InstructionStateText> getInstructionStates() {
        return instructionStates;
    }

    @JsonProperty("instructionStates")
    public void setInstructionStates(List<InstructionStateText> instructionStates) {
        this.instructionStates = instructionStates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("states", states).append("instructionStates", instructionStates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(states).append(instructionStates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployedWorkflowSearchFilter) == false) {
            return false;
        }
        DeployedWorkflowSearchFilter rhs = ((DeployedWorkflowSearchFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(states, rhs.states).append(instructionStates, rhs.instructionStates).isEquals();
    }

}
