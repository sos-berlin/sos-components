
package com.sos.joc.model.monitoring;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.MonitoringNotificationTypeText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * notification object in monitoring notifications collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "notificationId",
    "type",
    "created",
    "hasMonitors",
    "controllerId",
    "orderId",
    "workflow",
    "message",
    "job"
})
public class NotificationItem {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationId")
    private Long notificationId;
    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private MonitoringNotificationTypeText type;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date created;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hasMonitors")
    private Boolean hasMonitors;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    @JsonPropertyDescription("absolute path of an object.")
    private String workflow;
    @JsonProperty("message")
    private String message;
    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("job")
    private NotificationItemJobItem job;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationId")
    public Long getNotificationId() {
        return notificationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationId")
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public MonitoringNotificationTypeText getType() {
        return type;
    }

    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(MonitoringNotificationTypeText type) {
        this.type = type;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hasMonitors")
    public Boolean getHasMonitors() {
        return hasMonitors;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hasMonitors")
    public void setHasMonitors(Boolean hasMonitors) {
        this.hasMonitors = hasMonitors;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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
     * path
     * <p>
     * absolute path of an object.
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
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("job")
    public NotificationItemJobItem getJob() {
        return job;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("job")
    public void setJob(NotificationItemJobItem job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("notificationId", notificationId).append("type", type).append("created", created).append("hasMonitors", hasMonitors).append("controllerId", controllerId).append("orderId", orderId).append("workflow", workflow).append("message", message).append("job", job).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(workflow).append(orderId).append(created).append(hasMonitors).append(notificationId).append(type).append(message).append(job).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NotificationItem) == false) {
            return false;
        }
        NotificationItem rhs = ((NotificationItem) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(workflow, rhs.workflow).append(orderId, rhs.orderId).append(created, rhs.created).append(hasMonitors, rhs.hasMonitors).append(notificationId, rhs.notificationId).append(type, rhs.type).append(message, rhs.message).append(job, rhs.job).isEquals();
    }

}
