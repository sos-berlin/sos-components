
package com.sos.jobscheduler.model.event;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * OrderFinished event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderFinished
    extends Event
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderFinished() {
    }

    /**
     * 
     * @param eventId
     * 
     */
    public OrderFinished(Long eventId) {
        super(eventId);
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
        if ((other instanceof OrderFinished) == false) {
            return false;
        }
        OrderFinished rhs = ((OrderFinished) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
