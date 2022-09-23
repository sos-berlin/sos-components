
package com.sos.joc.model.workflow.search;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.model.inventory.search.RequestBaseSearchFilter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter workflow search
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "states",
    "instructionStates"
})
public class WorkflowSearchFilter
    extends RequestBaseSearchFilter
{

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("states")
    private List<SyncStateText> states = new ArrayList<SyncStateText>();
    @JsonProperty("instructionStates")
    private List<InstructionStateText> instructionStates = new ArrayList<InstructionStateText>();

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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("controllerId", controllerId).append("states", states).append("instructionStates", instructionStates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(controllerId).append(states).append(instructionStates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowSearchFilter) == false) {
            return false;
        }
        WorkflowSearchFilter rhs = ((WorkflowSearchFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(controllerId, rhs.controllerId).append(states, rhs.states).append(instructionStates, rhs.instructionStates).isEquals();
    }

}
