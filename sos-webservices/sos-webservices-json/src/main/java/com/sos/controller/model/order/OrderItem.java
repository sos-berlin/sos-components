
package com.sos.controller.model.order;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.Variables;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowPosition;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "arguments",
    "workflowPosition",
    "state",
    "attachedState",
    "historicOutcomes",
    "isSuspended",
    "removeWhenTerminated"
})
public class OrderItem {

    @JsonProperty("id")
    private String id;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;
    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    private WorkflowPosition workflowPosition;
    /**
     * OrderState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private OrderState state;
    /**
     * OrderAttachedState
     * <p>
     * 
     * 
     */
    @JsonProperty("attachedState")
    private OrderAttachedState attachedState;
    @JsonProperty("historicOutcomes")
    private List<HistoricOutcome> historicOutcomes = null;
    @JsonProperty("isSuspended")
    private Boolean isSuspended;
    @JsonProperty("removeWhenTerminated")
    private Boolean removeWhenTerminated;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderItem() {
    }

    /**
     * 
     * @param attachedState
     * @param isSuspended
     * @param historicOutcomes
     * @param workflowPosition
     * @param arguments
     * @param id
     * @param state
     * @param removeWhenTerminated
     */
    public OrderItem(String id, Variables arguments, WorkflowPosition workflowPosition, OrderState state, OrderAttachedState attachedState, List<HistoricOutcome> historicOutcomes, Boolean isSuspended, Boolean removeWhenTerminated) {
        super();
        this.id = id;
        this.arguments = arguments;
        this.workflowPosition = workflowPosition;
        this.state = state;
        this.attachedState = attachedState;
        this.historicOutcomes = historicOutcomes;
        this.isSuspended = isSuspended;
        this.removeWhenTerminated = removeWhenTerminated;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Variables getArguments() {
        return arguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Variables arguments) {
        this.arguments = arguments;
    }

    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    public WorkflowPosition getWorkflowPosition() {
        return workflowPosition;
    }

    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    public void setWorkflowPosition(WorkflowPosition workflowPosition) {
        this.workflowPosition = workflowPosition;
    }

    /**
     * OrderState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public OrderState getState() {
        return state;
    }

    /**
     * OrderState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(OrderState state) {
        this.state = state;
    }

    /**
     * OrderAttachedState
     * <p>
     * 
     * 
     */
    @JsonProperty("attachedState")
    public OrderAttachedState getAttachedState() {
        return attachedState;
    }

    /**
     * OrderAttachedState
     * <p>
     * 
     * 
     */
    @JsonProperty("attachedState")
    public void setAttachedState(OrderAttachedState attachedState) {
        this.attachedState = attachedState;
    }

    @JsonProperty("historicOutcomes")
    public List<HistoricOutcome> getHistoricOutcomes() {
        return historicOutcomes;
    }

    @JsonProperty("historicOutcomes")
    public void setHistoricOutcomes(List<HistoricOutcome> historicOutcomes) {
        this.historicOutcomes = historicOutcomes;
    }

    @JsonProperty("isSuspended")
    public Boolean getIsSuspended() {
        return isSuspended;
    }

    @JsonProperty("isSuspended")
    public void setIsSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

    @JsonProperty("removeWhenTerminated")
    public Boolean getRemoveWhenTerminated() {
        return removeWhenTerminated;
    }

    @JsonProperty("removeWhenTerminated")
    public void setRemoveWhenTerminated(Boolean removeWhenTerminated) {
        this.removeWhenTerminated = removeWhenTerminated;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("arguments", arguments).append("workflowPosition", workflowPosition).append("state", state).append("attachedState", attachedState).append("historicOutcomes", historicOutcomes).append("isSuspended", isSuspended).append("removeWhenTerminated", removeWhenTerminated).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(attachedState).append(isSuspended).append(historicOutcomes).append(workflowPosition).append(arguments).append(id).append(state).append(removeWhenTerminated).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderItem) == false) {
            return false;
        }
        OrderItem rhs = ((OrderItem) other);
        return new EqualsBuilder().append(attachedState, rhs.attachedState).append(isSuspended, rhs.isSuspended).append(historicOutcomes, rhs.historicOutcomes).append(workflowPosition, rhs.workflowPosition).append(arguments, rhs.arguments).append(id, rhs.id).append(state, rhs.state).append(removeWhenTerminated, rhs.removeWhenTerminated).isEquals();
    }

}
