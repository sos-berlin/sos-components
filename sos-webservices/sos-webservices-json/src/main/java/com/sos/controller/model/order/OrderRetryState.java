
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
 * OrderRetryState
 * <p>
 * set if state == DelayedAfterError
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "next",
    "attempt"
})
public class OrderRetryState {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("next")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date next;
    @JsonProperty("attempt")
    private Integer attempt;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderRetryState() {
    }

    /**
     * 
     * @param next
     * @param attempt
     */
    public OrderRetryState(Date next, Integer attempt) {
        super();
        this.next = next;
        this.attempt = attempt;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("next")
    public Date getNext() {
        return next;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("next")
    public void setNext(Date next) {
        this.next = next;
    }

    @JsonProperty("attempt")
    public Integer getAttempt() {
        return attempt;
    }

    @JsonProperty("attempt")
    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("next", next).append("attempt", attempt).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(next).append(attempt).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderRetryState) == false) {
            return false;
        }
        OrderRetryState rhs = ((OrderRetryState) other);
        return new EqualsBuilder().append(next, rhs.next).append(attempt, rhs.attempt).isEquals();
    }

}
