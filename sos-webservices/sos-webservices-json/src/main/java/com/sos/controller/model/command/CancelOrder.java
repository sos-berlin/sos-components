
package com.sos.controller.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sos.controller.model.order.OrderMode;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Cancel Order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelOrder
    extends CancelSuspendOrder
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public CancelOrder() {
    }

    /**
     * 
     * @param mode
     * @param orderIds
     */
    public CancelOrder(List<String> orderIds, OrderMode mode) {
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
        if ((other instanceof CancelOrder) == false) {
            return false;
        }
        CancelOrder rhs = ((CancelOrder) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
