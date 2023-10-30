
package com.sos.joc.model.event;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.IEventObject;
import com.sos.monitoring.notification.NotificationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * event from order monitoring
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventId",
    "level",
    "workflowName",
    "orderId",
    "jobName",
    "timestamp",
    "message"
})
public class EventOrderMonitoring implements IEventObject
{

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("level")
    private NotificationType level;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    private String workflowName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    private String jobName;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date timestamp;
    @JsonProperty("message")
    private String message;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("level")
    public NotificationType getLevel() {
        return level;
    }

    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("level")
    public void setLevel(NotificationType level) {
        this.level = level;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    public String getJobName() {
        return jobName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventId", eventId).append("level", level).append("workflowName", workflowName).append("orderId", orderId).append("jobName", jobName).append("timestamp", timestamp).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobName).append(level).append(orderId).append(workflowName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EventOrderMonitoring) == false) {
            return false;
        }
        EventOrderMonitoring rhs = ((EventOrderMonitoring) other);
        return new EqualsBuilder().append(jobName, rhs.jobName).append(level, rhs.level).append(orderId, rhs.orderId).append(workflowName, rhs.workflowName).isEquals();
    }

}
