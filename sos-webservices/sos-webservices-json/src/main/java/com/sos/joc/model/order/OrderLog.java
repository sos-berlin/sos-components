
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order log
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "complete",
    "eventId",
    "logEvents"
})
public class OrderLog {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    private Boolean complete = false;
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
    @JsonProperty("logEvents")
    private List<OrderLogItem> logEvents = new ArrayList<OrderLogItem>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    public Boolean getComplete() {
        return complete;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    public void setComplete(Boolean complete) {
        this.complete = complete;
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
    @JsonProperty("logEvents")
    public List<OrderLogItem> getLogEvents() {
        return logEvents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvents")
    public void setLogEvents(List<OrderLogItem> logEvents) {
        this.logEvents = logEvents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("complete", complete).append("eventId", eventId).append("logEvents", logEvents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(logEvents).append(complete).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderLog) == false) {
            return false;
        }
        OrderLog rhs = ((OrderLog) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(logEvents, rhs.logEvents).append(complete, rhs.complete).isEquals();
    }

}
