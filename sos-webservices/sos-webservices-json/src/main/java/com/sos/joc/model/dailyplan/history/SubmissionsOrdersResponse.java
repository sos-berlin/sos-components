
package com.sos.joc.model.dailyplan.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.dailyplan.history.items.OrderItem;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * collection of daily plan dates
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "orders",
    "warnMessages",
    "errorMessages"
})
public class SubmissionsOrdersResponse {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("orders")
    private List<OrderItem> orders = new ArrayList<OrderItem>();
    @JsonProperty("warnMessages")
    private List<String> warnMessages = new ArrayList<String>();
    @JsonProperty("errorMessages")
    private List<String> errorMessages = new ArrayList<String>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("orders")
    public List<OrderItem> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(List<OrderItem> orders) {
        this.orders = orders;
    }

    @JsonProperty("warnMessages")
    public List<String> getWarnMessages() {
        return warnMessages;
    }

    @JsonProperty("warnMessages")
    public void setWarnMessages(List<String> warnMessages) {
        this.warnMessages = warnMessages;
    }

    @JsonProperty("errorMessages")
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @JsonProperty("errorMessages")
    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("orders", orders).append("warnMessages", warnMessages).append("errorMessages", errorMessages).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorMessages).append(orders).append(deliveryDate).append(warnMessages).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubmissionsOrdersResponse) == false) {
            return false;
        }
        SubmissionsOrdersResponse rhs = ((SubmissionsOrdersResponse) other);
        return new EqualsBuilder().append(errorMessages, rhs.errorMessages).append(orders, rhs.orders).append(deliveryDate, rhs.deliveryDate).append(warnMessages, rhs.warnMessages).isEquals();
    }

}
