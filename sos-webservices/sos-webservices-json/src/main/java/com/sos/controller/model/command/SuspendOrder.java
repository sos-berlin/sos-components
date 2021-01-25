
package com.sos.controller.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sos.controller.model.order.OrderMode;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Cancel Order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuspendOrder
    extends CancelSuspendOrder
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public SuspendOrder() {
    }

    /**
     * 
     * @param mode
     * @param orderIds
     */
    public SuspendOrder(List<String> orderIds, OrderMode mode) {
        super(orderIds, mode);
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
        if ((other instanceof SuspendOrder) == false) {
            return false;
        }
        SuspendOrder rhs = ((SuspendOrder) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
