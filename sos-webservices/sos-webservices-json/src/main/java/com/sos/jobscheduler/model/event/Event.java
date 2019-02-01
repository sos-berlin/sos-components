
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "TYPE"
})
public class Event implements IEvent
{

    /**
     * eventType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private EventType tYPE;

    /**
     * eventType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public EventType getTYPE() {
        return tYPE;
    }

    /**
     * eventType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(EventType tYPE) {
        this.tYPE = tYPE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).toHashCode();
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
        return new EqualsBuilder().append(tYPE, rhs.tYPE).isEquals();
    }

}
