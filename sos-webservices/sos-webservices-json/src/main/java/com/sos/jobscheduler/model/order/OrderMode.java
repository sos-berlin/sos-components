
package com.sos.jobscheduler.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE"
})
public class OrderMode {

    /**
     * orderModeType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private OrderModeType tYPE;

    /**
     * orderModeType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public OrderModeType getTYPE() {
        return tYPE;
    }

    /**
     * orderModeType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(OrderModeType tYPE) {
        this.tYPE = tYPE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderMode) == false) {
            return false;
        }
        OrderMode rhs = ((OrderMode) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).isEquals();
    }

}
