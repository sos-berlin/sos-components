
package com.sos.jobscheduler.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "workflowPosition",
    "state",
    "attachedState",
    "payload"
})
public class OrderItem {

    @JsonProperty("id")
    private String id;
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
     * orderPayload
     * <p>
     * 
     * 
     */
    @JsonProperty("payload")
    private OrderPayload payload;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
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
     * orderPayload
     * <p>
     * 
     * 
     */
    @JsonProperty("payload")
    public OrderPayload getPayload() {
        return payload;
    }

    /**
     * orderPayload
     * <p>
     * 
     * 
     */
    @JsonProperty("payload")
    public void setPayload(OrderPayload payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("workflowPosition", workflowPosition).append("state", state).append("attachedState", attachedState).append("payload", payload).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowPosition).append(attachedState).append(id).append(state).append(payload).toHashCode();
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
        return new EqualsBuilder().append(workflowPosition, rhs.workflowPosition).append(attachedState, rhs.attachedState).append(id, rhs.id).append(state, rhs.state).append(payload, rhs.payload).isEquals();
    }

}
