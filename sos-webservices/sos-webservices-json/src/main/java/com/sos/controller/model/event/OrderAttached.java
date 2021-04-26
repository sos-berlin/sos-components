
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
    "workflowPosition",
    "state",
    "parent",
    "agentPath",
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
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    private String agentPath;
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
     * @param agentPath
     * @param parent
     * @param eventId
     * @param payload
     * @param workflowPosition
     * @param state
     * 
     */
    public OrderAttached(WorkflowPosition workflowPosition, OrderState state, String parent, String agentPath, OrderPayload payload, Long eventId) {
        super(eventId);
        this.workflowPosition = workflowPosition;
        this.state = state;
        this.parent = parent;
        this.agentPath = agentPath;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowPosition", workflowPosition).append("state", state).append("parent", parent).append("agentPath", agentPath).append("payload", payload).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowPosition).append(agentPath).append(parent).append(state).append(payload).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowPosition, rhs.workflowPosition).append(agentPath, rhs.agentPath).append(parent, rhs.parent).append(state, rhs.state).append(payload, rhs.payload).isEquals();
    }

}
