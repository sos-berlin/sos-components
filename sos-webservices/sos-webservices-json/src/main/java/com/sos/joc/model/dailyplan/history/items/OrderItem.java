
package com.sos.joc.model.dailyplan.history.items;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * date object in daily plan history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "scheduledFor",
    "workflowPath",
    "orderId",
    "submitted",
    "permitted"
})
public class OrderItem {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("scheduledFor")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date scheduledFor;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("submitted")
    private Boolean submitted;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("permitted")
    private Boolean permitted;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("scheduledFor")
    public Date getScheduledFor() {
        return scheduledFor;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("scheduledFor")
    public void setScheduledFor(Date scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("submitted")
    public Boolean getSubmitted() {
        return submitted;
    }

    @JsonProperty("submitted")
    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("permitted")
    public Boolean getPermitted() {
        return permitted;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("permitted")
    public void setPermitted(Boolean permitted) {
        this.permitted = permitted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("scheduledFor", scheduledFor).append("workflowPath", workflowPath).append("orderId", orderId).append("submitted", submitted).append("permitted", permitted).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(submitted).append(permitted).append(workflowPath).append(orderId).append(scheduledFor).toHashCode();
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
        return new EqualsBuilder().append(submitted, rhs.submitted).append(permitted, rhs.permitted).append(workflowPath, rhs.workflowPath).append(orderId, rhs.orderId).append(scheduledFor, rhs.scheduledFor).isEquals();
    }

}
