
package com.sos.controller.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.order.OrderPayload;
import com.sos.controller.model.order.OrderState;
import com.sos.controller.model.workflow.WorkflowPosition;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderAttached event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "workflowPosition",
    "state",
    "parent",
    "agentName",
    "payload"
})
public class OrderAttached
    extends Event
{

    /**
     * WorkflowPosition
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPosition")
    private WorkflowPosition workflowPosition;
    /**
     * OrderState
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private OrderState state;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("parent")
    private String parent;
    @JsonProperty("agentName")
    private String agentName;
    /**
     * orderPayload
     * <p>
     * 
     * 
     */
    @JsonProperty("payload")
    private OrderPayload payload;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderAttached() {
    }

    /**
     * 
     * @param parent
     * @param eventId
     * @param payload
     * @param workflowPosition
     * @param agentName
     * @param state
     * 
     */
    public OrderAttached(WorkflowPosition workflowPosition, OrderState state, String parent, String agentName, OrderPayload payload, Long eventId) {
        super(eventId);
        this.workflowPosition = workflowPosition;
        this.state = state;
        this.parent = parent;
        this.agentName = agentName;
        this.payload = payload;
    }

    /**
     * WorkflowPosition
     * <p>
     * 
     * (Required)
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
     * (Required)
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(OrderState state) {
        this.state = state;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("parent")
    public String getParent() {
        return parent;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("parent")
    public void setParent(String parent) {
        this.parent = parent;
    }

    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowPosition", workflowPosition).append("state", state).append("parent", parent).append("agentName", agentName).append("payload", payload).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowPosition).append(parent).append(agentName).append(state).append(payload).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderAttached) == false) {
            return false;
        }
        OrderAttached rhs = ((OrderAttached) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowPosition, rhs.workflowPosition).append(parent, rhs.parent).append(agentName, rhs.agentName).append(state, rhs.state).append(payload, rhs.payload).isEquals();
    }

}
