
package com.sos.joc.model.history.order.added;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * OrderOrderAdded event - AddOrder instruction
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowPath",
    "orderId",
    "arguments"
})
public class OrderAdded {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
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
        return new ToStringBuilder(this).append("workflowPath", workflowPath).append("orderId", orderId).append("arguments", arguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(arguments).append(workflowPath).append(orderId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderAdded) == false) {
            return false;
        }
        OrderAdded rhs = ((OrderAdded) other);
        return new EqualsBuilder().append(arguments, rhs.arguments).append(workflowPath, rhs.workflowPath).append(orderId, rhs.orderId).isEquals();
    }

}
