
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "orderId",
    "compact",
    "suppressNotExistException"
})
public class OrderFilter {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
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
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * compact parameter
     * <p>
     * controls if an exception raises when Object doesn't exist
     * 
     */
    @JsonProperty("suppressNotExistException")
    @JsonPropertyDescription("controls if an exception raises when Object doesn't exist")
    private Boolean suppressNotExistException = true;

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

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
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if an exception raises when Object doesn't exist
     * 
     */
    @JsonProperty("suppressNotExistException")
    public Boolean getSuppressNotExistException() {
        return suppressNotExistException;
    }

    /**
     * compact parameter
     * <p>
     * controls if an exception raises when Object doesn't exist
     * 
     */
    @JsonProperty("suppressNotExistException")
    public void setSuppressNotExistException(Boolean suppressNotExistException) {
        this.suppressNotExistException = suppressNotExistException;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("orderId", orderId).append("compact", compact).append("suppressNotExistException", suppressNotExistException).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(compact).append(orderId).append(suppressNotExistException).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderFilter) == false) {
            return false;
        }
        OrderFilter rhs = ((OrderFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(orderId, rhs.orderId).append(suppressNotExistException, rhs.suppressNotExistException).isEquals();
    }

}
