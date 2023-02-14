
package com.sos.joc.model.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "error",
    "eventId",
    "eventSnapshots",
    "eventsFromSystemMonitoring",
    "eventsFromOrderMonitoring",
    "deliveryDate"
})
public class Event {

    /**
     * controllerId
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
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventSnapshots")
    private List<EventSnapshot> eventSnapshots = new ArrayList<EventSnapshot>();
    @JsonProperty("eventsFromSystemMonitoring")
    private List<EventMonitoring> eventsFromSystemMonitoring = new ArrayList<EventMonitoring>();
    @JsonProperty("eventsFromOrderMonitoring")
    private List<EventOrderMonitoring> eventsFromOrderMonitoring = new ArrayList<EventOrderMonitoring>();
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;

    /**
     * controllerId
     * <p>
     * 
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
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventSnapshots")
    public List<EventSnapshot> getEventSnapshots() {
        return eventSnapshots;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventSnapshots")
    public void setEventSnapshots(List<EventSnapshot> eventSnapshots) {
        this.eventSnapshots = eventSnapshots;
    }

    @JsonProperty("eventsFromSystemMonitoring")
    public List<EventMonitoring> getEventsFromSystemMonitoring() {
        return eventsFromSystemMonitoring;
    }

    @JsonProperty("eventsFromSystemMonitoring")
    public void setEventsFromSystemMonitoring(List<EventMonitoring> eventsFromSystemMonitoring) {
        this.eventsFromSystemMonitoring = eventsFromSystemMonitoring;
    }

    @JsonProperty("eventsFromOrderMonitoring")
    public List<EventOrderMonitoring> getEventsFromOrderMonitoring() {
        return eventsFromOrderMonitoring;
    }

    @JsonProperty("eventsFromOrderMonitoring")
    public void setEventsFromOrderMonitoring(List<EventOrderMonitoring> eventsFromOrderMonitoring) {
        this.eventsFromOrderMonitoring = eventsFromOrderMonitoring;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("error", error).append("eventId", eventId).append("eventSnapshots", eventSnapshots).append("eventsFromSystemMonitoring", eventsFromSystemMonitoring).append("eventsFromOrderMonitoring", eventsFromOrderMonitoring).append("deliveryDate", deliveryDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(controllerId).append(eventsFromSystemMonitoring).append(error).append(eventSnapshots).append(deliveryDate).append(eventsFromOrderMonitoring).toHashCode();
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
        return new EqualsBuilder().append(eventId, rhs.eventId).append(controllerId, rhs.controllerId).append(eventsFromSystemMonitoring, rhs.eventsFromSystemMonitoring).append(error, rhs.error).append(eventSnapshots, rhs.eventSnapshots).append(deliveryDate, rhs.deliveryDate).append(eventsFromOrderMonitoring, rhs.eventsFromOrderMonitoring).isEquals();
    }

}
