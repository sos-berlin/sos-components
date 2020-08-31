
package com.sos.jobscheduler.model.command;

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
 * Resume Order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "orderId",
    "workflowPosition",
    "arguments"
})
public class ResumeOrder
    extends Command
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    private WorkflowPosition workflowPosition;
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
     * No args constructor for use in serialization
     * 
     */
    public ResumeOrder() {
    }

    /**
     * 
     * @param orderId
     * @param workflowPosition
     * @param arguments
     */
    public ResumeOrder(String orderId, WorkflowPosition workflowPosition, Variables arguments) {
        super();
        this.orderId = orderId;
        this.workflowPosition = workflowPosition;
        this.arguments = arguments;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderId", orderId).append("workflowPosition", workflowPosition).append("arguments", arguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowPosition).append(arguments).append(orderId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResumeOrder) == false) {
            return false;
        }
        ResumeOrder rhs = ((ResumeOrder) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowPosition, rhs.workflowPosition).append(arguments, rhs.arguments).append(orderId, rhs.orderId).isEquals();
    }

}
