
package com.sos.joc.model.inventory.release;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ResponseReleasable
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "releasable"
})
public class ResponseReleasable {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * ResponseReleasableTreeItem
     * <p>
     * 
     * 
     */
    @JsonProperty("releasable")
    private ResponseReleasableTreeItem releasable;

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

    /**
     * ResponseReleasableTreeItem
     * <p>
     * 
     * 
     */
    @JsonProperty("releasable")
    public ResponseReleasableTreeItem getReleasable() {
        return releasable;
    }

    /**
     * ResponseReleasableTreeItem
     * <p>
     * 
     * 
     */
    @JsonProperty("releasable")
    public void setReleasable(ResponseReleasableTreeItem releasable) {
        this.releasable = releasable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("releasable", releasable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(releasable).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseReleasable) == false) {
            return false;
        }
        ResponseReleasable rhs = ((ResponseReleasable) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(releasable, rhs.releasable).isEquals();
    }

}
