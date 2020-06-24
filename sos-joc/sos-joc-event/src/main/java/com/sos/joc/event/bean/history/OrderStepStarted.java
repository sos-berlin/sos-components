package com.sos.joc.event.bean.history;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class OrderStepStarted extends HistoryEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderStepStarted() {
    }

    /**
     * @param key
     * @param eventId
     * @param jobschedulerId
     * @param variables
     */
    public OrderStepStarted(String key, Long eventId, String jobschedulerId, Map<String, String> variables) {
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
        if ((other instanceof OrderStepStarted) == false) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }
}
