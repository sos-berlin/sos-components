
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderProcessingStarted event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderProcessingStarted
    extends Event
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderProcessingStarted() {
    }

    /**
     * 
     * @param eventId
     * @param tYPE
     */
    public OrderProcessingStarted(EventType tYPE, Long eventId) {
        super(tYPE, eventId);
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
        if ((other instanceof OrderProcessingStarted) == false) {
            return false;
        }
        OrderProcessingStarted rhs = ((OrderProcessingStarted) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
