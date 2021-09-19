
package com.sos.controller.model.board;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.order.OrderV;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * notice
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "endOfLife",
    "expectingOrders",
    "state"
})
public class Notice {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("endOfLife")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date endOfLife;
    @JsonProperty("expectingOrders")
    private List<OrderV> expectingOrders = null;
    /**
     * NoticeState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private NoticeState state;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Notice() {
    }

    /**
     * 
     * @param id
     * @param state
     * @param endOfLife
     * @param expectingOrders
     */
    public Notice(String id, Date endOfLife, List<OrderV> expectingOrders, NoticeState state) {
        super();
        this.id = id;
        this.endOfLife = endOfLife;
        this.expectingOrders = expectingOrders;
        this.state = state;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("endOfLife")
    public Date getEndOfLife() {
        return endOfLife;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("endOfLife")
    public void setEndOfLife(Date endOfLife) {
        this.endOfLife = endOfLife;
    }

    @JsonProperty("expectingOrders")
    public List<OrderV> getExpectingOrders() {
        return expectingOrders;
    }

    @JsonProperty("expectingOrders")
    public void setExpectingOrders(List<OrderV> expectingOrders) {
        this.expectingOrders = expectingOrders;
    }

    /**
     * NoticeState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public NoticeState getState() {
        return state;
    }

    /**
     * NoticeState
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(NoticeState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("endOfLife", endOfLife).append("expectingOrders", expectingOrders).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(state).append(endOfLife).append(expectingOrders).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Notice) == false) {
            return false;
        }
        Notice rhs = ((Notice) other);
        return new EqualsBuilder().append(id, rhs.id).append(state, rhs.state).append(endOfLife, rhs.endOfLife).append(expectingOrders, rhs.expectingOrders).isEquals();
    }

}
