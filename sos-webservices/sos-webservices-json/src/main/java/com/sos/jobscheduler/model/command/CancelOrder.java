
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
    "TYPE",
    "orderId",
    "mode"
})
public class CancelOrder
    extends Command
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("mode")
    private OrderMode mode = new OrderMode();

    /**
     * No args constructor for use in serialization
     * 
     */
    public CancelOrder() {
    }

    /**
     * 
     * @param mode
     * @param orderId
     */
    public CancelOrder(String orderId, OrderMode mode) {
        super();
        this.orderId = orderId;
        this.mode = mode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
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
