
package com.sos.joc.model.monitoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * notification filter with notification id
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "notificationId",
    "comment"
})
public class NotificationAcknowledgeFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationId")
    private Long notificationId;
    @JsonProperty("comment")
    private String comment;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

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

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("notificationId", notificationId).append("comment", comment).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(notificationId).append(comment).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NotificationAcknowledgeFilter) == false) {
            return false;
        }
        NotificationAcknowledgeFilter rhs = ((NotificationAcknowledgeFilter) other);
        return new EqualsBuilder().append(notificationId, rhs.notificationId).append(comment, rhs.comment).append(controllerId, rhs.controllerId).isEquals();
    }

}
