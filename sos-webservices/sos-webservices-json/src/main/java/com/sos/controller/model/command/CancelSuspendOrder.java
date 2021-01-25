
package com.sos.controller.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.order.OrderMode;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Super of Cancel or Suspend Order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "orderIds",
    "mode"
})
public class CancelSuspendOrder
    extends Command
{

    @JsonProperty("orderIds")
    private List<String> orderIds = null;
    @JsonProperty("mode")
    private OrderMode mode = new OrderMode();

    /**
     * No args constructor for use in serialization
     * 
     */
    public CancelSuspendOrder() {
    }

    /**
     * 
     * @param mode
     * @param orderIds
     */
    public CancelSuspendOrder(List<String> orderIds, OrderMode mode) {
        super();
        this.orderIds = orderIds;
        this.mode = mode;
    }

    @JsonProperty("orderIds")
    public List<String> getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderIds", orderIds).append("mode", mode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(mode).append(orderIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CancelSuspendOrder) == false) {
            return false;
        }
        CancelSuspendOrder rhs = ((CancelSuspendOrder) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(mode, rhs.mode).append(orderIds, rhs.orderIds).isEquals();
    }

}
