
package com.sos.joc.model.history.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderIds",
    "queuedOrderIds"
})
public class LockState {

    @JsonProperty("orderIds")
    private String orderIds;
    @JsonProperty("queuedOrderIds")
    private String queuedOrderIds;

    @JsonProperty("orderIds")
    public String getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(String orderIds) {
        this.orderIds = orderIds;
    }

    @JsonProperty("queuedOrderIds")
    public String getQueuedOrderIds() {
        return queuedOrderIds;
    }

    @JsonProperty("queuedOrderIds")
    public void setQueuedOrderIds(String queuedOrderIds) {
        this.queuedOrderIds = queuedOrderIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderIds", orderIds).append("queuedOrderIds", queuedOrderIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(queuedOrderIds).append(orderIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockState) == false) {
            return false;
        }
        LockState rhs = ((LockState) other);
        return new EqualsBuilder().append(queuedOrderIds, rhs.queuedOrderIds).append(orderIds, rhs.orderIds).isEquals();
    }

}
