
package com.sos.joc.model.monitoring.notification.system.items;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.monitoring.notification.common.AcknowledgementItem;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.SystemNotificationCategory;
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
    "category",
    "source",
    "notifier",
    "message",
    "exception",
    "hasMonitors",
    "created",
    "acknowledgement"
})
public class SystemNotificationItem {

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
    private NotificationType type;
    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    private SystemNotificationCategory category;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source")
    private String source;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notifier")
    private String notifier;
    @JsonProperty("message")
    private String message;
    @JsonProperty("exception")
    private String exception;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("hasMonitors")
    private Boolean hasMonitors;
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
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    public SystemNotificationCategory getCategory() {
        return category;
    }

    /**
     * notification type text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    public void setCategory(SystemNotificationCategory category) {
        this.category = category;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notifier")
    public String getNotifier() {
        return notifier;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notifier")
    public void setNotifier(String notifier) {
        this.notifier = notifier;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("exception")
    public String getException() {
        return exception;
    }

    @JsonProperty("exception")
    public void setException(String exception) {
        this.exception = exception;
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
        return new ToStringBuilder(this).append("notificationId", notificationId).append("type", type).append("category", category).append("source", source).append("notifier", notifier).append("message", message).append("exception", exception).append("hasMonitors", hasMonitors).append("created", created).append("acknowledgement", acknowledgement).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(exception).append(acknowledgement).append(created).append(notifier).append(hasMonitors).append(notificationId).append(source).append(type).append(category).append(message).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SystemNotificationItem) == false) {
            return false;
        }
        SystemNotificationItem rhs = ((SystemNotificationItem) other);
        return new EqualsBuilder().append(exception, rhs.exception).append(acknowledgement, rhs.acknowledgement).append(created, rhs.created).append(notifier, rhs.notifier).append(hasMonitors, rhs.hasMonitors).append(notificationId, rhs.notificationId).append(source, rhs.source).append(type, rhs.type).append(category, rhs.category).append(message, rhs.message).isEquals();
    }

}
