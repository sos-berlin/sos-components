
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
    "hasMonitors",
    "section",
    "notifier",
    "time",
    "message",
    "exception",
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
    @JsonProperty("hasMonitors")
    private Boolean hasMonitors;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("section")
    private String section;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notifier")
    private String notifier;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("time")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date time;
    @JsonProperty("message")
    private String message;
    @JsonProperty("exception")
    private String exception;
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
    @JsonProperty("section")
    public String getSection() {
        return section;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("section")
    public void setSection(String section) {
        this.section = section;
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

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("time")
    public Date getTime() {
        return time;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("time")
    public void setTime(Date time) {
        this.time = time;
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
        return new ToStringBuilder(this).append("notificationId", notificationId).append("type", type).append("category", category).append("hasMonitors", hasMonitors).append("section", section).append("notifier", notifier).append("time", time).append("message", message).append("exception", exception).append("created", created).append("acknowledgement", acknowledgement).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(exception).append(acknowledgement).append(created).append(hasMonitors).append(notifier).append(notificationId).append(section).append(time).append(type).append(category).append(message).toHashCode();
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
        return new EqualsBuilder().append(exception, rhs.exception).append(acknowledgement, rhs.acknowledgement).append(created, rhs.created).append(hasMonitors, rhs.hasMonitors).append(notifier, rhs.notifier).append(notificationId, rhs.notificationId).append(section, rhs.section).append(time, rhs.time).append(type, rhs.type).append(category, rhs.category).append(message, rhs.message).isEquals();
    }

}
