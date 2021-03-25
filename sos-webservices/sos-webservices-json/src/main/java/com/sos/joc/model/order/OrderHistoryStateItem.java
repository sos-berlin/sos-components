
package com.sos.joc.model.order;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order object in history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "state",
    "stateTime",
    "stateText"
})
public class OrderHistoryStateItem {

    /**
     * order state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private OrderState state;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("stateTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date stateTime;
    @JsonProperty("stateText")
    private String stateText;

    /**
     * order state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public OrderState getState() {
        return state;
    }

    /**
     * order state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(OrderState state) {
        this.state = state;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("stateTime")
    public Date getStateTime() {
        return stateTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("stateTime")
    public void setStateTime(Date stateTime) {
        this.stateTime = stateTime;
    }

    @JsonProperty("stateText")
    public String getStateText() {
        return stateText;
    }

    @JsonProperty("stateText")
    public void setStateText(String stateText) {
        this.stateText = stateText;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("state", state).append("stateTime", stateTime).append("stateText", stateText).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(state).append(stateTime).append(stateText).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderHistoryStateItem) == false) {
            return false;
        }
        OrderHistoryStateItem rhs = ((OrderHistoryStateItem) other);
        return new EqualsBuilder().append(state, rhs.state).append(stateTime, rhs.stateTime).append(stateText, rhs.stateText).isEquals();
    }

}
