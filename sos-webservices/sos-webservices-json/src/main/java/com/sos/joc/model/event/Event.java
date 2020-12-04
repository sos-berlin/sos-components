
package com.sos.joc.model.event;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "error",
    "eventId",
    "eventSnapshots",
    "notifications"
})
public class Event {

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    private Err error;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    @JsonProperty("eventSnapshots")
    private List<EventSnapshot> eventSnapshots = new ArrayList<EventSnapshot>();
    @JsonProperty("notifications")
    private List<EventSnapshot> notifications = new ArrayList<EventSnapshot>();

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    public Err getError() {
        return error;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    public void setError(Err error) {
        this.error = error;
    }

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

    @JsonProperty("eventSnapshots")
    public List<EventSnapshot> getEventSnapshots() {
        return eventSnapshots;
    }

    @JsonProperty("eventSnapshots")
    public void setEventSnapshots(List<EventSnapshot> eventSnapshots) {
        this.eventSnapshots = eventSnapshots;
    }

    @JsonProperty("notifications")
    public List<EventSnapshot> getNotifications() {
        return notifications;
    }

    @JsonProperty("notifications")
    public void setNotifications(List<EventSnapshot> notifications) {
        this.notifications = notifications;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("error", error).append("eventId", eventId).append("eventSnapshots", eventSnapshots).append("notifications", notifications).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(controllerId).append(error).append(eventSnapshots).append(notifications).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Event) == false) {
            return false;
        }
        Event rhs = ((Event) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(controllerId, rhs.controllerId).append(error, rhs.error).append(eventSnapshots, rhs.eventSnapshots).append(notifications, rhs.notifications).isEquals();
    }

}
