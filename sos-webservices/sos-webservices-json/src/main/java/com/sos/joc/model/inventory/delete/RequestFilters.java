
package com.sos.joc.model.inventory.delete;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * common inventory request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cancelOrdersDateFrom"
})
public class RequestFilters
    extends com.sos.joc.model.inventory.common.RequestFilters
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cancelOrdersDateFrom")
    private String cancelOrdersDateFrom;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cancelOrdersDateFrom")
    public String getCancelOrdersDateFrom() {
        return cancelOrdersDateFrom;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("cancelOrdersDateFrom")
    public void setCancelOrdersDateFrom(String cancelOrdersDateFrom) {
        this.cancelOrdersDateFrom = cancelOrdersDateFrom;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("cancelOrdersDateFrom", cancelOrdersDateFrom).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(cancelOrdersDateFrom).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFilters) == false) {
            return false;
        }
        RequestFilters rhs = ((RequestFilters) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(cancelOrdersDateFrom, rhs.cancelOrdersDateFrom).isEquals();
    }

}
