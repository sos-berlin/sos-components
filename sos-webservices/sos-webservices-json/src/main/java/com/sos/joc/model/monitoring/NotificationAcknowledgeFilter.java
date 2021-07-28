
package com.sos.joc.model.monitoring;

import java.util.ArrayList;
import java.util.List;
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
    "notificationIds",
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationIds")
    private List<Long> notificationIds = new ArrayList<Long>();
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationIds")
    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("notificationIds")
    public void setNotificationIds(List<Long> notificationIds) {
        this.notificationIds = notificationIds;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("notificationIds", notificationIds).append("comment", comment).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(notificationIds).append(comment).append(controllerId).toHashCode();
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
        return new EqualsBuilder().append(notificationIds, rhs.notificationIds).append(comment, rhs.comment).append(controllerId, rhs.controllerId).isEquals();
    }

}
