
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({

})
public class OrderProcessingStarted
    extends Event
    implements IEvent
{


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
