
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.history.order.OrderLogEntry;
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
    "historyId",
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
    @JsonProperty("historyId")
    private Long historyId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvents")
    private List<OrderLogEntry> logEvents = new ArrayList<OrderLogEntry>();

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
    
    @JsonProperty("historyId")
    public Long getHistoryId() {
        return eventId;
    }
    
    @JsonProperty("historyId")
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvents")
    public List<OrderLogEntry> getLogEvents() {
        return logEvents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logEvents")
    public void setLogEvents(List<OrderLogEntry> logEvents) {
        this.logEvents = logEvents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("complete", complete).append("eventId", eventId).append("historyId", historyId).append("logEvents", logEvents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(historyId).append(logEvents).append(complete).toHashCode();
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
        return new EqualsBuilder().append(eventId, rhs.eventId).append(historyId, rhs.historyId).append(logEvents, rhs.logEvents).append(complete, rhs.complete).isEquals();
    }

}
