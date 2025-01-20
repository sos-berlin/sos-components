
package com.sos.controller.model.order;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * OrderSleepState
 * <p>
 * set if state == OrderSleeping
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "until"
})
public class OrderSleepState {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date until;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderSleepState() {
    }

    /**
     * 
     * @param until
     */
    public OrderSleepState(Date until) {
        super();
        this.until = until;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    public Date getUntil() {
        return until;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    public void setUntil(Date until) {
        this.until = until;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("until", until).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(until).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderSleepState) == false) {
            return false;
        }
        OrderSleepState rhs = ((OrderSleepState) other);
        return new EqualsBuilder().append(until, rhs.until).isEquals();
    }

}
