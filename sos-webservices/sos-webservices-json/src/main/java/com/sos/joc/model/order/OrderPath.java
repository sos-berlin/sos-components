
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * orderPath
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflow",
    "orderId"
})
public class OrderPath {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String workflow;
    /**
     * if orderId undefined or empty then all orders of specified workflow are requested
     * 
     */
    @JsonProperty("orderId")
    @JsonPropertyDescription("if orderId undefined or empty then all orders of specified workflow are requested")
    private String orderId;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public String getWorkflow() {
        return workflow;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    /**
     * if orderId undefined or empty then all orders of specified workflow are requested
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * if orderId undefined or empty then all orders of specified workflow are requested
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflow", workflow).append("orderId", orderId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflow).append(orderId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderPath) == false) {
            return false;
        }
        OrderPath rhs = ((OrderPath) other);
        return new EqualsBuilder().append(workflow, rhs.workflow).append(orderId, rhs.orderId).isEquals();
    }

}
