
package com.sos.jobscheduler.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Resume Order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "orderId",
    "orderIds"
})
public class ResumeOrders
    extends Command
{

    @JsonProperty("orderIds")
    private List<String> orderIds = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ResumeOrders() {
    }

    /**
     * 
     * @param orderIds
     */
    public ResumeOrders(List<String> orderIds) {
        super();
        this.orderIds = orderIds;
    }

    @JsonProperty("orderIds")
    public List<String> getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderIds", orderIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(orderIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResumeOrders) == false) {
            return false;
        }
        ResumeOrders rhs = ((ResumeOrders) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(orderIds, rhs.orderIds).isEquals();
    }

}
