
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.AgentId;
import com.sos.jobscheduler.model.order.OrderPayload;
import com.sos.jobscheduler.model.order.OrderState;
import com.sos.jobscheduler.model.workflow.WorkflowPosition;
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
    "agentId",
    "payload"
})
public class OrderAttached
    extends Event
    implements IEvent
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
     * orderState
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
     * agentId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private AgentId agentId;
    /**
     * orderPayload
     * <p>
     * 
     * 
     */
    @JsonProperty("payload")
    private OrderPayload payload;

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
     * orderState
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
     * orderState
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
     * agentId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public AgentId getAgentId() {
        return agentId;
    }

    /**
     * agentId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(AgentId agentId) {
        this.agentId = agentId;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowPosition", workflowPosition).append("state", state).append("parent", parent).append("agentId", agentId).append("payload", payload).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowPosition).append(parent).append(agentId).append(state).append(payload).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowPosition, rhs.workflowPosition).append(parent, rhs.parent).append(agentId, rhs.agentId).append(state, rhs.state).append(payload, rhs.payload).isEquals();
    }

}
