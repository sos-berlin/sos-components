
package com.sos.joc.model.monitoring.notification.system;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * notification filter with notification id
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "notificationId"
})
public class SystemNotificationFilter {

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("notificationId", notificationId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(notificationId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SystemNotificationFilter) == false) {
            return false;
        }
        SystemNotificationFilter rhs = ((SystemNotificationFilter) other);
        return new EqualsBuilder().append(notificationId, rhs.notificationId).isEquals();
    }

}
