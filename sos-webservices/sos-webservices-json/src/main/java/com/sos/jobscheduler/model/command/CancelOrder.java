
package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.order.OrderMode;
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
@JsonPropertyOrder({
    "orderId",
    "mode"
})
public class CancelOrder
    extends Command
    implements ICommand
{

    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("mode")
    private OrderMode mode;

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("mode")
    public OrderMode getMode() {
        return mode;
    }

    @JsonProperty("mode")
    public void setMode(OrderMode mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderId", orderId).append("mode", mode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(mode).append(orderId).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(mode, rhs.mode).append(orderId, rhs.orderId).isEquals();
    }

}
