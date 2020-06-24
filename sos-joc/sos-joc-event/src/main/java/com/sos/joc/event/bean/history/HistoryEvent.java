package com.sos.joc.event.bean.history;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = com.sos.joc.event.bean.history.OrderStepStarted.class, name = "OrderStepStarted"),
    @JsonSubTypes.Type(value = com.sos.joc.event.bean.history.OrderStepFinished.class, name = "OrderStepFinished")
})

public abstract class HistoryEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public HistoryEvent() {
    }

    /**
     * @param key
     * @param eventId
     * @param jobschedulerId
     * @param variables
     */
    public HistoryEvent(String key, Long eventId, String jobschedulerId, Map<String, String> variables) {
        super(key, eventId, jobschedulerId, variables);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HistoryEvent) == false) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }
}
