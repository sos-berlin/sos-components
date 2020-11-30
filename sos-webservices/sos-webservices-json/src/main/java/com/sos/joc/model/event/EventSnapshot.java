
package com.sos.joc.model.event;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


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
    "objectType"
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
     * FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, OrderStarted, OrderStepStarted, OrderStepEnded, OrderNodeChanged, OrderFinished, OrderSetback, OrderSuspended, OrderResumed
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    @JsonPropertyDescription("FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, OrderStarted, OrderStepStarted, OrderStepEnded, OrderNodeChanged, OrderFinished, OrderSetback, OrderSuspended, OrderResumed")
    private String eventType;
    /**
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private EventType objectType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    
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
     * FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, OrderStarted, OrderStepStarted, OrderStepEnded, OrderNodeChanged, OrderFinished, OrderSetback, OrderSuspended, OrderResumed
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    /**
     * FileBasedAdded, FileBasedRemoved, FileBasedReplaced, FileBasedActivated, OrderStarted, OrderStepStarted, OrderStepEnded, OrderNodeChanged, OrderFinished, OrderSetback, OrderSuspended, OrderResumed
     * (Required)
     * 
     */
    @JsonProperty("eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * JobScheduler object type
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
     * JobScheduler object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(EventType objectType) {
        this.objectType = objectType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventId", eventId).append("path", path).append("eventType", eventType).append("objectType", objectType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(path).append(eventType).append(objectType).toHashCode();
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
        return new EqualsBuilder().append(eventId, rhs.eventId).append(path, rhs.path).append(eventType, rhs.eventType).append(objectType, rhs.objectType).isEquals();
    }

}
