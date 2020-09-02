
package com.sos.joc.model.order;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order with delivray date (permanent part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "order"
})
public class OrderP200 {

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
    /**
     * order (permanent part)
     * <p>
     * compact=true then ONLY surveyDate, path, id, jobChain and _type are responded, title is optional
     * (Required)
     * 
     */
    @JsonProperty("order")
    @JsonPropertyDescription("compact=true then ONLY surveyDate, path, id, jobChain and _type are responded, title is optional")
    private OrderP order;

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

    /**
     * order (permanent part)
     * <p>
     * compact=true then ONLY surveyDate, path, id, jobChain and _type are responded, title is optional
     * (Required)
     * 
     */
    @JsonProperty("order")
    public OrderP getOrder() {
        return order;
    }

    /**
     * order (permanent part)
     * <p>
     * compact=true then ONLY surveyDate, path, id, jobChain and _type are responded, title is optional
     * (Required)
     * 
     */
    @JsonProperty("order")
    public void setOrder(OrderP order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("order", order).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(order).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderP200) == false) {
            return false;
        }
        OrderP200 rhs = ((OrderP200) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(order, rhs.order).isEquals();
    }

}
