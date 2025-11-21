
package com.sos.joc.model.inventory.dependencies.get;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "requestedItems",
    "objects",
    "deliveryDate"
})
public class Response {

    @JsonProperty("requestedItems")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> requestedItems = new LinkedHashSet<Long>();
    @JsonProperty("objects")
    private ResponseObjects objects;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;

    @JsonProperty("requestedItems")
    public Set<Long> getRequestedItems() {
        return requestedItems;
    }

    @JsonProperty("requestedItems")
    public void setRequestedItems(Set<Long> requestedItems) {
        this.requestedItems = requestedItems;
    }

    @JsonProperty("objects")
    public ResponseObjects getObjects() {
        return objects;
    }

    @JsonProperty("objects")
    public void setObjects(ResponseObjects objects) {
        this.objects = objects;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestedItems", requestedItems).append("objects", objects).append("deliveryDate", deliveryDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(requestedItems).append(deliveryDate).append(objects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Response) == false) {
            return false;
        }
        Response rhs = ((Response) other);
        return new EqualsBuilder().append(requestedItems, rhs.requestedItems).append(deliveryDate, rhs.deliveryDate).append(objects, rhs.objects).isEquals();
    }

}
