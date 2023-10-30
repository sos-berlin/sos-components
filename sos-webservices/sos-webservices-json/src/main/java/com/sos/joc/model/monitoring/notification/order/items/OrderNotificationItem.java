
package com.sos.joc.model.monitoring.notification.order.items;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.monitoring.notification.common.AcknowledgementItem;
import com.sos.monitoring.notification.NotificationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * notification object in monitoring notifications collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "notificationId",
    "recoveredNotificationId",
    "type",
    "created",
    "hasMonitors",
    "controllerId",
    "orderId",
    "workflow",
    "message",
    "job",
    "acknowledgement"
})
public class OrderNotificationItem {

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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("recoveredNotificationId")
    private Long recoveredNotificationId;
    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private NotificationType type;
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
    private OrderNotificationJobItem job;
    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("acknowledgement")
    private AcknowledgementItem acknowledgement;

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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("recoveredNotificationId")
    public Long getRecoveredNotificationId() {
        return recoveredNotificationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("recoveredNotificationId")
    public void setRecoveredNotificationId(Long recoveredNotificationId) {
        this.recoveredNotificationId = recoveredNotificationId;
    }

    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public NotificationType getType() {
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
    public void setType(NotificationType type) {
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
    public OrderNotificationJobItem getJob() {
        return job;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("job")
    public void setJob(OrderNotificationJobItem job) {
        this.job = job;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("acknowledgement")
    public AcknowledgementItem getAcknowledgement() {
        return acknowledgement;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("acknowledgement")
    public void setAcknowledgement(AcknowledgementItem acknowledgement) {
        this.acknowledgement = acknowledgement;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("notificationId", notificationId).append("recoveredNotificationId", recoveredNotificationId).append("type", type).append("created", created).append("hasMonitors", hasMonitors).append("controllerId", controllerId).append("orderId", orderId).append("workflow", workflow).append("message", message).append("job", job).append("acknowledgement", acknowledgement).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(acknowledgement).append(controllerId).append(workflow).append(recoveredNotificationId).append(orderId).append(created).append(hasMonitors).append(notificationId).append(type).append(message).append(job).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderNotificationItem) == false) {
            return false;
        }
        OrderNotificationItem rhs = ((OrderNotificationItem) other);
        return new EqualsBuilder().append(acknowledgement, rhs.acknowledgement).append(controllerId, rhs.controllerId).append(workflow, rhs.workflow).append(recoveredNotificationId, rhs.recoveredNotificationId).append(orderId, rhs.orderId).append(created, rhs.created).append(hasMonitors, rhs.hasMonitors).append(notificationId, rhs.notificationId).append(type, rhs.type).append(message, rhs.message).append(job, rhs.job).isEquals();
    }

}
