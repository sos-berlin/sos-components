
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.order.OrderModeType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * modify order command
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderId",
    "orderType",
    "kill",
    "position",
    "arguments"
})
public class ModifyOrder {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    @JsonPropertyDescription("relevant for cancel or suspend order")
    private OrderModeType orderType = OrderModeType.fromValue("FreshOrStarted");
    @JsonProperty("kill")
    private Boolean kill = false;
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> position = new ArrayList<Object>();
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    public OrderModeType getOrderType() {
        return orderType;
    }

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    public void setOrderType(OrderModeType orderType) {
        this.orderType = orderType;
    }

    @JsonProperty("kill")
    public Boolean getKill() {
        return kill;
    }

    @JsonProperty("kill")
    public void setKill(Boolean kill) {
        this.kill = kill;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    public List<Object> getPosition() {
        return position;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    public void setPosition(List<Object> position) {
        this.position = position;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Variables getArguments() {
        return arguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Variables arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderId", orderId).append("orderType", orderType).append("kill", kill).append("position", position).append("arguments", arguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderType).append(arguments).append(position).append(kill).append(orderId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyOrder) == false) {
            return false;
        }
        ModifyOrder rhs = ((ModifyOrder) other);
        return new EqualsBuilder().append(orderType, rhs.orderType).append(arguments, rhs.arguments).append(position, rhs.position).append(kill, rhs.kill).append(orderId, rhs.orderId).isEquals();
    }

}
