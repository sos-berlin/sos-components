
package com.sos.joc.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * event snapshot
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventId",
    "path",
    "eventType",
    "objectType",
    "accessToken",
    "message"
})
public class EventSnapshot {

    /**
     * unique id of an event, monoton increasing, id/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    @JsonPropertyDescription("unique id of an event, monoton increasing, id/1000=milliseconds of UTC time")
    private Long eventId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * e.g. OrderStateChanged, OrderAdded, OrderTerminated, ControllerStateChanged, WorkflowStateChanged, JobStateChanged
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    @JsonPropertyDescription("e.g. OrderStateChanged, OrderAdded, OrderTerminated, ControllerStateChanged, WorkflowStateChanged, JobStateChanged")
    private String eventType;
    /**
     * event types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private EventType objectType;
    @JsonProperty("accessToken")
    private String accessToken;
    @JsonProperty("message")
    private String message;

    /**
     * unique id of an event, monoton increasing, id/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * unique id of an event, monoton increasing, id/1000=milliseconds of UTC time
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * e.g. OrderStateChanged, OrderAdded, OrderTerminated, ControllerStateChanged, WorkflowStateChanged, JobStateChanged
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    /**
     * e.g. OrderStateChanged, OrderAdded, OrderTerminated, ControllerStateChanged, WorkflowStateChanged, JobStateChanged
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * event types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public EventType getObjectType() {
        return objectType;
    }

    /**
     * event types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(EventType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("accessToken")
    public String getAccessToken() {
        return accessToken;
    }

    @JsonProperty("accessToken")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
        return new ToStringBuilder(this).append("eventId", eventId).append("path", path).append("eventType", eventType).append("objectType", objectType).append("accessToken", accessToken).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(eventType).append(accessToken).append(message).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EventSnapshot) == false) {
            return false;
        }
        EventSnapshot rhs = ((EventSnapshot) other);
        return new EqualsBuilder().append(path, rhs.path).append(eventType, rhs.eventType).append(accessToken, rhs.accessToken).append(message, rhs.message).append(objectType, rhs.objectType).isEquals();
    }

}
