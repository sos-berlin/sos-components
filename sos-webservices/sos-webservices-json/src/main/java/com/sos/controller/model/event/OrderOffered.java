
package com.sos.controller.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * OrderOffered event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "orderId",
    "until"
})
public class OrderOffered
    extends Event
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("until")
    private Integer until;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderOffered() {
    }

    /**
     * 
     * @param eventId
     * @param orderId
     * @param until
     * 
     */
    public OrderOffered(String orderId, Integer until, Long eventId) {
        super(eventId);
        this.orderId = orderId;
        this.until = until;
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

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("until")
    public Integer getUntil() {
        return until;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("until")
    public void setUntil(Integer until) {
        this.until = until;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderId", orderId).append("until", until).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(orderId).append(until).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderOffered) == false) {
            return false;
        }
        OrderOffered rhs = ((OrderOffered) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(orderId, rhs.orderId).append(until, rhs.until).isEquals();
    }

}
