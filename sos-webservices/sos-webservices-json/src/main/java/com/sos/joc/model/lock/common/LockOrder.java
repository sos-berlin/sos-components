
package com.sos.joc.model.lock.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.order.OrderV;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * lock order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lock",
    "order"
})
public class LockOrder {

    /**
     * lock in a workflow
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lock")
    private WorkflowLock lock;
    /**
     * order with delivery date (volatile part)
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("order")
    private OrderV order;

    /**
     * lock in a workflow
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lock")
    public WorkflowLock getLock() {
        return lock;
    }

    /**
     * lock in a workflow
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("lock")
    public void setLock(WorkflowLock lock) {
        this.lock = lock;
    }

    /**
     * order with delivery date (volatile part)
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("order")
    public OrderV getOrder() {
        return order;
    }

    /**
     * order with delivery date (volatile part)
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("order")
    public void setOrder(OrderV order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lock", lock).append("order", order).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lock).append(order).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockOrder) == false) {
            return false;
        }
        LockOrder rhs = ((LockOrder) other);
        return new EqualsBuilder().append(lock, rhs.lock).append(order, rhs.order).isEquals();
    }

}
