
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler custom event for calendar and calendar usage
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "key",
    "eventId",
    "variables"
})
public class CalendarEvent {

    @JsonProperty("TYPE")
    private String tYPE = "VariablesCustomEvent";
    @JsonProperty("key")
    private String key;
    @JsonProperty("eventId")
    private Long eventId;
    @JsonProperty("variables")
    private CalendarVariables variables;

    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("variables")
    public CalendarVariables getVariables() {
        return variables;
    }

    @JsonProperty("variables")
    public void setVariables(CalendarVariables variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("key", key).append("eventId", eventId).append("variables", variables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(variables).append(tYPE).append(key).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CalendarEvent) == false) {
            return false;
        }
        CalendarEvent rhs = ((CalendarEvent) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(variables, rhs.variables).append(tYPE, rhs.tYPE).append(key, rhs.key).isEquals();
    }

}
