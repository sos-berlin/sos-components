
package com.sos.joc.model.inventory.delete;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.RequestFilter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * response Delete Draft
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "updated",
    "deleted"
})
public class ResponseItem {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    @JsonProperty("updated")
    private List<RequestFilter> updated = new ArrayList<RequestFilter>();
    @JsonProperty("deleted")
    private List<RequestFilter> deleted = new ArrayList<RequestFilter>();

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

    @JsonProperty("updated")
    public List<RequestFilter> getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(List<RequestFilter> updated) {
        this.updated = updated;
    }

    @JsonProperty("deleted")
    public List<RequestFilter> getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(List<RequestFilter> deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("updated", updated).append("deleted", deleted).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deleted).append(deliveryDate).append(updated).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseItem) == false) {
            return false;
        }
        ResponseItem rhs = ((ResponseItem) other);
        return new EqualsBuilder().append(deleted, rhs.deleted).append(deliveryDate, rhs.deliveryDate).append(updated, rhs.updated).isEquals();
    }

}
