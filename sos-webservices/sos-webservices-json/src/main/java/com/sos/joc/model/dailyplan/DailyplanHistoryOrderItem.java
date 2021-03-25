
package com.sos.joc.model.dailyplan;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * DailyplanHistoryOrderItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "scheduledFor",
    "workflowPath",
    "orderId",
    "submitted"
})
public class DailyplanHistoryOrderItem {

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
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("submitted")
    private Boolean submitted;

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

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("scheduledFor", scheduledFor).append("workflowPath", workflowPath).append("orderId", orderId).append("submitted", submitted).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(submitted).append(workflowPath).append(orderId).append(scheduledFor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyplanHistoryOrderItem) == false) {
            return false;
        }
        DailyplanHistoryOrderItem rhs = ((DailyplanHistoryOrderItem) other);
        return new EqualsBuilder().append(submitted, rhs.submitted).append(workflowPath, rhs.workflowPath).append(orderId, rhs.orderId).append(scheduledFor, rhs.scheduledFor).isEquals();
    }

}
