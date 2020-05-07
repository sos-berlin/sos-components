
package com.sos.jobscheduler.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.workflow.WorkflowPosition;
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
    "historicOutcomes"
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
    /**
     * HistoricOutcomes
     * <p>
     * 
     * 
     */
    @JsonProperty("historicOutcomes")
    private List<Integer> historicOutcomes = new ArrayList<Integer>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderItem() {
    }

    /**
     * 
     * @param attachedState
     * @param historicOutcomes
     * @param workflowPosition
     * @param arguments
     * @param id
     * @param state
     */
    public OrderItem(String id, Variables arguments, WorkflowPosition workflowPosition, OrderState state, OrderAttachedState attachedState, List<Integer> historicOutcomes) {
        super();
        this.id = id;
        this.arguments = arguments;
        this.workflowPosition = workflowPosition;
        this.state = state;
        this.attachedState = attachedState;
        this.historicOutcomes = historicOutcomes;
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

    /**
     * HistoricOutcomes
     * <p>
     * 
     * 
     */
    @JsonProperty("historicOutcomes")
    public List<Integer> getHistoricOutcomes() {
        return historicOutcomes;
    }

    /**
     * HistoricOutcomes
     * <p>
     * 
     * 
     */
    @JsonProperty("historicOutcomes")
    public void setHistoricOutcomes(List<Integer> historicOutcomes) {
        this.historicOutcomes = historicOutcomes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("arguments", arguments).append("workflowPosition", workflowPosition).append("state", state).append("attachedState", attachedState).append("historicOutcomes", historicOutcomes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(attachedState).append(historicOutcomes).append(workflowPosition).append(arguments).append(id).append(state).toHashCode();
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
        return new EqualsBuilder().append(attachedState, rhs.attachedState).append(historicOutcomes, rhs.historicOutcomes).append(workflowPosition, rhs.workflowPosition).append(arguments, rhs.arguments).append(id, rhs.id).append(state, rhs.state).isEquals();
    }

}
