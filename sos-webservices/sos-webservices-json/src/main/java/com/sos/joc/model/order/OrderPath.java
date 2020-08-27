
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
    "workflowPath",
    "orderId"
})
public class OrderPath {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("workflowPath")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String workflowPath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    private String orderId;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflowPath", workflowPath).append("orderId", orderId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowPath).append(orderId).toHashCode();
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
        return new EqualsBuilder().append(workflowPath, rhs.workflowPath).append(orderId, rhs.orderId).isEquals();
    }

}
