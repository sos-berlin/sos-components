
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
 * OrderCycleState
 * <p>
 * set if state == BetweenCycles or processing inside a cycle
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "next",
    "since",
    "index"
})
public class OrderCycleState {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("next")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date next;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("since")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date since;
    @JsonProperty("index")
    private Integer index;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderCycleState() {
    }

    /**
     * 
     * @param next
     * @param index
     * @param since
     */
    public OrderCycleState(Date next, Date since, Integer index) {
        super();
        this.next = next;
        this.since = since;
        this.index = index;
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

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("since")
    public Date getSince() {
        return since;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("since")
    public void setSince(Date since) {
        this.since = since;
    }

    @JsonProperty("index")
    public Integer getIndex() {
        return index;
    }

    @JsonProperty("index")
    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("next", next).append("since", since).append("index", index).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(next).append(index).append(since).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderCycleState) == false) {
            return false;
        }
        OrderCycleState rhs = ((OrderCycleState) other);
        return new EqualsBuilder().append(next, rhs.next).append(index, rhs.index).append(since, rhs.since).isEquals();
    }

}
