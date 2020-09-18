
package com.sos.joc.model.job;

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
    "orderId",
    "position"
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
     * if orderId undefined or empty then all orders of specified job chain are requested
     * 
     */
    @JsonProperty("orderId")
    @JsonPropertyDescription("if orderId undefined or empty then all orders of specified job chain are requested")
    private String orderId;
    @JsonProperty("position")
    private String position;

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
     * if orderId undefined or empty then all orders of specified job chain are requested
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * if orderId undefined or empty then all orders of specified job chain are requested
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("position")
    public String getPosition() {
        return position;
    }

    @JsonProperty("position")
    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflowPath", workflowPath).append("orderId", orderId).append("position", position).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(position).append(workflowPath).append(orderId).toHashCode();
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
        return new EqualsBuilder().append(position, rhs.position).append(workflowPath, rhs.workflowPath).append(orderId, rhs.orderId).isEquals();
    }

}
